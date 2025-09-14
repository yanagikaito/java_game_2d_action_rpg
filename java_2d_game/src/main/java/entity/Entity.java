package entity;

import frame.FrameApp;
import object.Projectile;
import player.Player;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public abstract class Entity {

    private GameWindow gameWindow;
    private int worldX;
    private int worldY;
    private int width;
    private int height;
    private int speed;
    private int defaultSpeed;
    private String direction;
    private String attackDirection;
    private String axeDirection;
    private BufferedImage[][] sprites;
    private BufferedImage[][] attackSprites;
    private BufferedImage image;
    private BufferedImage image2;
    private BufferedImage image3;
    private boolean collision;
    private int spriteCounter;
    private int spriteNum;
    private Rectangle solidArea;
    private Rectangle attackArea;
    private int solidAreaDefaultX;
    private int solidAreaDefaultY;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private String[] dialogue = new String[20];
    private static final int SPRITE_COUNT = 3;
    private static final int SPRITE_ANIMATION_THRESHOLD = 10;
    private static final int SPRITE_DYING_COUNT = 5;
    private static final int MAX_RANDOM_VALUE = 100;
    private static final int THRESHOLD_UP = 25;
    private static final int THRESHOLD_DOWN = 50;
    private static final int THRESHOLD_LEFT = 75;
    private int pixelCounter = 0;
    private int knockBackCounter = 0;
    private int actionLockCounter = 0;
    private static final int COLLISION_COOLDOWN_FRAMES = 30;
    private int collisionCooldown = 0;
    private int dialogueIndex = 0;
    private int maxLife;
    private int life;
    private String name;
    private boolean invincible = false;
    private int invincibleCounter = 0;
    private EntityType type;
    private int value = 1;
    private boolean attacking = false;
    private boolean alive = true;
    private boolean dying = false;
    private boolean hpBarOn = false;
    private int dyingCounter = 0;
    private int hpBarCounter = 0;
    private int level;
    private int strength;
    private int dexterity;
    private int attack;
    private int defense;
    private int exp;
    private int nextLevelExp;
    private int coin;
    private Entity currentWeapon;
    private Entity currentShield;
    private Entity attacker;
    private String knockBackDirection;
    private Projectile projectile;
    private int attackValue;
    private int defenseValue;
    private boolean respawning = false;
    private boolean knockBack = false;
    private int knockBackPower = 0;
    private String description = "";
    private int maxMana;
    private int mana;
    private int useCost;
    private int shotAvailableCounter = 0;
    private static final int[][] PARTICLE_OFFSETS = {
            {-1, -1},
            {1, -1},
            {-1, 1},
            {1, 1}
    };
    private ArrayList<Entity> inventory;
    private final int maxInventorySize;
    protected int mapId;
    private int price;
    protected int count;
    private int hitBoxX;
    private int hitBoxY;
    private double hitBoxWidth;
    private double hitBoxHeight;

    /**
     * Entity を初期化。
     *
     * @param gameWindow このエンティティが所属するゲームウィンドウ
     * @throws NullPointerException 引数 gameWindow が null の場合
     */

    public Entity(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.worldX = 0;
        this.worldY = 0;
        this.speed = 0;
        this.direction = "";
        this.attackDirection = "";
        this.name = "";
        this.collision = false;
        this.spriteCounter = 0;
        this.spriteNum = 1;
        this.solidArea = new Rectangle(0, 0, FrameApp.getTileSize(), FrameApp.getTileSize());
        this.attackArea = new Rectangle(0, 0, 0, 0);
        this.solidAreaDefaultX = 0;
        this.solidAreaDefaultY = 0;
        this.sprites = new BufferedImage[4][3];
        this.attackSprites = new BufferedImage[4][3];
        this.attackValue = 0;
        this.defenseValue = 0;
        this.level = 0;
        this.strength = 0;
        this.dexterity = 0;
        this.attack = 0;
        this.defense = 0;
        this.exp = 0;
        this.nextLevelExp = 0;
        this.coin = 0;
        this.inventory = new ArrayList<>();
        this.maxInventorySize = 20;
        this.count = 1;
    }

    /**
     * エンティティの行動を決定する処理を実装。
     *
     * @throws UnsupportedOperationException 行動決定ロジックが実装されていない場合
     */

    public void setAction() {

    }

    /**
     * ダメージを受けた際のリアクションを実装します。
     *
     * @throws UnsupportedOperationException ダメージリアクションが実装されていない場合
     */

    public void damageReaction() {

    }

    /**
     * 毎フレーム呼び出され、エンティティの状態更新を行う。
     *
     * @throws IllegalStateException 衝突判定機能が利用できない場合
     */

    public void update() {
        if (gameWindow.getCollisionChecker() == null) {
            throw new IllegalStateException("CollisionChecker の初期化が行われていない");
        }

        if (knockBack == true) {
            checkCollision();
            if (collision == true) {
                knockBackCounter = 0;
                knockBack = false;
                speed = defaultSpeed;
            } else if (collision == false) {
                switch (knockBackDirection) {
                    case "up":
                        worldY -= getSpeed();
                        break;
                    case "down":
                        worldY += getSpeed();
                        break;
                    case "left":
                        worldX -= getSpeed();
                        break;
                    case "right":
                        worldX += getSpeed();
                        break;
                }
            }

            knockBackCounter++;
            if (knockBackCounter == 10) {
                knockBackCounter = 0;
                knockBack = false;
                speed = defaultSpeed;
            }

        } else {
            // 方向を設定（フロー・フィールドで決定）
            setAction();
            checkCollision();

            if (!isCollision()) {
                move();
                collisionCooldown = 0;
            } else {
                if (collisionCooldown == 0) {
                    chooseNewDirection();
                    collisionCooldown = COLLISION_COOLDOWN_FRAMES;
                } else {
                    collisionCooldown--;
                }
            }
        }

        // スプライトアニメーション
        setSpriteCounter(getSpriteCounter() + 1);
        if (getSpriteCounter() > SPRITE_ANIMATION_THRESHOLD) {
            setSpriteNum((getSpriteNum() % SPRITE_COUNT) + 1);
            setSpriteCounter(0);
        }

        pixelCounter += getSpeed();
        if (pixelCounter >= FrameApp.getTileSize()) {
            pixelCounter = 0;
        }

        if (shotAvailableCounter < 30) {
            shotAvailableCounter++;
        }
    }

    public void checkCollision() {
        setCollision(false);
        gameWindow.getCollisionChecker().checkTile(this);
        gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC());
        gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile());
        boolean contactPlayer = gameWindow.getCollisionChecker().checkPlayer(this);

        if (getType() instanceof MonsterType && contactPlayer && !gameWindow.getPlayer().getInvincible()) {
            if (!getInvincible()) {
                damagePlayer(attack, knockBackPower);
                gameWindow.getPlayer().setInvincible(true);
            }
        }
    }

    /**
     * プレイヤーにダメージを与え、ライフが0以下になったら
     * ゲームオーバー状態に遷移する。
     *
     * @param attack プレイヤーに与えられる攻撃力
     * @throws NullPointerException gameWindowまたはplayerが未初期化の場合
     */

    public void damagePlayer(int attack, int knockBackPower) {

        Player player = gameWindow.getPlayer();

        if (!gameWindow.getPlayer().getInvincible()) {

            gameWindow.getSoundmanager().damageWAV("sound/damage-sound.wav");

            // ノックバック
            if (knockBackPower > 0) {
                setKnockBack(player, this, knockBackPower);
            }

            int damage = setAttack(Math.max(attack - gameWindow.getPlayer().calculateTotalDefense(), 1));
            if (damage < 0) {
                damage = 0;
            }

            gameWindow.getPlayer().setLife(gameWindow.getPlayer().getLife() - damage);
            gameWindow.getPlayer().setInvincible(true);
            setInvincibleCounter(0);
        }

        if (gameWindow.getPlayer().getLife() <= 0) {
            gameWindow.getPlayer().setLife(0);
            gameWindow.setGameState(gameWindow.getGameOverState());
        }
    }

    /**
     * キャラクターが話しかける動作を行うメソッド。
     *
     * @throws UnsupportedOperationException 実装されていない場合
     */

    public void speak() {

    }

    /**
     * 現在の方向に応じて、エンティティを移動させる。
     */

    protected void move() {

        switch (getDirection()) {
            case "up":
                worldY -= getSpeed();
                break;
            case "down":
                worldY += getSpeed();
                break;
            case "left":
                worldX -= getSpeed();
                break;
            case "right":
                worldX += getSpeed();
                break;
        }
    }

    /**
     * ランダムな数値を生成して次の移動方向を決定する。
     *
     * @throws IllegalStateException MAX_RANDOM_VALUE や
     *                               THRESHOLD_* 定数が不正な値の場合
     */

    private void chooseNewDirection() {

        actionLockCounter++;

        int i = (new Random()).nextInt(MAX_RANDOM_VALUE) + 1;
        if (i <= THRESHOLD_UP) {
            setDirection("up");
        } else if (i <= THRESHOLD_DOWN) {
            setDirection("down");
        } else if (i <= THRESHOLD_LEFT) {
            setDirection("left");
        } else {
            setDirection("right");
        }
        actionLockCounter = 0;
    }

    /**
     * ワールド上の X 座標を取得。
     *
     * @return worldX（ワールド座標系の X 値）
     * @throws IllegalStateException worldX が不正な状態（未初期化など）の場合
     */

    public int getWorldX() {
        return worldX;
    }

    /**
     * ワールド上の X 座標を設定。
     *
     * @param worldX セットする X 座標
     * @throws IllegalArgumentException worldX に負の値が渡された場合
     */

    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }

    /**
     * ワールド上の Y 座標を取得。
     *
     * @return worldY（ワールド座標系の Y 値）
     * @throws IllegalStateException worldY が不正な状態（未初期化など）の場合
     */

    public int getWorldY() {
        return worldY;
    }

    /**
     * ワールド上の Y 座標を設定。
     *
     * @param worldY セットする Y 座標
     * @throws IllegalArgumentException worldY に負の値が渡された場合
     */

    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }

    /**
     * キャラクターの移動速度を取得。
     *
     * @return speed（ピクセル/フレーム）
     * @throws IllegalStateException speed が不正（未初期化など）な場合
     */

    public int getSpeed() {
        return speed;
    }

    /**
     * キャラクターの移動速度を設定。
     *
     * @param speed セットする移動速度（ピクセル/フレーム）
     * @throws IllegalArgumentException speed に 0 以下の値が渡された場合
     */

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * キャラクターの向きを取得。
     *
     * @return direction（"up"、"down"、"left"、"right" のいずれか）
     * @throws IllegalStateException direction が未設定または不正な場合
     */

    public String getDirection() {
        return direction;
    }

    /**
     * 向きごとのスプライト配列を取得。
     *
     * @return sprites[方向][フレームインデックス]
     * @throws IllegalStateException sprites が未初期化の場合
     */

    public BufferedImage[][] getSprites() {
        return sprites;
    }

    /**
     * スプライト配列を設定。
     *
     * @param sprites 方向およびフレームごとのスプライト画像配列
     * @throws NullPointerException sprites が null の場合
     */

    public void setSprites(BufferedImage[][] sprites) {
        if (sprites == null) {
            throw new NullPointerException("sprites は null であってはいけない");
        }
        this.sprites = sprites;
    }

    /**
     * キャラクターの向きを設定。
     *
     * @param direction "up"、"down"、"left"、"right" のいずれか
     * @throws IllegalArgumentException direction が不正な場合
     */

    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * 衝突フラグを取得。
     *
     * @return collision true: 衝突中, false: 衝突していない
     * @throws IllegalStateException collision が未定義の場合
     */

    public boolean isCollision() {
        return collision;
    }

    /**
     * 衝突フラグを設定。
     *
     * @param collision true にすると衝突状態とみなす
     */

    public void setCollision(boolean collision) {
        this.collision = collision;
    }

    /**
     * アニメーション用カウンタを取得。
     *
     * @return spriteCounter 現在のフレームカウンタ
     */

    public int getSpriteCounter() {
        return spriteCounter;
    }

    /**
     * アニメーション用カウンタを設定。
     *
     * @param spriteCounter セットするフレームカウンタ
     * @throws IllegalArgumentException spriteCounter が負の値の場合
     */

    public void setSpriteCounter(int spriteCounter) {
        this.spriteCounter = spriteCounter;
    }

    /**
     * 現在のスプライト番号を取得。
     *
     * @return spriteNum (0 以上 sprites.length-1 の範囲)
     */

    public int getSpriteNum() {
        return spriteNum;
    }

    /**
     * 衝突判定領域の既定 X オフセットを取得。
     *
     * @return solidAreaDefaultX デフォルトの X オフセット
     */

    public int getSolidAreaDefaultX() {
        return solidAreaDefaultX;
    }

    /**
     * 衝突判定領域の既定 Y オフセットを取得。
     *
     * @return solidAreaDefaultY デフォルトの Y オフセット
     */

    public int getSolidAreaDefaultY() {
        return solidAreaDefaultY;
    }

    /**
     * スプライト番号を設定。
     *
     * @param spriteNum セットするスプライト番号
     * @throws IllegalArgumentException spriteNum が負またはスプライト数以上の場合
     */

    public void setSpriteNum(int spriteNum) {
        this.spriteNum = spriteNum;
    }

    /**
     * 衝突判定用の領域矩形を取得。
     *
     * @return solidArea 衝突領域を表す Rectangle
     * @throws IllegalStateException solidArea が未初期化の場合
     */

    public Rectangle getSolidArea() {
        return solidArea;
    }

    /**
     * 衝突判定領域を設定。
     *
     * @param solidArea 衝突領域を表す Rectangle
     * @throws NullPointerException solidArea が null の場合
     */

    public void setSolidArea(Rectangle solidArea) {
        this.solidArea = solidArea;
    }

    /**
     * 衝突判定領域の既定 X オフセットを設定。
     *
     * @param solidAreaDefaultX セットする X オフセット
     * @throws IllegalArgumentException オフセットが負の場合
     */

    public void setSolidAreaDefaultX(int solidAreaDefaultX) {
        this.solidAreaDefaultX = solidAreaDefaultX;
    }

    /**
     * 衝突判定領域の既定 Y オフセットを設定。
     *
     * @param solidAreaDefaultY セットする Y オフセット
     * @throws IllegalArgumentException オフセットが負の場合
     */

    public void setSolidAreaDefaultY(int solidAreaDefaultY) {
        this.solidAreaDefaultY = solidAreaDefaultY;
    }

    /**
     * ダイアログ文字列配列を取得。
     *
     * @return dialogue 吹き出しで表示するメッセージ配列
     * @throws IllegalStateException dialogue が未初期化の場合
     */

    public String[] getDialogue() {
        return dialogue;
    }

    /**
     * ゲームウィンドウインスタンスを取得。
     *
     * @return gameWindow UI やサウンドを管理する GameWindow
     * @throws IllegalStateException gameWindow が未初期化の場合
     */

    public GameWindow getGameWindow() {
        return gameWindow;
    }

    /**
     * 現在のダイアログインデックスを取得。
     *
     * @return dialogueIndex 次に表示するメッセージのインデックス
     */

    public int getDialogueIndex() {
        return dialogueIndex;
    }

    /**
     * ダイアログインデックスを設定。
     *
     * @param dialogueIndex セットするインデックス
     * @throws IllegalArgumentException インデックスが範囲外の場合
     */

    public void setDialogueIndex(int dialogueIndex) {
        this.dialogueIndex = dialogueIndex;
    }

    /**
     * 最大ライフを取得。
     *
     * @return maxLife このエンティティの最大ライフ値
     * @throws IllegalStateException maxLife が未設定または不正な場合
     */

    public int getMaxLife() {
        return maxLife;
    }

    /**
     * 最大ライフを設定。
     *
     * @param maxLife 設定する最大ライフ値（0以上）
     * @throws IllegalArgumentException maxLife に負の値が渡された場合
     */

    public void setMaxLife(int maxLife) {
        this.maxLife = maxLife;
    }

    /**
     * 現在のライフを取得。
     *
     * @return life このエンティティの現在ライフ値
     * @throws IllegalStateException life が未設定または不正な場合
     */

    public int getLife() {
        return life;
    }

    /**
     * 現在のライフを設定。
     *
     * @param life 設定するライフ値（0以上）
     * @throws IllegalArgumentException life に負の値が渡された場合
     */

    public void setLife(int life) {
        this.life = life;
    }

    /**
     * エンティティ名を取得。
     *
     * @return name このエンティティの名前文字列
     * @throws IllegalStateException name が未設定の場合
     */

    public String getName() {
        return name;
    }

    /**
     * メイン表示用の画像を取得。
     *
     * @return BufferedImage このエンティティのメイン画像
     * @throws IllegalStateException 画像が未初期化（null）の場合
     */
    public BufferedImage getImage() {
        if (image == null) {
            throw new IllegalStateException("image が未初期化");
        }
        return image;
    }

    /**
     * サブ表示用の２枚目の画像を取得。
     *
     * @return BufferedImage このエンティティの２番目の画像
     * @throws IllegalStateException 画像2が未初期化（null）の場合
     */
    public BufferedImage getImage2() {
        if (image2 == null) {
            throw new IllegalStateException("image2 が未初期化");
        }
        return image2;
    }

    /**
     * サブ表示用の３枚目の画像を取得。
     *
     * @return BufferedImage このエンティティの３番目の画像
     * @throws IllegalStateException 画像3が未初期化（null）の場合
     */
    public BufferedImage getImage3() {
        if (image3 == null) {
            throw new IllegalStateException("image3 が未初期化");
        }
        return image3;
    }

    /**
     * エンティティ名を設定します。
     *
     * @param name 設定する名前文字列（非null・非空文字列）
     * @throws IllegalArgumentException name が null または空文字列の場合
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 無敵状態フラグを取得。
     *
     * @return invincible 無敵状態の場合は true、無敵状態でない場合は false
     * @throws IllegalStateException invincible フィールドが初期化されていない場合
     */

    public boolean getInvincible() {
        return invincible;
    }

    /**
     * 無敵状態フラグを設定。
     *
     * @param invincible 設定する無敵状態（true：無敵、false：通常）
     */

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    /**
     * 無敵状態の持続カウンターを取得。
     *
     * @return invincibleCounter 無敵状態が続いている残りフレーム数
     * @throws IllegalStateException invincibleCounter フィールドが初期化されていない場合
     */

    public int getInvincibleCounter() {
        return invincibleCounter;
    }

    /**
     * 無敵状態の持続カウンターを設定。
     *
     * @param invincibleCounter 設定するカウンター値（0以上）
     * @throws IllegalArgumentException invincibleCounter に負の値が渡された場合
     */

    public void setInvincibleCounter(int invincibleCounter) {
        this.invincibleCounter = invincibleCounter;
    }

    /**
     * このエンティティのタイプを取得。
     *
     * @return type エンティティタイプを示す定数値
     * @throws IllegalStateException type フィールドが未設定の場合
     */

    public EntityType getType() {
        return type;
    }

    /**
     * エンティティのタイプを設定。
     *
     * @param type 設定するエンティティタイプ（0以上の定数値）
     * @throws IllegalArgumentException type に負の値が渡された場合
     */

    public void setType(EntityType type) {
        this.type = type;
    }

    /**
     * 攻撃中かどうかを取得。
     *
     * @return attacking true: 攻撃中、false: 非攻撃状態
     * @throws IllegalStateException attacking フィールドが未初期化の場合
     */

    public boolean getAttacking() {
        return attacking;
    }

    /**
     * 攻撃中フラグを設定。
     *
     * @param attacking true にすると攻撃中、false にすると非攻撃状態
     */

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    /**
     * 現在選択中の攻撃方向を取得します。
     *
     * @return attackDirection 現在の攻撃方向文字列
     * @throws IllegalStateException attackDirection フィールドが未設定の場合
     */

    public String getAttackDirection() {
        return attackDirection;
    }

    /**
     * 攻撃方向を設定。
     *
     * @param attackDirection 設定する攻撃方向（ATTACK_DIRECTIONS のいずれか）
     * @throws IllegalArgumentException attackDirection が配列に含まれない場合
     */

    public void setAttackDirection(String attackDirection) {
        this.attackDirection = attackDirection;
    }

    /**
     * 攻撃判定領域を取得。
     *
     * @return attackArea 攻撃判定用の Rectangle オブジェクト
     * @throws IllegalStateException attackArea フィールドが未設定の場合
     */

    public Rectangle getAttackArea() {
        return attackArea;
    }

    /**
     * 攻撃判定領域を設定。
     *
     * @param attackArea 設定する攻撃領域（非 null）
     * @throws IllegalArgumentException attackArea が null の場合
     */

    public void setAttackArea(Rectangle attackArea) {
        this.attackArea = attackArea;
    }

    /**
     * 死亡中フラグを取得。
     *
     * @return dying true: 死亡アニメーション中、false: 通常状態
     * @throws IllegalStateException dying フィールドが未初期化の場合
     */

    public boolean getDying() {
        return dying;
    }

    /**
     * 死亡中フラグを設定。
     *
     * @param dying true にすると死亡アニメーション中、false にすると通常状態
     */

    public void setDying(boolean dying) {
        this.dying = dying;
    }

    /**
     * 生存中かどうかを取得。
     *
     * @return alive true: 生存中、false: 死亡中
     * @throws IllegalStateException alive フィールドが未初期化の場合
     */

    public boolean getAlive() {
        return alive;
    }

    /**
     * 生存フラグを設定。
     *
     * @param alive true にすると生存状態、false にすると死亡状態
     */

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * 基本攻撃力を取得。
     *
     * @return attackValue このエンティティの攻撃力
     * @throws IllegalStateException attackValue が未初期化の場合
     */

    public int getAttackValue() {
        return attackValue;
    }

    /**
     * 基本攻撃力を設定。
     *
     * @param attackValue 設定する攻撃力（0以上）
     * @throws IllegalArgumentException attackValue に負の値が渡された場合
     */

    public void setAttackValue(int attackValue) {
        this.attackValue = attackValue;
    }

    /**
     * 基本防御力を取得。
     *
     * @return defenseValue このエンティティの防御力
     * @throws IllegalStateException defenseValue が未初期化の場合
     */

    public int getDefenseValue() {
        return defenseValue;
    }

    /**
     * 基本防御力を設定。
     *
     * @param defenseValue 設定する防御力（0以上）
     * @throws IllegalArgumentException defenseValue に負の値が渡された場合
     */

    public void setDefenseValue(int defenseValue) {
        this.defenseValue = defenseValue;
    }

    /**
     * 現在のレベルを取得。
     *
     * @return level 現在のレベル
     * @throws IllegalStateException level が未初期化の場合
     */

    public int getLevel() {
        return level;
    }

    /**
     * レベルを設定。
     *
     * @param level 設定するレベル（1以上）
     * @throws IllegalArgumentException level に1未満の値が渡された場合
     */

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * 力（Strength）値を取得。
     *
     * @return strength このエンティティの力パラメータ
     * @throws IllegalStateException strength が未初期化の場合
     */

    public int getStrength() {
        return strength;
    }

    /**
     * 力（Strength）値を設定。
     *
     * @param strength 設定する力パラメータ（0以上）
     * @throws IllegalArgumentException strength に負の値が渡された場合
     */

    public void setStrength(int strength) {
        this.strength = strength;
    }

    /**
     * 器用さ（Dexterity）値を取得。
     *
     * @return dexterity このエンティティの器用さパラメータ
     * @throws IllegalStateException dexterity が未初期化の場合
     */

    public int getDexterity() {
        return dexterity;
    }

    /**
     * 器用さ（Dexterity）値を設定。
     *
     * @param dexterity 設定する器用さパラメータ（0以上）
     * @throws IllegalArgumentException dexterity に負の値が渡された場合
     */

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    /**
     * 装備による追加攻撃力を取得。
     *
     * @return attack このエンティティの追加攻撃力
     * @throws IllegalStateException attack が未初期化の場合
     */

    public int getAttack() {
        return attack;
    }

    /**
     * 装備による追加攻撃力を設定。
     *
     * @param attack 設定する追加攻撃力（0以上）
     * @return attack 更新後の追加攻撃力
     * @throws IllegalArgumentException attack に負の値が渡された場合
     */

    public int setAttack(int attack) {
        return this.attack = attack;
    }

    /**
     * 装備による追加防御力を取得。
     *
     * @return defense このエンティティの追加防御力
     * @throws IllegalStateException defense が未初期化の場合
     */

    public int getDefense() {
        return defense;
    }

    /**
     * 装備による追加防御力を設定。
     *
     * @param defense 設定する追加防御力（0以上）
     * @throws IllegalArgumentException defense に負の値が渡された場合
     */

    public void setDefense(int defense) {
        this.defense = defense;
    }

    /**
     * 現在の経験値を取得。
     *
     * @return exp このエンティティの経験値
     * @throws IllegalStateException exp が未初期化の場合
     */

    public int getExp() {
        return exp;
    }

    /**
     * 経験値を設定。
     *
     * @param exp 設定する経験値（0以上）
     * @throws IllegalArgumentException exp に負の値が渡された場合
     */

    public void setExp(int exp) {
        this.exp = exp;
    }

    /**
     * 次レベルまでに必要な経験値を取得。
     *
     * @return nextLevelExp 次のレベルまでに必要な経験値
     * @throws IllegalStateException nextLevelExp が未初期化の場合
     */

    public int getNextLevelExp() {
        return nextLevelExp;
    }

    /**
     * 次レベルまでに必要な経験値を設定。
     *
     * @param nextLevelExp 設定する必要経験値（0以上）
     * @throws IllegalArgumentException nextLevelExp に負の値が渡された場合
     */

    public void setNextLevelExp(int nextLevelExp) {
        this.nextLevelExp = nextLevelExp;
    }

    /**
     * 所持コイン数を取得。
     *
     * @return coin このエンティティの所持コイン数
     * @throws IllegalStateException coin が未初期化の場合
     */

    public int getCoin() {
        return coin;
    }

    /**
     * 所持コイン数を設定。
     *
     * @param coin 設定するコイン数（0以上）
     * @throws IllegalArgumentException coin に負の値が渡された場合
     */

    public void setCoin(int coin) {
        this.coin = coin;
    }

    /**
     * 現在装備している武器エンティティを取得。
     *
     * @return currentWeapon 装備中の武器エンティティ、未装備時は null
     */

    public Entity getCurrentWeapon() {
        return currentWeapon;
    }

    /**
     * 装備する武器エンティティを設定。
     *
     * @param currentWeapon 新たに装備する武器エンティティ（null許容）
     */

    public void setCurrentWeapon(Entity currentWeapon) {
        this.currentWeapon = currentWeapon;
    }

    /**
     * 現在装備している盾エンティティを取得。
     *
     * @return currentShield 装備中の盾エンティティ、未装備時は null
     */

    public Entity getCurrentShield() {
        return currentShield;
    }

    /**
     * 装備する盾エンティティを設定。
     *
     * @param currentShield 新たに装備する盾エンティティ（null許容）
     */

    public void setCurrentShield(Entity currentShield) {
        this.currentShield = currentShield;
    }

    /**
     * リスポーン中かどうかを返す。
     *
     * @return respawning true: リスポーン中, false: 通常状態
     */

    public boolean isRespawning() {
        return respawning;
    }

    /**
     * リスポーン中フラグを設定す。
     *
     * @param respawning true にセットするとリスポーン中と見なす
     */

    public void setRespawning(boolean respawning) {
        this.respawning = respawning;
    }

    /**
     * 説明文を取得。
     *
     * @return description このエンティティの説明テキスト
     * @throws IllegalStateException description が未設定または空文字列の場合
     */

    public String getDescription() {
        return description;
    }

    /**
     * 説明文を設定します。
     *
     * @param description 設定する説明テキスト（null または空文字列不可）
     * @throws IllegalArgumentException description が null または空文字列の場合
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 使用コストを設定。
     *
     * @param useCost 設定する使用コスト（0 以上）
     * @throws IllegalArgumentException useCost が負の値の場合
     */

    public void setUseCost(int useCost) {
        this.useCost = useCost;
    }

    /**
     * 発射するプロジェクタイルを取得。
     *
     * @return projectile このエンティティが保持する Projectile インスタンス
     * @throws IllegalStateException projectile が未設定の場合
     */

    public Projectile getProjectile() {
        return projectile;
    }

    /**
     * 発射するプロジェクタイルを設定。
     *
     * @param projectile 設定する Projectile インスタンス（null 不可）
     * @throws NullPointerException projectile が null の場合
     */

    public void setProjectile(Projectile projectile) {
        this.projectile = projectile;
    }

    /**
     * 発射可能カウンターを取得。
     *
     * @return shotAvailableCounter 発射可能になるまでの残りフレーム数
     * @throws IllegalStateException shotAvailableCounter が未初期化の場合
     */

    public int getShotAvailableCounter() {
        return shotAvailableCounter;
    }

    /**
     * 発射可能カウンターを設定。
     *
     * @param shotAvailableCounter 設定するカウンター値（0 以上）
     * @throws IllegalArgumentException shotAvailableCounter が負の値の場合
     */

    public void setShotAvailableCounter(int shotAvailableCounter) {
        this.shotAvailableCounter = shotAvailableCounter;
    }

    /**
     * 最大マナ量を取得。
     *
     * @return maxMana このエンティティの最大マナ値
     * @throws IllegalStateException maxMana が未設定または不正な場合
     */

    public int getMaxMana() {
        return maxMana;
    }

    /**
     * 最大マナ量を設定。
     *
     * @param maxMana 設定する最大マナ値（0 以上）
     * @throws IllegalArgumentException maxMana が負の値の場合
     */

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    /**
     * 現在のマナ量を取得。
     *
     * @return mana このエンティティの現在マナ値
     * @throws IllegalStateException mana が未設定または不正な場合
     */

    public int getMana() {
        return mana;
    }

    /**
     * マナ量を設定。
     *
     * @param mana 設定するマナ値（0 以上）
     * @throws IllegalArgumentException mana が負の値の場合
     */

    public void setMana(int mana) {
        this.mana = mana;
    }

    /**
     * 基本値を取得。
     *
     * @return value このエンティティの基本パラメータ値
     */

    public int getValue() {
        return value;
    }

    /**
     * 基本値を設定。
     *
     * @param value 設定する基本パラメータ値（0 以上）
     * @throws IllegalArgumentException value が負の値の場合
     */

    public void setValue(int value) {
        this.value = value;
    }

    /**
     * パーティクルの初期色を取得。
     *
     * @return Color このエンティティのパーティクル色
     * @throws IllegalStateException パーティクル色が未初期化の場合
     */

    public Color getParticleColor() {
        Color color = null;
        return color;
    }

    /**
     * パーティクルのサイズを取得。
     *
     * @return size パーティクルのピクセルサイズ
     * @throws IllegalStateException パーティクルサイズが未初期化の場合
     */

    public int getParticleSize() {
        int size = 0;
        return size;
    }

    /**
     * パーティクルの速度を取得。
     *
     * @return speed パーティクルのピクセル/フレーム速度
     * @throws IllegalStateException パーティクル速度が未初期化の場合
     */

    public int getParticleSpeed() {
        int speed = 0;
        return speed;
    }

    /**
     * パーティクルの最大ライフを取得。
     *
     * @return maxLife パーティクルの最大ライフ値
     * @throws IllegalStateException パーティクル最大ライフが未初期化の場合
     */

    public int getParticleMaxLife() {
        int maxLife = 0;
        return maxLife;
    }

    /**
     * このエンティティが存在するマップIDを取得。
     *
     * @return mapId 現在のマップID
     * @throws IllegalStateException mapId が未初期化の場合
     */

    public int getMapId() {
        return mapId;
    }

    /**
     * インベントリを返す。
     *
     * @return inventory 現在所持しているアイテム一覧
     */

    public ArrayList<Entity> getInventory() {
        System.out.println("getInventory: " + (inventory == null ? "null" : inventory.size() + " items"));
        return inventory;
    }

    public ArrayList<Entity> getShopItems() {
        return new ArrayList<>(); // デフォルトは空
    }

    public Entity copy() {
        return null;
    }

    /**
     * 価格を取得。
     *
     * @return price この商品の価格
     * @throws IllegalStateException price が負の値の場合
     */

    public int getPrice() {
        return price;
    }

    /**
     * 価格を設定。
     *
     * @param price 設定する価格（0以上）
     * @throws IllegalArgumentException price が負の値の場合
     */

    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * インベントリを置き換える。
     *
     * @param inventory 新しいアイテムリスト（非null）
     * @throws IllegalArgumentException inventory が null の場合
     */

    public void setInventory(ArrayList<Entity> inventory) {
        this.inventory = inventory;
    }

    /**
     * インベントリの最大サイズを取得。
     *
     * @return maxInventorySize 許可されるアイテム数の最大値
     */

    public int getMaxInventorySize() {
        return maxInventorySize;
    }

    /**
     * 指定した生成元エンティティとターゲットエンティティからパーティクルを複数生成し、
     * ゲームウィンドウのパーティクルリストに追加。
     *
     * @param generator パーティクルを生成するエンティティ (null不可)
     * @param target    パーティクルのターゲットとなるエンティティ (null不可)
     * @throws NullPointerException generator または target が null の場合
     */

    public void generateParticle(Entity generator, Entity target) {
        if (generator == null || target == null) {
            throw new NullPointerException("generator および target は null にできない");
        }

        Color color = generator.getParticleColor();
        int size = generator.getParticleSize();
        int speed = generator.getParticleSpeed();
        int maxLife = generator.getParticleMaxLife();

        for (int[] offset : PARTICLE_OFFSETS) {
            int dx = offset[0];
            int dy = offset[1];
            Particle p = new Particle(
                    gameWindow,
                    target,
                    color,
                    size,
                    speed,
                    maxLife,
                    dx, dy
            );
            gameWindow.getParticleList().add(p);
        }
    }

    /**
     * 花火のような動きをする FireworkParticle を指定数生成し、
     * ゲームウィンドウのパーティクルリストに追加。
     *
     * @param target パーティクルを表示する位置となるエンティティ (null不可)
     * @throws NullPointerException target が null の場合
     */

    public void spawnFireworkParticles(Entity target) {
        if (target == null) {
            throw new NullPointerException("target は null にできない");
        }

        int count = 30;
        int tileSize = FrameApp.getTileSize();
        int originX = target.getWorldX() + tileSize / 2;
        int originY = target.getWorldY() + tileSize / 2;
        int size = tileSize / 5;
        double gravity = 0.15;
        Color color = new Color(255, 140, 0);

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 2 + Math.random() * 3;
            FireworkParticle p = new FireworkParticle(
                    gameWindow, this,
                    originX, originY,
                    color, size,
                    maxLife,
                    angle,
                    speed,
                    gravity
            );
            gameWindow.getParticleList().add(p);
        }
    }

    /**
     * デフォルトの ImageResizer。渡されたソース画像を指定の幅・高さでアルファ付きでリサイズして返す。
     */

    private static final ImageResizer DEFAULT_RESIZER = (src, width, height) -> {
        if (src == null) {
            throw new NullPointerException("src は null にできない");
        }
        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dst.createGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2.drawImage(src, 0, 0, width, height, null);
        g2.dispose();
        return dst;
    };

    /**
     * クラスパスから指定した 3 枚の画像を読み込み、タイルサイズにリサイズして
     * this.image／this.image2／this.image3 に設定。
     *
     * @param path1    1 枚目の画像リソースパス（クラスパス）
     * @param path2    2 枚目の画像リソースパス（クラスパス）
     * @param path3    3 枚目の画像リソースパス（クラスパス）
     * @param tileSize リサイズ後の幅・高さ（ピクセル単位）
     * @throws IOException          いずれかの画像読み込み時に入出力例外が発生した場合
     * @throws NullPointerException path1, path2, path3 のいずれかが null の場合
     */

    protected void loadAnimationFrames(String path1,
                                       String path2,
                                       String path3,
                                       int tileSize) throws IOException {
        if (path1 == null || path2 == null || path3 == null) {
            throw new NullPointerException("path1, path2, path3 は null にできない");
        }

        BufferedImage raw1 = ImageIO.read(
                getClass().getClassLoader().getResourceAsStream(path1));
        BufferedImage raw2 = ImageIO.read(
                getClass().getClassLoader().getResourceAsStream(path2));
        BufferedImage raw3 = ImageIO.read(
                getClass().getClassLoader().getResourceAsStream(path3));

        this.image = DEFAULT_RESIZER.resize(raw1, tileSize, tileSize);
        this.image2 = DEFAULT_RESIZER.resize(raw2, tileSize, tileSize);
        this.image3 = DEFAULT_RESIZER.resize(raw3, tileSize, tileSize);

        this.width = tileSize;
        this.height = tileSize;
    }

    /**
     * 与えられた生画像をタイルサイズにリサイズし、this.image に設定。
     *
     * @param raw      リサイズ対象の BufferedImage（null 不可）
     * @param tileSize リサイズ後の幅・高さ（ピクセル単位）
     * @throws NullPointerException raw が null の場合
     */

    public void setImage(BufferedImage raw, int tileSize) {
        if (raw == null) {
            throw new NullPointerException("raw は null にできない");
        }
        this.image = DEFAULT_RESIZER.resize(raw, tileSize, tileSize);
        this.width = tileSize;
        this.height = tileSize;
    }

    /**
     * エンティティを画面上に描画します。視界内に存在する場合のみスプライト表示、
     * HPバー、無敵フラッシュ、死亡アニメーションを適用。
     *
     * @param g2 描画に使用する Graphics2D オブジェクト（null 不可）
     * @throws NullPointerException g2 が null の場合
     */

    public void draw(Graphics2D g2) {

        BufferedImage image = null;

        int dirIndex = Arrays.asList(DIRECTIONS).indexOf(getDirection());
        if (dirIndex != -1) {
            image = sprites[dirIndex][getSpriteNum() - 1];
        }

        // image が null なら即リターン
        if (image == null) {
            return;
        }

        // null チェック後に初めてサイズを取得
        int spriteW = image.getWidth();
        int spriteH = image.getHeight();

        int screenX = worldX - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = worldY - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        // 画面外判定
        int canvasW = gameWindow.getWidth();
        int canvasH = gameWindow.getHeight();
        if (screenX + spriteW < 0 || screenX > canvasW
                || screenY + spriteH < 0 || screenY > canvasH) {
            return;
        }

        if (getType() instanceof MonsterType && hpBarOn) {

            double oneScale = (double) FrameApp.getTileSize() / maxLife;
            double hpBarValue = oneScale * life;

            g2.setColor(new Color(35, 35, 35));
            g2.fillRect(screenX + 4, screenY - 21, FrameApp.getTileSize(), 12);

            g2.setColor(new Color(255, 0, 30));
            g2.fillRect(screenX + 5, screenY - 20, (int) hpBarValue, 10);

            hpBarCounter++;

            if (hpBarCounter > 600) {
                hpBarCounter = 0;
                hpBarOn = false;
            }
        }

        if (getInvincible()) {
            hpBarOn = true;
            hpBarCounter = 0;
            changeAlpha(g2, 0.4f);
        } else {
            changeAlpha(g2, 1F);
        }

        if (getDying()) {
            dyingAnimation(g2);
        }

        g2.drawImage(image, screenX, screenY, null);

        changeAlpha(g2, 1F);
    }

    public void setKnockBack(Entity target, Entity attacker, int knockBackPower) {

        this.attacker = attacker;
        target.knockBackDirection = attacker.direction;
        target.knockBackPower = knockBackPower;
        target.knockBack = true;

    }

    /**
     * 死亡中アニメーションを進行し、一定時間後にエンティティを非表示。
     *
     * @param g2 描画に使用する Graphics2D オブジェクト（null 不可）
     * @throws NullPointerException g2 が null の場合
     */

    public void dyingAnimation(Graphics2D g2) {
        dyingCounter++;

        if (dyingCounter <= SPRITE_DYING_COUNT) {
            changeAlpha(g2, 0f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 2) {
            changeAlpha(g2, 1f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 3) {
            changeAlpha(g2, 0f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 4) {
            changeAlpha(g2, 1f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 5) {
            changeAlpha(g2, 0f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 6) {
            changeAlpha(g2, 1f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 7) {
            changeAlpha(g2, 0f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 8) {
            changeAlpha(g2, 1f);
        } else {
            dying = false;
            alive = false;
        }
    }

    /**
     * 描画の透明度を設定。
     *
     * @param g2         描画に使用する Graphics2D オブジェクト（null 不可）
     * @param alphaValue 透明度(0.0f 〜 1.0f)
     * @throws NullPointerException     g2 が null の場合
     * @throws IllegalArgumentException alphaValue が 0.0 未満または 1.0 超過の場合
     */

    public void changeAlpha(Graphics2D g2, float alphaValue) {

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue));

    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getCount() {
        return count;
    }

    public int getDefaultSpeed() {
        return defaultSpeed;
    }

    public void setDefaultSpeed(int defaultSpeed) {
        this.defaultSpeed = defaultSpeed;
    }

    public int getKnockBackPower() {
        return knockBackPower;
    }

    public void setKnockBackPower(int knockBackPower) {
        this.knockBackPower = knockBackPower;
    }

    public boolean isInKnockBack() {
        return knockBack;
    }

    public void setInKnockBack(boolean knockBack) {
        this.knockBack = knockBack;
    }

    public int getKnockBackCounter() {
        return knockBackCounter;
    }

    public void setKnockBackCounter(int knockBackCounter) {
        this.knockBackCounter = knockBackCounter;
    }

    public String getKnockBackDirection() {
        return knockBackDirection;
    }

    public void setKnockBackDirection(String knockBackDirection) {
        this.knockBackDirection = knockBackDirection;
    }

    /**
     * エンティティのヒットボックスを表す Rectangle2D を生成して返却。
     * worldX, worldY に対してヒットボックスのオフセットとサイズを適用する。
     */

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(
                worldX + hitBoxX,
                worldY + hitBoxY,
                hitBoxWidth,
                hitBoxHeight
        );
    }

    public void setHitBoxX(int hitBoxX) {
        this.hitBoxX = hitBoxX;
    }

    public void setHitBoxY(int hitBoxY) {
        this.hitBoxY = hitBoxY;
    }

    public void setHitBoxWidth(double hitBoxWidth) {
        this.hitBoxWidth = hitBoxWidth;
    }

    public void setHitBoxHeight(double hitBoxHeight) {
        this.hitBoxHeight = hitBoxHeight;
    }
}