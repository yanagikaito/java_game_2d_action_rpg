package npc;

import collision.CollisionChecker;
import entity.Entity;
import entity.type.ChickenType;
import frame.FrameApp;
import map.GameMap;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class NpcChicken extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private BufferedImage[][] spritesChicken = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private static final int SPRITE_COUNT = 3;
    private static final int ACTION_LOCK_THRESHOLD = 120;
    private static final int MAX_RANDOM_VALUE = 100;
    private static final int THRESHOLD_UP = 25;
    private static final int THRESHOLD_DOWN = 50;
    private static final int THRESHOLD_LEFT = 75;
    private int invincibleCounter = 0;
    private Random random = new Random();
    private int actionLockCounter = 0;
    private final CollisionChecker collisionChecker;

    private static final long PICKABLE_DELAY_MS = 500L;
    private long landedAt = 0L;

    private int hitCount = 0;
    private static final int HELP_TRIGGER_THRESHOLD = 30;
    private int life = 30;
    private boolean hasTriggeredHelp = false;
    private static final long TRIGGER_COOLDOWN_MS = 30_000L;
    private long lastTriggerTime = 0L;
    private boolean following = false;
    private boolean playerFollowing = false;

    private boolean inCoop = false;
    private int prevTileX = -1;
    private int prevTileY = -1;
    private GameMap gameMap;

    // 1体あたりのダメージ
    private int attackDamage = 2;
    // 1体が連続で与える最短間隔（ms）
    private long attackCooldownMs = 800L;
    // 最後に攻撃した時刻
    private long lastAttackAt = 0L;

    // 投げられたときの物理挙動用
    private boolean hasShadow = false;
    private double vx = 0;
    private double vy = 0;
    // 高さ方向速度（上向き正）
    private double vz = 0.0;
    // 地面からの高さ（ピクセル）
    private double z = 0.0;
    // 毎フレーム vy に加算する重力
    private double gravity = 0.3;
    // 垂直重力（正、調整可）
    private double verticalGravity = 0.45;
    // 投げられて飛んでいる状態
    private boolean thrown = false;
    private Entity user = null;
    // 置かれていて拾える状態
    private boolean pickable = false;

    // 地面に着地しているか
    private boolean landed = false;

    // 割れ管理
    private boolean shattering = false;
    private int shatterFrame = 0;
    private boolean damageGiven = false;


    public NpcChicken(GameWindow gameWindow) {
        super(gameWindow);
        this.collisionChecker = new CollisionChecker(gameWindow);
        setDirection("down");
        setType(new ChickenType());
        setMaxLife(life);
        setLife(getMaxLife());
        setSpeed(1);
        setCollision(true);
        getSolidArea().x = 1;
        getSolidArea().y = 1;
        getSolidArea().width = FrameApp.getTileSize() * 2;
        getSolidArea().height = FrameApp.getTileSize() * 2;
        setSolidAreaDefaultX(getSolidArea().x);
        setSolidAreaDefaultY(getSolidArea().y);

        try {
            this.gameMap = gameWindow.getCurrentMap();
        } catch (Exception e) {
            this.gameMap = null;
        }

        loadNPCImages();
    }

    public void loadNPCImages() {

        int s = 2;
        setSprites(spritesChicken);
        try {
            int tileSize = FrameApp.getTileSize();
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream("npc/chicken-" + DIRECTIONS[dir] + "-" + (i + 1) + ".gif"));
                    BufferedImage processed = createImage(original, tileSize * s, tileSize * s);
                    spritesChicken[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAction() {

        prevTileX = worldToTile(getWorldX());
        prevTileY = worldToTile(getWorldY());

        // --- 着地後の復元処理 ---
        if (landed && !pickable) {
            long now = System.currentTimeMillis();
            if (now - landedAt >= PICKABLE_DELAY_MS) {
                pickable = true;
                landed = false;
                this.setCollision(true);
                if (this.getSolidArea() != null) {
                    this.getSolidArea().x = 1;
                    this.getSolidArea().y = 1;
                    this.getSolidArea().width = 48;
                    this.getSolidArea().height = 48;
                }
                System.out.println("[CHICKEN] pickable now true at world=(" + getWorldX() + "," + getWorldY() + ")");
            }
        }

        if (getInvincible()) {
            invincibleCounter--;
            if (invincibleCounter <= 0) setInvincible(false);
        }

        // 追尾フラグが立っていれば常にプレイヤー追尾
        if (playerFollowing || following) {
            followPlayerStep();
            checkPlayerCollision();
            return;
        }

        // --- 投げられている状態（空中） ---
        if (thrown) {
            // 空中では move() を使わず world 座標を直接更新する
            setWorldX(getWorldX() + (int) Math.round(vx));
            setWorldY(getWorldY() + (int) Math.round(vy));

            vy += gravity;

            z += vz;
            vz -= verticalGravity;

//            getGameWindow().getUi().addMessage("z = " + z);
//            getGameWindow().getUi().addMessage("vz = " + vz);
//            getGameWindow().getUi().addMessage("worldY = " + getWorldY());

            if (z <= 0) {
                z = 0;
                vz = 0;

                boolean tileCollision = getGameWindow().getCollisionChecker().checkTile(this);

                if (tileCollision) {
                    landed = true;
                    thrown = false;
                    pickable = false;
                    hasShadow = false;

                    // 判定が済んだら当たり判定を縮める
                    this.setCollision(false);
                    if (this.getSolidArea() != null) {
                        this.getSolidArea().width = 0;
                        this.getSolidArea().height = 0;
                    }
                } else {
                    thrown = false;
                    pickable = true;
                    hasShadow = false;
                }
            }
            return;
        } else {
            // 地面に置かれている状態
            if (!pickable && !landed) {
                randomWalkStep();
                return;
            }
        }

        // それ以外は従来のランダム歩行
        randomWalkStep();
    }

    private void randomWalkStep() {
        actionLockCounter++;
        if (actionLockCounter < ACTION_LOCK_THRESHOLD) return;

        int i = random.nextInt(MAX_RANDOM_VALUE) + 1;
        if (i <= THRESHOLD_UP) setDirection("up");
        else if (i <= THRESHOLD_DOWN) setDirection("down");
        else if (i <= THRESHOLD_LEFT) setDirection("left");
        else setDirection("right");

        actionLockCounter = 0;
    }

    /**
     * 移動後に必ず呼ぶ衝突判定
     */

    private void checkPlayerCollision() {
        if (getGameWindow().getCollisionChecker().checkPlayer(this)) {
            long now = System.currentTimeMillis();
            if (now - lastAttackAt >= attackCooldownMs) {
                lastAttackAt = now;
                try {
                    getGameWindow().getPlayer().takeDamage(attackDamage);
                    onHitPlayer();
                } catch (Exception e) {
                    System.out.println("[CHICKEN] failed to damage player: " + e);
                }
            }
        }
    }

    private void onHitPlayer() {
        this.following = false;
        getGameWindow().getSoundmanager().damageWAV("sound/damage-sound.wav");
    }

    public void setFollowing(boolean f) {
        this.following = f;
        if (f) {
            setSpeed(2);
        } else {
            setSpeed(1);
        }
    }

    public void setPlayerFollowing(boolean f) {
        this.playerFollowing = f;
        if (f) {
            setSpeed(2);
        } else {
            setSpeed(1);
        }
    }

    private void callForHelp() {

        GameMap map = getGameWindow().getCurrentMap();
        int current = map.countChickens();
        int allowed = Math.max(0, GameMap.GLOBAL_MAX_CHICKENS - current);
        int toSpawn = Math.min(GameMap.SWARM_SPAWN_COUNT, allowed);
        if (toSpawn <= 0) return;

        int tileSize = FrameApp.getTileSize();

        // プレイヤー基準で逆算
        int cameraWorldX = getGameWindow().getPlayer().getWorldX() - getGameWindow().getPlayer().getScreenX();

        int spawnX = cameraWorldX + FrameApp.getScreenWidth() + tileSize * 2;
        int playerY = getGameWindow().getPlayer().getWorldY();

        for (int i = 0; i < toSpawn; i++) {
            NpcChicken baby = new NpcChicken(getGameWindow());
            int offsetY = (random.nextInt(7) - 3) * tileSize;
            int spawnY = playerY + offsetY;
            int spawnPosX = spawnX + i * (tileSize / 2);

            if (!map.canPlaceNpcAt(spawnPosX, spawnY, baby.getWidth(), baby.getHeight())) {
                continue;
            }

            baby.setWorldX(spawnPosX);
            baby.setWorldY(spawnY);
            baby.setFollowing(true);
            baby.setDirection("left");
            map.addMonster(baby);
        }
    }

    private void followPlayerStep() {
        int px = getGameWindow().getPlayer().getWorldX();
        int py = getGameWindow().getPlayer().getWorldY();
        int npcX = getWorldX();
        int npcY = getWorldY();

        int dx = px - npcX;
        int dy = py - npcY;
        int speed = getSpeed();

        // X/Y のステップを speed に制限して移動
        int stepX = 0;
        int stepY = 0;
        if (Math.abs(dx) > 0) stepX = Math.min(Math.abs(dx), speed) * (dx > 0 ? 1 : -1);
        if (Math.abs(dy) > 0) stepY = Math.min(Math.abs(dy), speed) * (dy > 0 ? 1 : -1);

        // 衝突判定を使うならここでチェックしてから setWorldX/Y する
        setWorldX(npcX + stepX);
        setWorldY(npcY + stepY);

        // 向き更新
        if (Math.abs(dx) >= Math.abs(dy)) {
            setDirection(dx > 0 ? "right" : "left");
        } else {
            setDirection(dy > 0 ? "down" : "up");
        }
    }

    /**
     * プレイヤーなどからのダメージ処理。
     * dmg: 与ダメージ、kbX/kbY: ノックバック量（ワールド座標単位）
     */

    public void takeDamage(int dmg, int knockBackPower) {

        if (this.getInvincible()) return;

        try {
            getGameWindow().getSoundmanager().damageWAV("sound/hit.wav");
        } catch (Exception ignored) {
        }

        // ノックバック
        int kbX = 0, kbY = 0;
        switch (getDirection()) {
            case "up" -> kbY = -knockBackPower;
            case "down" -> kbY = knockBackPower;
            case "left" -> kbX = -knockBackPower;
            case "right" -> kbX = knockBackPower;
        }
        // CollisionChecker を使って移動可否を確認する
        setWorldX(getWorldX() + kbX);
        setWorldY(getWorldY() + kbY);

        // HP を減らすが 1 未満にはしない（倒せない）
        int newLife = Math.max(1, getLife() - dmg);
        setLife(newLife);

        // 無敵フレームを付与するなら
        this.setInvincible(true);
        this.invincibleCounter = 30;

        // 被攻撃回数カウント
        hitCount++;
        long now = System.currentTimeMillis();
        if (!hasTriggeredHelp && hitCount >= HELP_TRIGGER_THRESHOLD && (now - lastTriggerTime) >= TRIGGER_COOLDOWN_MS) {
            hitCount = 0;
            hasTriggeredHelp = true;
            lastTriggerTime = now;
            callForHelp();
        }
    }

    public void resetState() {
        this.hitCount = 0;
        this.hasTriggeredHelp = false;
        this.lastTriggerTime = 0L;
        this.setInvincible(false);
        this.playerFollowing = false;
        this.following = false;
    }

    private BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    /**
     * 所持中に Player が頭上に描画するためのスプライトを返す。
     * デフォルトは Down 方向の最初のフレームを使い、タイルサイズに合わせてリサイズする。
     */

    public BufferedImage getHeldSprite() {
        int ts = FrameApp.getTileSize();
        int downIndex = 1;
        int frameIndex = 0; // 所持時は最初のフレームを使う
        if (spritesChicken != null && spritesChicken.length > downIndex && spritesChicken[downIndex].length > frameIndex) {
            BufferedImage src = spritesChicken[downIndex][frameIndex];
            // 小さめにリサイズして返す
            int w = ts * 3 / 2;
            int h = ts * 3 / 2;
            BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = buf.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, w, h, null);
            g.dispose();
            return buf;
        }
        return null;
    }

    // 投げるときの向き・フレーム別スプライトを返す（存在しなければ null を返す）
    public BufferedImage getThrowSprite(String dir, int frame) {
        // ここでは sprites の向きインデックスとフレームをそのまま使う例
        int dirIndex = 1; // default Down
        switch (dir) {
            case "up" -> dirIndex = 0;
            case "down" -> dirIndex = 1;
            case "left" -> dirIndex = 2;
            case "right" -> dirIndex = 3;
        }
        int frameIndex = Math.max(0, Math.min(frame, spritesChicken[dirIndex].length - 1));
        return spritesChicken[dirIndex][frameIndex];
    }

    public void onPickedUp() {
        setPickable(false);
        setAlive(false);
        setThrown(false);
        setLife(getMaxLife());
    }

    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
        this.thrown = true;
        setAlive(true);
    }

    private int worldToTile(int worldCoord) {
        int tileSize = FrameApp.getTileSize();
        return Math.floorDiv(worldCoord, tileSize);
    }

    public boolean isHasShadow() {
        return hasShadow;
    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public double getVz() {
        return vz;
    }

    public void setVz(double vz) {
        this.vz = vz;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public double getVerticalGravity() {
        return verticalGravity;
    }

    public void setVerticalGravity(double verticalGravity) {
        this.verticalGravity = verticalGravity;
    }

    public void setVerticalVelocity(double vz) {
        this.vz = Math.max(-40.0, Math.min(40.0, vz));
    }

    @Override
    public boolean isThrown() {
        return thrown;
    }

    @Override
    public void setThrown(boolean thrown) {
        this.thrown = thrown;
    }

    public Entity getUser() {
        return user;
    }

    public void setUser(Entity user) {
        this.user = user;
    }

    public boolean isPickable() {
        return pickable;
    }

    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }

    public boolean isLanded() {
        return landed;
    }

    public void setLanded(boolean landed) {
        this.landed = landed;
    }

    public boolean isShattering() {
        return shattering;
    }

    public void setShattering(boolean shattering) {
        this.shattering = shattering;
    }

    public int getShatterFrame() {
        return shatterFrame;
    }

    public void setShatterFrame(int shatterFrame) {
        this.shatterFrame = shatterFrame;
    }

    public boolean isDamageGiven() {
        return damageGiven;
    }

    public void setDamageGiven(boolean damageGiven) {
        this.damageGiven = damageGiven;
    }

    public boolean isInCoop() {
        return inCoop;
    }

    public void setInCoop(boolean v) {
        inCoop = v;
    }

    public int getPrevTileX() {
        return prevTileX;
    }

    public int getPrevTileY() {
        return prevTileY;
    }
}