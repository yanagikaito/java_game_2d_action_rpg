package monster;

import entity.Entity;
import entity.type.CrabGuardianType;
import frame.FrameApp;
import game.GameState;
import pathfinding_editor.Node;
import pathfinding_editor.PathFinder;
import player.Player;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class MonCrabGuardian extends Entity {

    private PathFinder pathfinder;
    private Node[][] grid;
    private int goalX, goalY;
    private boolean flowFieldInitialized = false;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final int SPRITE_COUNT = 30;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private int actionLockCounter = 0;
    private int animFrame = 0;
    // 1コマあたりの経過フレーム数
    private int animTick = 0;
    private static final int ANIM_SPEED = 2;
    private String activeDirection = "down";
    private String queuedDirection = null;

    public MonCrabGuardian(GameWindow gameWindow) {

        super(gameWindow);
        setType(new CrabGuardianType());
        setName("Crab Guardian");
        activeDirection = "down";
        setDirection(activeDirection);
        setSpriteNum(1);
        setDefaultSpeed(1);
        setSpeed(getDefaultSpeed());
        setMaxLife(100);
        setLife(getMaxLife());
        setAttack(10);
        setDefense(2);
        setExp(15);

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
    }

    public void loadMonsterImages() {
        int s = 5;
        int tileSize = FrameApp.getTileSize();
        int drawSize = tileSize * s;

        sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];

        for (int dir = 0; dir < DIRECTIONS.length; dir++) {
            for (int i = 0; i < SPRITE_COUNT; i++) {
                String path = "monster/crabGuardian-" + DIRECTIONS[dir] + "-" + (i + 1) + ".gif";
                try (java.io.InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
                    if (input == null) {
                        System.err.println("Image not found: " + path);
                        continue;
                    }
                    BufferedImage original = ImageIO.read(input);
                    if (original != null) {
                        sprites[dir][i] = createImage(original, drawSize, drawSize);
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

        if (getGameWindow().getGameState() == GameState.BOSS_CUTSCENE) {
            return;
        }

        updateMonsterInvincibility();

        // プレイヤーとの距離を計算
        Player player = getGameWindow().getPlayer();

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
        String desiredDirection = switch (dir) {
            case 0 -> "up";
            case 1 -> "right";
            case 2 -> "down";
            case 3 -> "left";
            default -> activeDirection;
        };

        // まだ現在の向きの30枚が終わっていない場合は、希望の向きを予約しておく
        if (animFrame < SPRITE_COUNT - 1) {
            queuedDirection = desiredDirection;
            setDirection(activeDirection);
        } else {
            // 30枚目まで到達したら次の方向へ切り替え
            if (queuedDirection != null) {
                activeDirection = queuedDirection;
                queuedDirection = null;
            } else {
                activeDirection = desiredDirection;
            }
            animFrame = 0;
            animTick = 0;
            setDirection(activeDirection);
        }
        setSpriteNum(animFrame + 1);
    }

    private void initializeFlowField() {

        int mapWidth = FrameApp.getMaxWorldRow();
        System.out.println(mapWidth);
        int mapHeight = FrameApp.getMaxWorldCol();
        System.out.println(mapHeight);

        // グリッドの作成
        grid = new Node[mapWidth][mapHeight];
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                grid[x][y] = new Node(x, y);
                // 壁の位置を設定（必要に応じて）
                grid[x][y].walkable = !getGameWindow().getTileManager().isObstacle(x, y);
            }
        }

        // パスファインダーの初期化
        pathfinder = new PathFinder(grid);
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
//            System.out.println("getInvincibleCounter() :" + getInvincibleCounter());
            if (getInvincibleCounter() > 40) {
//                System.out.println("getInvincibleCounter() :" + getInvincibleCounter());
                setInvincible(false);
//                System.out.println("getInvincible() :" + getInvincible());
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

        int s = 5;
        int tileSize = FrameApp.getTileSize();
        int drawSize = tileSize * s;

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

        int walkDirIndex = Arrays.asList(DIRECTIONS).indexOf(activeDirection);
        if (walkDirIndex < 0) {
            walkDirIndex = 1;
        }

        int safeFrame = Math.max(0, Math.min(animFrame, SPRITE_COUNT - 1));
        BufferedImage img = (sprites != null && sprites[walkDirIndex] != null)
                ? sprites[walkDirIndex][safeFrame]
                : null;

        if (img != null) {
            g2.drawImage(img, screenX, screenY, drawSize, drawSize, null);
        }

        // 描画ごとにコマを進める（30枚到達時は次の setAction() タイミングで切替）
        if (getGameWindow().getGameState() != GameState.BOSS_CUTSCENE) {
            animTick++;
            if (animTick >= ANIM_SPEED) {
                animTick = 0;
                if (animFrame < SPRITE_COUNT - 1) {
                    animFrame++;
                }
            }
        }

        drawHpBarIfNeeded(g2, screenX, screenY);

        // デバッグ：攻撃エリアを矩形で描画
//        Rectangle attackBox = new Rectangle(screenX, screenY, getAttackArea().width, getAttackArea().height);
//        g2.setColor(new Color(255, 0, 0, 255));
//        g2.drawRect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);


        g2.setComposite(original);
    }
}