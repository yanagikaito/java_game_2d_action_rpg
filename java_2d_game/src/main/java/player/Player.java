package player;

import entity.*;
import entity.type.*;
import frame.FrameApp;
import game.GameState;
import key.KeyHandler;
import npc.NpcChicken;
import npc.NpcMerChant;
import npc.NpcSave;
import object.*;
import popup.PopupVariant;
import tile.Tile;
import tile.TileManager;
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

    // 状態列挙
    public enum PlayerState {IDLE, WALK, PICKUP, HOLD_WALK, THROW}

    private PlayerState state = PlayerState.IDLE;

    // 持つ処理用
    private boolean holding = false;
    private ObjBomb heldBomb = null;
    private ObjPot heldPot = null;
    private ObjRock heldRock = null;
    private NpcChicken heldChicken = null;
    private Map<String, Point> holdOffset = new HashMap<>();

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final String[] ATTACK_DIRECTIONS = {"attackUp", "attackDown", "attackLeft", "attackRight"};
    private static final String[] GUARD_DIRECTIONS = {"guardUp", "guardDown", "guardLeft", "guardRight"};
    private static final String[] AXE_DIRECTIONS = {"axeUp", "axeDown", "axeLeft", "axeRight"};
    private static final int SPRITE_COUNT = 3;
    private static final int PICKUP_SPRITE_COUNT = 4;
    private static final int SPRITE_ANIMATION_THRESHOLD = 10;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM1 = 5;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM2 = 15;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM3 = 25;
    private static final int SPRITE_GUARDING_THRESHOLD_NUM1 = 5;
    private static final int SPRITE_GUARDING_THRESHOLD_NUM2 = 15;
    private static final int SPRITE_GUARDING_THRESHOLD_NUM3 = 25;
    private static final int DEFAULT_INVENTORY_CAPACITY = 20;
    private static final int ATTACK_ANIMATION_FRAMES = SPRITE_ATTACKING_THRESHOLD_NUM3;
    private static final int GUARD_ANIMATION_FRAMES = SPRITE_GUARDING_THRESHOLD_NUM3;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] attackSprites = new BufferedImage[ATTACK_DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] guardSprites = new BufferedImage[GUARD_DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] axeSprites = new BufferedImage[AXE_DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] pickupSprites = new BufferedImage[DIRECTIONS.length][PICKUP_SPRITE_COUNT];
    private BufferedImage[][] currentAttackSprites;
    private BufferedImage[][] currentGuardSprites;
    private int characterTypeId;
    private static final long FIRE_COOLDOWN_MS = 1000;
    private long lastSnapTime = 0;
    private static final long SNAP_COOLDOWN_MS = 150; // 100ms
    private long lastFireTime = 0;
    private int fireCooldown = 0;
    private int bombCooldown = 0;
    private static final int COOLDOWN_FRAMES = 60 * 3;
    private int invincibleCounter = 0;
    private final int INVINCIBLE_DURATION = 60; // 60フレーム無敵
    private int attackCounter;
    private int guardCounter;

    private boolean blockingLeft = false;

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

    // 保存・表示する秒単位のプレイ時間
    private long playTimeSeconds = 0L;
    // フレームの端数（秒）を保持
    private double playTimeAccumulator = 0;
    // 999時間59分59秒 = 3,599,999 秒
    public static final long MAX_PLAY_SECONDS = 3_599_999L;

    private int pickupCooldown = 0;
    private static final int PICKUP_COOLDOWN_FRAMES = 5;
    // ピックアップ対象を保持しておく（アニメ終了時に実際に取得する）
    private int pendingPickupIndex = -1;

    public static final int SNAP_PENETRATION_THRESHOLD = 4;

    public long lastSnapTimeMs = 0;
    public int lastSafeWorldX = 0;
    public int lastSafeWorldY = 0;
    public int lastSafeFrame = 0;
    private int lastSafeSaWidth = 0;
    private int lastSafeSaHeight = 0;
    private static final int STUCK_THRESHOLD_FRAMES = 6; // 挟まり判定までのフレーム
    private static final int UNSTUCK_PUSH_MAX = 4; // 最大押し出しpx
    private static final int UNSTUCK_BFS_RADIUS = 6; // BFS 探索半径（タイル単位）
    // クラスフィールド
    private int lastAttemptStepX = 0;
    private int lastAttemptStepY = 0;
    private boolean attemptedMoveThisFrame = false;


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

        getSolidArea().width = (FrameApp.getTileSize() - 8);
        getSolidArea().height = (FrameApp.getTileSize() - 8);

        setHitBoxX(getSolidArea().x);
        setHitBoxY(getSolidArea().y);
        setHitBoxWidth(getSolidArea().width);
        setHitBoxHeight(getSolidArea().height);

        setAttackArea(new Rectangle());
//        getAttackArea().width = 36;
//        getAttackArea().height = 36;
        loadPlayerImages();
        loadAllAttackSprites();
        loadGuardSprites();
        loadPickupSprites();
        initHoldOffsets();
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

    private void initHoldOffsets() {
        // 向きごとの描画オフセット
        holdOffset.put("down", new Point(+6, -10));
        holdOffset.put("up", new Point(+4, -18));
        holdOffset.put("left", new Point(-8, -6));
        holdOffset.put("right", new Point(+14, -6));
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

    private void loadGuardSprites() {

        int tileSize = FrameApp.getTileSize();

        // 盾
        for (int d = 0; d < GUARD_DIRECTIONS.length; d++) {
            for (int i = 0; i < SPRITE_COUNT; i++) {
                String path = "player/image-" + GUARD_DIRECTIONS[d] + "-" + (i + 1) + ".gif";
                try {
                    BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
                    if (d == 0 || d == 1) {
                        guardSprites[d][i] = createImage(img, tileSize, tileSize);
                    } else {
                        guardSprites[d][i] = createImage(img, tileSize, tileSize);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * pickup（持ち上げ）用スプライトを読み込む。
     * リソース名は "player/image-pickup-{direction}-{frame}.gif" を想定。
     * 読み込み失敗時は既存の歩行フレームを代替としてセットする。
     */

    private void loadPickupSprites() {
        int tileSize = FrameApp.getTileSize();
        for (int dir = 0; dir < DIRECTIONS.length; dir++) {
            for (int i = 0; i < PICKUP_SPRITE_COUNT; i++) {
                String path = "player/image-pickup-" + DIRECTIONS[dir] + "-" + (i + 1) + ".gif";
                try {
                    var is = getClass().getClassLoader().getResourceAsStream(path);
                    if (is == null) {
                        throw new IOException("resource not found: " + path);
                    }
                    BufferedImage img = ImageIO.read(is);
                    pickupSprites[dir][i] = createImage(img, tileSize, tileSize);
                } catch (IOException e) {
                    // ロード失敗時はログを出して既存歩行フレームを代替に使う
                    System.err.println("pickup sprite load failed: " + path + " -> using fallback sprite");
                    // sprites が読み込まれていれば同方向の第0フレームを代替にする
                    if (sprites != null && sprites.length > dir && sprites[dir][0] != null) {
                        pickupSprites[dir][i] = sprites[dir][0];
                    } else {
                        // 最終手段：空の画像を作る（NullPointer を避ける）
                        pickupSprites[dir][i] = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                    }
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

    private void updateCurrentGuardSprites() {
        if (getCurrentShield().getType() instanceof ShieldType) {
            currentGuardSprites = guardSprites;
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
                long auraDurationMs = 30_000L;
                aura.activate(auraDurationMs);
                System.out.println("DEBUG: RedPotion triggered aura.activate durationMs=" + auraDurationMs);
            }
            int tileSize = FrameApp.getTileSize();
            int sx = this.getScreenX();
            int sy = this.getScreenY();

            sx += tileSize / 2;
            sy -= tileSize / 2;
            PopupVariant variant = PopupVariant.HEAL;
            gameWindow.getUi().getDamagePopupManager().pop(String.valueOf(heal), sx, sy, variant, 60);
            gameWindow.getSoundmanager().redPotionWAV("sound/potion-sound.wav");
            // スタック処理：1個だけ減らす。0ならスロット削除
            int current = potion.getAmount();
            if (current > 1) {
                potion.setAmount(current - 1);
            } else {
                // 1個だったのでスロットを削除
                getInventory().remove(index);
            }
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

            int tileSize = FrameApp.getTileSize();
            int sx = this.getScreenX();
            int sy = this.getScreenY();

            sx += tileSize / 2;
            sy -= tileSize / 2;
            PopupVariant variant = PopupVariant.HEAL;
            gameWindow.getUi().getDamagePopupManager().pop(String.valueOf(heal), sx, sy, variant, 60);
            gameWindow.getSoundmanager().greenPotionWAV("sound/potion-sound.wav");
            // スタック処理：1個だけ減らす。0ならスロット削除
            int current = potion.getAmount();
            if (current > 1) {
                potion.setAmount(current - 1);
            } else {
                // 1個だったのでスロットを削除
                getInventory().remove(index);
            }
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
            gameWindow.getSoundmanager().redPotionWAV("sound/potion-sound.wav");
            // スタック処理：1個だけ減らす。0ならスロット削除
            int current = potion.getAmount();
            if (current > 1) {
                potion.setAmount(current - 1);
            } else {
                // 1個だったのでスロットを削除
                getInventory().remove(index);
            }
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

    private void startGuard() {

        String base = capitalize(getDirection());
        if (getCurrentShield().getType() instanceof ShieldType) {
            setGuardDirection("guard" + base);
        }

        updateCurrentGuardSprites();
    }

    public void startBlockingLeft() {
        if (!hasLeftShield()) return;
        if (!blockingLeft) {
            blockingLeft = true;
            setGuarding(true); // 既存のガード状態と同期
            // アニメや音、スタミナ処理など
        }
    }

    public void stopBlockingLeft() {
        if (blockingLeft) {
            blockingLeft = false;
            setGuarding(false);
            // アニメ停止など
        }
    }

    /**
     * 毎フレーム呼び出される更新処理。
     * 無敵時間のカウントダウン、攻撃クールダウンの減算、攻撃・移動入力の判定、
     * 衝突判定、アニメーション切り替え、タイル移動の実行などを行う。
     */

    @Override
    public void update() {

        if (pickupCooldown > 0) pickupCooldown--;

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
                // 透明フラグを解除して通常表示に戻す
                setTransparent(false);
                invincibleCounter = 0;
            }
        }
        if (bombCooldown > 0) bombCooldown--;
        if (fireCooldown > 0) fireCooldown--;

        // 攻撃処理
        if (getAttacking()) {
            startAttack();
            playerAttacking();
            return;
        } else if (isGuarding()) {
            // デバッグ
//            gameWindow.getUi().addMessage("isGuarding() = " + isGuarding());
//            gameWindow.getUi().addMessage("isBlockingLeft() = " + isBlockingLeft());
            startGuard();
            playerGuarding();
            return;
        } else {
            if (gameWindow.getKeyHandler().isBombKeyPressed() && bombCooldown == 0) {
                bombCooldown = COOLDOWN_FRAMES;
                placeBomb();
            } else if (gameWindow.getKeyHandler().isShotKeyPressed() && fireCooldown == 0) {
                playerAttackingFireball();
            }
        }

        // 入力受付
        processInput();

        // ここでエンター押下をチェックして近接オブジェクトに対する処理を行う
        handleInteractInput(nearbyIndex);

        // G の瞬間判定を使う
        if (gameWindow.getKeyHandler().isThrowKeyPressed()) {
            if (pickupCooldown > 0) {
                // 拾った直後の猶予中は何もしない
            } else if (!holding && state != PlayerState.PICKUP) {
                // 近くに置かれた爆弾やアイテムを拾う
                startPickup(nearbyIndex);
            } else if (holding && state == PlayerState.HOLD_WALK) {
                // 所持中かつ通常歩行状態のときだけ投げる
                if (heldBomb != null) {
                    // 爆弾を持っているなら爆弾を投げる
                    throwHeldBom();
                } else if (heldPot != null) {
                    // 壺を持っているなら壺を投げる
                    throwHeldPot();
                } else if (heldChicken != null) {
                    // ニワトリを持っているならニワトリを投げる
                    throwHeldChicken();
                } else if (heldRock != null) {
                    // ニワトリを持っているならニワトリを投げる
                    throwHeldRock();
                }
            }
        }

        if (state == PlayerState.PICKUP) {
            // spriteCounter を増やして閾値でフレームを進める
            setSpriteCounter(getSpriteCounter() + 1);
            if (getSpriteCounter() > SPRITE_ANIMATION_THRESHOLD) {
                setSpriteCounter(0);
                setSpriteNum(getSpriteNum() + 1);
                // 終了判定：PICKUP_SPRITE_COUNT を超えたら持ち状態へ遷移
                if (getSpriteNum() > PICKUP_SPRITE_COUNT) {
                    finishPickup();
                }
            }
            return;
        }

        if (state == PlayerState.HOLD_WALK) {
            // 所持中は爆弾をプレイヤー頭上に固定表示
            if (heldBomb != null) {
                // heldBomb のワールド座標はプレイヤーの頭上に合わせる
                int headX = getWorldX();
                int headY = getWorldY() - FrameApp.getTileSize();
                heldBomb.setWorldX(headX);
                heldBomb.setWorldY(headY);
                heldBomb.setAlive(false);
            }

            // 歩行中は持ち物の表示位置を更新
            if (holding && heldBomb != null) {
                // プレイヤーの頭上に追従させる
                int headX = getWorldX();
                int headY = getWorldY() - FrameApp.getTileSize();
                heldBomb.setWorldX(headX);
                heldBomb.setWorldY(headY);
            }

            // 所持中は壺をプレイヤー頭上に固定表示
            if (heldPot != null) {
                // heldPot のワールド座標はプレイヤーの頭上に合わせる
                int headX = getWorldX();
                int headY = getWorldY() - FrameApp.getTileSize();
                heldPot.setWorldX(headX);
                heldPot.setWorldY(headY);
                heldPot.setAlive(false);
            }

            // 歩行中は持ち物の表示位置を更新
            if (holding && heldPot != null) {
                // プレイヤーの頭上に追従させる
                int headX = getWorldX();
                int headY = getWorldY() - FrameApp.getTileSize();
                heldPot.setWorldX(headX);
                heldPot.setWorldY(headY);
            }

            // 所持中は岩をプレイヤー頭上に固定表示
            if (heldRock != null) {
                // heldPot のワールド座標はプレイヤーの頭上に合わせる
                int headX = getWorldX();
                int headY = getWorldY() - FrameApp.getTileSize();
                heldRock.setWorldX(headX);
                heldRock.setWorldY(headY);
                heldRock.setAlive(false);
            }

            // 歩行中は持ち物の表示位置を更新
            if (holding && heldRock != null) {
                // プレイヤーの頭上に追従させる
                int headX = getWorldX();
                int headY = getWorldY() - FrameApp.getTileSize();
                heldRock.setWorldX(headX);
                heldRock.setWorldY(headY);
            }

            // 所持中はニワトリをプレイヤー頭上に固定表示
            if (heldChicken != null) {
                // heldChicken のワールド座標はプレイヤーの頭上に合わせる
                int headX = getWorldX();
                int headY = getWorldY() - FrameApp.getTileSize();
                heldChicken.setWorldX(headX);
                heldChicken.setWorldY(headY);
                heldChicken.setAlive(false);
            }

            // 歩行中は持ち物の表示位置を更新
            if (holding && heldChicken != null) {
                // プレイヤーの頭上に追従させる
                int headX = getWorldX();
                int headY = getWorldY() - FrameApp.getTileSize();
                heldChicken.setWorldX(headX);
                heldChicken.setWorldY(headY);
            }
        }

        // 拾うボタン（G）を押したら startPickup を呼ぶ
        if (gameWindow.getKeyHandler().isThrowKeyPressed()) {
            if (state == PlayerState.PICKUP) {
                // ピックアップ中は無視
            } else if (!holding) {
                startPickup(nearbyIndex);
            } else if (holding && state == PlayerState.HOLD_WALK) {
                if (heldBomb != null) {
                    throwHeldBom();
                } else if (heldPot != null) {
                    throwHeldPot();
                } else if (heldRock != null) {
                    throwHeldRock();
                } else if (heldChicken != null) {
                    throwHeldChicken();
                }
            }
        }

        if (moving) {
            updateMovement();
            updateCollision();
            updateAnimation();
            updateTileMovement();
        }
        updateInvincibility();
    }

    private void finishPickup() {

        // 既に持っているなら何もしない
        if (this.holding) {
            setState(PlayerState.HOLD_WALK);
            return;
        }

        // pendingPickupIndex を使って取得
        int index = this.pendingPickupIndex;
        this.pendingPickupIndex = -1; // 消す

        ObjBomb[] objs = (ObjBomb[]) gameWindow.getObj();
        if (objs == null || index < 0 || index >= objs.length) {
            // 対象が消えていた場合は所持遷移だけ
            setState(PlayerState.HOLD_WALK);
            setSpriteNum(1);
            setSpriteCounter(0);
            return;
        }

        ObjBomb bomb = objs[index];
        if (bomb == null || !bomb.isPickable() || !bomb.getAlive()) {
            setState(PlayerState.HOLD_WALK);
            setSpriteNum(1);
            setSpriteCounter(0);
            return;
        }

        ObjPot[] objPot = (ObjPot[]) gameWindow.getObj();
        if (objPot == null || index < 0 || index >= objPot.length) {
            // 対象が消えていた場合は所持遷移だけ
            setState(PlayerState.HOLD_WALK);
            setSpriteNum(1);
            setSpriteCounter(0);
            return;
        }

        ObjPot pot = objPot[index];
        if (pot == null || !pot.isPickable() || !pot.getAlive()) {
            setState(PlayerState.HOLD_WALK);
            setSpriteNum(1);
            setSpriteCounter(0);
            return;
        }

        ObjRock[] objRock = (ObjRock[]) gameWindow.getObj();
        if (objRock == null || index < 0 || index >= objRock.length) {
            // 対象が消えていた場合は所持遷移だけ
            setState(PlayerState.HOLD_WALK);
            setSpriteNum(1);
            setSpriteCounter(0);
            return;
        }

        ObjRock rock = objRock[index];
        if (rock == null || !pot.isPickable() || !pot.getAlive()) {
            setState(PlayerState.HOLD_WALK);
            setSpriteNum(1);
            setSpriteCounter(0);
            return;
        }

        NpcChicken[] npcChicken = (NpcChicken[]) gameWindow.getMonster();
        if (npcChicken == null || index < 0 || index >= npcChicken.length) {
            // 対象が消えていた場合は所持遷移だけ
            setState(PlayerState.HOLD_WALK);
            setSpriteNum(1);
            setSpriteCounter(0);
            return;
        }

        NpcChicken chicken = npcChicken[index];
        if (chicken == null || !pot.isPickable() || !pot.getAlive()) {
            setState(PlayerState.HOLD_WALK);
            setSpriteNum(1);
            setSpriteCounter(0);
            return;
        }

        // ワールドから除去
        objs[index] = null;
        objPot[index] = null;
        objRock[index] = null;
        npcChicken[index] = null;

        // 所持状態にする
        this.heldBomb = bomb;
        this.heldPot = pot;
        this.heldRock = rock;
        this.heldChicken = chicken;
        this.holding = true;

        // オブジェクト側の状態更新
        bomb.onPickedUp();
        pot.onPickedUp();
        rock.onPickedUp();
        chicken.onPickedUp();

        // プレイヤー状態を所持歩行に変更
        setState(PlayerState.HOLD_WALK);
        setSpriteNum(1);
        setSpriteCounter(0);

        // 投げ誤トリガ防止の猶予
        this.pickupCooldown = PICKUP_COOLDOWN_FRAMES;
    }

    private void startPickup(int nearbyIndex) {

        if (pickupCooldown > 0) return;

        Entity[] objs = gameWindow.getObj();

        // オブジェクト側取得（存在しない場合は null）
        Entity obj = (objs != null && nearbyIndex >= 0 && nearbyIndex < objs.length) ? objs[nearbyIndex] : null;

        if (obj == null) {
            return;
        }

        if (obj != null) {

            // 宝箱は既存処理
            if (obj instanceof object.ObjChest) {
                object.ObjChest chest = (object.ObjChest) obj;
                chest.interact(this);
                if (chest.isOpened()) {
                    gameWindow.getObj()[nearbyIndex] = null;
                }
                return;
            }

            // --- 爆弾を拾う ---
            if (obj instanceof object.ObjBomb) {
                object.ObjBomb bomb = (object.ObjBomb) obj;
                if (!bomb.isPickable() || !bomb.getAlive()) {
                    return;
                }

                // ワールドから除去して所持に移す
                gameWindow.getObj()[nearbyIndex] = null;

                // オブジェクト側の状態更新（onPickedUp 内で setAlive(false) 等を行う想定）
                bomb.onPickedUp();

                // プレイヤー側の所持フィールドにセット（既存の heldBomb を使用）
                this.heldBomb = bomb;
                this.heldPot = null;
                this.heldRock = null;
                this.heldChicken = null;
                this.holding = true;
                this.pendingPickupIndex = nearbyIndex;
                this.state = PlayerState.PICKUP;
                setSpriteNum(0);
                setSpriteCounter(0);
                setAttacking(false);
                return;
            }

            // --- 壺を拾う ---
            if (obj instanceof object.ObjPot) {
                object.ObjPot pot = (object.ObjPot) obj;
                if (!pot.isPickable() || !pot.getAlive()) {
                    pickupCooldown = PICKUP_COOLDOWN_FRAMES;
                    return;
                }

                // ワールドから除去して所持に移す
                gameWindow.getObj()[nearbyIndex] = null;

                // オブジェクト側の状態更新
                pot.onPickedUp();

                // プレイヤー側の所持フィールドにセット
                this.heldPot = pot;
                this.heldBomb = null;
                this.heldRock = null;
                this.heldChicken = null;
                this.holding = true;
                this.pendingPickupIndex = nearbyIndex;
                this.state = PlayerState.PICKUP;
                setSpriteNum(0);
                setSpriteCounter(0);
                setAttacking(false);
            }

            // --- 岩を拾う ---
            if (obj instanceof object.ObjRock) {
                object.ObjRock rock = (object.ObjRock) obj;
                if (!rock.isPickable() || !rock.getAlive()) {
                    pickupCooldown = PICKUP_COOLDOWN_FRAMES;
                    return;
                }

                // ワールドから除去して所持に移す
                gameWindow.getObj()[nearbyIndex] = null;

                // オブジェクト側の状態更新
                rock.onPickedUp();

                // プレイヤー側の所持フィールドにセット
                this.heldPot = null;
                this.heldBomb = null;
                this.heldRock = rock;
                this.heldChicken = null;
                this.holding = true;
                this.pendingPickupIndex = nearbyIndex;
                this.state = PlayerState.PICKUP;
                setSpriteNum(0);
                setSpriteCounter(0);
                setAttacking(false);
            }
        }
    }

    private void startPickupMonster(int nearbyIndex) {

        Entity[] chickens = gameWindow.getMonster();

        Entity monster = (chickens != null && nearbyIndex >= 0 && nearbyIndex < chickens.length) ? chickens[nearbyIndex] : null;

        if (monster == null) {
            return;
        }

        // --- ニワトリを拾う ---
        if (monster != null && monster instanceof npc.NpcChicken) {
            npc.NpcChicken ch = (npc.NpcChicken) monster;
            if (!ch.isPickable() || !ch.getAlive()) {
                pickupCooldown = PICKUP_COOLDOWN_FRAMES;
                return;
            }
            // ワールドから除去して所持に移す
            gameWindow.getMonster()[nearbyIndex] = null;
            ch.onPickedUp();
            this.heldChicken = ch;
            this.heldBomb = null;
            this.heldPot = null;
            this.heldRock = null;
            this.holding = true;
            this.pendingPickupIndex = nearbyIndex;
            this.state = PlayerState.PICKUP;
            setSpriteNum(0);
            setSpriteCounter(0);
            setAttacking(false);
            return;
        }
    }

    private void throwHeldBom() {

        if (!holding || heldBomb == null) return;

        int tiles = 2;
        int tileSize = FrameApp.getTileSize();
        double distance = tiles * tileSize;

        // ObjBomb の重力（正の値で扱う）
        double gBom = Math.abs(heldBomb.getGravity());

        // プレイヤーの向き（"left","right","up","down" を返す）
        String dir = getDirection();

        double vx = 0.0;
        double vy = 0.0;
        double initialVz;

        // 基本的な水平速度の目安（45度近似）
        double baseSpeed = Math.sqrt(Math.max(1.0, distance * gBom / Math.sin(2 * Math.toRadians(45))));

        switch (dir) {
            case "left" -> {
                vx = -baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25; // 少し上向きに見せる
                initialVz = 8.0;
            }
            case "right" -> {
                vx = baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25;
                initialVz = 8.0;
            }
            case "up" -> {
                // 上投げ：垂直成分を強めに、画面Y速度は小さめ（上方向は負）
                vx = 0.0;
                vy = -baseSpeed * 1.5;
                initialVz = 12.0;
            }
            case "down" -> {
                // 下投げ：画面Y方向に下向きの速度を与え、垂直成分は下向き（地面に叩きつける）
                vx = 0.0;
                vy = baseSpeed * 0.6;
                initialVz = 6.0; // 下向きに押し出す（正は下）
            }
            default -> {
                // フェールセーフ：左右どちらかに投げる
                if ("left".equals(getDirection())) vx = -baseSpeed;
                else vx = baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25;
                initialVz = -8.0;
            }
        }

        System.out.println("THROW dir=" + dir + " initVz=" + initialVz + " setZ=" + heldBomb.getZ() + " worldY=" + heldBomb.getWorldY());

        // 速度制限
        double maxSpeed = 40.0;
        if (Math.abs(vx) > maxSpeed) vx = Math.signum(vx) * maxSpeed;
        if (Math.abs(vy) > maxSpeed) vy = Math.signum(vy) * maxSpeed;

        // heldBomb を投げる準備
        heldBomb.setWorldX(getWorldX());
        heldBomb.setWorldY(getWorldY() - tileSize / 4);
        heldBomb.setZ(tileSize);
        heldBomb.setHasShadow(true);
        heldBomb.setUser(this);
        heldBomb.setThrown(true);
        heldBomb.setPickable(false);
        heldBomb.setAlive(true);
        heldBomb.setLife(heldBomb.getMaxLife());

        // 垂直成分（z/vz）と影を設定するためのメソッドを呼ぶ
        heldBomb.setHasShadow(true);
        heldBomb.setVerticalVelocity(tiles);
        // 水平成分は既存の setVelocity を利用
        heldBomb.setVelocity(vx, vy);

        // ワールドに戻す
        boolean added = false;
        try {
            added = gameWindow.addObject(heldBomb);
        } catch (Throwable ignored) {
        }
        if (!added) {
            Entity[] arr = gameWindow.getObj();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    arr[i] = heldBomb;
                    added = true;
                    break;
                }
            }
        }

        // 所持解除と状態更新
        heldBomb = null;
        holding = false;
        state = PlayerState.THROW;

        // 投げた直後の誤拾い防止（pickupCooldown と入力消費があるなら設定）
        this.pickupCooldown = PICKUP_COOLDOWN_FRAMES;
        gameWindow.getKeyHandler().consumeThrowOnce();
    }

    private void throwHeldPot() {

        if (!holding || heldPot == null) return;

        int tiles = 2;
        int tileSize = FrameApp.getTileSize();
        double distance = tiles * tileSize;

        // ObjPot の重力（正の値で扱う）
        double gPot = Math.abs(heldPot.getGravity());

        // プレイヤーの向き（"left","right","up","down" を返す）
        String dir = getDirection();

        double vx = 0.0;
        double vy = 0.0;
        double initialVz;

        // 基本的な水平速度の目安（45度近似）
        double baseSpeed = Math.sqrt(Math.max(1.0, distance * gPot / Math.sin(2 * Math.toRadians(45))));

        switch (dir) {
            case "left" -> {
                vx = -baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25; // 少し上向きに見せる
                initialVz = 8.0;
            }
            case "right" -> {
                vx = baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25;
                initialVz = 8.0;
            }
            case "up" -> {
                // 上投げ：垂直成分を強めに、画面Y速度は小さめ（上方向は負）
                vx = 0.0;
                vy = -baseSpeed * 1.5;
                initialVz = 12.0;
            }
            case "down" -> {
                // 下投げ：画面Y方向に下向きの速度を与え、垂直成分は下向き（地面に叩きつける）
                vx = 0.0;
                vy = baseSpeed * 0.6;
                initialVz = 6.0; // 下向きに押し出す（正は下）
            }
            default -> {
                // フェールセーフ：左右どちらかに投げる
                if ("left".equals(getDirection())) vx = -baseSpeed;
                else vx = baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25;
                initialVz = -8.0;
            }
        }

        System.out.println("THROW dir=" + dir + " initVz=" + initialVz + " setZ=" + heldPot.getZ() + " worldY=" + heldPot.getWorldY());

        // 速度制限
        double maxSpeed = 40.0;
        if (Math.abs(vx) > maxSpeed) vx = Math.signum(vx) * maxSpeed;
        if (Math.abs(vy) > maxSpeed) vy = Math.signum(vy) * maxSpeed;

        // heldPot を投げる準備
        heldPot.setWorldX(getWorldX());
        heldPot.setWorldY(getWorldY() - tileSize / 4);
        heldPot.setZ(tileSize);
        heldPot.setHasShadow(true);
        heldPot.setUser(this);
        heldPot.setThrown(true);
        heldPot.setPickable(false);
        heldPot.setAlive(true);
        heldPot.setLife(heldPot.getMaxLife());

        // 垂直成分（z/vz）と影を設定するためのメソッドを呼ぶ
        heldPot.setHasShadow(true);
        heldPot.setVerticalVelocity(tiles);
        // 水平成分は既存の setVelocity を利用
        heldPot.setVelocity(vx, vy);

        // ワールドに戻す
        boolean added = false;
        try {
            added = gameWindow.addObject(heldPot);
        } catch (Throwable ignored) {
        }
        if (!added) {
            Entity[] arr = gameWindow.getObj();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    arr[i] = heldPot;
                    added = true;
                    break;
                }
            }
        }

        // 所持解除と状態更新
        heldPot = null;
        holding = false;
        state = PlayerState.THROW;

        // 投げた直後の誤拾い防止（pickupCooldown と入力消費があるなら設定）
        this.pickupCooldown = PICKUP_COOLDOWN_FRAMES;
        gameWindow.getKeyHandler().consumeThrowOnce();
    }

    private void throwHeldRock() {

        if (!holding || heldRock == null) return;

        int tiles = 2;
        int tileSize = FrameApp.getTileSize();
        double distance = tiles * tileSize;

        // ObjPot の重力（正の値で扱う）
        double gPot = Math.abs(heldRock.getGravity());

        // プレイヤーの向き（"left","right","up","down" を返す）
        String dir = getDirection();

        double vx = 0.0;
        double vy = 0.0;
        double initialVz;

        // 基本的な水平速度の目安（45度近似）
        double baseSpeed = Math.sqrt(Math.max(1.0, distance * gPot / Math.sin(2 * Math.toRadians(45))));

        switch (dir) {
            case "left" -> {
                vx = -baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25; // 少し上向きに見せる
                initialVz = 8.0;
            }
            case "right" -> {
                vx = baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25;
                initialVz = 8.0;
            }
            case "up" -> {
                // 上投げ：垂直成分を強めに、画面Y速度は小さめ（上方向は負）
                vx = 0.0;
                vy = -baseSpeed * 1.5;
                initialVz = 12.0;
            }
            case "down" -> {
                // 下投げ：画面Y方向に下向きの速度を与え、垂直成分は下向き（地面に叩きつける）
                vx = 0.0;
                vy = baseSpeed * 0.6;
                initialVz = 6.0; // 下向きに押し出す（正は下）
            }
            default -> {
                // フェールセーフ：左右どちらかに投げる
                if ("left".equals(getDirection())) vx = -baseSpeed;
                else vx = baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25;
                initialVz = -8.0;
            }
        }

        System.out.println("THROW dir=" + dir + " initVz=" + initialVz + " setZ=" + heldRock.getZ() + " worldY=" + heldRock.getWorldY());

        // 速度制限
        double maxSpeed = 40.0;
        if (Math.abs(vx) > maxSpeed) vx = Math.signum(vx) * maxSpeed;
        if (Math.abs(vy) > maxSpeed) vy = Math.signum(vy) * maxSpeed;

        // heldPot を投げる準備
        heldRock.setWorldX(getWorldX());
        heldRock.setWorldY(getWorldY() - tileSize / 4);
        heldRock.setZ(tileSize);
        heldRock.setHasShadow(true);
//        heldRock.setUser(this);
        heldRock.setThrown(true);
        heldRock.setPickable(false);
        heldRock.setAlive(true);
        heldRock.setLife(heldRock.getMaxLife());

        // 垂直成分（z/vz）と影を設定するためのメソッドを呼ぶ
        heldRock.setHasShadow(true);
        heldRock.setVerticalVelocity(tiles);
        // 水平成分は既存の setVelocity を利用
        heldRock.setVelocity(vx, vy);

        // ワールドに戻す
        boolean added = false;
        try {
            added = gameWindow.addObject(heldRock);
        } catch (Throwable ignored) {
        }
        if (!added) {
            Entity[] arr = gameWindow.getObj();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    arr[i] = heldRock;
                    added = true;
                    break;
                }
            }
        }

        // 所持解除と状態更新
        heldRock = null;
        holding = false;
        state = PlayerState.THROW;

        // 投げた直後の誤拾い防止（pickupCooldown と入力消費があるなら設定）
        this.pickupCooldown = PICKUP_COOLDOWN_FRAMES;
        gameWindow.getKeyHandler().consumeThrowOnce();
    }

    public void throwHeldChicken() {

        if (!holding || heldChicken == null) return;

        int tiles = 2;
        int tileSize = FrameApp.getTileSize();
        double distance = tiles * tileSize;

        // ObjPot の重力（正の値で扱う）
        double gPot = Math.abs(heldChicken.getGravity());

        // プレイヤーの向き（"left","right","up","down" を返す）
        String dir = getDirection();

        double vx = 0.0;
        double vy = 0.0;
        double initialVz;

        // 基本的な水平速度の目安（45度近似）
        double baseSpeed = Math.sqrt(Math.max(1.0, distance * gPot / Math.sin(2 * Math.toRadians(45))));

        switch (dir) {
            case "left" -> {
                vx = -baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25; // 少し上向きに見せる
                initialVz = 8.0;
            }
            case "right" -> {
                vx = baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25;
                initialVz = 8.0;
            }
            case "up" -> {
                // 上投げ：垂直成分を強めに、画面Y速度は小さめ（上方向は負）
                vx = 0.0;
                vy = -baseSpeed * 1.5;
                initialVz = 12.0;
            }
            case "down" -> {
                // 下投げ：画面Y方向に下向きの速度を与え、垂直成分は下向き（地面に叩きつける）
                vx = 0.0;
                vy = baseSpeed * 0.6;
                initialVz = 6.0; // 下向きに押し出す（正は下）
            }
            default -> {
                // フェールセーフ：左右どちらかに投げる
                if ("left".equals(getDirection())) vx = -baseSpeed;
                else vx = baseSpeed;
                vy = -Math.abs(baseSpeed) * 0.25;
                initialVz = -8.0;
            }
        }

        System.out.println("THROW dir=" + dir + " initVz=" + initialVz + " setZ=" + heldChicken.getZ() + " worldY=" + heldChicken.getWorldY());

        // 速度制限
        double maxSpeed = 40.0;
        if (Math.abs(vx) > maxSpeed) vx = Math.signum(vx) * maxSpeed;
        if (Math.abs(vy) > maxSpeed) vy = Math.signum(vy) * maxSpeed;

        // heldChicken を投げる準備
        heldChicken.setWorldX(getWorldX());
        heldChicken.setWorldY(getWorldY() - tileSize / 4);
        heldChicken.setZ(tileSize);
        heldChicken.setHasShadow(true);
        heldChicken.setUser(this);
        heldChicken.setThrown(true);
        heldChicken.setPickable(false);
        heldChicken.setAlive(true);
        heldChicken.setLife(heldChicken.getMaxLife());

        // 垂直成分（z/vz）と影を設定するためのメソッドを呼ぶ
        heldChicken.setHasShadow(true);
        heldChicken.setVerticalVelocity(tiles);
        // 水平成分は既存の setVelocity を利用
        heldChicken.setVelocity(vx, vy);

        // ワールドに戻す
        boolean added = false;
        try {
            added = gameWindow.addObject(heldChicken);
        } catch (Throwable ignored) {
        }
        if (!added) {
            Entity[] arr = gameWindow.getMonster();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    arr[i] = heldChicken;
                    added = true;
                    break;
                }
            }
        }

        // 所持解除と状態更新
        heldChicken = null;
        holding = false;
        state = PlayerState.THROW;

        // 投げた直後の誤拾い防止（pickupCooldown と入力消費があるなら設定）
        this.pickupCooldown = PICKUP_COOLDOWN_FRAMES;
        gameWindow.getKeyHandler().consumeThrowOnce();
    }

    public void updateAura(long deltaMs) {

//      System.out.println("updateAura deltaMs=" + deltaMs + " ms");

        // 既存の移動・入力・アニメ更新処理...
        // プレイヤー座標にオーラを追従させる
        if (aura != null) {
            aura.setWorldX(this.getWorldX());
            aura.setWorldY(this.getWorldY());
            aura.updateAnimation(deltaMs);
//            System.out.println("DEBUG: Enter判定 now=" + System.currentTimeMillis()
//                    + " aura.isActive()=" + aura.isActive()
//                    + " aura.expireAt=" + (aura != null ? aura.getExpireAt() : "null")
//                    + " aura.activeFlag=" + (aura != null ? aura.isActiveRaw() : "null"));
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
        } else {
            if (isBlockingLeft() || isGuarding()) {
                startBlockingLeft();
                moving = false;
            } else {
                moving = false;
            }
        }
    }

    private void updateMovement() {
        attemptedMoveThisFrame = false;
        lastAttemptStepX = 0;
        lastAttemptStepY = 0;

        int dx = 0, dy = 0;
        switch (getDirection()) {
            case "up" -> dy = -getSpeed();
            case "down" -> dy = getSpeed();
            case "left" -> dx = -getSpeed();
            case "right" -> dx = getSpeed();
        }

        int maxSplit = Math.max(1, Math.min(Math.max(Math.abs(dx), Math.abs(dy)), 4));
        int steps = maxSplit;
        int remX = dx;
        int remY = dy;

        for (int i = 0; i < steps; i++) {
            int stepX = Math.round((float) remX / (steps - i));
            int stepY = Math.round((float) remY / (steps - i));
            remX -= stepX;
            remY -= stepY;

            if (stepX != 0 || stepY != 0) {
                attemptedMoveThisFrame = true;
                lastAttemptStepX = stepX;
                lastAttemptStepY = stepY;
            }

            // 1) タイル判定（オフセット版を必ず使う）
            boolean tileCollision = gameWindow.getCollisionChecker().checkTile(this, stepX, stepY);
            // 呼び出し側で状態を合わせる（checkTile は副作用なしの想定）
            setCollision(tileCollision);
            System.out.println("[DBG] step=(" + stepX + "," + stepY + ") tileCollision=" + tileCollision);

            if (tileCollision) {
                // 2) 押し出し
                boolean pushed = resolveTileOverlapWithPush(stepX, stepY);
                System.out.println("[DBG] resolvePush=" + pushed);
                if (pushed) {
                    setCollision(false);
                    recordSafePosition();
                    continue;
                }

                // 3) スナップ
                boolean snapped = snapToTileEdge(getDirection(), stepX, stepY);
                System.out.println("[DBG] snap=" + snapped);
                if (snapped) {
                    lastSnapTimeMs = System.currentTimeMillis();
                    recordSafePosition();
                    setCollision(false);
                    break;
                }

                // 4) アンストック
                boolean unstuck = attemptUnstuck();
                System.out.println("[DBG] unstuck=" + unstuck);
                if (unstuck) {
                    recordSafePosition();
                    lastSnapTimeMs = System.currentTimeMillis();
                    setCollision(false);
                    break;
                }

                // どれもダメなら衝突状態を保持してループを抜ける
                setCollision(true);
                System.out.println("[DBG] collision unresolved, stopping movement");
                break;
            }

            // 5) itile 判定
            int itIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile(), stepX, stepY);
            if (itIndex != 999 && gameWindow.getItile()[itIndex].isBlocking()) {
                System.out.println("[DBG] hit itile index=" + itIndex);
                boolean snapped = snapToTileEdge(getDirection(), stepX, stepY);
                if (snapped) {
                    lastSnapTimeMs = System.currentTimeMillis();
                    recordSafePosition();
                    setCollision(false);
                    break;
                }
                if (attemptUnstuck()) {
                    recordSafePosition();
                    lastSnapTimeMs = System.currentTimeMillis();
                    setCollision(false);
                    break;
                }
                setCollision(true);
                break;
            }

            // 6) NPC 判定
            int npcIndexAtDest = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC(), stepX, stepY);
            if (npcIndexAtDest != 999) {
                boolean tileCollisionAtDest = gameWindow.getCollisionChecker().checkTile(this, stepX, stepY);
                if (!tileCollisionAtDest) {
                    setCollision(true);
                    talkNpcIndex = npcIndexAtDest;
                    break;
                }
            }

            // 6.5) OBJ 判定（オブジェクト配列）
            int objIndexAtDest = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getObj(), stepX, stepY);
            if (objIndexAtDest != 999) {
                Entity obj = gameWindow.getObj()[objIndexAtDest];

                // 岩オブジェクトに当たった場合は拾えるフラグを立てる
                if (obj instanceof ObjRock) {
                    ObjRock rock = (ObjRock) obj;
                    // 投擲中や所持中の岩は無視する
                    if (!rock.isPickable() && !rock.isThrown() && rock.getAlive()) {
                        // プレイヤーが近接しているなら拾えるようにする
                        rock.setPickable(true);
                        // HUD 表示やサウンドを出すならここで
                    }
                    // 岩は基本的に通行不可にするので移動はブロック
                    setCollision(true);
                    talkNpcIndex = -1;
                    break;
                } else if (obj instanceof ObjPot) {
                    ObjPot pot = (ObjPot) obj;
                    // 投擲中や所持中の岩は無視する
                    if (!pot.isPickable() && !pot.isThrown() && pot.getAlive()) {
                        // プレイヤーが近接しているなら拾えるようにする
                        pot.setPickable(true);
                        // HUD 表示やサウンドを出すならここで
                    }
                    // 岩は基本的に通行不可にするので移動はブロック
                    setCollision(true);
                    talkNpcIndex = -1;
                    break;
                } else {
                    // 他のオブジェクト（壺や箱など）をブロックする場合
                    if (obj.isBlocking()) {
                        setCollision(true);
                        break;
                    }
                    // もし押し出し可能なオブジェクトなら resolvePush を試す
                    boolean pushedObj = resolveTileOverlapWithPush(stepX, stepY);
                    if (pushedObj) {
                        setCollision(false);
                        recordSafePosition();
                        continue;
                    } else {
                        setCollision(true);
                        break;
                    }
                }
            }


            // 7) 実移動
            if (!isCollision()) {
                setWorldX(getWorldX() + stepX);
                setWorldY(getWorldY() + stepY);
                setCollision(false);
                recordSafePosition();
            } else {
                // 衝突フラグが立っているなら移動しない
                System.out.println("[DBG] movement skipped due to collision flag");
                break;
            }
        }
    }

    private void recordSafePosition() {

        if (!isCollision()) {
            lastSafeWorldX = getWorldX();
            lastSafeWorldY = getWorldY();
            lastSafeFrame = gameWindow.getFrameCounter();
            lastSafeSaWidth = getSolidArea().width;
            lastSafeSaHeight = getSolidArea().height;
        }
    }

    private boolean snapToTileEdge(String direction, int dx, int dy) {
        int tileSize = FrameApp.getTileSize();
        long now = System.currentTimeMillis();
        if (now - lastSnapTimeMs <= SNAP_COOLDOWN_MS) return false;

        Rectangle sa = getSolidArea();
        int saWorldX = getWorldX() + sa.x;
        int saWorldY = getWorldY() + sa.y;
        int left = saWorldX;
        int right = saWorldX + sa.width - 1;
        int top = saWorldY;
        int bottom = saWorldY + sa.height - 1;

        // 優先軸と向きを移動ベクトルから決める（dx,dy が 0 の場合は direction を使う）
        boolean preferHorizontal = Math.abs(dx) >= Math.abs(dy);
        int signX = Integer.signum(dx);
        int signY = Integer.signum(dy);
        if (signX == 0 && signY == 0) {
            signX = direction.equals("right") ? 1 : direction.equals("left") ? -1 : 0;
            signY = direction.equals("down") ? 1 : direction.equals("up") ? -1 : 0;
            preferHorizontal = Math.abs(signX) >= Math.abs(signY);
        }

        // penetration を方向ごとに正しく計算する（タイル境界への侵入量）
        int penetration = 0;
        int collidedCol = -1;
        int collidedRow = -1;
        switch (direction) {
            case "right" -> {
                int mod = Math.floorMod(right, tileSize);
                penetration = (mod == 0) ? 0 : tileSize - mod;
                collidedCol = Math.floorDiv(right, tileSize);
            }
            case "left" -> {
                int mod = Math.floorMod(left, tileSize);
                penetration = (mod == 0) ? 0 : mod;
                collidedCol = Math.floorDiv(left, tileSize);
            }
            case "down" -> {
                int mod = Math.floorMod(bottom, tileSize);
                penetration = (mod == 0) ? 0 : tileSize - mod;
                collidedRow = Math.floorDiv(bottom, tileSize);
            }
            case "up" -> {
                int mod = Math.floorMod(top, tileSize);
                penetration = (mod == 0) ? 0 : mod;
                collidedRow = Math.floorDiv(top, tileSize);
            }
            default -> {
                return false;
            }
        }

        // 早期リターン緩和：penetration が小さくても既に衝突中ならスナップを試す
        if (penetration <= SNAP_PENETRATION_THRESHOLD && !isCollision()) return false;

        // --- 1) 単純補正（侵入量の逆方向へ戻す）を先に試す ---
        int minAdjust = isCollision() ? 1 : 0;
        int move = Math.max(minAdjust, penetration);
        int adjustX = 0, adjustY = 0;
        switch (direction) {
            case "right" -> adjustX = -move;
            case "left" -> adjustX = move;
            case "down" -> adjustY = -move;
            case "up" -> adjustY = move;
        }

        // NPC はスナップのブロック対象から外す（itile はブロック対象）
        if (!gameWindow.getCollisionChecker().checkTile(this, adjustX, adjustY)
                && gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile(), adjustX, adjustY) == 999) {
            setWorldX(getWorldX() + adjustX);
            setWorldY(getWorldY() + adjustY);
            setCollision(false);
            lastSnapTimeMs = now;
            recordSafePosition();
            return true;
        }

        // --- 2) フォールバック候補（preferHorizontal を反映） ---
        int sx = Integer.signum(dx);
        int sy = Integer.signum(dy);
        int tileHalf = Math.max(1, tileSize / 2);
        int magX = Math.max(1, Math.min(Math.abs(dx), tileHalf));
        int magY = Math.max(1, Math.min(Math.abs(dy), tileHalf));
        int halfX = Math.max(1, magX / 2);
        int halfY = Math.max(1, magY / 2);
        int micro = 1;

        List<int[]> list = new ArrayList<>();

        if (sx != 0 && sy != 0) {
            // 斜め移動時：主軸優先（preferHorizontal）に応じて候補順を変える
            if (preferHorizontal) {
                list.add(new int[]{sx * magX, sy * halfY}); // 横寄り斜め（横成分強め）
                list.add(new int[]{sx * halfX, sy * halfY}); // 小さめ斜め
                list.add(new int[]{sx * magX, 0});          // 横フル
                list.add(new int[]{0, sy * magY});  // 縦フル
                list.add(new int[]{sx * micro, sy * micro}); // 小さな斜め（微調整）
            } else {
                list.add(new int[]{sx * halfX, sy * magY});  // 縦寄り斜め（縦成分強め）
                list.add(new int[]{sx * halfX, sy * halfY}); // 小さめ斜め
                list.add(new int[]{0, sy * magY});  // 縦フル
                list.add(new int[]{sx * magX, 0});          // 横フル
                list.add(new int[]{sx * micro, sy * micro}); // 小さな斜め（微調整）
            }
            // 共通の後退候補
            list.add(new int[]{-sx * magX, 0});
            list.add(new int[]{0, -sy * magY});
        } else {
            // 直進時は必ず4方向（前方・左右/上下微調整・後方）を試す
            if (preferHorizontal) {
                int useSx = sx == 0 ? (direction.equals("right") ? 1 : -1) : sx;
                list.add(new int[]{useSx * magX, 0});      // 前方（横フル）
                list.add(new int[]{useSx * halfX, -micro}); // 少し上に寄せる
                list.add(new int[]{useSx * halfX, micro});  // 少し下に寄せる
                list.add(new int[]{-useSx * magX, 0});     // 後方（横後退）
            } else {
                int useSy = sy == 0 ? (direction.equals("down") ? 1 : -1) : sy;
                list.add(new int[]{0, useSy * magY});      // 前方（縦フル）
                list.add(new int[]{-micro, useSy * halfY}); // 少し左に寄せる
                list.add(new int[]{micro, useSy * halfY});  // 少し右に寄せる
                list.add(new int[]{0, -useSy * magY});     // 後方（縦後退）
            }
        }

        // --- 3) 候補を順にチェックして適用 ---
        for (int[] t : list) {
            int tryDx = t[0], tryDy = t[1];

            // checkTile/checkEntity はピクセル単位のオフセットを期待する前提
            boolean tileOk = !gameWindow.getCollisionChecker().checkTile(this, tryDx, tryDy);
            boolean itileOk = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile(), tryDx, tryDy) == 999;
            boolean npcOk = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC(), tryDx, tryDy) == 999;

            if (tileOk && itileOk && npcOk) {
                setWorldX(getWorldX() + tryDx);
                setWorldY(getWorldY() + tryDy);
                setCollision(false);
                lastSnapTimeMs = now;
                recordSafePosition();
                return true;
            }
        }

        // どれもダメならスナップ失敗
        return false;
    }

    /**
     * タイルとの重なりを解析して最小の移動で押し出す。
     * true を返すと押し出しで衝突が解消された（位置を変更した）。
     */

    private boolean resolveTileOverlapWithPush(int offsetX, int offsetY) {
        Rectangle sa = getSolidArea();
        int myX = getWorldX() + getSolidAreaDefaultX() + offsetX;
        int myY = getWorldY() + getSolidAreaDefaultY() + offsetY;
        Rectangle myRect = new Rectangle(myX, myY, sa.width, sa.height);

        int tileSize = FrameApp.getTileSize();

        // 周囲走査範囲（X=Row, Y=Col の扱いに合わせる）
        int minX = Math.floorDiv(myX, tileSize) - 1;
        int maxX = Math.floorDiv(myX + sa.width, tileSize) + 1;
        int minY = Math.floorDiv(myY, tileSize) - 1;
        int maxY = Math.floorDiv(myY + sa.height, tileSize) + 1;

        // clamp to map
        minX = Math.max(minX, 0);
        minY = Math.max(minY, 0);
        maxX = Math.min(maxX, getMapCols() - 1);
        maxY = Math.min(maxY, getMapRows() - 1);

        int bestDx = 0;
        int bestDy = 0;
        int bestPenetration = Integer.MAX_VALUE;
        boolean found = false;

        for (int tx = minX; tx <= maxX; tx++) {
            for (int ty = minY; ty <= maxY; ty++) {
                Rectangle tileRect = getTileSolidRect(tx, ty); // getTileSolidRect(x,y) は blocking 判定済み
                if (tileRect == null) continue;
                if (!myRect.intersects(tileRect)) continue;

                Rectangle inter = myRect.intersection(tileRect);
                if (inter.isEmpty()) continue;

                int penX = inter.width;
                int penY = inter.height;

                // 候補押し出し量（まず最小量＝重なり幅）
                int dxLeft = -penX;
                int dxRight = penX;
                int dyUp = -penY;
                int dyDown = penY;

                // タイルの座標（端チェック用）
                int tileX = tx;
                int tileY = ty;

                // 中心で方向を決めるが、端なら反転してマップ外へ押さない
                int myCenterX = myRect.x + myRect.width / 2;
                int tileCenterX = tileRect.x + tileRect.width / 2;
                int candDx = (myCenterX < tileCenterX) ? dxLeft : dxRight;
                if (tileX == 0 && candDx < 0) candDx = dxRight;
                if (tileX == getMapCols() - 1 && candDx > 0) candDx = dxLeft;

                int myCenterY = myRect.y + myRect.height / 2;
                int tileCenterY = tileRect.y + tileRect.height / 2;
                int candDy = (myCenterY < tileCenterY) ? dyUp : dyDown;
                if (tileY == 0 && candDy < 0) candDy = dyDown;
                if (tileY == getMapRows() - 1 && candDy > 0) candDy = dyUp;

                // 1) X 単独で試す（優先）
                if (Math.abs(penX) < bestPenetration) {
                    // try minimal push, if blocked try +1 px
                    if (canMoveBy(candDx, 0)) {
                        bestPenetration = Math.abs(penX);
                        bestDx = candDx;
                        bestDy = 0;
                        found = true;
                    } else if (canMoveBy(candDx < 0 ? candDx - 1 : candDx + 1, 0)) {
                        bestPenetration = Math.abs(penX) + 1;
                        bestDx = (candDx < 0) ? candDx - 1 : candDx + 1;
                        bestDy = 0;
                        found = true;
                    }
                }

                // 2) Y 単独で試す
                if (Math.abs(penY) < bestPenetration) {
                    if (canMoveBy(0, candDy)) {
                        bestPenetration = Math.abs(penY);
                        bestDx = 0;
                        bestDy = candDy;
                        found = true;
                    } else if (canMoveBy(0, candDy < 0 ? candDy - 1 : candDy + 1)) {
                        bestPenetration = Math.abs(penY) + 1;
                        bestDx = 0;
                        bestDy = (candDy < 0) ? candDy - 1 : candDy + 1;
                        found = true;
                    }
                }

                // 3) 斜め（両軸同時）を最後に試す
                int diagDx = (myRect.x < tileRect.x) ? -penX : penX;
                int diagDy = (myRect.y < tileRect.y) ? -penY : penY;
                if (Math.abs(penX + penY) < bestPenetration) {
                    if (canMoveBy(diagDx, diagDy)) {
                        bestPenetration = Math.abs(penX + penY);
                        bestDx = diagDx;
                        bestDy = diagDy;
                        found = true;
                    } else if (canMoveBy(diagDx < 0 ? diagDx - 1 : diagDx + 1, diagDy < 0 ? diagDy - 1 : diagDy + 1)) {
                        bestPenetration = Math.abs(penX + penY) + 1;
                        bestDx = (diagDx < 0) ? diagDx - 1 : diagDx + 1;
                        bestDy = (diagDy < 0) ? diagDy - 1 : diagDy + 1;
                        found = true;
                    }
                }

                // デバッグ（必要なら有効化）
                // System.out.println("[DBG-PUSH] tileX=" + tileX + " tileY=" + tileY + " penX=" + penX + " penY=" + penY + " candDx=" + candDx + " candDy=" + candDy);
            }
        }

        if (found) {
            setWorldX(getWorldX() + bestDx);
            setWorldY(getWorldY() + bestDy);
            setCollision(false);
            recordSafePosition();
            System.out.println("[UNSTUCK-PUSH] moved by (" + bestDx + "," + bestDy + ")");
            return true;
        }

        return false;
    }

    // シグネチャを明確にする（推奨）
    private Rectangle getTileSolidRect(int col, int row) {
        if (!isValidTile(row, col)) return null; // isValidTile の引数順に合わせるか修正
        if (!isTileBlocking(row, col)) return null;

        int tileSize = FrameApp.getTileSize();
        int tx = col * tileSize;
        int ty = row * tileSize;
        return new Rectangle(tx, ty, tileSize, tileSize);
    }

    // タイル座標がマップ内かどうか
    private boolean isValidTile(int row, int col) {
        if (row < 0 || col < 0) return false;
        if (row >= getMapRows() || col >= getMapCols()) return false;
        return true;
    }

    /**
     * タイル番号（int）を参照して blocking 判定を返す。
     * gameWindow.getTileManager().getMapTileNum() が int[row][col] を返す想定。
     */
    // 推奨シグネチャ（可能なら変更）
    private boolean isTileBlocking(int col, int row) {
        // マップ外は blocking
        if (!isValidTile(col, row)) return true;

        int[][] mapTileNum = gameWindow.getTileManager().getMapTileNum(); // mapTileNum[col][row]
        if (mapTileNum == null) return true;

        int maxCol = mapTileNum.length - 1;
        int maxRow = mapTileNum[0].length - 1;
        if (col < 0 || col > maxCol) return true;
        if (row < 0 || row > maxRow) return true;

        int tileNum = mapTileNum[col][row];
        Tile tile = gameWindow.getTileManager().getTiles()[tileNum];
        if (tile == null) return true;
        return tile.collision;
    }

    // 現在位置から dx,dy 移動しても衝突しないかチェックするユーティリティ
    private boolean canMoveBy(int dx, int dy) {
        // checkTile/checkEntity はオフセット版を想定
        boolean tileBlock = gameWindow.getCollisionChecker().checkTile(this, dx, dy);
        int itIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile(), dx, dy);
        int npcIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC(), dx, dy);
        boolean itBlock = (itIndex != 999 && gameWindow.getItile()[itIndex].isBlocking());
        boolean npcBlock = (npcIndex != 999);
        return !(tileBlock || itBlock || npcBlock);
    }


    /**
     * 挟まり解除を試みる。
     *
     * @return true if freed (位置を変更して衝突を解除した)
     */

    private boolean attemptUnstuck() {

        int[][] offsets = new int[][]{
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {2, 0}, {-2, 0}, {0, 2}, {0, -2},
                {3, 0}, {-3, 0}, {0, 3}, {0, -3},
                {4, 0}, {-4, 0}, {0, 4}, {0, -4}
        };

        // 1) 周囲への小移動を試す
        for (int[] off : offsets) {
            int ox = off[0];
            int oy = off[1];

            boolean tileBlock = gameWindow.getCollisionChecker().checkTile(this, ox, oy);
            int itIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile(), ox, oy);
            int npcIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC(), ox, oy);
            boolean itBlock = (itIndex != 999 && gameWindow.getItile()[itIndex].isBlocking());
            boolean npcBlock = (npcIndex != 999);

            if (!tileBlock && !itBlock && !npcBlock) {
                setWorldX(getWorldX() + ox);
                setWorldY(getWorldY() + oy);
                setCollision(false);
                recordSafePosition();
                System.out.println("[UNSTUCK] pushed by (" + ox + "," + oy + ")");
                return true;
            }
        }

        // 2) 軸分離（Xのみ、Yのみ）
        for (int px = -UNSTUCK_PUSH_MAX; px <= UNSTUCK_PUSH_MAX; px++) {
            if (px == 0) continue;
            boolean tileBlockX = gameWindow.getCollisionChecker().checkTile(this, px, 0);
            int itIndexX = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile(), px, 0);
            int npcIndexX = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC(), px, 0);
            if (!tileBlockX && itIndexX == 999 && npcIndexX == 999) {
                setWorldX(getWorldX() + px);
                setCollision(false);
                recordSafePosition();
                System.out.println("[UNSTUCK] axis X push " + px);
                return true;
            }
        }
        for (int py = -UNSTUCK_PUSH_MAX; py <= UNSTUCK_PUSH_MAX; py++) {
            if (py == 0) continue;
            boolean tileBlockY = gameWindow.getCollisionChecker().checkTile(this, 0, py);
            int itIndexY = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile(), 0, py);
            int npcIndexY = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC(), 0, py);
            if (!tileBlockY && itIndexY == 999 && npcIndexY == 999) {
                setWorldY(getWorldY() + py);
                setCollision(false);
                recordSafePosition();
                System.out.println("[UNSTUCK] axis Y push " + py);
                return true;
            }
        }

        // 3) snap を試す（オフセット付き snap を呼び、戻り値で判定）
        boolean snapped = snapToTileEdge(getDirection(), 0, 0);
        if (snapped) {
            setCollision(false);
            recordSafePosition();
            System.out.println("[UNSTUCK] snapToTileEdge succeeded");
            return true;
        } else {
            System.out.println("[UNSTUCK] snapToTileEdge failed or blocked");
        }

        // 4) BFS で近傍の空きタイルを探してテレポート（最終手段）
        Point free = findNearestFreeTile(getWorldX(), getWorldY(), UNSTUCK_BFS_RADIUS);
        if (free != null) {
            setWorldX(free.x);
            setWorldY(free.y);
            setCollision(false);
            recordSafePosition();
            System.out.println("[UNSTUCK] teleported to nearest free tile (" + free.x + "," + free.y + ")");
            return true;
        }

        // 5) 最後の安全位置へ復帰（最終フォールバック）
        restoreToLastSafePosition();
        System.out.println("[UNSTUCK] restored to last safe position");
        return false;
    }

    private void restoreToLastSafePosition() {
        setWorldX(lastSafeWorldX);
        setWorldY(lastSafeWorldY);
        setCollision(false);
        System.out.println("[UNSTUCK] restoreToLastSafePosition -> (" + lastSafeWorldX + "," + lastSafeWorldY + ")");
    }

    // BFS ヘルパー（タイル中心にスナップして返す）
    private Point findNearestFreeTile(int startWX, int startWY, int maxRadiusTiles) {
        int tileSize = FrameApp.getTileSize();
        int startCol = Math.floorDiv(startWX, tileSize);
        int startRow = Math.floorDiv(startWY, tileSize);

        int size = maxRadiusTiles * 2 + 1;
        boolean[][] visited = new boolean[size][size];
        Queue<Point> q = new LinkedList<>();
        q.add(new Point(startCol, startRow));
        visited[maxRadiusTiles][maxRadiusTiles] = true;

        int[][] dirs = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!q.isEmpty()) {
            Point p = q.poll();
            int col = p.x;
            int row = p.y;

            int wx = col * tileSize + (tileSize - getSolidArea().width) / 2 - getSolidAreaDefaultX();
            int wy = row * tileSize + (tileSize - getSolidArea().height) / 2 - getSolidAreaDefaultY();

            // 空き判定（ワールドオフセット版の checkTile を使う）
            boolean tileBlock = gameWindow.getCollisionChecker().checkTile(this, wx - getWorldX(), wy - getWorldY());
            int itIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile(), wx - getWorldX(), wy - getWorldY());
            int npcIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC(), wx - getWorldX(), wy - getWorldY());

            if (!tileBlock && itIndex == 999 && npcIndex == 999) {
                return new Point(wx, wy);
            }

            for (int[] d : dirs) {
                int nc = col + d[0];
                int nr = row + d[1];
                int vi = nc - (startCol - maxRadiusTiles);
                int vj = nr - (startRow - maxRadiusTiles);
                if (vi < 0 || vj < 0 || vi >= size || vj >= size) continue;
                if (visited[vi][vj]) continue;
                visited[vi][vj] = true;
                q.add(new Point(nc, nr));
            }
        }
        return null;
    }


    /**
     * プレイヤー前方にいる NPC を range タイル以内で探す（少しのズレを許容）
     * 正面度（向きベクトルへの投影）を優先し、同じなら距離で近い方を返す。
     * 戻り値: 見つかった NPC の配列インデックス、見つからなければ -1
     */

    public int checkNpcInFront(Entity[] npc, int range) {
        int tileSize = FrameApp.getTileSize();

        // プレイヤーの当たり矩形（ワールド座標）
        Rectangle playerRect = getCollisionBoxWorld();
        if (playerRect == null) return -1;

        // プレイヤー中心
        double px = playerRect.x + playerRect.width / 2.0;
        double py = playerRect.y + playerRect.height / 2.0;

        // 向きベクトル（正規化）
        double vx = 0, vy = 0;
        switch (this.getDirection()) {
            case "up" -> {
                vx = 0;
                vy = -1;
            }
            case "down" -> {
                vx = 0;
                vy = 1;
            }
            case "left" -> {
                vx = -1;
                vy = 0;
            }
            case "right" -> {
                vx = 1;
                vy = 0;
            }
            default -> {
                return -1;
            }
        }

        // 検索矩形（許容ピクセルを含む）
        int tolerancePx = Math.max(4, tileSize / 16);
        int forwardPx = range * tileSize;
        Rectangle searchRect;
        int regionW = playerRect.width + tolerancePx * 2;
        int regionH = playerRect.height + tolerancePx * 2;

        switch (this.getDirection()) {
            case "up" -> searchRect = new Rectangle(
                    (int) (px - regionW / 2.0),
                    (int) (py - forwardPx - regionH / 2.0),
                    regionW,
                    forwardPx + regionH
            );
            case "down" -> searchRect = new Rectangle(
                    (int) (px - regionW / 2.0),
                    (int) (py - regionH / 2.0),
                    regionW,
                    forwardPx + regionH
            );
            case "left" -> searchRect = new Rectangle(
                    (int) (px - forwardPx - regionW / 2.0),
                    (int) (py - regionH / 2.0),
                    forwardPx + regionW,
                    regionH
            );
            default -> searchRect = new Rectangle(
                    (int) (px - regionW / 2.0),
                    (int) (py - regionH / 2.0),
                    forwardPx + regionW,
                    regionH
            );
        }

        // 候補を評価（正面度, 距離）して最良を選ぶ
        int bestIdx = -1;
        double bestScoreFront = Double.NEGATIVE_INFINITY;
        double bestDistSq = Double.POSITIVE_INFINITY;

        for (int i = 0; i < npc.length; i++) {
            Entity e = npc[i];
            if (e == null) continue;
            if (!e.getAlive()) continue;
            if (e.getMapId() != this.getMapId()) continue;

            // NPC のワールド矩形を取得（必ず e.getCollisionBoxWorld() を使う）
            Rectangle npcRect = e.getCollisionBoxWorld();
            if (npcRect == null) continue;

            // まず検索矩形と交差するか（早期除外）
            if (!searchRect.intersects(npcRect)) continue;

            // NPC 中心
            double ex = npcRect.x + npcRect.width / 2.0;
            double ey = npcRect.y + npcRect.height / 2.0;

            // プレイヤーから NPC へのベクトル
            double dx = ex - px;
            double dy = ey - py;
            double distSq = dx * dx + dy * dy;

            // 正面度: 向きベクトルとの内積（投影長）
            double front = dx * vx + dy * vy; // 正なら前方、負なら後方
            // front が負なら後ろにいるので除外
            if (front <= 0) continue;

            // 正面度を距離で正規化してスコア化（近くて正面の方を高評価）
            // front/dist を使う（front が大きく、距離が小さいほど良い）
            double scoreFront = front / Math.sqrt(distSq + 1e-6);

            // 優先ルール: scoreFront を第一キー、距離を第二キー（小さい方）
            if (scoreFront > bestScoreFront || (Math.abs(scoreFront - bestScoreFront) < 1e-6 && distSq < bestDistSq)) {
                bestScoreFront = scoreFront;
                bestDistSq = distSq;
                bestIdx = i;
            }
        }

        return bestIdx;
    }


    /**
     * プレイヤーの衝突判定を更新。
     * タイル、NPC、オブジェクト、モンスター、インタラクティブタイルとの当たり判定を行い、
     * マップ切り替えやNPCとの会話開始、ダメージ処理を呼び出す。
     */

    private void updateCollision() {

        int tileSize = FrameApp.getTileSize();

        // solidArea のワールド矩形を基準にグリッド座標を取る（左上ではなく中心でも可）
        Rectangle sa = getSolidArea();
        int saWorldLeft = getWorldX() + sa.x;
        int saWorldTop = getWorldY() + sa.y;
        int saWorldRight = saWorldLeft + sa.width - 1;
        int saWorldBottom = saWorldTop + sa.height - 1;

        // タイル判定に使う代表点（中心）を使うのが安全
        int playerGridX = (saWorldLeft + saWorldRight) / 2 / tileSize;
        int playerGridY = (saWorldTop + saWorldBottom) / 2 / tileSize;

        boolean tileCollision = gameWindow.getCollisionChecker().checkTile(this);
        setCollision(tileCollision);

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
            Entity monster = gameWindow.getMonster()[monsterIndex];

            // 追加安全チェック：念のため生存確認
            if (!monster.getAlive()) {
                // 既に死亡しているなら判定対象外
                return;
            }

            // タイプ側で hostile を判定
            if (monster.getType().isHostileToPlayer()) {
                contactMonster(monsterIndex, this, monster.getKnockBackPower());
                setInvincible(true);
            } else {
                // 非敵（ニワトリ等）：ダメージ処理は行わない
                startPickupMonster(monsterIndex);
            }

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

        pixelCounter++;

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

    public void playerGuarding() {

        guardCounter++;

        int counter = getSpriteCounter() + 1;
        setSpriteCounter(counter);

        if (counter <= SPRITE_GUARDING_THRESHOLD_NUM1) {
            setSpriteNum(1);
//            System.out.println("counter:" + counter + " num:" + getSpriteNum());
        } else if (counter <= SPRITE_GUARDING_THRESHOLD_NUM2) {
            setSpriteNum(2);
//            System.out.println("counter:" + counter + " num:" + getSpriteNum());
        } else if (counter <= SPRITE_GUARDING_THRESHOLD_NUM3) {
            setSpriteNum(3);
//            System.out.println("counter:" + counter + " num:" + getSpriteNum());
        } else if (guardCounter >= GUARD_ANIMATION_FRAMES) {
            // 状態をリセット
            guardCounter = 0;
            setSpriteCounter(0);
            setGuarding(false);
            setSpriteNum(1);
            return;
        }
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

        // 発射直後にプロジェクタイル側でエフェクトを初期化させる
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

    private void placeBomb() {

        KeyHandler kh = gameWindow.getKeyHandler();

        if (kh.isBombKeyPressed() &&
                !getProjectile().getAlive() &&
                getShotAvailableCounter() == 30 &&
                consumeMana(FIREBALL_MANA_COST)) {

            bombCooldown = COOLDOWN_FRAMES;

            long now = System.currentTimeMillis();

            if (now - lastFireTime >= FIRE_COOLDOWN_MS) {
                lastFireTime = now;

                ObjBomb b = new ObjBomb(gameWindow);
                b.setWorldX(getWorldX());
                b.setWorldY(getWorldY());
                b.setUser(this);
                b.setPickable(true);
                b.setThrown(false);
                b.setAlive(true);
                gameWindow.addObject(b);
                gameWindow.getSoundmanager().explosionWAV("sound/explosion-sound.wav");
            }
        }
    }

    public void pickUpObject(int i) {

        if (i == 999) return;

        if (!gameWindow.getKeyHandler().isPlayerEnterJustPressed() &&
                !gameWindow.getKeyHandler().isThrowKeyPressed()) {
            return;
        }

        Entity[] objs = gameWindow.getObj();
        if (objs == null) return;
        if (i < 0 || i >= objs.length) return;

        Entity obj = objs[i];
        if (obj == null) return;

        // 宝箱は開ける処理（既存）
        if (obj instanceof object.ObjChest) {
            object.ObjChest chest = (object.ObjChest) obj;
            chest.interact(this);
            if (chest.isOpened()) {
                gameWindow.getObj()[i] = null;
            }
            return;
        }

        // --- 爆弾は「持ち上げた」扱いにする ---
        if (obj instanceof object.ObjBomb && keyHandler.isThrowKeyPressed()) {
            object.ObjBomb bomb = (object.ObjBomb) obj;
            if (!bomb.isPickable() || !bomb.getAlive()) {
                return;
            }
            // ワールドから除去して所持に移す
            gameWindow.getObj()[i] = null;
            bomb.setAlive(false);
            bomb.setPickable(false);
            bomb.setThrown(false);
            bomb.setVelocity(0, 0);

            this.heldBomb = bomb;
            this.holding = true;
            this.state = PlayerState.PICKUP;
            return;
        }

        if (obj instanceof object.ObjPot && keyHandler.isThrowKeyPressed()) {
            object.ObjPot pot = (object.ObjPot) obj;
            // pickable / alive のチェック（必要に応じて）
            if (!pot.isPickable() || !pot.getAlive()) {
                return;
            }

            // ワールドから除去して所持に移す
            gameWindow.getObj()[i] = null;
            pot.setAlive(false);
            pot.setPickable(false);
            pot.setThrown(false);
            pot.setVelocity(0, 0);

            this.heldPot = pot;
            this.holding = true;
            this.state = PlayerState.PICKUP;
            return;
        }

        if (obj instanceof object.ObjRock && keyHandler.isThrowKeyPressed()) {
            object.ObjRock rock = (object.ObjRock) obj;
            // pickable / alive のチェック（必要に応じて）
            if (!rock.isPickable() || !rock.getAlive()) {
                return;
            }

            // ワールドから除去して所持に移す
            gameWindow.getObj()[i] = null;
            rock.setAlive(false);
            rock.setPickable(false);
            rock.setThrown(false);
            rock.setVelocity(0, 0);

            this.heldRock = rock;
            this.holding = true;
            this.state = PlayerState.PICKUP;
            return;
        }

        if (obj instanceof NpcChicken && keyHandler.isThrowKeyPressed()) {

            NpcChicken chicken = (NpcChicken) obj;
            // pickable / alive のチェック（必要に応じて）
            if (!chicken.isPickable() || !chicken.getAlive()) {
                return;
            }

            // ワールドから除去して所持に移す
            gameWindow.getObj()[i] = null;
            chicken.setAlive(false);
            chicken.setPickable(false);
            chicken.setThrown(false);
            chicken.setVelocity(0, 0);

            this.heldChicken = chicken;
            this.holding = true;
            this.state = PlayerState.PICKUP;
            return;
        }

        String text;
        if (canObtainItem(obj) == true) {
            text = obj.getName() + " を手に入れた!";
            gameWindow.getObj()[i] = null;
        } else {
            text = "手に入れていない!";
        }
    }


    // 近接オブジェクトを探してインデックスを返す
    private int findNearbyObjectIndex() {
        Entity[] objs = gameWindow.getObj();
        if (objs == null) return -1;
        Rectangle pBox = this.getCollisionBoxWorld();
        for (int i = 0; i < objs.length; i++) {
            Entity o = objs[i];
            if (o == null || !o.getAlive()) continue;
            if (o instanceof ObjBomb) {
                ObjBomb b = (ObjBomb) o;
                if (!b.isPickable()) continue;
            }
            if (o instanceof ObjPot) {
                ObjPot p = (ObjPot) o;
                if (!p.isPickable()) continue;
            }
            if (o instanceof ObjRock) {
                ObjRock p = (ObjRock) o;
                if (!p.isPickable()) continue;
            }
            if (o instanceof NpcChicken) {
                NpcChicken c = (NpcChicken) o;
                if (!c.isPickable()) continue;
            }
            Rectangle oBox = o.getCollisionBoxWorld();
            // findNearbyObjectIndex のヒット箇所に一時ログ
            if (pBox.intersects(oBox)) {
                System.out.println("nearby hit index=" + i
                        + " class=" + o.getClass().getSimpleName()
                        + " alive=" + o.getAlive()
                        + " pickable=" + (o instanceof ObjPot ? ((ObjPot) o).isPickable() : (o instanceof ObjBomb ? ((ObjBomb) o).isPickable() : "N/A")));
                return i;
            }
        }
        return -1;
    }

    private void handleInteractInput(int i) {

        if (i == -1) return;

        var keyHandler = gameWindow.getKeyHandler();
        if (!keyHandler.isPlayerEnter()) return;

        Entity obj = gameWindow.getObj()[i];
        Entity chicken = gameWindow.getMonster()[i];

        if (obj == null) {
            return;
        }

        // 宝箱なら開封処理を呼ぶ（ObjChest が中身を生成してくれる）
        if (obj instanceof object.ObjChest) {
            object.ObjChest chest = (object.ObjChest) obj;
            chest.interact(this);

            // chest が開いて中身を取り終えたら配列から削除
            if (chest.isOpened()) {
                gameWindow.getObj()[i] = null;
            }
            return;
        }

        // --- 爆弾はインベントリに入れず地面に落とす（pickable にする） ---
        if (obj instanceof object.ObjBomb) {
            object.ObjBomb bomb = (object.ObjBomb) obj;

            // 初期化：ワールドに置ける状態にする
            bomb.setPickable(true);
            bomb.setThrown(false);
            bomb.setAlive(true);
            bomb.setLife(bomb.getMaxLife());
            bomb.setVelocity(0, 0);

            // チェスト位置に落とす（必要なら少しオフセット）
            bomb.setWorldX(this.getWorldX());
            bomb.setWorldY(this.getWorldY());
            return;
        }

        if (obj instanceof object.ObjPot) {
            object.ObjPot pot = (object.ObjPot) obj;

            // 初期化：ワールドに置ける状態にする
            pot.setPickable(true);
            pot.setThrown(false);
            pot.setAlive(true);
            pot.setLife(pot.getMaxLife());
            pot.setVelocity(0, 0);

            // チェスト位置に落とす（必要なら少しオフセット）
            pot.setWorldX(this.getWorldX());
            pot.setWorldY(this.getWorldY());
            return;
        }

        if (obj instanceof object.ObjRock) {
            object.ObjRock rock = (object.ObjRock) obj;

            // 初期化：ワールドに置ける状態にする
            rock.setPickable(true);
            rock.setThrown(false);
            rock.setAlive(true);
            rock.setLife(rock.getMaxLife());
            rock.setVelocity(0, 0);

            // チェスト位置に落とす（必要なら少しオフセット）
            rock.setWorldX(this.getWorldX());
            rock.setWorldY(this.getWorldY());
            return;
        }

        if (chicken instanceof NpcChicken) {
            NpcChicken npcChicken = (NpcChicken) chicken;

            // 初期化：ワールドに置ける状態にする
            npcChicken.setPickable(true);
            npcChicken.setThrown(false);
            npcChicken.setAlive(true);
            npcChicken.setLife(chicken.getMaxLife());
            npcChicken.setVelocity(0, 0);

            // チェスト位置に落とす（必要なら少しオフセット）
            npcChicken.setWorldX(this.getWorldX());
            npcChicken.setWorldY(this.getWorldY());
            return;
        }

        String text;
        if (getInventory().size() < getMaxInventorySize()) {
            getInventory().add(obj);
            text = obj.getName() + " を手に入れた!";
            System.out.println("DEBUG pickup attempt: obj=" + obj + " at " + obj.getWorldX() + "," + obj.getWorldY() + " stacktrace:");
            Thread.dumpStack();

            updateInvincibility();
            gameWindow.getObj()[i] = null;
        } else {
            text = "手に入れていない!";
        }
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

            Entity monster = gameWindow.getMonster()[monsterIndex];
            // 相対ベクトルを計算
            double dx = getWorldX() - monster.getWorldX();
            double dy = getWorldY() - monster.getWorldY();
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
                setKnockBack(gameWindow.getMonster()[monsterIndex], attacker, monster.getKnockBackPower());
            }

            // モンスターのダメージ量
            int damage = setAttack(Math.max(gameWindow.getMonster()[monsterIndex].getAttack() - calculateTotalDefense(), 1));
            if (damage < 1) {
                damage = 1;
                setInvincible(true);
                setTransparent(true);
            }

            System.out.println("isCollision() = " + isCollision());
            setLife(getLife() - damage);

            int tileSize = FrameApp.getTileSize();

            int sx = gameWindow.getPlayer().getWorldX() - this.getWorldX() + this.getScreenX();
            int sy = gameWindow.getPlayer().getWorldY() - this.getWorldY() + this.getScreenY();

            sx += tileSize / 2;
            sy -= tileSize / 2;

            PopupVariant variant = PopupVariant.MONSTER_DAMAGE;

            gameWindow.getUi().getDamagePopupManager().pop(String.valueOf(damage), sx, sy, variant, 60);
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

        if (i == 999) return;

        Entity target = gameWindow.getMonster()[i];

        // 無敵チェック（ニワトリもここで無敵なら処理しない）
        if (target.getInvincible()) return;

        gameWindow.getSoundmanager().damageWAV("sound/damage-sound.wav");

        if (knockBackPower > 0) {
            setKnockBack(target, this, knockBackPower);
        }

        long end = System.nanoTime();
        System.out.println("サウンド再生にかかった時間: " + (end - start) + " ns");

        // プレイヤーのダメージ量
        int damage = setAttack(attack - target.getDefense());
        if (damage < 0) damage = 0;

        // --- ニワトリならモンスター用の死亡処理を行わず、専用の takeDamage を呼ぶ ---
        if (target instanceof NpcChicken) {
            ((NpcChicken) target).takeDamage(damage, knockBackPower);
            target.setRespawning(false);
            return;
        }

        // モンスター処理
        target.setLife(target.getLife() - damage);

        int tileSize = FrameApp.getTileSize();
        int sx = target.getWorldX() - this.getWorldX() + this.getScreenX();
        int sy = target.getWorldY() - this.getWorldY() + this.getScreenY();

        sx += tileSize / 2;
        sy -= tileSize / 2;

        PopupVariant variant = PopupVariant.PLAYER_DAMAGE;

        gameWindow.getUi().getDamagePopupManager().pop(String.valueOf(damage), sx, sy, variant, 60);

        target.setInvincible(true);
        target.damageReaction();

        if (target.getLife() <= 0) {
            Entity monster = target;

            target.setAlive(false);
            target.setDying(true);
            getSolidArea().width = 40;
            getSolidArea().height = 40;
            recordSafePosition();

            List<Supplier<Entity>> drops = Arrays.asList(
                    () -> new ObjCoinBronze(gameWindow),
                    () -> new ObjRedPotion(gameWindow),
                    () -> new ObjGreenPotion(gameWindow)
            );

            Collections.shuffle(drops);
            Entity dropped = drops.get(0).get();

            gameWindow.dropItem(dropped, monster);
            System.out.println("drops = " + drops);

            int gainedExp = target.getExp();
            this.setExp(getExp() + gainedExp);

            variant = PopupVariant.XP;
            gameWindow.getUi().getDamagePopupManager().pop(String.format("+%d XP", gainedExp), sx, sy, variant, 60);

            checkLevelUp();
            gameWindow.getSoundmanager().defeatedWAV("sound/defeated-sound.wav");

            // リスボーン
            int aliveCount = 0;
            for (Entity m : gameWindow.getMonster()) {
                if (m == null) continue;
                NpcChicken npcChicken = (NpcChicken) monster;
                if (!npcChicken.getType().canRespawn()) continue;
                if (!m.getDying() && m.getLife() > 0) aliveCount++;
            }

            if (aliveCount == 0 && !monster.isRespawning()) {
                monster.setRespawning(true);
                Timer respawnTimer = getTimer(monster);
                respawnTimer.start();
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
        return item == getCurrentWeapon() || item == getCurrentShield();
    }

    public int searchItemInInventory(String itemName) {

        int itemIndex = 999;

        for (int i = 0; i < getInventory().size(); i++) {
            if (getInventory().get(i).getName().equals(itemName)) {
                itemIndex = i;
                break;
            }
        }
        return itemIndex;
    }

    public boolean canObtainItem(Entity item) {

        boolean canObtain = false;
        if (item == null) return false;
        if (item instanceof object.ObjBomb) return false;

        //スタック可能かチェックする
        if (item.isStackable() == true) {

            int index = searchItemInInventory(item.getName());

            if (index != 999) {
                getInventory().get(index).setAmount(getInventory().get(index).getAmount() + 1);
                canObtain = true;

                // 新しいアイテムの空きスロットをチェックする
            } else {
                if (getInventory().size() != getMaxInventorySize()) {
                    getInventory().add(item);
                    canObtain = true;
                }
            }

            // スタック不可能なので空きスロットを確認する
        } else {
            if (getInventory().size() != getMaxInventorySize()) {
                getInventory().add(item);
                canObtain = true;
            }
        }
        return canObtain;
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

        // 透明表示
        if (isTransparent()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }

        // aura の描画（省略せず安全に）
        if (aura != null) {
            if (!(aura.getWorldX() == 0 && aura.getWorldY() == 0)) {
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

        BufferedImage img = null;

        boolean debugCollision = gameWindow.getKeyHandler().isShowDebugText();

        if (debugCollision) {

            // 元の composite / color を保存
            Composite beforeComp = g2.getComposite();
            Color beforeColor = g2.getColor();

            // カメラ（画面基準）を一度だけ取得
            int playerWorldX = gameWindow.getPlayer().getWorldX();
            int playerWorldY = gameWindow.getPlayer().getWorldY();
            int camScreenX = gameWindow.getPlayer().getScreenX();
            int camScreenY = gameWindow.getPlayer().getScreenY();

// 判定で使う solidArea（world基準）
            Rectangle sa = getSolidArea();
            int saWorldX = getWorldX() + sa.x;
            int saWorldY = getWorldY() + sa.y;

// スクリーン座標に変換（描画は必ずこの式を使う）
            int saScreenX = saWorldX - playerWorldX + camScreenX;
            int saScreenY = saWorldY - playerWorldY + camScreenY;

// 描画（現在位置）
            g2.setColor(new Color(255, 0, 0, 200));
            g2.drawRect(saScreenX, saScreenY, sa.width, sa.height);

// 移動先（world基準で計算してからスクリーンに変換）
            int moveWorldX = saWorldX + (getDirection().equals("left") ? -getSpeed() : getDirection().equals("right") ? getSpeed() : 0);
            int moveWorldY = saWorldY + (getDirection().equals("up") ? -getSpeed() : getDirection().equals("down") ? getSpeed() : 0);
            int moveScreenX = moveWorldX - playerWorldX + camScreenX;
            int moveScreenY = moveWorldY - playerWorldY + camScreenY;

            g2.setColor(new Color(255, 0, 0, 80));
            g2.fillRect(moveScreenX, moveScreenY, sa.width, sa.height);
            g2.setColor(new Color(255, 0, 0, 200));
            g2.drawRect(moveScreenX, moveScreenY, sa.width, sa.height);


            // 判定に使われるタイルを描画（プレイヤー周辺のタイルをチェック）
            TileManager tm = gameWindow.getTileManager();
            int[][] mapTileNum = tm.getMapTileNum();

            // プレイヤーのワールド矩形（左上）を計算
            int worldLeft = playerWorldX + getSolidArea().x;
            int worldTop = playerWorldY + getSolidArea().y;
            int worldRight = worldLeft + getSolidArea().width;
            int worldBottom = worldTop + getSolidArea().height;

            int leftCol = Math.floorDiv(worldLeft, tileSize);
            int rightCol = Math.floorDiv(worldRight, tileSize);
            int topRow = Math.floorDiv(worldTop, tileSize);
            int bottomRow = Math.floorDiv(worldBottom, tileSize);

            // 周辺タイル（余裕を持って1タイル分拡張）を描画
            int minCol = Math.max(0, leftCol - 1);
            int maxCol = Math.min(mapTileNum.length - 1, rightCol + 1);
            int minRow = Math.max(0, topRow - 1);
            int maxRow = Math.min(mapTileNum[0].length - 1, bottomRow + 1);

            for (int c = minCol; c <= maxCol; c++) {
                for (int r = minRow; r <= maxRow; r++) {
                    int tileWorldX = c * tileSize;
                    int tileWorldY = r * tileSize;
                    int tileScreenX = tileWorldX - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
                    int tileScreenY = tileWorldY - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

                    // タイル矩形を薄い赤で塗る（衝突タイルは濃くする）
                    boolean isCollisionTile = false;
                    try {
                        int tileId = mapTileNum[c][r];
                        Tile[] tiles = tm.getTiles();
                        if (tileId >= 0 && tileId < tiles.length) {
                            isCollisionTile = tiles[tileId].collision || tiles[tileId].bombCollision || tiles[tileId].potCollision;
                        } else {
                            isCollisionTile = true; // 安全側
                        }
                    } catch (Throwable ignored) {
                    }

                    if (isCollisionTile) {
                        g2.setColor(new Color(255, 0, 0, 100));
                        g2.fillRect(tileScreenX, tileScreenY, tileSize, tileSize);
                        //            // 枠線の描画
                        int margine = 2;
                        int size = FrameApp.getTileSize() - (margine * 2);
                        g2.drawRect(screenX + margine, screenY + margine, size, size);

                        for (int i = 0; i < 10; i++) {
                            // ランダムな位置とサイズを生成
                            int particleX = screenX + margine + (int) (Math.random() * size);
                            int particleY = screenY + margine + (int) (Math.random() * size);
                            // 1～5のサイズ
                            int particleSize = (int) (Math.random() * 5 + 1);

                            // 半透明の白色またはランダムな色
                            g2.setColor(new Color(255, 255, 255, (int) (Math.random() * 128 + 128)));
                            g2.fillOval(particleX, particleY, particleSize, particleSize);
                        }
                        g2.drawRect(tileScreenX, tileScreenY, tileSize, tileSize);
                    } else {
                        g2.setColor(new Color(255, 0, 0, 40));
                        g2.drawRect(tileScreenX, tileScreenY, tileSize, tileSize);
                    }
                }
            }

            // restore
            g2.setComposite(beforeComp);
            g2.setColor(beforeColor);
        }

        // --- ガード（盾）描画を判定 ---
        boolean isShield = getCurrentShield() != null && getCurrentShield().getType() instanceof ShieldType;
        if (isGuarding() && isShield) {
            String[] animationKeys = GUARD_DIRECTIONS;   // ガード用方向配列
            BufferedImage[][] spriteSet = guardSprites;  // ガード用スプライト配列

            int dirIndex = Arrays.asList(animationKeys).indexOf(getGuardDirection()); // ガードは向きベース
            if (dirIndex >= 0 && spriteSet != null && dirIndex < spriteSet.length && spriteSet[dirIndex] != null) {
                int frameIndex = Math.max(0, getSpriteNum() - 1);
                int maxIndex = spriteSet[dirIndex].length - 1;
                if (frameIndex > maxIndex) frameIndex = maxIndex;
                img = spriteSet[dirIndex][frameIndex];
                if (img != null) {
                    int drawWidth = (dirIndex == 2 || dirIndex == 3) ? tileSize : tileSize;
                    int drawHeight = (dirIndex == 0 || dirIndex == 1) ? tileSize : tileSize;
                    int drawX = animationKeys[dirIndex].endsWith("Left") ? screenX : screenX;
                    int drawY = animationKeys[dirIndex].endsWith("Up") ? screenY : screenY;
                    g2.drawImage(img, drawX, drawY, drawWidth, drawHeight, null);
                }
            }
        }

        // --- 通常歩行／待機描画 ---
        else if (!getAttacking()) {
            int walkDirIndex = Arrays.asList(DIRECTIONS).indexOf(getDirection());
            if (walkDirIndex >= 0) {
                // sprites 配列の存在と長さチェック
                if (sprites != null && walkDirIndex < sprites.length && sprites[walkDirIndex] != null) {
                    int spriteNumIndex = Math.max(0, getSpriteNum() - 1);
                    int maxIndex = sprites[walkDirIndex].length - 1;
                    if (spriteNumIndex > maxIndex) spriteNumIndex = maxIndex;
                    if (spriteNumIndex < 0) spriteNumIndex = 0;
                    img = sprites[walkDirIndex][spriteNumIndex];
                    if (img != null) g2.drawImage(img, screenX, screenY, tileSize, tileSize, null);
                }
            }
        } else {
            // --- 攻撃アニメ/盾アニメ描画（axe/attack） ---
            boolean isWeapon = getCurrentWeapon() != null && getCurrentWeapon().getType() instanceof AxeType;

            String[] animationKeys;
            BufferedImage[][] spriteSet;

            animationKeys = isWeapon ? AXE_DIRECTIONS : ATTACK_DIRECTIONS;
            spriteSet = isWeapon ? axeSprites : attackSprites;

            int directionIndex = Arrays.asList(animationKeys).indexOf(getAttackDirection());
            if (directionIndex >= 0 && spriteSet != null && directionIndex < spriteSet.length && spriteSet[directionIndex] != null) {
                int frameIndex = Math.max(0, getSpriteNum() - 1);
                int maxIndex = spriteSet[directionIndex].length - 1;
                if (frameIndex > maxIndex) frameIndex = maxIndex;
                if (frameIndex < 0) frameIndex = 0;
                img = spriteSet[directionIndex][frameIndex];

                if (img != null) {
                    int drawWidth = (directionIndex == 2 || directionIndex == 3) ? tileSize * 2 : tileSize;
                    int drawHeight = (directionIndex == 0 || directionIndex == 1) ? tileSize * 2 : tileSize;
                    int drawX = animationKeys[directionIndex].endsWith("Left") ? screenX - tileSize : screenX;
                    int drawY = animationKeys[directionIndex].endsWith("Up") ? screenY - tileSize : screenY;
                    g2.drawImage(img, drawX, drawY, drawWidth, drawHeight, null);
                }

                // デバッグ：攻撃エリアを矩形で描画
//                int drawWidth = (directionIndex == 2 || directionIndex == 3) ? tileSize * 2 : tileSize;
//                int drawHeight = (directionIndex == 0 || directionIndex == 1) ? tileSize * 2 : tileSize;
//                int drawX = animationKeys[directionIndex].endsWith("Left") ? screenX - tileSize : screenX;
//                int drawY = animationKeys[directionIndex].endsWith("Up") ? screenY - tileSize : screenY;
//                Rectangle attackBox = new Rectangle(drawX, drawY, drawWidth, drawHeight);
//                gameWindow.getUi().addMessage("drawWidth =" + drawWidth);
//                gameWindow.getUi().addMessage("drawWidth =" + drawHeight);
//                g2.setColor(new Color(255, 0, 0, 255));
//                g2.drawRect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
            }
        }

        // --- 所持中の爆弾を頭上に描画 ---
        if (state == PlayerState.HOLD_WALK) {
            if (this.holding && this.heldBomb != null) {
                BufferedImage heldImg = heldBomb.getHeldSprite();
                if (heldImg != null) {
                    int ts = FrameApp.getTileSize();
                    Rectangle sa = getSolidArea();
                    int headScreenX = screenX + sa.x + sa.width / 2 - heldImg.getWidth() / 2;
                    int headScreenY = screenY + sa.y - heldImg.getHeight();

                    switch (getDirection()) {
                        case "up" -> headScreenY -= ts / 8;
                        case "down" -> headScreenY += ts / 16;
                        case "left" -> headScreenX -= ts / 8;
                        case "right" -> headScreenX += ts / 8;
                    }

                    Composite before = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    g2.setColor(new Color(0, 0, 0, 80));
                    int shadowW = heldImg.getWidth() * 9 / 10;
                    int shadowH = Math.max(2, heldImg.getHeight() / 8);
                    g2.fillOval(headScreenX + (heldImg.getWidth() - shadowW) / 2,
                            headScreenY + heldImg.getHeight() - shadowH / 2,
                            shadowW, shadowH);
                    g2.setComposite(before);

                    g2.drawImage(heldImg, headScreenX, headScreenY, null);
                }
            }
        }

        // --- 所持中の壺を頭上に描画 ---
        if (state == PlayerState.HOLD_WALK) {
            if (this.holding && this.heldPot != null) {
                BufferedImage heldImg = heldPot.getHeldSprite();
                if (heldImg != null) {
                    int ts = FrameApp.getTileSize();
                    Rectangle sa = getSolidArea();
                    int headScreenX = screenX + sa.x + sa.width / 2 - heldImg.getWidth() / 2;
                    int headScreenY = screenY + sa.y - heldImg.getHeight();

                    switch (getDirection()) {
                        case "up" -> headScreenY -= ts / 8;
                        case "down" -> headScreenY += ts / 16;
                        case "left" -> headScreenX -= ts / 8;
                        case "right" -> headScreenX += ts / 8;
                    }

                    Composite before = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    g2.setColor(new Color(0, 0, 0, 80));
                    int shadowW = heldImg.getWidth() * 9 / 10;
                    int shadowH = Math.max(2, heldImg.getHeight() / 8);
                    g2.fillOval(headScreenX + (heldImg.getWidth() - shadowW) / 2,
                            headScreenY + heldImg.getHeight() - shadowH / 2,
                            shadowW, shadowH);
                    g2.setComposite(before);

                    g2.drawImage(heldImg, headScreenX, headScreenY, null);
                }
            }
        }

        // --- 所持中の岩を頭上に描画 ---
        if (state == PlayerState.HOLD_WALK) {
            if (this.holding && this.heldRock != null) {
                BufferedImage heldImg = heldRock.getHeldSprite();
                if (heldImg != null) {
                    int ts = FrameApp.getTileSize();
                    Rectangle sa = getSolidArea();
                    int headScreenX = screenX + sa.x + sa.width / 2 - heldImg.getWidth() / 2;
                    int headScreenY = screenY + sa.y - heldImg.getHeight();

                    switch (getDirection()) {
                        case "up" -> headScreenY -= ts / 8;
                        case "down" -> headScreenY += ts / 16;
                        case "left" -> headScreenX -= ts / 8;
                        case "right" -> headScreenX += ts / 8;
                    }

                    Composite before = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    g2.setColor(new Color(0, 0, 0, 80));
                    int shadowW = heldImg.getWidth() * 9 / 10;
                    int shadowH = Math.max(2, heldImg.getHeight() / 8);
                    g2.fillOval(headScreenX + (heldImg.getWidth() - shadowW) / 2,
                            headScreenY + heldImg.getHeight() - shadowH / 2,
                            shadowW, shadowH);
                    g2.setComposite(before);

                    g2.drawImage(heldImg, headScreenX, headScreenY, null);
                }
            }
        }

        // --- 所持中のニワトリを頭上に描画 ---
        if (state == PlayerState.HOLD_WALK) {
            if (this.holding && this.heldChicken != null) {
                BufferedImage heldImg = heldChicken.getHeldSprite();
                if (heldImg != null) {
                    int ts = FrameApp.getTileSize();
                    Rectangle sa = getSolidArea();
                    int headScreenX = screenX + sa.x + sa.width / 2 - heldImg.getWidth() / 2;
                    int headScreenY = screenY + sa.y - heldImg.getHeight();

                    switch (getDirection()) {
                        case "up" -> headScreenY -= ts / 8;
                        case "down" -> headScreenY += ts / 16;
                        case "left" -> headScreenX -= ts / 8;
                        case "right" -> headScreenX += ts / 8;
                    }

                    Composite before = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    g2.setColor(new Color(0, 0, 0, 80));
                    int shadowW = heldImg.getWidth() * 9 / 10;
                    int shadowH = Math.max(2, heldImg.getHeight() / 8);
                    g2.fillOval(headScreenX + (heldImg.getWidth() - shadowW) / 2,
                            headScreenY + heldImg.getHeight() - shadowH / 2,
                            shadowW, shadowH);
                    g2.setComposite(before);

                    g2.drawImage(heldImg, headScreenX, headScreenY, null);
                }
            }
        }

        // --- PICKUP アニメ描画（安全に） ---
        if (state == PlayerState.PICKUP) {
            int dirIndex = Arrays.asList(DIRECTIONS).indexOf(getDirection());
            if (dirIndex >= 0 && pickupSprites != null && dirIndex < pickupSprites.length && pickupSprites[dirIndex] != null) {
                int frameIndex = Math.max(0, getSpriteNum() - 1);
                int maxIndex = pickupSprites[dirIndex].length - 1;
                if (frameIndex > maxIndex) frameIndex = maxIndex;
                if (frameIndex < 0) frameIndex = 0;
                BufferedImage pickImg = pickupSprites[dirIndex][frameIndex];
                if (pickImg != null) {
                    g2.drawImage(pickImg, screenX, screenY, tileSize, tileSize, null);
                }
            }
            g2.setComposite(original);
            return;
        }

        // --- THROW アニメ描画（安全に） ---
        if (state == PlayerState.THROW) {
            if (heldBomb != null) {
                String dir = getDirection();
                int frame = Math.max(0, getSpriteNum() - 1);
                // SPRITE_COUNT と実際の heldBomb throw sprite 配列長の整合性をチェック
                BufferedImage throwImg = heldBomb.getThrowSprite(dir, frame);
                if (throwImg != null) {
                    int tx = screenX;
                    int ty = screenY;
                    switch (dir) {
                        case "up" -> ty -= tileSize;
                        case "down" -> ty += tileSize / 2;
                        case "left" -> tx -= tileSize;
                        case "right" -> tx += tileSize;
                    }
                    int tw = tileSize;
                    int th = tileSize;
                    g2.drawImage(throwImg, tx, ty, tw, th, null);
                }
            } else if (heldPot != null) {
                String dir = getDirection();
                int frame = Math.max(0, getSpriteNum() - 1);
                // SPRITE_COUNT と実際の heldBomb throw sprite 配列長の整合性をチェック
                BufferedImage throwImg = heldPot.getThrowSprite(dir, frame);
                if (throwImg != null) {
                    int tx = screenX;
                    int ty = screenY;
                    switch (dir) {
                        case "up" -> ty -= tileSize;
                        case "down" -> ty += tileSize / 2;
                        case "left" -> tx -= tileSize;
                        case "right" -> tx += tileSize;
                    }
                    int tw = tileSize;
                    int th = tileSize;
                    g2.drawImage(throwImg, tx, ty, tw, th, null);
                }
            } else if (heldRock != null) {
                String dir = getDirection();
                int frame = Math.max(0, getSpriteNum() - 1);
                // SPRITE_COUNT と実際の heldBomb throw sprite 配列長の整合性をチェック
                BufferedImage throwImg = heldRock.getThrowSprite(dir, frame);
                if (throwImg != null) {
                    int tx = screenX;
                    int ty = screenY;
                    switch (dir) {
                        case "up" -> ty -= tileSize;
                        case "down" -> ty += tileSize / 2;
                        case "left" -> tx -= tileSize;
                        case "right" -> tx += tileSize;
                    }
                    int tw = tileSize;
                    int th = tileSize;
                    g2.drawImage(throwImg, tx, ty, tw, th, null);
                }
            } else if (heldChicken != null) {

                String dir = getDirection();
                int frame = Math.max(0, getSpriteNum() - 1);
                // SPRITE_COUNT と実際の heldChicken throw sprite 配列長の整合性をチェック
                BufferedImage throwImg = heldChicken.getThrowSprite(dir, frame);
                if (throwImg != null) {
                    int tx = screenX;
                    int ty = screenY;
                    switch (dir) {
                        case "up" -> ty -= tileSize;
                        case "down" -> ty += tileSize / 2;
                        case "left" -> tx -= tileSize;
                        case "right" -> tx += tileSize;
                    }
                    int tw = tileSize;
                    int th = tileSize;
                    g2.drawImage(throwImg, tx, ty, tw, th, null);

                }
            }

            g2.setComposite(original);
        }
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
    }

    /**
     * 赤ポーションを拾ったときの処理を行う。
     * 最大HPを超えない範囲でライフを回復し、UIにメッセージを表示。
     *
     * @param potion 拾った赤ポーションオブジェクト
     */

    public void healRedPotion(ObjRedPotion potion) {

        int heal = potion.getHealAmount();
        setLife(Math.min(getLife() + heal, getMaxLife()));
        if (aura != null && getLife() == getMaxLife()) {
            long auraDurationMs = 30_000L;
            aura.activate(auraDurationMs);
        }
        int tileSize = FrameApp.getTileSize();
        int sx = this.getScreenX();
        int sy = this.getScreenY();

        sx += tileSize / 2;
        sy -= tileSize / 2;
        PopupVariant variant = PopupVariant.HEAL;
        gameWindow.getUi().getDamagePopupManager().pop(String.valueOf(heal), sx, sy, variant, 60);
    }

    /**
     * 緑ポーションを拾ったときの処理を行う。
     * 最大MPを超えない範囲でマナを回復し、UIにメッセージを表示。
     *
     * @param potion 拾った緑ポーションオブジェクト
     */

    public void healGreenPotion(ObjGreenPotion potion) {
        int heal = potion.getHealAmount();
        setMana(Math.min(getMana() + heal, getMaxMana()));
        int tileSize = FrameApp.getTileSize();
        int sx = this.getScreenX();
        int sy = this.getScreenY();

        sx += tileSize / 2;
        sy -= tileSize / 2;
        PopupVariant variant = PopupVariant.HEAL;
        gameWindow.getUi().getDamagePopupManager().pop(String.valueOf(heal), sx, sy, variant, 60);
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

    public PlayerState getState() {
        return state;
    }

    public PlayerState setState(PlayerState state) {
        return this.state = state;
    }

    public int getMapCols() {
        return FrameApp.getMaxWorldCol();
    }

    public int getMapRows() {
        return FrameApp.getMaxWorldRow();
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

    public boolean isBlockingLeft() {
        return blockingLeft;
    }

    public boolean hasLeftShield() {
        return getCurrentShield() != null && getCurrentShield().getType() instanceof ShieldType;
    }
}