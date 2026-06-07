package monster;

import entity.type.BossMonsterType;
import entity.Entity;
import frame.FrameApp;
import player.Player;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MonGreenGoblin extends Entity {

    private core.PathFinder pathfinder;
    private core.Node[][] grid;
    private int goalX, goalY;
    private boolean flowFieldInitialized = false;
    private static final int ACTION_LOCK_THRESHOLD = 180;
    private static final int MAX_RANDOM_VALUE = 100;
    private static final int THRESHOLD_UP = 25;
    private static final int THRESHOLD_DOWN = 50;
    private static final int THRESHOLD_LEFT = 75;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final String[] ATTACK_DIRECTIONS = {"attackUp", "attackDown", "attackLeft", "attackRight"};
    private BufferedImage[][] attackSprites = new BufferedImage[ATTACK_DIRECTIONS.length][SPRITE_COUNT];
    private static final int SPRITE_COUNT = 3;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private Random random = new Random();
    private int actionLockCounter = 0;
    private static final int ROCK_COOLDOWN_FRAMES = 180;
    private int shotAvailableCounter = ROCK_COOLDOWN_FRAMES;
    private static final int SPRITE_ANIMATION_THRESHOLD = 10;

    public MonGreenGoblin(GameWindow gameWindow) {

        super(gameWindow);
        setType(new BossMonsterType());
        setName("Green Goblin");
        setDefaultSpeed(1);
        setSpeed(getDefaultSpeed());
        setMaxLife(50);
        setLife(getMaxLife());
        setAttack(10);
        setDefense(0);
        setExp(10);

        int tileSize = FrameApp.getTileSize() * 5;
        getSolidArea().x = 1;
        getSolidArea().y = 1;
        getSolidArea().width = tileSize - 48 * 2;
        getSolidArea().height = tileSize - 48 * 2;
        setSolidAreaDefaultX(getSolidArea().x);
        setSolidAreaDefaultY(getSolidArea().y);
        setAttackArea(new Rectangle());
        getAttackArea().width = tileSize;
        getAttackArea().height = tileSize;
        loadMonsterImages();
        loadMonsterAttackSprites();
    }

    public void loadMonsterImages() {

        int s = 5;

        try {
            int tileSize = FrameApp.getTileSize();
            setSprites(new BufferedImage[DIRECTIONS.length][SPRITE_COUNT]);
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream("monster/greenGoblin-" + DIRECTIONS[dir] + "-" + (i + 1) + ".gif"));
                    BufferedImage processed = createImage(original, tileSize * s, tileSize * s);
                    getSprites()[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMonsterAttackSprites() {

        int tileSize = FrameApp.getTileSize();
        int s = 5;

        for (int d = 0; d < ATTACK_DIRECTIONS.length; d++) {
            for (int i = 0; i < SPRITE_COUNT; i++) {
                String path = "monster/greenGoblin-" + ATTACK_DIRECTIONS[d] + "-" + (i + 1) + ".gif";
                try {
                    BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
                    if (d == 0 || d == 1) {
                        attackSprites[d][i] = createImage(img, tileSize, tileSize * s * 2);
                    } else {
                        attackSprites[d][i] = createImage(img, tileSize * s * 2, tileSize);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    @Override
    public void setAction() {

        updateMonsterInvincibility();

        // プレイヤーとの距離を計算
        Player player = getGameWindow().getPlayer();
        int dx = player.getWorldX() - getWorldX();
        int dy = player.getWorldY() - getWorldY();
        int visionRange = FrameApp.getTileSize() * 5;

        if (Math.abs(dx) < visionRange && Math.abs(dy) < visionRange) {
            // 攻撃モードに移行
            setAttacking(true);
            setAttackDirection("attackRight");

            // 攻撃方向を決定
            if (Math.abs(dx) > Math.abs(dy)) {
                setAttackDirection(dx > 0 ? "attackRight" : "attackLeft");
            } else {
                setAttackDirection(dy > 0 ? "attackDown" : "attackUp");
            }

            return;
        }

        // ここからは通常の移動処理（プレイヤーを追う）
        if (!flowFieldInitialized) {
            initializeFlowField();
            flowFieldInitialized = true;
        }

        goalX = player.getWorldX() / FrameApp.getTileSize();
        goalY = player.getWorldY() / FrameApp.getTileSize();
        pathfinder.calculateIntegrationField(goalX, goalY);

        int currentX = getWorldX() / FrameApp.getTileSize();
        int currentY = getWorldY() / FrameApp.getTileSize();
        currentX = Math.max(0, Math.min(currentX, pathfinder.getWidth() - 1));
        currentY = Math.max(0, Math.min(currentY, pathfinder.getHeight() - 1));

        int dir = pathfinder.getMoveDirection(currentX, currentY);

        switch (dir) {
            case 0 -> setDirection("up");
            case 1 -> setDirection("right");
            case 2 -> setDirection("down");
            case 3 -> setDirection("left");
        }
    }

    private void initializeFlowField() {

        int mapWidth = FrameApp.getMaxWorldRow();
        System.out.println(mapWidth);
        int mapHeight = FrameApp.getMaxWorldCol();
        System.out.println(mapHeight);

        // グリッドの作成
        grid = new core.Node[mapWidth][mapHeight];
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                grid[x][y] = new core.Node(x, y);
                // 壁の位置を設定（必要に応じて）
                grid[x][y].walkable = !getGameWindow().getTileManager().isObstacle(x, y);
            }
        }

        // パスファインダーの初期化
        pathfinder = new core.PathFinder(grid);
    }

    public void damageReaction() {

        actionLockCounter = 0;
        setDirection(getGameWindow().getPlayer().getDirection());
        setHpBarOn(true);
        setHpBarCounter(0);
    }

    private void updateMonsterInvincibility() {
        if (getInvincible()) {
            setInvincibleCounter(getInvincibleCounter() + 1);
            System.out.println("getInvincibleCounter() :" + getInvincibleCounter());
            if (getInvincibleCounter() > 40) {
                System.out.println("getInvincibleCounter() :" + getInvincibleCounter());
                setInvincible(false);
                System.out.println("getInvincible() :" + getInvincible());
                setInvincibleCounter(0);
            }
        }
    }

    /**
     * エンティティを画面上に描画します。視界内に存在する場合のみスプライト表示、
     * HPバー、無敵フラッシュ、死亡アニメーションを適用。
     *
     * @param g2 描画に使用する Graphics2D オブジェクト（null 不可）
     * @throws NullPointerException g2 が null の場合
     */

    @Override
    public void draw(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        int s = 5;

        int screenX = getWorldX()
                - getGameWindow().getPlayer().getWorldX()
                + getGameWindow().getPlayer().getScreenX();

        int screenY = getWorldY()
                - getGameWindow().getPlayer().getWorldY()
                + getGameWindow().getPlayer().getScreenY();

        Composite original = g2.getComposite();
        if (getInvincible()) {
            g2.setComposite(
                    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
            );
        }

        BufferedImage img;
        if (!getAttacking()) {
            int walkDirIndex = Arrays.asList(DIRECTIONS)
                    .indexOf(getDirection());
            if (walkDirIndex >= 0) {
                img = sprites[walkDirIndex][getSpriteNum() - 1];
                g2.drawImage(img, screenX, screenY, tileSize, tileSize, null);
            }
        } else {
            String[] animationKeys = ATTACK_DIRECTIONS;
            BufferedImage[][] spriteSet = attackSprites;

            int directionIndex = Arrays.asList(animationKeys)
                    .indexOf(getAttackDirection());
            if (directionIndex >= 0) {
                int frameIndex = Math.max(0,
                        Math.min(getSpriteNum() - 1, SPRITE_COUNT - 1)
                );
                img = spriteSet[directionIndex][frameIndex];

                int drawWidth = (directionIndex == 2 || directionIndex == 3)
                        ? tileSize * s * 2 : tileSize * s;
                int drawHeight = (directionIndex == 0 || directionIndex == 1)
                        ? tileSize * s * 2 : tileSize * s;
                int drawX = animationKeys[directionIndex]
                        .endsWith("Left") ? screenX - tileSize * s : screenX;
                int drawY = animationKeys[directionIndex]
                        .endsWith("Up") ? screenY - tileSize * s : screenY;

                g2.drawImage(img, drawX, drawY, drawWidth, drawHeight, null);

                drawHpBarIfNeeded(g2, screenX, screenY);

//                // デバッグ：攻撃エリアを矩形で描画
//                Rectangle attackBox = new Rectangle(drawX, drawY, getAttackArea().width, getAttackArea().height);
//                g2.setColor(new Color(255, 0, 0, 255));
//                g2.drawRect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
            }
        }

        g2.setComposite(original);
    }
}