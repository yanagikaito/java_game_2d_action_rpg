package monster;

import entity.Entity;
import entity.MonsterType;
import frame.FrameApp;
import object.ObjFireball;
import object.ObjStone;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

public class MonMintSoldier extends Entity {

    private core.PathFinder pathfinder;
    private core.Node[][] grid;
    private int goalX, goalY;
    private boolean flowFieldInitialized = false;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final int SPRITE_COUNT = 3;
    private Random random = new Random();
    private int actionLockCounter = 0;
    private static final int ROCK_COOLDOWN_FRAMES = 180;
    private int shotAvailableCounter = ROCK_COOLDOWN_FRAMES;
    private static final int NEW_HAIR_COLOR = 0xFF00FF00;

    public MonMintSoldier(GameWindow gameWindow) {

        super(gameWindow);
        setType(new MonsterType());
        setName("Mint Soldier");
        setDefaultSpeed(1);
        setSpeed(getDefaultSpeed());
        setMaxLife(50);
        setLife(getMaxLife());
        setAttack(5);
        setDefense(0);
        setExp(5);
        setProjectile(new ObjStone(gameWindow));
        getSolidArea().x = 1;
        getSolidArea().y = 1;
        getSolidArea().width = 46;
        getSolidArea().height = 46;
        setSolidAreaDefaultX(getSolidArea().x);
        setSolidAreaDefaultY(getSolidArea().y);
        loadMonsterImages();
    }

    // 完全一致リスト（必要に応じて色を追加）
    private static final Set<Integer> HAIR_COLORS = Set.of(
            0xFFF3D131, 0xFFCCAA0A,
            0xFFEEBE93, 0xFF534721,
            0xFF997F03
    );

    public void loadMonsterImages() {

        try {
            int tileSize = FrameApp.getTileSize();
            setSprites(new BufferedImage[DIRECTIONS.length][SPRITE_COUNT]);
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream(
                                            "player/image-"
                                                    + DIRECTIONS[dir]
                                                    + "-" + (i + 1) + ".gif"));

                    // 髪ピクセルを全走査してログ出力
                    logHairPositions(original, DIRECTIONS[dir], i + 1);

                    BufferedImage processed = createImage(original, tileSize, tileSize);
                    getSprites()[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 画像中の髪色ピクセルを全走査し、見つかった座標と色をログ出力する
     *
     * @param img   元画像(リサイズ前)
     * @param dir   方向ラベル（例："up", "down"...）
     * @param index スプライト番号(1〜)
     */

    private void logHairPositions(BufferedImage img, String dir, int index) {
        int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                if (HAIR_COLORS.contains(rgb)) {
                    System.out.printf(
                            "Hair pixel detected in %s-%d @(%2d,%2d): 0x%08X%n",
                            dir, index, x, y, rgb
                    );
                }
            }
        }
    }


    private BufferedImage createImage(
            BufferedImage original,
            int width,
            int height) {

        // リサイズ
        BufferedImage resized = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();

        // 最近傍補間でズレを防ぐ
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();

        // 髪色置換ループ
        int w = resized.getWidth(), h = resized.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = resized.getRGB(x, y);
                // 完全一致リストにある色なら緑に置換
                if (HAIR_COLORS.contains(rgb)) {
                    resized.setRGB(x, y, NEW_HAIR_COLOR);
                }
            }
        }

        return resized;
    }

    @Override
    public void setAction() {

        updateMonsterInvincibility();

        // フロー・フィールドの初期化（1回だけ）
        if (!flowFieldInitialized) {
            initializeFlowField();
            flowFieldInitialized = true;
        }

        // ゴール（プレイヤー）の位置を取得
        goalX = getGameWindow().getPlayer().getWorldX() / FrameApp.getTileSize();
        goalY = getGameWindow().getPlayer().getWorldY() / FrameApp.getTileSize();

        // 統合フィールドを再計算（プレイヤーが動いたら）
        pathfinder.calculateIntegrationField(goalX, goalY);

        // 現在の位置から進むべき方向を取得
        int currentX = getWorldX() / FrameApp.getTileSize();
        int currentY = getWorldY() / FrameApp.getTileSize();

        currentX = Math.max(0, Math.min(currentX, pathfinder.getWidth() - 1));
        currentY = Math.max(0, Math.min(currentY, pathfinder.getHeight() - 1));

        int dir = pathfinder.getMoveDirection(currentX, currentY);

        // 方向に応じて移動
        switch (dir) {
            case 0 -> setDirection("up");
            case 1 -> setDirection("right");
            case 2 -> setDirection("down");
            case 3 -> setDirection("left");
        }

        // 射撃の処理は残す
        if (shotAvailableCounter < ROCK_COOLDOWN_FRAMES) {
            shotAvailableCounter++;
        }
        if (shotAvailableCounter >= ROCK_COOLDOWN_FRAMES) {
            int i = random.nextInt(100) + 1;
            if (i < 30) {
                shootRandomFireball();
                shotAvailableCounter = 0;
            }
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

    private void shootRandomFireball() {

        String[] dirs = {"up", "down", "left", "right"};
        String dir = dirs[random.nextInt(dirs.length)];

        ObjFireball objFireball = new ObjFireball(getGameWindow());
        objFireball.set(getWorldX(), getWorldY(), dir, true, this);
        getGameWindow().getProjectileList().add(objFireball);
    }

    public void damageReaction() {

        actionLockCounter = 0;
        setDirection(getGameWindow().getPlayer().getDirection());
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
}