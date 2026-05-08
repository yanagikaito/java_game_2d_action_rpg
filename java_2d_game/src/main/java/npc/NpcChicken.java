package npc;


import collision.CollisionChecker;
import entity.Entity;
import entity.MonsterType;
import entity.NpcType;
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
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
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

    private int hitCount = 0;
    private static final int HELP_TRIGGER_THRESHOLD = 30; // 30回で発動
    private int life = 30;
    private boolean hasTriggeredHelp = false; // 同一個体からの重複防止
    private static final long TRIGGER_COOLDOWN_MS = 30_000L; // 個体ごとの再発動クールダウン
    private long lastTriggerTime = 0L;
    private boolean following = false;
    // プレイヤー追跡用フラグ（ルート追従の following と分離）
    private boolean playerFollowing = false;


    public NpcChicken(GameWindow gameWindow) {
        super(gameWindow);
        this.collisionChecker = new CollisionChecker(gameWindow);
        setDirection("down");
        setType(new MonsterType());
        setMaxLife(life);
        setLife(getMaxLife());
        setSpeed(1);
        getSolidArea().x = 1;
        getSolidArea().y = 1;
        getSolidArea().width = 46;
        getSolidArea().height = 46;
        setSolidAreaDefaultX(getSolidArea().x);
        setSolidAreaDefaultY(getSolidArea().y);
        loadNPCImages();
    }

    public void loadNPCImages() {

        int s = 2;
        setSprites(sprites);
        try {
            int tileSize = FrameApp.getTileSize();
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream("npc/chicken-" + DIRECTIONS[dir] + "-" + (i + 1) + ".gif"));
                    BufferedImage processed = createImage(original, tileSize * s, tileSize * s);
                    sprites[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAction() {

        if (getInvincible()) {
            invincibleCounter--;
            if (invincibleCounter <= 0) setInvincible(false);
        }

        if (playerFollowing) {
            // プレイヤー追跡優先
            callForHelp();
            checkPlayerCollision();
        } else if (following) {
            // 既存のルート追従（following は route 用）
            followPlayerStep();
            checkPlayerCollision();
        } else {
            randomWalkStep();
        }
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
        boolean contactPlayer = collisionChecker.checkPlayer(this);
        if (contactPlayer) {
            onHitPlayer();
        }
    }

    private void onHitPlayer() {
        this.following = false;
        getGameWindow().getSoundmanager().damageWAV("sound/damage-sound.wav");
    }

    public void setFollowing(boolean f) {
        this.following = f;
        if (f) {
            setSpeed(2); // 追跡時に速くしたければ調整
        } else {
            setSpeed(1); // 元に戻す
        }
    }

    public void setPlayerFollowing(boolean f) {
        this.playerFollowing = f;
        if (f) {
            setSpeed(2); // 追跡時に速くする（調整可）
        } else {
            setSpeed(1); // 元に戻す
        }
    }

    private void callForHelp() {
        GameMap map = getGameWindow().getCurrentMap();
        int current = map.countChickens();
        int allowed = Math.max(0, GameMap.GLOBAL_MAX_CHICKENS - current);
        int toSpawn = Math.min(GameMap.SWARM_SPAWN_COUNT, allowed);
        if (toSpawn <= 0) return;

        int tileSize = FrameApp.getTileSize();

        // カメラが無ければプレイヤー基準で逆算
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
            map.addNpc(baby);
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

        // ノックバック（簡易）
        int kbX = 0, kbY = 0;
        switch (getDirection()) {
            case "up" -> kbY = -knockBackPower;
            case "down" -> kbY = knockBackPower;
            case "left" -> kbX = -knockBackPower;
            case "right" -> kbX = knockBackPower;
        }
        // 衝突を考慮するなら CollisionChecker を使って移動可否を確認する
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
            hasTriggeredHelp = true;
            lastTriggerTime = now;
            callForHelp();
        }
    }

    private BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }
}