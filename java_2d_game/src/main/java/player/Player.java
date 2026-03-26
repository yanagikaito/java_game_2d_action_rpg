package player;

import entity.*;
import frame.FrameApp;
import game.GameState;
import key.KeyHandler;
import npc.NpcMerChant;
import npc.NpcSave;
import object.*;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Timer;

public class Player extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final String[] ATTACK_DIRECTIONS = {"attackUp", "attackDown", "attackLeft", "attackRight"};
    private static final String[] AXE_DIRECTIONS = {"axeUp", "axeDown", "axeLeft", "axeRight"};
    private static final int SPRITE_COUNT = 3;
    private static final int SPRITE_ANIMATION_THRESHOLD = 10;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM1 = 5;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM2 = 15;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM3 = 25;
    private static final int DEFAULT_INVENTORY_CAPACITY = 20;
    private static final int MAX_KB_FRAMES = 10;
    private static final int ATTACK_ANIMATION_FRAMES = SPRITE_ATTACKING_THRESHOLD_NUM3;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] attackSprites = new BufferedImage[ATTACK_DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] axeSprites = new BufferedImage[AXE_DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] currentAttackSprites;
    private int characterTypeId;
    private static final long FIRE_COOLDOWN_MS = 1000;
    private int interactIndex = 999;
    private long lastFireTime = 0;
    private int fireCooldown = 0;
    private int coin = 0;
    private static final int COOLDOWN_FRAMES = 60 * 3;
    private int invincibleCounter = 0;
    private final int INVINCIBLE_DURATION = 60; // 60フレーム無敵
    private int attackCounter;
    private boolean invincible = false;

    private GameWindow gameWindow;
    private KeyHandler keyHandler;
    private final int screenX;
    private final int screenY;
    private boolean moving = false;
    private int pixelCounter = 0;
    private final int playerSolidAreaX = 1;
    private final int playerSolidAreaY = 1;
    private static final int FIREBALL_MANA_COST = 20;
    private int talkNpcIndex = -1;
    private boolean loaded = false;

    private int knockbackTimer = 0;

    private double knockBackDx, knockBackDy;

    private static final int KNOCKBACK_FRAMES = 10;
    private static final double KNOCKBACK_SPEED = 4.0;

    // aura 用キャッシュ画像（事前レンダリング）
    private ObjAura aura;

    // 3分
    private static final long BLUE_POTION_DURATION_MS = 3 * 60 * 1000L;

    // 保存・表示する秒単位のプレイ時間
    private long playTimeSeconds = 0L;
    // フレームの端数（秒）を保持
    private double playTimeAccumulator = 0;
    // 999時間59分59秒 = 3,599,999 秒
    public static final long MAX_PLAY_SECONDS = 3_599_999L;


    /**
     * プレイヤーを初期化するコンストラクタ。
     *
     * @param gameWindow メインのゲームウィンドウ。描画やUIアクセスに使用する。
     * @param keyHandler キー入力ハンドラ。プレイヤーの移動や操作を受け付ける。
     */

    public Player(GameWindow gameWindow, KeyHandler keyHandler) {
        super(gameWindow);
        // inventory が null の可能性があるなら初期化しておく
        if (this.getInventory() == null) {
            this.setInventory(new ArrayList<>(DEFAULT_INVENTORY_CAPACITY));
        }
        this.gameWindow = gameWindow;
        this.keyHandler = keyHandler;
        this.spriteKey = "player_basic";
        this.aura = new ObjAura(gameWindow);
        initializeDefaultStats();

        screenX = FrameApp.getScreenWidth() / 2 - (FrameApp.getTileSize() / 2);
        screenY = FrameApp.getScreenHeight() / 2 - (FrameApp.getTileSize() / 2);

        setSolidArea(new Rectangle());
        getSolidArea().x = playerSolidAreaX;
        getSolidArea().y = playerSolidAreaY;

        setSolidAreaDefaultX(getSolidArea().x);
        setSolidAreaDefaultY(getSolidArea().y);

        getSolidArea().width = (int) (FrameApp.getTileSize() - 1.3);
        getSolidArea().height = (int) (FrameApp.getTileSize() - 1.2);


        setHitBoxX(getSolidArea().x);
        setHitBoxY(getSolidArea().y);
        setHitBoxWidth(getSolidArea().width);
        setHitBoxHeight(getSolidArea().height);

        setAttackArea(new Rectangle());
//        getAttackArea().width = 36;
//        getAttackArea().height = 36;
        loadPlayerImages();
        loadAllAttackSprites();
        setItems();
    }

    private void initializeDefaultStats() {
        if (loaded) return;

        setDefaultValues();
    }

    /**
     * プレイヤーのステータス、装備、初期経験値などをデフォルト値で設定する。
     * ゲーム開始時の初期化やリスタート時に呼び出す。
     */

    public void setDefaultValues() {

        setWorldX(FrameApp.getTileSize() * 23);
        setWorldY(FrameApp.getTileSize() * 21);
        setSpeed(4);
        setKnockBackPower(2);
        setDirection("down");

        setLevel(1);
        setMaxLife(6);
        setLife(getMaxLife());
        setMaxMana(100);
        setMana(getMaxMana());
        setStrength(1);
        setDexterity(1);
        setExp(0);
        setNextLevelExp(5);
        setCoin(500);
        setCurrentWeapon(new ObjSwordNormal(gameWindow));
        setCurrentShield(new ObjShieldWood(gameWindow));
        setProjectile(new ObjFireball(gameWindow));
        setProjectile(new ObjBomb(gameWindow));
        setAttack(calculateBaseAttack());
        setDefense(calculateBaseDefense());
    }

    /**
     * プレイヤーのワールド座標・速度・向きをデフォルトにリセットする。
     * ステータスや装備は変更せず、位置のみ初期化したい場合に使用する。
     */

    public void setDefaultPositions() {
        setWorldX(FrameApp.getTileSize() * 23);
        setWorldY(FrameApp.getTileSize() * 21);
        setDefaultSpeed(4);
        setSpeed(getDefaultSpeed());
        setDirection("down");
    }

    /**
     * プレイヤーのレベル、最大HP/MP、現在HP/MP、無敵状態をリセットする。
     * 主にゲームオーバー後のリスタート時などに全回復させたい場合に呼ぶ。
     */

    public void restoreLifeAndMan() {
        setLevel(1);
        setMaxLife(6);
        setLife(getMaxLife());
        setMaxMana(100);
        setMana(getMaxMana());
        setInvincible(false);
    }

    /**
     * インベントリをクリアし、初期アイテム（剣、盾、赤ポーション、緑ポーション）を装備・追加する。
     */

    public void setItems() {

        getInventory().clear();
        ArrayList<Entity> items = new ArrayList<>();
        items.add(getCurrentWeapon());
        items.add(getCurrentShield());
        items.add(new ObjRedPotion(gameWindow));
        items.add(new ObjGreenPotion(gameWindow));
        items.add(new ObjBluePotion(gameWindow));

        setInventory(items);
    }

    /**
     * 歩行スプライトをディレクション・フレームごとに読み込み、タイルサイズにリサイズする。
     * リソースパス: /player/image-{direction}-{frame}.gif
     */

    public void loadPlayerImages() {
        try {
            int tileSize = FrameApp.getTileSize();
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream("player/image-" + DIRECTIONS[dir] + "-" + (i + 1) + ".gif"));
                    BufferedImage processed = createImage(original, tileSize, tileSize);
                    sprites[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 剣用・斧用の攻撃スプライトを全方向・全フレーム分読み込み、
     * 向きに応じて縦長 or 横長にリサイズする。
     */

    private void loadAllAttackSprites() {

        int tileSize = FrameApp.getTileSize();

        // 剣用
        for (int d = 0; d < ATTACK_DIRECTIONS.length; d++) {
            for (int i = 0; i < SPRITE_COUNT; i++) {
                String path = "player/image-" + ATTACK_DIRECTIONS[d] + "-" + (i + 1) + ".gif";
                try {
                    BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
                    if (d == 0 || d == 1) {
                        attackSprites[d][i] = createImage(img, tileSize, tileSize * 2);
                    } else {
                        attackSprites[d][i] = createImage(img, tileSize * 2, tileSize);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 斧用
        for (int d = 0; d < AXE_DIRECTIONS.length; d++) {
            for (int i = 0; i < SPRITE_COUNT; i++) {
                String path = "player/image-" + AXE_DIRECTIONS[d] + "-" + (i + 1) + ".gif";
                try {
                    BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
                    if (d == 0 || d == 1) {
                        axeSprites[d][i] = createImage(img, tileSize, tileSize * 2);
                    } else {
                        axeSprites[d][i] = createImage(img, tileSize * 2, tileSize);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 装備中の武器タイプに応じて、攻撃スプライト配列を剣用か斧用に切り替える。
     */

    private void updateCurrentAttackSprites() {
        if (getCurrentWeapon().getType() instanceof AxeType) {
            currentAttackSprites = axeSprites;
        } else {
            currentAttackSprites = attackSprites;
        }
    }

    /**
     * 赤ポーションをインベントリの指定スロットから使用する。
     * HPを回復し、効果音を再生、インベントリからアイテムを削除する。
     *
     * @param index 使用するインベントリのスロット番号
     */

    public void useRedPotion(int index) {
        System.out.println("useRedPotion が呼ばれた index=" + index
                + " invSize=" + getInventory().size());

        if (index < 0 || index >= getInventory().size()) {
            System.out.println("index 範囲外で return");
            return;
        }

        Entity e = getInventory().get(index);

        if (e instanceof ObjRedPotion) {
            System.out.println("アイテムは RedPotion です。処理続行");
            ObjRedPotion potion = (ObjRedPotion) e;
            int heal = potion.getHealAmount();
            setLife(Math.min(getLife() + heal, getMaxLife()));
            // オーラ発動条件：HPが満タンになったら
            if (aura != null && getLife() == getMaxLife()) {
                long auraDurationMs = 30_000L; // 例: 30秒。仕様に合わせて変更
                aura.activate(auraDurationMs);
                gameWindow.getUi().addMessage("オーラが発動した！");
                System.out.println("DEBUG: RedPotion triggered aura.activate durationMs=" + auraDurationMs);
            }
            gameWindow.getUi().addMessage("レッドポーションを使った。HPが" + heal + "回復！");
            gameWindow.getSoundmanager().redPotionWAV("sound/potion-sound.wav");
            getInventory().remove(index);
            return;
        } else {
            System.out.println("選択アイテムはポーションではありません: " + e.getClass().getSimpleName());
        }
    }

    /**
     * 緑ポーションをインベントリの指定スロットから使用する。
     * 魔力を回復し、効果音を再生、インベントリからアイテムを削除する。
     *
     * @param index 使用するインベントリのスロット番号
     */

    public void useGreenPotion(int index) {
        System.out.println("useRedPotion が呼ばれた index=" + index
                + " invSize=" + getInventory().size());

        if (index < 0 || index >= getInventory().size()) {
            System.out.println("index 範囲外で return");
            return;
        }

        Entity e = getInventory().get(index);

        if (e instanceof ObjGreenPotion) {
            System.out.println("アイテムは GreenPotion です。処理続行");
            ObjGreenPotion potion = (ObjGreenPotion) e;
            int heal = potion.getHealAmount();
            setMana(Math.min(getMana() + heal, getMaxMana()));
            gameWindow.getUi().addMessage("グリーンポーションを使った。魔力が" + heal + "回復！");
            gameWindow.getSoundmanager().greenPotionWAV("sound/potion-sound.wav");
            getInventory().remove(index);
            return;
        } else {
            System.out.println("選択アイテムはポーションではありません: " + e.getClass().getSimpleName());
        }
    }

    public void useBluePotion(int index) {
        System.out.println("useBluePotion が呼ばれた index=" + index
                + " invSize=" + getInventory().size());

        if (index < 0 || index >= getInventory().size()) {
            System.out.println("index 範囲外で return");
            return;
        }

        Entity e = getInventory().get(index);

        if (e instanceof ObjBluePotion) {
            System.out.println("アイテムは BluePotion です。処理続行");
            ObjBluePotion potion = (ObjBluePotion) e;
            int heal = potion.getHealAmount();
            setMana(Math.min(getMana() + heal, getMaxMana()));
            gameWindow.getUi().addMessage("ブルーポーションを使った。MPが全回復！");
            gameWindow.getSoundmanager().redPotionWAV("sound/potion-sound.wav");
            getInventory().remove(index);
            return;
        } else {
            System.out.println("選択アイテムはポーションではありません: " + e.getClass().getSimpleName());
        }
    }

    /**
     * 現在の武器種別と向きをもとに攻撃アニメーションを開始する準備を行う。
     * axeなら「axe＋方向」、それ以外は「attack＋方向」を attackDirection に設定し、
     * 対応する攻撃スプライト配列を更新。
     */

    private void startAttack() {

        String base = capitalize(getDirection());
        if (getCurrentWeapon().getType() instanceof AxeType) {
            setAttackDirection("axe" + base);
        } else {
            setAttackDirection("attack" + base);
        }

        updateCurrentAttackSprites();
    }

    /**
     * 毎フレーム呼び出される更新処理。
     * 無敵時間のカウントダウン、攻撃クールダウンの減算、攻撃・移動入力の判定、
     * 衝突判定、アニメーション切り替え、タイル移動の実行などを行う。
     */

    @Override
    public void update() {

        updateAura(500);

        // 1) フレーム先頭で移動フラグと衝突フラグをリセット
        moving = false;
        setCollision(false);

        int tileSize = FrameApp.getTileSize();
        int playerGridX = getWorldX() / tileSize;
        int playerGridY = getWorldY() / tileSize;

        gameWindow.getCollisionChecker().checkTile(this);
        gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC());
        gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getObj());
        gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile());
        gameWindow.getEventHandler().checkEvent();
        int nearbyIndex = findNearbyObjectIndex();
        int collidedTileId = gameWindow.getTileManager().getTileIdAt(playerGridX, playerGridY);
        gameWindow.changeMap(collidedTileId);

        // changeMap のプレイヤー座標設定直後
        if (aura != null) {
            // プレイヤーのワールド座標に対するオフセットを設定
            int offsetX = 0;
            int offsetY = 0;
            aura.setWorldX(getWorldX() + offsetX);
            aura.setWorldY(getWorldY() + offsetY);
            aura.setMapId(collidedTileId);
        }

        // ノックバック優先
        if (knockbackTimer > 0) {

            // 毎フレームdx,dyだけ押し出す
            setWorldX(getWorldX() + (int) knockBackDx);
            setWorldY(getWorldY() + (int) knockBackDy);
            knockbackTimer--;

            // ノックバック終了なら重なり解消
            if (knockbackTimer == 0) {
                resolvePenetration();
            }
            return;
        }

        // 無敵時間・クールダウンなど
        if (getInvincible()) {
            if (++invincibleCounter > INVINCIBLE_DURATION) {
                setInvincible(false);
                invincibleCounter = 0;
            }
        }
        if (fireCooldown > 0) fireCooldown--;

        // 攻撃処理
        if (getAttacking()) {
            startAttack();
            playerAttacking();
            return;
        } else {
            if (gameWindow.getKeyHandler().isBombKeyPressed() && fireCooldown == 0) {
                fireCooldown = COOLDOWN_FRAMES;
                playerAttackingBomb();
            } else if (gameWindow.getKeyHandler().isShotKeyPressed() && fireCooldown == 0) {
                playerAttackingFireball();
            }
        }

        // 入力受付
        processInput();

        // ここでエンター押下をチェックして近接オブジェクトに対する処理を行う
        handleInteractInput(nearbyIndex);

        if (moving) {
            updateMovement();
            updateCollision();
            updateAnimation();
            updateTileMovement();
        }
        updateInvincibility();
    }

    public void updateAura(long deltaMs) {

        System.out.println("updateAura deltaMs=" + deltaMs + " ms");

        // 既存の移動・入力・アニメ更新処理...
        // プレイヤー座標にオーラを追従させる
        if (aura != null) {
            aura.setWorldX(this.getWorldX());
            aura.setWorldY(this.getWorldY());
            aura.updateAnimation(deltaMs);
            System.out.println("DEBUG: Enter判定 now=" + System.currentTimeMillis()
                    + " aura.isActive()=" + aura.isActive()
                    + " aura.expireAt=" + (aura != null ? aura.getExpireAt() : "null")
                    + " aura.activeFlag=" + (aura != null ? aura.isActiveRaw() : "null"));
        }
    }

    private void resolvePenetration() {
        for (Entity m : gameWindow.getMonster()) {
            if (m == null) continue;
            Rectangle2D playerB = this.getBounds();
            Rectangle2D monsterB = m.getBounds();
            if (playerB.intersects(monsterB)) {
                Rectangle2D overlap = playerB.createIntersection(monsterB);
                double w = overlap.getWidth(), h = overlap.getHeight();
                if (w < h) {
                    // 左右いずれかに押し出し
                    double shiftX = (playerB.getCenterX() < monsterB.getCenterX() ? -w : w);
                    setWorldX(getWorldX() + (int) shiftX);
                } else {
                    // 上下に押し出し
                    double shiftY = (playerB.getCenterY() < monsterB.getCenterY() ? -h : h);
                    setWorldY(getWorldY() + (int) shiftY);
                }
            }
        }
    }

    /**
     * キーハンドラから上下左右キーの押下をチェックし、
     * 押されていれば向きを設定して moving を true。
     * 一度に複数キーが押されても最初に判定された方向のみを採用。
     */

    private void processInput() {

        var keyHandler = gameWindow.getKeyHandler();

        if (keyHandler.isPlayerUp() || keyHandler.isPlayerDown()
                || keyHandler.isPlayerLeft() || keyHandler.isPlayerRight()) {
            if (keyHandler.isPlayerUp()) {
                setDirection("up");
            } else if (keyHandler.isPlayerDown()) {
                setDirection("down");
            } else if (keyHandler.isPlayerRight()) {
                setDirection("right");
            } else if (keyHandler.isPlayerLeft()) {
                setDirection("left");
            }
            moving = true;
        }
    }

    /**
     * 衝突判定フラグをチェックし、当たり判定に引っかかっていなければ
     * ワールド座標を currentSpeed 分だけ移動。
     */

    private void updateMovement() {

        if (!isCollision()) {

            switch (getDirection()) {
                case "up" -> setWorldY(getWorldY() - getSpeed());
                case "down" -> setWorldY(getWorldY() + getSpeed());
                case "left" -> setWorldX(getWorldX() - getSpeed());
                case "right" -> setWorldX(getWorldX() + getSpeed());
            }
        }
    }

    /**
     * プレイヤーの向きの前方に、指定タイル数（range）以内の NPC がいるかを検索。
     * マップIDとワールド座標が一致する NPC を見つけたらその配列インデックスを返す。
     *
     * @param npc   NPC オブジェクトの配列
     * @param range プレイヤーの向きに沿って何タイル先まで調べるか
     * @return 見つかった NPC の配列インデックス、見つからない場合は -1
     */

    public int checkNpcInFront(Entity[] npc, int range) {

        int px = this.getWorldX();
        int py = this.getWorldY();
        int tx = 0, ty = 0;

        // 向きに応じたベクトルを設定
        switch (this.getDirection()) {
            case "up":
                ty = -FrameApp.getTileSize();
                break;
            case "down":
                ty = FrameApp.getTileSize();
                break;
            case "left":
                tx = -FrameApp.getTileSize();
                break;
            case "right":
                tx = FrameApp.getTileSize();
                break;
        }

        // 1～range タイル先をチェック
        for (int i = 1; i <= range; i++) {
            int checkX = px + tx * i;
            int checkY = py + ty * i;

            for (int idx = 0; idx < npc.length; idx++) {
                Entity e = npc[idx];
                if (e != null
                        && e.getMapId() == this.mapId
                        && e.getWorldX() == checkX
                        && e.getWorldY() == checkY) {
                    return idx;
                }
            }
        }
        return -1;
    }

    /**
     * プレイヤーの衝突判定を更新。
     * タイル、NPC、オブジェクト、モンスター、インタラクティブタイルとの当たり判定を行い、
     * マップ切り替えやNPCとの会話開始、ダメージ処理を呼び出す。
     */

    private void updateCollision() {

        int tileSize = FrameApp.getTileSize();
        int playerGridX = getWorldX() / tileSize;
        int playerGridY = getWorldY() / tileSize;

        setCollision(false);
        gameWindow.getCollisionChecker().checkTile(this);

        int npcIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC());
        interactNPC(npcIndex);


        if (npcIndex != 999) {
            Entity e = gameWindow.getNPC()[npcIndex];
            if (e instanceof NpcMerChant) {
                // 商人との接触
                talkNpcIndex = npcIndex;
                gameWindow.setGameState(GameState.DIALOGUE);
                gameWindow.getUi().addDialogue(((NpcMerChant) e).getNextDialogue());
            } else if (e instanceof NpcSave) {
                talkNpcIndex = npcIndex;
                gameWindow.setGameState(GameState.DIALOGUE);
                gameWindow.getUi().addDialogue(((NpcSave) e).getNextDialogue());
            }
        }

        int objIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getObj());
        pickUpObject(objIndex);

        int monsterIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        if (monsterIndex != 999 && !getInvincible()) {
            Entity slime = gameWindow.getMonster()[monsterIndex];
            contactMonster(monsterIndex, this, slime.getKnockBackPower());
            setInvincible(true);

            int iTileIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile());
            damageInteractiveTile(iTileIndex);


            int collidedTileId = gameWindow.getTileManager().getTileIdAt(playerGridX, playerGridY);
            gameWindow.changeMap(collidedTileId);

        }
    }

    /**
     * プレイヤーのスプライトアニメーションを更新。
     * カウンタが閾値を超えたらフレーム番号を進め、ループ。
     */

    private void updateAnimation() {

        setSpriteCounter(getSpriteCounter() + 1);

        if (getSpriteCounter() > SPRITE_ANIMATION_THRESHOLD) {
            setSpriteNum((getSpriteNum() % SPRITE_COUNT) + 1);
            setSpriteCounter(0);
        }
    }

    /**
     * タイル移動中のピクセルカウンタを更新し、
     * 指定距離（タイルサイズ）移動し終えたら移動フラグをリセット。
     */

    private void updateTileMovement() {

        pixelCounter += getSpeed();

        if (pixelCounter >= FrameApp.getTileSize()) {
            moving = false;
            pixelCounter = 0;
        }
    }

    /**
     * 無敵状態および発射可能カウンタを更新。
     * 無敵時間が終了したら無敵状態を解除し、発射再使用待機時間を進める。
     */

    private void updateInvincibility() {

        if (getInvincible()) {
            setInvincibleCounter(getInvincibleCounter() + 1);
            if (getInvincibleCounter() > 60) {
                setInvincible(false);
                System.out.println("無敵状態が解除されました。");
                setInvincibleCounter(0);
            }
        }
        if (getShotAvailableCounter() < 30) {
            setShotAvailableCounter(getShotAvailableCounter() + 1);
        }
    }

    /**
     * 剣や斧による近接攻撃のアニメーションと当たり判定を処理。
     * スプライトフレームを進め、攻撃範囲内のモンスターやタイルにダメージを与え、
     * 攻撃終了後に位置や当たり判定を元に戻す。
     */

    public void playerAttacking() {

        attackCounter++;

        int counter = getSpriteCounter() + 1;
        setSpriteCounter(counter);

        if (counter <= SPRITE_ATTACKING_THRESHOLD_NUM1) {
            setSpriteNum(1);
//            System.out.println("counter:" + counter + " num:" + getSpriteNum());
        } else if (counter <= SPRITE_ATTACKING_THRESHOLD_NUM2) {
            setSpriteNum(2);
//            System.out.println("counter:" + counter + " num:" + getSpriteNum());
        } else if (counter <= SPRITE_ATTACKING_THRESHOLD_NUM3) {
            setSpriteNum(3);
//            System.out.println("counter:" + counter + " num:" + getSpriteNum());
        } else if (attackCounter >= ATTACK_ANIMATION_FRAMES) {
            // 状態をリセット
            attackCounter = 0;
            setSpriteCounter(0);
            setAttacking(false);
            setSpriteNum(1);
            return;
        }

        final int originalWorldX = getWorldX();
        final int originalWorldY = getWorldY();
        final int originalSolidWidth = getSolidArea().width;
        final int originalSolidHeight = getSolidArea().height;

        switch (getDirection()) {
            case "up" -> setWorldY(getWorldY() - getAttackArea().height);
            case "down" -> setWorldY(getWorldY() + getAttackArea().height);
            case "left" -> setWorldX(getWorldX() - getAttackArea().width);
            case "right" -> setWorldX(getWorldX() + getAttackArea().width);
            default -> {
            }
        }

        getSolidArea().width = FrameApp.getTileSize();
        getSolidArea().height = FrameApp.getTileSize();

        int monsterIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        damageMonster(monsterIndex, calculateTotalAttack(), getCurrentWeapon().getKnockBackPower());

        int iTileIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile());
        damageInteractiveTile(iTileIndex);

        setWorldX(originalWorldX);
        setWorldY(originalWorldY);
        getSolidArea().width = originalSolidWidth;
        getSolidArea().height = originalSolidHeight;
    }

    /**
     * オーラ有効かつ剣装備時に Enter で呼ぶ剣の射出処理。
     * ObjSwordProjectile を生成して gameWindow.getProjectileList() に追加する。
     */

    public void shootSword() {

        // 自分が発射した生存中の弾だけをチェックする
        if (getProjectile() != null && getProjectile().getAlive()) {
            return;
        }

        // 剣専用プロジェクタイルを生成
        ObjSwordProjectile sp = new ObjSwordProjectile(gameWindow);

        sp.set(
                getWorldX(),
                getWorldY(),
                getDirection(),
                true,
                this
        );

        sp.setLife(sp.getMaxLife());
        sp.setSpriteNum(1);
        sp.setSpriteCounter(0);

        // ここで projectileList に追加
        gameWindow.getProjectileList().add(sp);

        // 発射時の演出
        gameWindow.getSoundmanager().explosionWAV("sound/explosion-sound.wav");
        gameWindow.getUi().addMessage("剣を発射した！");

        // 追加：発射直後にプロジェクタイル側でエフェクトを初期化させる
        sp.onFire(getDirection());

        // 発射クールダウンやカウンタ類を必要に応じて設定
        setShotAvailableCounter(0);
        fireCooldown = COOLDOWN_FRAMES;
        lastFireTime = System.currentTimeMillis();
    }


    /**
     * ファイアボールを発射する処理を行う。
     * ショットキー入力、射出可能状態、マナ消費、クールダウン判定を満たしたときに
     * プロジェクタイルを生成し、リストに追加して効果音を再生。
     */

    public void playerAttackingFireball() {

        KeyHandler kh = gameWindow.getKeyHandler();

        if (kh.isShotKeyPressed() &&
                !getProjectile().getAlive() &&
                getShotAvailableCounter() == 30 &&
                consumeMana(FIREBALL_MANA_COST)) {

            fireCooldown = COOLDOWN_FRAMES;

            long now = System.currentTimeMillis();

            if (now - lastFireTime >= FIRE_COOLDOWN_MS) {
                lastFireTime = now;

                System.out.println("DEBUG: Fキーが押されている");

                ObjFireball fb = new ObjFireball(gameWindow);
                fb.set(
                        getWorldX(),
                        getWorldY(),
                        getDirection(),
                        true,
                        this
                );
                fb.setLife(fb.getMaxLife());
                fb.setSpriteNum(1);
                fb.setSpriteCounter(0);

                gameWindow.getProjectileList().add(fb);
                setShotAvailableCounter(0);
                gameWindow.getSoundmanager().explosionWAV("sound/explosion-sound.wav");
                System.out.println("DEBUG: ファイアボール発射！向き=" + getDirection());
            }
        }
    }

    public void playerAttackingBomb() {

        KeyHandler kh = gameWindow.getKeyHandler();

        if (kh.isBombKeyPressed() &&
                !getProjectile().getAlive() &&
                getShotAvailableCounter() == 30 &&
                consumeMana(FIREBALL_MANA_COST)) {

            fireCooldown = COOLDOWN_FRAMES;

            long now = System.currentTimeMillis();

            if (now - lastFireTime >= FIRE_COOLDOWN_MS) {
                lastFireTime = now;

                System.out.println("DEBUG: Fキーが押されている");

                ObjBomb bom = new ObjBomb(gameWindow);
                bom.set(
                        getWorldX(),
                        getWorldY(),
                        getDirection(),
                        true,
                        this
                );
                bom.setLife(bom.getMaxLife());
                bom.setSpriteNum(1);
                bom.setSpriteCounter(0);

                gameWindow.getProjectileList().add(bom);
                setShotAvailableCounter(0);
                gameWindow.getSoundmanager().explosionWAV("sound/explosion-sound.wav");
                System.out.println("DEBUG: ボム発射！向き=" + getDirection());
            }
        }
    }

    /**
     * 指定したオブジェクト配列インデックスのオブジェクトを拾う処理を行う。
     * インベントリに空きがあれば追加し、メッセージを表示。オブジェクトをマップから削除。
     *
     * @param i オブジェクト配列内のインデックス
     */

    public void pickUpObject(int i) {
        if (i == 999) return;

        Entity obj = gameWindow.getObj()[i];
        if (obj == null) return;

        // 宝箱なら拾わず開封処理を呼ぶ
        if (obj instanceof object.ObjChest) {
            object.ObjChest chest = (object.ObjChest) obj;
            chest.interact(this); // chest 内で中身生成・インベントリ追加・落下処理を行う

            // chest が開いて中身処理が終わったらマップ上の宝箱を削除
            if (chest.isOpened()) {
                gameWindow.getObj()[i] = null;
            }
            return;
        }

        // 通常アイテムの取得処理
        String text;
        if (getInventory().size() < getMaxInventorySize()) {
            getInventory().add(obj);
            text = obj.getName() + " を手に入れた!";
            updateInvincibility();
            gameWindow.getObj()[i] = null;
        } else {
            text = "手に入れていない!";
        }
        gameWindow.getUi().addMessage(text);
    }


    // 近接オブジェクトを探してインデックスを返す（見つからなければ 999）
    private int findNearbyObjectIndex() {
        Rectangle playerRect = new Rectangle(getWorldX() + getSolidArea().x,
                getWorldY() + getSolidArea().y,
                getSolidArea().width,
                getSolidArea().height);

        Entity[] objs = gameWindow.getObj(); // または gameWindow.getCurrentMap().getObjects()
        if (objs == null) return 999;

        for (int idx = 0; idx < objs.length; idx++) {
            Entity obj = objs[idx];
            if (obj == null) continue;
            if (!(obj instanceof object.ObjChest)) continue; // 宝箱以外も検出したければ条件を変える

            Rectangle objRect = new Rectangle(obj.getWorldX() + obj.getSolidArea().x,
                    obj.getWorldY() + obj.getSolidArea().y,
                    obj.getSolidArea().width,
                    obj.getSolidArea().height);

            if (playerRect.intersects(objRect)) {
                // 近接したらプロンプトを出す（毎フレーム出すと重複するなら UI 側で抑制）
                gameWindow.getUi().addMessage("エンターで調べる");
                return idx;
            }
        }
        return 999;
    }

    // Player.java
    private void handleInteractInput(int i) {
        if (i == 999) return;

        var keyHandler = gameWindow.getKeyHandler();
        if (!keyHandler.isPlayerEnter()) return; // 押した瞬間だけ true を返す実装を想定

        Entity obj = gameWindow.getObj()[i];
        if (obj == null) {
            return;
        }

        // 宝箱なら開封処理を呼ぶ（ObjChest が中身を生成してくれる）
        if (obj instanceof object.ObjChest) {
            object.ObjChest chest = (object.ObjChest) obj;
            chest.interact(this); // chest 内で opened = true にする、ドロップは chest.open() が行う

            // chest が開いて中身を取り終えたら配列から削除（配列管理の場合）
            if (chest.isOpened()) {
                gameWindow.getObj()[i] = null;
            }

            gameWindow.setGameState(GameState.DIALOGUE);
            return;
        }

        // 宝箱以外（地面アイテムなど）は従来どおりインベントリに入れる
        gameWindow.setGameState(GameState.DIALOGUE);
        String text;
        if (getInventory().size() < getMaxInventorySize()) {
            getInventory().add(obj);
//            text = obj.getName() + " を手に入れた!";
            // CollisionChecker や Player 側の拾う処理の直前に入れる
            System.out.println("DEBUG pickup attempt: obj=" + obj + " at " + obj.getWorldX() + "," + obj.getWorldY() + " stacktrace:");
            Thread.dumpStack();

            updateInvincibility();
            gameWindow.getObj()[i] = null;
        } else {
            text = "手に入れていない!";
        }
//        gameWindow.getUi().addMessage(text);
    }


    /**
     * NPCとのインタラクトを行う。
     * Enterキーが押されていれば、NPCが存在する場合は会話を開始し、
     * いない場合は近接攻撃を実行。
     *
     * @param i NPC配列内のインデックス
     */

    public void interactNPC(int i) {

        var keyHandler = gameWindow.getKeyHandler();

        if (keyHandler.isPlayerEnter()) {
            if (i != 999) {
                gameWindow.setGameState(GameState.DIALOGUE);
                gameWindow.getNPC()[i].speak();
            } else {
                // NPC がいない（素振り／攻撃）時の Enter 処理
                // ここで「オーラが有効かつ剣を装備している」なら剣を発射する
                boolean hasSwordEquipped = getCurrentWeapon() != null
                        && getCurrentWeapon().getType() instanceof SwordType;

                Player p = this;
                boolean auraActive = (aura != null && aura.isActive() && p.getLife() == p.getMaxLife());

                if (auraActive && hasSwordEquipped) {
                    // オーラ中かつ剣装備なら射出
                    shootSword();
                } else {
                    // 通常の近接攻撃（既存処理）
                    setAttackDirection("attack" + getDirection().substring(0, 1).toUpperCase()
                            + getDirection().substring(1));
                    setAttacking(true);
                    gameWindow.getSoundmanager().defeatedWAV("sound/thrust-sound.wav");
                }
            }
            keyHandler.setPlayerEnter(false);
        }
    }

    /**
     * プレイヤーからモンスターと接触した際のダメージ計算・HP減少・無敵状態移行・
     * ゲームオーバー判定を行う。
     *
     * @param monsterIndex モンスター配列内のインデックス
     */

    public void contactMonster(int monsterIndex, Entity attacker, int knockBackPower) {

        if (monsterIndex != 999) {
            gameWindow.getSoundmanager().damageWAV("sound/damage-sound.wav");

            Entity slime = gameWindow.getMonster()[monsterIndex];
            // 相対ベクトルを計算
            double dx = getWorldX() - slime.getWorldX();
            double dy = getWorldY() - slime.getWorldY();
            double len = Math.hypot(dx, dy);
            if (len == 0) {
                // 完全に重なっている場合は上方向に押し出す例
                dx = 0;
                dy = -1;
                len = 1;
            }
            // 単位ベクトル×速度
            knockBackDx = dx / len * KNOCKBACK_SPEED;
            knockBackDy = dy / len * KNOCKBACK_SPEED;
            knockbackTimer = KNOCKBACK_FRAMES;

            // 無敵時間・移動禁止フラグ
            moving = false;

            if (knockBackPower > 0) {
                setKnockBack(gameWindow.getMonster()[monsterIndex], attacker, slime.getKnockBackPower());
            }

            // スライムのダメージ量
            int damage = setAttack(Math.max(gameWindow.getMonster()[monsterIndex].getAttack() - calculateTotalDefense(), 1));
            if (damage < 0) {
                damage = 0;
            }

            System.out.println("isCollision() = " + isCollision());
            setLife(getLife() - damage);
            setInvincible(true);

            if (getLife() <= 0) {
                setLife(0);
                gameWindow.setGameState(GameState.GAME_OVER);
            }

//                System.out.println("モンスター衝突: " + i);
//                System.out.println("無敵状態: " + getInvincible());
//                System.out.println(i);
//                System.out.println("衝突時の無敵状態: " + getInvincible());
//                System.out.println("プレイヤーのHP: " + getLife());
        }
    }

    /**
     * 指定したモンスターにダメージを与える。
     * ダメージ音を再生し、HPが0以下になった場合は死亡演出、アイテムドロップ、
     * 経験値取得、レベルアップ判定、リスポーン処理を行う。
     *
     * @param i      モンスター配列内の対象インデックス（存在しない場合は999）
     * @param attack プレイヤーの攻撃力（防御値計算前の値）
     */

    public void damageMonster(int i, int attack, int knockBackPower) {

        long start = System.nanoTime();

        if (i != 999) {
            if (!gameWindow.getMonster()[i].getInvincible()) {
                gameWindow.getSoundmanager().damageWAV("sound/damage-sound.wav");

                if (knockBackPower > 0) {
                    setKnockBack(gameWindow.getMonster()[i], this, knockBackPower);
                }


                long end = System.nanoTime();

                System.out.println("サウンド再生にかかった時間: " + (end - start) + " ns");

                // プレイヤーのダメージ量
                int damage = setAttack(attack - gameWindow.getMonster()[i].getDefense());
                if (damage < 0) {
                    damage = 0;
                }
                gameWindow.getMonster()[i].setLife(gameWindow.getMonster()[i].getLife() - damage);
                gameWindow.getUi().addMessage(damage + "ダメージ!");
                gameWindow.getMonster()[i].setInvincible(true);
                gameWindow.getMonster()[i].damageReaction();
                System.out.println("スライムのHP:" + gameWindow.getMonster()[i].getLife());

                if (gameWindow.getMonster()[i].getLife() <= 0) {

                    Entity monster = gameWindow.getMonster()[i];

                    gameWindow.getMonster()[i].setAlive(false);
                    gameWindow.getMonster()[i].setDying(true);

                    List<Supplier<Entity>> drops = Arrays.asList(
                            () -> new ObjCoinBronze(gameWindow),
                            () -> new ObjRedPotion(gameWindow),
                            () -> new ObjGreenPotion(gameWindow)
                    );

                    Collections.shuffle(drops);
                    Entity dropped = drops.get(0).get();

                    gameWindow.dropItem(dropped, monster);
                    System.out.println("drops = " + drops);

                    gameWindow.getUi().addMessage(gameWindow.getMonster()[i].getName() + "を倒した!");
                    int gainedExp = gameWindow.getMonster()[i].getExp();
                    setExp(getExp() + gainedExp);
                    gameWindow.getUi().addMessage("経験値" + gainedExp + " 入手!");
                    checkLevelUp();
                    gameWindow.getSoundmanager().defeatedWAV("sound/defeated-sound.wav");

                    int aliveCount = 0;
                    for (Entity m : gameWindow.getMonster()) {
                        if (m != null && !m.getDying() && m.getLife() > 0) {
                            aliveCount++;
                        }
                    }

                    if (aliveCount == 0 && !monster.isRespawning()) {
                        monster.setRespawning(true);

                        Timer respawnTimer = getTimer(monster);
                        respawnTimer.start();
                    }
                }
            }
        }
    }

    /**
     * インタラクティブタイルに対して破壊判定を行い、
     * 正しいアイテム使用時にダメージ音再生・パーティクル生成・ライフ減少・
     * 破壊完了後は破壊済みオブジェクトへ置き換える。
     *
     * @param i インタラクティブタイル配列内の対象インデックス（存在しない場合は999）
     */

    public void damageInteractiveTile(int i) {

        if (!gameWindow.getPlayer().getAttacking()) return;

        if (i != 999 && gameWindow.getItile()[i].isDestructible()
                && gameWindow.getItile()[i].isCorrectItem(this) == true
                && gameWindow.getItile()[i].getInvincible() == false) {

            gameWindow.getSoundmanager().damageWAV("sound/thrust-sound.wav");
            gameWindow.getItile()[i].setLife(gameWindow.getItile()[i].getLife() - 1);
            gameWindow.getItile()[i].setInvincible(true);
            generateParticle(gameWindow.getItile()[i], gameWindow.getItile()[i]);

            if (gameWindow.getItile()[i].getLife() == 0) {
                gameWindow.getItile()[i] = gameWindow.getItile()[i].createDestroyedForm();
            }
        }
    }

    /**
     * モンスターのリスポーン処理用タイマーを生成。
     * 指定時間後にAssetSetterで再度モンスターを配置し、リスポーンフラグを解除。
     *
     * @param monster リスポーン対象のモンスターエンティティ
     * @return 一度だけ実行される javax.swing.Timer インスタンス
     */

    private Timer getTimer(Entity monster) {
        Timer respawnTimer = new Timer(5000, e -> {
            gameWindow.getAssetSetter().setMonster();
            monster.setRespawning(false);
            ((Timer) e.getSource()).stop();
        });
        respawnTimer.setRepeats(false);
        return respawnTimer;
    }

    /**
     * 現在の経験値が次レベル経験値を超えているかを判定、
     * レベルアップ時には各ステータスを強化、ダイアログ表示、HP全回復を行う。
     */

    public void checkLevelUp() {

        if (getExp() >= getNextLevelExp()) {

            setLevel(getLevel() + 1);
            setNextLevelExp(getNextLevelExp() * 2);
            setMaxLife(getMaxLife() + 2);
            setStrength(getStrength() + 1);
            setDexterity(getDexterity() + 1);
            setAttack(calculateTotalAttack());
            setDefense(calculateTotalDefense());
            gameWindow.getSoundmanager().levelWAV("sound/level-up-sound.wav");
            gameWindow.setGameState(GameState.DIALOGUE);
            gameWindow.getUi().setCurrentDialogueMessage("プレイヤーはレベル" + gameWindow.getPlayer().getLevel() + "になった!");
            gameWindow.getPlayer().setLife(gameWindow.getPlayer().getMaxLife());
            aura.setActive(true);
        }
    }

    /**
     * インベントリ内の選択スロットからアイテムを取得し、
     * 武器／盾なら装備を更新、ポーションなら使用処理を呼び出す。
     *
     * @param index メニュー画面で選択されたインベントリスロットのインデックス
     */

    public void selectItem(int index) {

        int itemIndex = gameWindow.getUi().getItemIndexOnSlot(gameWindow.getUi().getPlayerSlotRow()
                , gameWindow.getUi().getPlayerSlotCol());

        if (itemIndex < getInventory().size()) {

            Entity selectedItem = getInventory().get(itemIndex);
            System.out.println("selectedItem = " + selectedItem);

            if (selectedItem.getType() instanceof SwordType || selectedItem.getType() instanceof AxeType) {

                setCurrentWeapon(selectedItem);
                setAttack(calculateTotalAttack());
                loadAllAttackSprites();
            }
            if (selectedItem.getType() instanceof ShieldType) {

                setCurrentShield(selectedItem);
                setDefense(calculateTotalDefense());
            }
            if (selectedItem.getType() instanceof RedPotionType) {
                useRedPotion(index);
            }
            if (selectedItem.getType() instanceof GreenPotionType) {
                useGreenPotion(index);
            }
            if (selectedItem.getType() instanceof BluePotionType) {
                useBluePotion(index);
            }
        }
    }

    /**
     * 指定アイテムが現在装備中の武器または盾かどうかを判定。
     *
     * @param item 判定対象のエンティティ
     * @return 装備中であれば true、そうでなければ false
     */

    public boolean isEquipped(Entity item) {
        if (item == null) return false;
        return item.equals(getCurrentWeapon())
                || item.equals(getCurrentShield());
    }

    /**
     * プレイヤーおよび攻撃スプライトを画面に描画。
     * 無敵状態時は半透明、攻撃中は武器種別ごとの拡大スプライトを使用。
     *
     * @param g2 描画用Graphics2Dオブジェクト
     */

    @Override
    public void draw(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

        int screenX = getWorldX()
                - gameWindow.getPlayer().getWorldX()
                + gameWindow.getPlayer().getScreenX();

        int screenY = getWorldY()
                - gameWindow.getPlayer().getWorldY()
                + gameWindow.getPlayer().getScreenY();

        Composite original = g2.getComposite();
        if (getInvincible()) {
            g2.setComposite(
                    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
            );
        }

        // aura の描画
        if (aura != null) {
            // ワールド座標が未初期化(0,0)なら描画しない
            if (aura.getWorldX() == 0 && aura.getWorldY() == 0) {
                // 初期化待ちのフレームなので描画をスキップ
            } else {
                // プレイヤーのHPが満タンのときだけaura描画
                Player p = this;
                if (p.getLife() == p.getMaxLife()) {
                    aura.setActive(true);
                    int auraScreenX = aura.getWorldX()
                            - gameWindow.getPlayer().getWorldX()
                            + gameWindow.getPlayer().getScreenX();
                    int auraScreenY = aura.getWorldY()
                            - gameWindow.getPlayer().getWorldY()
                            + gameWindow.getPlayer().getScreenY();

                    auraScreenX += 8;

                    float auraScale = 3.2f;
                    float auraAlpha = 0.7f;

                    aura.drawAt(g2, auraScreenX, auraScreenY, auraScale, auraAlpha);
                }
            }
        }

        BufferedImage img;
        if (!getAttacking()) {
            int walkDirIndex = Arrays.asList(DIRECTIONS)
                    .indexOf(getDirection());
            if (walkDirIndex >= 0) {
                img = sprites[walkDirIndex][getSpriteNum() - 1];
                g2.drawImage(img, screenX, screenY, tileSize, tileSize, null);

                // デバッグ
//              g2.setColor(new Color(255, 0, 0, 255));
//              g2.drawRect(screenX, screenY, getSolidArea().width, getSolidArea().height);
            }
        } else {
            boolean isAxe = getCurrentWeapon().getType() instanceof AxeType;
            String[] animationKeys = isAxe
                    ? AXE_DIRECTIONS
                    : ATTACK_DIRECTIONS;
            BufferedImage[][] spriteSet = isAxe
                    ? axeSprites
                    : attackSprites;

            int directionIndex = Arrays.asList(animationKeys)
                    .indexOf(getAttackDirection());
            if (directionIndex >= 0) {
                int frameIndex = Math.max(0,
                        Math.min(getSpriteNum() - 1, SPRITE_COUNT - 1)
                );
                img = spriteSet[directionIndex][frameIndex];

                int drawWidth = (directionIndex == 2 || directionIndex == 3)
                        ? tileSize * 2 : tileSize;
                int drawHeight = (directionIndex == 0 || directionIndex == 1)
                        ? tileSize * 2 : tileSize;
                int drawX = animationKeys[directionIndex]
                        .endsWith("Left") ? screenX - tileSize : screenX;
                int drawY = animationKeys[directionIndex]
                        .endsWith("Up") ? screenY - tileSize : screenY;

                g2.drawImage(img, drawX, drawY, drawWidth, drawHeight, null);
            }
        }

        g2.setComposite(original);
    }

    /**
     * BufferedImage を指定サイズにリサイズして返す。
     *
     * @param original 元画像
     * @param width    新しい幅（ピクセル）
     * @param height   新しい高さ（ピクセル）
     * @return 指定サイズにリサイズされた BufferedImage
     */

    private BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    @Override
    public String getSpriteKey() {
        // 装備やスキンで切り替えるロジック
        if (characterTypeId == 1) return "player_knight";
        return spriteKey;
    }

    /**
     * 画面上におけるプレイヤーの描画位置X座標を取得。
     *
     * @return プレイヤーの画面上X座標
     */

    public int getScreenX() {
        return screenX;
    }

    /**
     * 画面上におけるプレイヤーの描画位置Y座標を取得。
     *
     * @return プレイヤーの画面上Y座標
     */

    public int getScreenY() {
        return screenY;
    }

    /**
     * プレイヤーの移動状態を設定する。
     *
     * @param moving true に設定すると移動中、false に設定すると停止状態
     */

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    /**
     * 装備中の武器が持つ攻撃範囲を基本攻撃範囲として設定。
     */

    public void calculateBaseAttackArea() {
        setAttackArea(getCurrentWeapon().getAttackArea());
    }

    /**
     * プレイヤーの基本攻撃力（筋力に依存）を計算して返す。
     *
     * @return 基本攻撃力
     */

    public int calculateBaseAttack() {
        return getStrength();
    }

    /**
     * 装備中の武器から得られる攻撃力ボーナスを計算して返す。
     *
     * @return 武器ボーナス値（装備なし時は1）
     */

    public int calculateWeaponBonus() {
        return getCurrentWeapon() != null
                ? getCurrentWeapon().getAttackValue()
                : 1;
    }

    /**
     * 総合攻撃力を計算して返す。
     * 基本攻撃範囲を設定したうえで、基本攻撃力 × 武器ボーナス を返す。
     *
     * @return 総合攻撃力
     */

    public int calculateTotalAttack() {
        calculateBaseAttackArea();
        return calculateBaseAttack()
                * calculateWeaponBonus();
    }

    /**
     * プレイヤーの基本防御力（器用さに依存）を計算して返す。
     *
     * @return 基本防御力
     */

    public int calculateBaseDefense() {
        return getDexterity();
    }

    /**
     * 装備中の盾から得られる防御力ボーナスを計算して返す。
     *
     * @return 盾ボーナス値（装備なし時は1）
     */

    public int calculateShieldBonus() {
        return getCurrentShield() != null
                ? getCurrentShield().getDefenseValue()
                : 1;
    }

    /**
     * 総合防御力を計算して返す。
     * 基本防御力 × 盾ボーナス を返す。
     *
     * @return 総合防御力
     */

    public int calculateTotalDefense() {
        return calculateBaseDefense()
                * calculateShieldBonus();
    }

    /**
     * 指定されたマナコストを消費できるか判定し、可能ならマナを減算。
     *
     * @param cost 消費するマナ量
     * @return 消費可能なら true（消費後）、マナ不足なら false（消費せず）
     */

    public boolean consumeMana(int cost) {
        if (getMana() < cost) return false;
        setMana(getMana() - cost);
        return true;
    }

    /**
     * ブロンズコインを拾ったときの処理を行う。
     * 所持ゴールドに値を加算し、UIにメッセージを表示。
     *
     * @param coin 拾ったブロンズコインオブジェクト
     */

    public void addCoin(ObjCoinBronze coin) {
        int value = coin.getValue();
        setCoin(getCoin() + value);
        gameWindow.getUi().addMessage(
                "コインを拾った。所持金が" + value + "獲得！"
        );
    }

    /**
     * 赤ポーションを拾ったときの処理を行う。
     * 最大HPを超えない範囲でライフを回復し、UIにメッセージを表示。
     *
     * @param potion 拾った赤ポーションオブジェクト
     */

    public void healRedPotion(ObjRedPotion potion) {
        int amount = potion.getHealAmount();
        setLife(Math.min(getLife() + amount, getMaxLife()));
        gameWindow.getUi().addMessage(
                "レッドポーションを拾った。HPが" + amount + "回復！"
        );
    }

    /**
     * 緑ポーションを拾ったときの処理を行う。
     * 最大MPを超えない範囲でマナを回復し、UIにメッセージを表示。
     *
     * @param potion 拾った緑ポーションオブジェクト
     */

    public void healGreenPotion(ObjGreenPotion potion) {
        int amount = potion.getHealAmount();
        setMana(Math.min(getMana() + amount, getMaxMana()));
        gameWindow.getUi().addMessage(
                "グリーンポーションを拾った。MPが" + amount + "回復！"
        );
    }

    /**
     * 文字列の先頭文字を大文字化して返す。
     *
     * @param s 対象文字列
     * @return 先頭が大文字になった文字列
     */

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * NPCとの会話対象インデックスを取得。
     *
     * @return 会話対象のNPC配列インデックス
     */

    public int getTalkNpcIndex() {
        return talkNpcIndex;
    }

    /**
     * NPCとの会話対象インデックスを設定。
     *
     * @param talkNpcIndex 会話対象のNPC配列インデックス
     */

    public void setTalkNpcIndex(int talkNpcIndex) {
        this.talkNpcIndex = talkNpcIndex;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void takeDamage(int dmg) {
        setLife(getLife() - dmg);
        gameWindow.getUi().addMessage("何かにぶつかり" + dmg + "ダメージ!");
        if (getLife() <= 0) {
            gameWindow.setGameState(GameState.GAME_OVER);
        }
    }

    // プレイ時間の getter/setter
    public long getPlayTimeSeconds() {
        return playTimeSeconds;
    }

    // setter（ロード時などに呼ぶ。上限でクランプ）
    public void setPlayTimeSeconds(long seconds) {
        if (seconds < 0) seconds = 0;
        this.playTimeSeconds = Math.min(seconds, MAX_PLAY_SECONDS);
        this.playTimeAccumulator = 0.0;
    }

    /**
     * 毎フレーム呼ぶ。deltaSeconds はそのフレームの経過秒
     */

    public void updatePlayTime(double deltaSeconds) {
        if (deltaSeconds <= 0) return;
        if (playTimeSeconds >= MAX_PLAY_SECONDS) return; // 既にカンストしていれば何もしない

        playTimeAccumulator += deltaSeconds;
        while (playTimeAccumulator >= 1.0) {
            playTimeAccumulator -= 1.0;
            if (playTimeSeconds < MAX_PLAY_SECONDS) {
                playTimeSeconds++;
                if (playTimeSeconds >= MAX_PLAY_SECONDS) {
                    playTimeSeconds = MAX_PLAY_SECONDS;
                    // 必要ならここでカンスト時のイベントを発火
                }
            } else {
                // カンスト到達後はループを抜ける
                playTimeAccumulator = 0.0;
                break;
            }
        }
    }

    /**
     * 単一アイテムをインベントリに追加するラッパー。
     * 成功したら true、満杯などで失敗したら false を返す。
     */

    public boolean setInventory(Entity object) {
        if (object == null) return false;

        // コインは所持金として扱う場合の特別処理
        if (object instanceof object.ObjCoinBronze) {
            object.ObjCoinBronze coin = (object.ObjCoinBronze) object;
            addCoin(coin);
            // マップに置かれたインスタンスを削除する責務は呼び出し側で行うかここで行う
            return true;
        }

        // インベントリ容量チェック（簡易）
        int capacity = DEFAULT_INVENTORY_CAPACITY;
        // もし capacity をフィールドで持っているならそちらを使う
        if (this.getInventory().size() >= capacity) {
            return false;
        }

        // アイテムをインベントリに追加
        this.getInventory().add(object);
        return true;
    }
}