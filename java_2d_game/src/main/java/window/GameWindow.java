package window;

import asset.AssetSetter;
import collision.CollisionChecker;
import entity.Entity;
import event.EventHandler;
import frame.FrameApp;
import game.GameState;
import map.GameMap;
import npc.NpcOldMan;
import object.ObjCoinBronze;
import object.ObjGreenPotion;
import object.ObjRedPotion;
import object.Projectile;
import player.Player;
import factory.FrameFactory;
import frame.GameFrame;
import key.KeyHandler;
import save.LoadManager;
import sound.SoundManager;
import tile.TileManager;
import tileInteractive.InteractiveTile;
import ui.UI;

import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import static frame.FrameApp.baseDisplay;

/**
 * ゲームのメイン描画パネルを表すクラス。
 * ゲームループの管理、キー入力、マップ描画、エンティティ更新および描画を担当。
 * Window インタフェースと Runnable インタフェースを実装し、シングルトンとして扱われる。
 */

public class GameWindow extends JPanel implements Window, Runnable {

    private KeyHandler keyHandler = new KeyHandler(this);
    private Player player = new Player(this, keyHandler);
    private TileManager tileManager = new TileManager(this);
    private CollisionChecker collisionChecker = new CollisionChecker(this);
    private AssetSetter assetSetter = new AssetSetter(this);
    private EventHandler eventHandler = new EventHandler(this);
    private SoundManager soundManager = new SoundManager(this);
    private GameMap currentMap;
    private Entity[] npc = new Entity[10];
    private Entity[] monster = new Entity[20];
    private Entity[] obj = new Entity[20];
    private InteractiveTile[] iTile = new InteractiveTile[50];
    private UI ui = new UI(this);
    private ArrayList<Projectile> projectileList = new ArrayList<>();
    private ArrayList<Entity> particleList = new ArrayList<>();
    public ArrayList<Entity> itemList = new ArrayList<>();
    private static GameWindow instance;
    private GameState gameState = GameState.TITLE;
    private Thread gameThread;
    private boolean onTransition = false;
    private boolean fadingOut = true;
    private float alpha = 0f;
    private int frameCount = 0;
    private final int TRANSITION_DURATION = 30;
    private int pendingMapId;
    private int currentMapIndex = 1;
    private boolean showHitBoxes = false;
    private boolean dialogueActive = false;
    private Map<String, Boolean> mapFlags = new HashMap<>();
    private Map<Integer, String> mapBgmMap = new HashMap<>();
    private long lastUpdateTimeNano;
    private long loadedPlayTimeSeconds = -1L;


    /**
     * GameWindow のコンストラクタ。
     */

    protected GameWindow() {
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.setLayout(null);
        this.addKeyListener(keyHandler);
        this.setUpGame();
    }

    /**
     * ゲーム開始時の初期配置を行います。
     */

    public void setUpGame() {

        lastUpdateTimeNano = System.nanoTime();
        assetSetter.setNpcOldMan();
        assetSetter.setNpcChicken();
        assetSetter.setMonster();
        assetSetter.setInteractiveTile();
        assetSetter.setObjAxe();
        assetSetter.setObjChest();
        assetSetter.setObjPot();
        gameState = GameState.TITLE;
        initMapBgm();
        getSoundmanager().stopBGM();
    }

    private void initMapBgm() {
        mapBgmMap.put(TileManager.MEADOW_TILE_ID, "sound/meadow_G110.wav");
        mapBgmMap.put(TileManager.FOREST_TILE_ID, "sound/meadow_G110.wav");
        mapBgmMap.put(TileManager.HUT_TILE_ID, "sound/meadow_G110.wav");
        // 事前ロードしておく
        for (String path : mapBgmMap.values()) {
            getSoundmanager().preload(path);
        }
    }

    /**
     * シングルトンインスタンスを取得。
     *
     * @return GameWindow の唯一のインスタンス
     */

    public static synchronized GameWindow getInstance() {
        if (instance == null) {
            instance = new GameWindow();
        }
        return instance;
    }

    /**
     * ゲームループ用スレッドを安全に開始。
     *
     * <p>
     * このメソッドは {@code gameThread} が未生成または既に終了している場合にのみ新しいスレッドを生成して開始。
     * スレッド生成と開始処理は {@code synchronized} により排他制御されるため、二重起動の競合を防ぐ。
     * </p>
     *
     * <p>
     * 生成されるスレッドは {@code Runnable}（このクラスの {@code run()} 実装）を実行し、名前は "GameThread" に設定。
     * </p>
     *
     * <h4>注意</h4>
     * <ul>
     *   <li>UI（Swing）の初期化が完了してから呼び出すこと（例: {@code windowOpened} イベント）。</li>
     *   <li>二重起動は内部で防止されるが、呼び出し側でも起動タイミングを管理するとより安全。</li>
     * </ul>
     *
     * @see #stopGame()
     */

    public synchronized void startGame() {
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this, "GameThread");
            gameThread.start();
        }
    }

    public synchronized void stopGame() {
        // gameThread を null にすることでループを抜けさせる
        Thread t = gameThread;
        gameThread = null;
        if (t != null) {
            try {
                t.join(1000); // 最大1秒待つ
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * ゲームオーバー後のリトライ処理。
     * セーブデータがあればそこから再開、なければ新規ゲーム。
     */

    public void retry() {

        int slot = keyHandler.getCommandNum();
        if (LoadManager.hasSaveData(slot)) {

            // セーブデータからロード
            Entity loadedPlayer = LoadManager.loadPlayer(slot, this);
            if (loadedPlayer != null) {
                setPlayer((Player) loadedPlayer);
                setGameState(GameState.PLAY);
                System.out.println("Resuming from Map" + loadedPlayer.getMapId());
                return;
            }
        }

        // セーブデータなし or ロード失敗 → 新規ゲーム
        restartSafely();
        setGameState(GameState.PLAY);
        System.out.println("Starting a new game");
    }

    /**
     * ゲームを安全に再起動。
     *
     * <p>
     * このメソッドは次の順序で処理を行う：
     * </p>
     * <ol>
     *   <li>{@link #stopGame()} を呼んで現在のゲームループを安全に停止する。</li>
     *   <li>{@link #restart()} を呼んでゲーム状態（プレイヤー、マップ配置、アイテム等）を初期化する。</li>
     *   <li>{@link #startGame()} を呼んでゲームループを再起動する。</li>
     * </ol>
     *
     * <h4>設計上の注意</h4>
     * <ul>
     *   <li>{@code stopGame()} と {@code startGame()} は {@code synchronized} で保護されているため、
     *       再起動処理中の競合は基本的に防がれる。</li>
     *   <li>{@code stopGame()} は内部で {@code join(...)} を行う可能性があり、呼び出し元スレッドを一時的に待機させる。
     *       そのため Swing の EDT（UI スレッド）上で直接呼び出すと UI が固まる恐れがあります。UI ハンドラから呼ぶ場合は
     *       別スレッドで実行するか、非同期に処理する。</li>
     *   <li>ゲームスレッド自身からこのメソッドを呼ぶとデッドロックや自己待機が発生する可能性があるため避ける。</li>
     * </ul>
     *
     * <h4>用途</h4>
     * <p>
     * セーブデータのロード失敗後に新規ゲームを開始する場合や、ゲームオーバー後に完全に状態をリセットして再開したい場合などに使用する。
     * </p>
     *
     * @see #stopGame()
     * @see #restart()
     * @see #startGame()
     */

    public void restartSafely() {
        stopGame();    // ゲームループを止める
        restart();     // 状態初期化（プレイヤー等）
        startGame();   // 再起動
    }


    /**
     * ゲーム再スタート（ニューゲーム）時の初期化処理を行う。
     */

    public void restart() {

        getSoundmanager().playBGM("sound/meadow_G110.wav");
        getPlayer().setDefaultValues();
        getPlayer().setDefaultPositions();
        getPlayer().restoreLifeAndMan();
        getPlayer().setItems();
        getPlayer().setCoin(500);
        assetSetter.setNpcOldMan();
        assetSetter.setNpcChicken();
        assetSetter.setMonster();
        assetSetter.setInteractiveTile();
        assetSetter.setObjAxe();
        assetSetter.setObjChest();
        assetSetter.setObjRedPotion();
        assetSetter.setObjGreenPotion();
        assetSetter.setObjBluePotion();
        assetSetter.setObjPot();
    }

    /**
     * マップ遷移エフェクトを開始。
     *
     * @param newMapId 遷移先のマップID
     */

    public void startMapTransition(int newMapId) {
        this.pendingMapId = newMapId;
        this.onTransition = true;
        this.fadingOut = true;
        this.frameCount = 0;
        this.alpha = 0f;

        keyHandler.clearAllKeys();
        getPlayer().setMoving(false);
    }

    /**
     * ゲームループを実行。
     * FPS の管理を行い、定期的に更新および再描画を呼び出す。
     */

    @Override
    public void run() {
        int fps = 60;
        int nanosecond = 1000000000;
        double drawInterval = (double) nanosecond / fps;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }

            if (timer >= nanosecond) {
                System.out.println("FPS :" + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    /**
     * フレームを作成して表示します（Window インタフェースの実装）。
     *
     * <p>
     * このメソッドはフレームの遅延初期化を行い、以下の責務を持つ：
     * </p>
     * <ul>
     *   <li>FrameFactory を使って {@code GameFrame} を生成し、実際の {@link javax.swing.JFrame} を取得する。</li>
     *   <li>ウィンドウリスナを登録し、ウィンドウが開いたときに {@link #startGame()} を呼んでゲームループを開始する。</li>
     *   <li>ウィンドウが閉じられたときは別スレッド（"ShutdownWorker"）で {@link #stopGame()} を呼び、ゲームループの安全な停止を待った後に
     *       {@link javax.swing.SwingUtilities#invokeLater(Runnable)} を使って EDT 上で {@code JFrame#dispose()} を実行する。</li>
     * </ul>
     *
     * <h4>設計上の注意</h4>
     * <ul>
     *   <li>フィールド初期化時に {@code this} を外部に渡さない（遅延初期化）ことで、未初期化状態で別スレッドに参照されるリスクを避ける。</li>
     *   <li>{@code startGame()} は UI の初期化が完了したタイミング（{@code windowOpened}）で呼ぶことで、ゲームスレッドが未初期化のフィールドを参照することを防ぐ。</li>
     *   <li>{@code stopGame()} は内部で {@code join()} を行う可能性があるため、EDT をブロックしないよう必ず別スレッドから呼び出す。</li>
     *   <li>ウィンドウ破棄（dispose）は必ず EDT 上で行うこと（Swing のスレッドルールに従う）。</li>
     * </ul>
     *
     * <h4>副作用と例外</h4>
     * <ul>
     *   <li>FrameFactory の実装が期待する型（{@code GameFrame} が正しく {@code JFrame} を返す）であることを前提とする。</li>
     *   <li>フレーム生成や表示に失敗した場合は呼び出し側で適切にログや例外処理を行うこと。</li>
     * </ul>
     */

    @Override
    public void frame() {
        // フィールドは遅延初期化しておく（コンストラクタで this を渡さない）
        GameFrame gf = FrameFactory.createFrame(baseDisplay(), this);
        JFrame jf = gf.create();

        jf.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                startGame();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                new Thread(() -> {
                    stopGame();
                    javax.swing.SwingUtilities.invokeLater(jf::dispose);
                }, "ShutdownWorker").start();
            }
        });
    }


    /**
     * フェードアウト／フェードインを含むマップ遷移エフェクトを更新。
     * フェードアウト完了時に map を切り替え、その後フェードインを行う。
     */

    private void updateTransition() {

        frameCount++;

        if (fadingOut) {
            alpha = frameCount / (float) TRANSITION_DURATION;

            if (frameCount >= TRANSITION_DURATION) {
                changeMap(pendingMapId);
                fadingOut = false;
                frameCount = 0;
            }
        } else {
            alpha = 1f - frameCount / (float) TRANSITION_DURATION;

            if (frameCount >= TRANSITION_DURATION) {
                alpha = 0f;
                onTransition = false;
                frameCount = 0;
            }
        }
    }

    /**
     * 毎フレーム呼び出され、ゲームの各エンティティやオブジェクトの状態を更新。
     * playState 時にはプレイヤー、NPC、モンスター、アイテム、発射物、パーティクル、タイル等を順次更新し、
     * pauseState 時には何も処理を行わない。
     */

    public void update() {

        // --- フレーム間の経過秒を計算してプレイヤーに渡す ---
        long nowNano = System.nanoTime();
        double deltaSeconds = (nowNano - lastUpdateTimeNano) / 1_000_000_000.0;
        // 極端に大きな delta を防ぐ（ウィンドウ切替や一時停止復帰時のジャンプ対策）
        if (deltaSeconds < 0) deltaSeconds = 0;
        if (deltaSeconds > 1.0) deltaSeconds = 1.0; // 1秒以上は切り捨て（必要に応じて調整）
        lastUpdateTimeNano = nowNano;

        // プレイ時間更新（1秒刻みで増える）
        player.updatePlayTime(deltaSeconds);
        // ----------------------------------------------------

        if (gameState == GameState.PLAY) {

            if (onTransition) {
                updateTransition();
                return;
            }

            player.update();

            for (Entity entity : npc) {
                if (entity != null) {
                    if (entity.getMapId() != currentMapIndex) continue;
                    entity.update();
                }
            }
            for (int i = 0; i < monster.length; i++) {
                if (monster[i] != null) {
                    if (monster[i].getAlive() && !monster[i].getDying()) {
                        monster[i].update();
                    }
                    if (!monster[i].getAlive()) {
                        monster[i] = null;
                    }
                }
            }
            for (int i = 0; i < itemList.size(); i++) {
                Entity item = itemList.get(i);
                if (item == null) continue;

                if (item.getAlive() && collisionChecker.checkPlayer(item)) {
                    if (item instanceof ObjCoinBronze) {
                        player.addCoin(((ObjCoinBronze) item));
                    } else if (item instanceof ObjRedPotion) {
                        player.healRedPotion((ObjRedPotion) item);
                    } else if (item instanceof ObjGreenPotion) {
                        player.healGreenPotion((ObjGreenPotion) item);
                    }
                    item.setAlive(false);
                }

                if (!item.getAlive()) {
                    itemList.remove(i--);
                }
            }
            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null) {
                    if (projectileList.get(i).getAlive()) {
                        projectileList.get(i).update();
                    }
                    if (!projectileList.get(i).getAlive()) {
                        projectileList.remove(i--);
                    }
                }
            }
            for (int i = 0; i < particleList.size(); i++) {
                if (particleList.get(i) != null) {
                    if (particleList.get(i).getAlive()) {
                        particleList.get(i).update();
                    }
                    if (!particleList.get(i).getAlive()) {
                        particleList.remove(i--);
                    }
                }
            }
            for (int i = 0; i < iTile.length; i++) {
                if (iTile[i] != null) {
                    iTile[i].update();
                }
            }
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    obj[i].update();
                }
            }
        }
        if (gameState == GameState.PAUSE) {

        }
    }

    /**
     * マップを新しい ID のマップへ切り替える。
     * 古いマップのエンティティやオブジェクトをクリアした上で新規ロードし、
     * プレイヤー位置や NPC/モンスター配置を初期化。
     *
     * @param newMap 切り替え先のマップID
     */

    public void changeMap(int newMap) {

        int tileSize = FrameApp.getTileSize();
        currentMapIndex = newMap;

        // BGM 切替（フェードアウト→新BGM再生）
        String newBgm = getBgmForMap(currentMapIndex);
        if (newBgm != null) {
            // 既に同じBGMが流れているなら何もしない
            if (!newBgm.equals(getSoundmanager().getCurrentBgmName())) {
                // フェードアウト（非同期に実行される想定）
                getSoundmanager().fadeOutBGM(500);
                // SoundManager が非同期なら直接呼べばよい
                // 少し遅延を置いて新BGMを再生（フェード完了後に再生）
                try {
                    Thread.sleep(520);
                } catch (InterruptedException ignored) {
                }
                getSoundmanager().playBGM(newBgm);
            }
        }


        if (currentMapIndex == TileManager.HUT_TILE_ID) {

            Arrays.fill(npc, null);
            Arrays.fill(monster, null);
            Arrays.fill(obj, null);
            Arrays.fill(iTile, null);
            itemList.clear();
            projectileList.clear();
            particleList.clear();

            getKeyHandler().clearAllKeys();
            tileManager.loadMap(2);
            startMapTransition(2);
            getPlayer().setWorldX(tileSize * 29);
            getPlayer().setWorldY(tileSize * 14);
            assetSetter.setNpcMerChant();
            assetSetter.setNpcSave();

            repaint();

        } else if (currentMapIndex == TileManager.MEADOW_TILE_ID) {

            Arrays.fill(npc, null);
            getKeyHandler().clearAllKeys();
            tileManager.loadMap(1);
            startMapTransition(2);
            getPlayer().setWorldX(tileSize * 23);
            getPlayer().setWorldY(tileSize * 10);

            assetSetter.setNpcOldMan();
            assetSetter.setNpcChicken();
            assetSetter.setMonster();
            assetSetter.setInteractiveTile();
            assetSetter.setObjAxe();
            assetSetter.setObjChest();
            assetSetter.setObjPot();

            repaint();

        } else if (currentMapIndex == TileManager.FOREST_TILE_ID) {

            Arrays.fill(npc, null);
            Arrays.fill(monster, null);
            Arrays.fill(obj, null);
            Arrays.fill(iTile, null);
            itemList.clear();
            projectileList.clear();
            particleList.clear();

            getKeyHandler().clearAllKeys();
            tileManager.loadMap(3);
            startMapTransition(2);
            getPlayer().setWorldX(tileSize * 2);
            getPlayer().setWorldY(tileSize * 48);

            assetSetter.setBossMonster();

            repaint();

        }
    }

    /**
     * ゲーム画面および UI、デバッグテキストの描画を行う。
     * フェード中は黒のオーバーレイを適用し、タイトル画面とワールド描画を切り替える。
     *
     * @param g 描画に使用する Graphics オブジェクト
     * @throws IllegalArgumentException g が null の場合
     */

    @Override
    public void paintComponent(Graphics g) {

        long drawStart = System.nanoTime();

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == GameState.TITLE || gameState == GameState.LOAD) {
            getUi().draw(g2);

        } else {

            if (onTransition) {
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, Math.min(1f, Math.max(0f, alpha))
                ));
                g2.setColor(Color.black);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(old);
            }
            // フェードアウト中は前のマップ
            if (!onTransition || !fadingOut) {
                // 通常のワールド描画
                tileManager.draw(g2);
                renderEntitiesAndObjects(g2);
                ui.draw(g2);
            }

            // 遷移中は常に黒オーバーレイを描画
            if (onTransition) {
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, alpha
                ));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(old);
            }

            if (keyHandler.isShowDebugText()) {

                int tileSize = FrameApp.getTileSize();
                long drawEnd = System.nanoTime();
                long passed = drawEnd - drawStart;

                g2.setFont(new Font("アリアル", Font.PLAIN, 20));
                g2.setColor(Color.WHITE);

                int debugX = 10;
                int debugY = 400;
                int lineHeight = 20;
                double nanosecond = 1000000000.0;

                g2.drawString("描画時間: " + passed / nanosecond + "秒", debugX, debugY);
                debugY += lineHeight;

                g2.drawString("WorldX: " + player.getWorldX(), debugX, debugY);
                debugY += lineHeight;
                g2.drawString("WorldY: " + player.getWorldY(), debugX, debugY);
                debugY += lineHeight;

                int row = player.getWorldX() / tileSize;
                int col = player.getWorldY() / tileSize;

                g2.drawString("Row  : " + row, debugX, debugY);
                debugY += lineHeight;
                g2.drawString("Col    : " + col, debugX, debugY);
            }
        }
    }

    /**
     * レンダリング対象のエンティティおよびオブジェクトを描画。
     *
     * @param g2 描画に使用する Graphics2D オブジェクト
     * @throws IllegalArgumentException g2 が null の場合
     */

    private void renderEntitiesAndObjects(Graphics2D g2) {

        for (int i = 0; i < iTile.length; i++) {
            if (iTile[i] != null) {
                iTile[i].draw(g2);
            }
        }

        for (int i = 0; i < obj.length; i++) {
            if (obj[i] != null) {
                obj[i].draw(g2);
            }
        }

        for (Entity item : itemList) {
            if (item != null && item.getAlive()) {
                item.draw(g2);
            }
        }

        for (Entity paList : particleList) {
            if (paList != null) {
                paList.draw(g2);
            }
        }

        List<Entity> entityList = new ArrayList<>();

        entityList.add(player);

        for (Entity entity : npc) {
            if (entity != null) {
                entityList.add(entity);
            }
        }
        for (Entity entity : monster) {
            if (entity != null) {
                entityList.add(entity);
            }
        }
        for (Entity proList : projectileList) {
            if (proList != null && proList.getAlive()) {
                entityList.add(proList);
            }
        }

        entityList.sort(Comparator.comparingInt(Entity::getWorldY));

        for (Entity entity : entityList) {
            entity.draw(g2);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        entityList.clear();
    }

    /**
     * 現在のプレイヤーインスタンスを返す。
     *
     * @return Player オブジェクト
     */

    public Player getPlayer() {
        return player;
    }

    /**
     * 現在の KeyHandler インスタンスを返す。
     *
     * @return KeyHandler オブジェクト
     */

    public KeyHandler getKeyHandler() {
        return keyHandler;
    }

    /**
     * 現在の TileManager インスタンスを返す。
     *
     * @return TileManager オブジェクト
     */

    public TileManager getTileManager() {
        return tileManager;
    }

    /**
     * 現在の CollisionChecker インスタンスを返す。
     *
     * @return CollisionChecker オブジェクト
     */

    public CollisionChecker getCollisionChecker() {
        return collisionChecker;
    }

    /**
     * 現在のゲームステートを取得する。
     *
     * @return 現在の gameState 値
     */

    public synchronized GameState getGameState() {
        return gameState;
    }

    public synchronized void setGameState(GameState newState) {
        if (newState == null) throw new IllegalArgumentException("gameState must not be null");
        this.gameState = newState;
    }

    /**
     * 現在配置されている NPC 配列を返す。
     *
     * @return Entity 型の配列（npc）
     */

    public Entity[] getNPC() {
        return npc;
    }

    /**
     * 現在配置されているモンスター配列を返す。
     *
     * @return Entity 型の配列（monster）
     */

    public Entity[] getMonster() {
        return monster;
    }

    /**
     * NPC 配列を新しいものに置き換える。
     *
     * @param npc 新しい Entity 配列
     * @throws IllegalArgumentException npc が null の場合
     */

    public void setNPC(Entity[] npc) {
        this.npc = npc;
    }

    /**
     * モンスター配列を新しいものに置き換える。
     *
     * @param monster 新しい Entity 配列
     * @throws IllegalArgumentException monster が null の場合
     */

    public void setMonster(Entity[] monster) {
        this.monster = monster;
    }

    /**
     * UI 管理オブジェクトを返す。
     *
     * @return UI オブジェクト
     */

    public UI getUi() {
        return ui;
    }

    /**
     * サウンド管理オブジェクトを返す。
     *
     * @return SoundManager オブジェクト
     */

    public SoundManager getSoundmanager() {
        return soundManager;
    }

    /**
     * AssetSetter 管理オブジェクトを返す。
     *
     * @return AssetSetter オブジェクト
     */

    public AssetSetter getAssetSetter() {
        return assetSetter;
    }

    /**
     * 現在の Projectile リストを返す。
     *
     * @return ArrayList&lt;Projectile&gt; オブジェクト
     */

    public ArrayList<Projectile> getProjectileList() {
        return projectileList;
    }

    /**
     * 現在のパーティクルリストを返す。
     *
     * @return ArrayList&lt;Entity&gt; オブジェクト
     */

    public ArrayList<Entity> getParticleList() {
        return particleList;
    }

    /**
     * 現在のインタラクティブタイル配列を返す。
     *
     * @return InteractiveTile[] オブジェクト
     */

    public InteractiveTile[] getItile() {
        return iTile;
    }

    /**
     * インタラクティブタイル配列を置き換える。
     *
     * @param iTile 新しい InteractiveTile 配列
     * @throws IllegalArgumentException iTile が null の場合
     */

    public void setItile(InteractiveTile[] iTile) {
        this.iTile = iTile;
    }

    /**
     * 現在のオブジェクト配列を返す。
     *
     * @return Entity[] オブジェクト
     */

    public Entity[] getObj() {
        return obj;
    }

    /**
     * ダイアログアクティブ状態を設定する。
     *
     * @param active 新しいダイアログアクティブ状態
     * @return void
     */

    public void setDialogueActive(boolean active) {
        this.dialogueActive = active;
    }

    /**
     * アイテムをドロップし、ワールド座標をソースと同じ位置に設定する。
     *
     * @param droppedItem ドロップするアイテム Entity
     * @param source      ドロップ元となる Entity
     * @throws IllegalArgumentException droppedItem または source が null の場合
     */

    public void dropItem(Entity droppedItem, Entity source) {
        System.out.println("【dropItem】呼ばれた／itemList before=" + itemList.size());
        droppedItem.setWorldX(source.getWorldX());
        droppedItem.setWorldY(source.getWorldY());
        droppedItem.setAlive(true);
        itemList.add(droppedItem);
        System.out.println("【dropItem】 今の itemList サイズ=" + itemList.size());
    }

    /**
     * 指定された NPC インデックスのルート追従を開始する。
     *
     * @param npcIndex NPC 配列内のインデックス
     * @param mapId    ルート追従を行うマップID
     * @param pathId   ルートID
     * @throws IndexOutOfBoundsException npcIndex が範囲外の場合
     */

    public void startNpcRoute(int npcIndex, int mapId, int pathId) {
        if (npcIndex < 0 || npcIndex >= npc.length) return;
        Entity e = npc[npcIndex];
        if (e instanceof NpcOldMan oldMan) {
            oldMan.startRouteFollow(mapId, pathId);
        }
    }

    /**
     * デバッグ用にヒットボックス表示のトグルを行う。
     */

    public void toggleHitBoxDebug() {
        showHitBoxes = !showHitBoxes;
    }

    /**
     * マップ遷移中かどうかを返す。
     *
     * @return onTransition（boolean）
     */

    public boolean isOnTransition() {
        return onTransition;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }

    private String getBgmForMap(int mapId) {
        return mapBgmMap.get(mapId);
    }

    public long getLoadedPlayTimeSeconds() {
        return loadedPlayTimeSeconds;
    }

    public void setLoadedPlayTimeSeconds(long seconds) {
        this.loadedPlayTimeSeconds = seconds;
    }

    public map.GameMap getCurrentMap() {
        return currentMap;
    }

    public boolean addObject(Entity e) {
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] == null) {
                obj[i] = e;
                return true;
            }
        }
        return false;
    }
}