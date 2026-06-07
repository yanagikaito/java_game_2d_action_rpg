package monster;

import entity.type.AxeType;
import entity.Entity;
import entity.type.MonsterType;
import frame.FrameApp;
import object.ObjAxe;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

public class MonMintSoldier extends Entity {

    private core.PathFinder pathfinder;
    private core.Node[][] grid;
    private int goalX, goalY;
    private boolean flowFieldInitialized = false;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final String[] ATTACK_DIRECTIONS = {"attackUp", "attackDown", "attackLeft", "attackRight"};
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] attackSprites = new BufferedImage[ATTACK_DIRECTIONS.length][SPRITE_COUNT];
    private static final String[] AXE_DIRECTIONS = {"axeUp", "axeDown", "axeLeft", "axeRight"};
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM1 = 40;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM2 = 60;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM3 = 80;
    private static final int ATTACK_ANIMATION_FRAMES = SPRITE_ATTACKING_THRESHOLD_NUM3;
    private int attackCounter;
    private BufferedImage[][] axeSprites = new BufferedImage[AXE_DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] currentAttackSprites;
    private static final int SPRITE_COUNT = 3;
    private static final int ANIMATION_RATE = 3;
    private Random random = new Random();
    private int actionLockCounter = 0;
    private static final int ROCK_COOLDOWN_FRAMES = 180;
    private int shotAvailableCounter = ROCK_COOLDOWN_FRAMES;
    private static final int NEW_HAIR_COLOR = 0xFF00FF00;

    public MonMintSoldier(GameWindow gameWindow) {

        super(gameWindow);
        setType(new MonsterType());
        setName("Mint Soldier");
        setDirection("down");
        setDefaultSpeed(1);
        setSpeed(getDefaultSpeed());
        setMaxLife(50);
        setLife(getMaxLife());
        setAttack(5);
        setDefense(0);
        setExp(5);
        setCurrentWeapon(new ObjAxe(gameWindow));
        getSolidArea().x = 1;
        getSolidArea().y = 1;
        getSolidArea().width = 46;
        getSolidArea().height = 46;
        setSolidAreaDefaultX(getSolidArea().x);
        setSolidAreaDefaultY(getSolidArea().y);
        setAttackArea(new Rectangle());
        getAttackArea().width = 48;
        getAttackArea().height = 48;
        loadMonsterImages();
        loadMonsterAttackSprites();
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

                    if (original == null) {
                        System.out.println("Image not found at path: " + original);
                    }

//                    // 髪ピクセルを全走査してログ出力
//                    logHairPositions(original, DIRECTIONS[dir], i + 1);

                    BufferedImage processed = createImage(original, tileSize, tileSize);
                    getSprites()[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 斧用の攻撃スプライトを全方向・全フレーム分読み込み、
     * 向きに応じて縦長 or 横長にリサイズする。
     */

    private void loadMonsterAttackSprites() {

        int tileSize = FrameApp.getTileSize();
        setSprites(new BufferedImage[AXE_DIRECTIONS.length][SPRITE_COUNT]);
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

//    /**
//     * 画像中の髪色ピクセルを全走査し、見つかった座標と色をログ出力する
//     *
//     * @param img   元画像(リサイズ前)
//     * @param dir   方向ラベル（例："up", "down"...）
//     * @param index スプライト番号(1〜)
//     */
//
//    private void logHairPositions(BufferedImage img, String dir, int index) {
//        int w = img.getWidth(), h = img.getHeight();
//        for (int y = 0; y < h; y++) {
//            for (int x = 0; x < w; x++) {
//                int rgb = img.getRGB(x, y);
//                if (HAIR_COLORS.contains(rgb)) {
//                    System.out.printf(
//                            "Hair pixel detected in %s-%d @(%2d,%2d): 0x%08X%n",
//                            dir, index, x, y, rgb
//                    );
//                }
//            }
//        }
//    }


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

        // 距離チェック（プレイヤーとのタイル差分で計算し直す）
        int mx = getWorldX() / FrameApp.getTileSize();
        int my = getWorldY() / FrameApp.getTileSize();
        int px = getGameWindow().getPlayer().getWorldX() / FrameApp.getTileSize();
        int py = getGameWindow().getPlayer().getWorldY() / FrameApp.getTileSize();
        double dist = Math.hypot(px - mx, py - my);

        // プレイヤーが2タイル以内に近づいたら攻撃開始
        if (dist < FrameApp.getTileSize() * 2 && !getAttacking()) {
            setAttacking(true);
            return;
        }

        // 攻撃処理
        if (getAttacking()) {
            startAttack();
            monsterAttacking();
            return;
        }
    }


    /**
     * 現在の武器種別と向きをもとに攻撃アニメーションを開始する準備を行う。
     * axeなら「axe＋方向」、それ以外は「attack＋方向」を attackDirection に設定し、
     * 対応する攻撃スプライト配列を更新。
     */

    private void startAttack() {
        String dir = getDirection();
//        System.out.println("dir = " + dir);
        if (dir == null || dir.isEmpty()) dir = getDirection();
        String base = capitalize(dir);
//        System.out.println("base = " + base);
        if (getCurrentWeapon().getType() instanceof AxeType) {
            setAttackDirection("axe" + base);
        } else {
            setAttackDirection("attack" + base);
        }

        updateCurrentAttackSprites();
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
     * 斧による近接攻撃のアニメーションと当たり判定を処理。
     * スプライトフレームを進め、攻撃範囲内のモンスターやタイルにダメージを与え、
     * 攻撃終了後に位置や当たり判定を元に戻す。
     */

    public void monsterAttacking() {

        attackCounter++;

        // 3フレームに1度だけスプライト更新する
        if (attackCounter / ANIMATION_RATE != 0) {
//            System.out.println("attackCounter = " + attackCounter / ANIMATION_RATE);
            return;
        }

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

        if (getType() instanceof MonsterType) {

            int monsterIndex = getGameWindow().getCollisionChecker().checkEntity(this, getGameWindow().getMonster());
            getGameWindow().getPlayer().damageMonster(monsterIndex, getAttack(), getCurrentWeapon().getKnockBackPower());

            int iTileIndex = getGameWindow().getCollisionChecker().checkEntity(this, getGameWindow().getItile());
            getGameWindow().getPlayer().damageInteractiveTile(iTileIndex);

            setWorldX(originalWorldX);
            setWorldY(originalWorldY);
            getSolidArea().width = originalSolidWidth;
            getSolidArea().height = originalSolidHeight;
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
     * モンスターおよび攻撃スプライトを画面に描画。
     * 無敵状態時は半透明、攻撃中は武器種別ごとの拡大スプライトを使用。
     *
     * @param g2 描画用Graphics2Dオブジェクト
     */

    @Override
    public void draw(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

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

                drawHpBarIfNeeded(g2, screenX, screenY);
            }
        }

        g2.setComposite(original);
    }
}