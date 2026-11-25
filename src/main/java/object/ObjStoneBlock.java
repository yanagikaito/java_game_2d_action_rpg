package object;

import entity.Entity;
import entity.type.StoneBlockType;
import frame.FrameApp;
import tile.TileManager;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ObjStoneBlock extends Entity {

    private final GameWindow gameWindow;
    private static final String[] DIRS = {"Up", "Down", "Left", "Right"};
    private static final int OBJ_STONE_BLOCK_SPRITE_COUNT = 1;

    private final BufferedImage[] upSprites = new BufferedImage[OBJ_STONE_BLOCK_SPRITE_COUNT];
    private final BufferedImage[] downSprites = new BufferedImage[OBJ_STONE_BLOCK_SPRITE_COUNT];
    private final BufferedImage[] leftSprites = new BufferedImage[OBJ_STONE_BLOCK_SPRITE_COUNT];
    private final BufferedImage[] rightSprites = new BufferedImage[OBJ_STONE_BLOCK_SPRITE_COUNT];

    private int targetX;
    private int targetY;
    private boolean moving = false;
    private boolean targetInitialized = false;

    public ObjStoneBlock(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;

        setType(new StoneBlockType());
        setName("石のブロック");
        setSpeed(4);
        setBlocking(true);
        setDirection("down");

        loadSprites();
        updateCurrentImage();
    }

    private void ensureTargetInitialized() {
        if (!targetInitialized) {
            this.targetX = getWorldX();
            this.targetY = getWorldY();
            targetInitialized = true;
        }
    }

    protected void loadSprites() {
        int ts = FrameApp.getTileSize();

        for (int i = 0; i < DIRS.length; i++) {
            String dirName = DIRS[i]; // "Up", "Down", "Left", "Right"

            for (int j = 0; j < OBJ_STONE_BLOCK_SPRITE_COUNT; j++) {
                String path = String.format("/object/image-pushStoneBlock%s-%d.gif", dirName, j + 1);

                try (InputStream inputStream = getClass().getResourceAsStream(path)) {
                    if (inputStream == null) {
                        throw new IllegalArgumentException("Stone block image not found at path: " + path);
                    }
                    BufferedImage raw = ImageIO.read(inputStream);
                    if (raw == null) {
                        throw new IllegalArgumentException("Failed to decode image at path: " + path);
                    }

                    BufferedImage scaled = scaleImage(raw, ts, ts);
                    assignSprite(dirName, j, scaled);
                } catch (IOException e) {
                    throw new IllegalStateException("Error loading stone block image: " + path, e);
                }
            }
        }
    }

    private void assignSprite(String dirName, int index, BufferedImage img) {
        switch (dirName) {
            case "Up" -> upSprites[index] = img;
            case "Down" -> downSprites[index] = img;
            case "Left" -> leftSprites[index] = img;
            case "Right" -> rightSprites[index] = img;
            default -> throw new IllegalArgumentException("Unknown direction: " + dirName);
        }
    }

    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return scaled;
    }

    private void updateCurrentImage() {
        int tileSize = FrameApp.getTileSize();
        String dir = getDirection();
        BufferedImage sprite = switch (dir == null ? "down" : dir.toLowerCase()) {
            case "up" -> upSprites[0];
            case "down" -> downSprites[0];
            case "left" -> leftSprites[0];
            case "right" -> rightSprites[0];
            default -> downSprites[0];
        };
        if (sprite != null) {
            this.setImage(sprite, tileSize);
        }
    }

    public boolean isMoving() {
        return moving;
    }

    /**
     * プレイヤーから指定方向（"up", "down", "left", "right"）に押されたときの処理
     */

    public boolean tryPush(String direction) {
        ensureTargetInitialized();
        if (moving || direction == null) {
            return false;
        }

        int ts = FrameApp.getTileSize();
        int dx = 0;
        int dy = 0;

        switch (direction.toLowerCase()) {
            case "up" -> dy = -ts;
            case "down" -> dy = ts;
            case "left" -> dx = -ts;
            case "right" -> dx = ts;
            default -> {
                return false;
            }
        }

        int nextX = getWorldX() + dx;
        int nextY = getWorldY() + dy;

        if (!canMoveTo(nextX, nextY)) {
            return false;
        }

        setDirection(direction.toLowerCase());
        updateCurrentImage();
        this.targetX = nextX;
        this.targetY = nextY;
        this.moving = true;
        return true;
    }

    private boolean canMoveTo(int nextX, int nextY) {

        int tileSize = FrameApp.getTileSize();

        // タイル・マップ範囲チェック（14〜21を含む collision = true のタイルをブロック）
        if (gameWindow != null && gameWindow.getTileManager() != null) {
            var tileManager = gameWindow.getTileManager();

            int margin = 2;
            int leftCol = (nextX + margin) / tileSize;
            int rightCol = (nextX + tileSize - 1 - margin) / tileSize;
            int topRow = (nextY + margin) / tileSize;
            int bottomRow = (nextY + tileSize - 1 - margin) / tileSize;

            int[][] corners = {
                    {leftCol, topRow},
                    {rightCol, topRow},
                    {leftCol, bottomRow},
                    {rightCol, bottomRow}
            };

            for (int[] c : corners) {
                int tileId = tileManager.getTileIdAt(c[0], c[1]);
                // マップ範囲外（-1）なら移動不可
                if (tileId < 0) {
                    return false;
                }
                // 水タイルまたは壁（14〜21 / collision == true）なら移動不可
                if (tileId == TileManager.STAIRS_UP_TILE_ID ||
                        tileId == TileManager.WATER_TILE_ID || isBlockedTile(tileManager, tileId)) {
                    return false;
                }
            }
        }

        // 他のオブジェクト（別の ObjStoneBlock や障害物）との重なりチェック
        if (gameWindow != null && gameWindow.getObj() != null) {
            for (Entity other : gameWindow.getObj()) {
                if (other == null || other == this || !other.getAlive()) {
                    continue;
                }
                if (isRectOverlap(nextX, nextY, tileSize, tileSize, other.getWorldX(), other.getWorldY(), tileSize, tileSize)) {
                    return false;
                }
            }
        }

        // プレイヤーとの重なりチェック
        if (gameWindow != null && gameWindow.getPlayer() != null) {
            var player = gameWindow.getPlayer();
            if (isRectOverlap(nextX, nextY, tileSize, tileSize, player.getWorldX(), player.getWorldY(), tileSize, tileSize)) {
                return false;
            }
        }

        return true;
    }

    // 押し出し
    @Override
    public void update() {
        ensureTargetInitialized();
        updateCurrentImage();

        if (!moving) {
            return;
        }

        int speed = Math.max(1, getSpeed());
        int x = getWorldX();
        int y = getWorldY();

        if (x < targetX) {
            x = Math.min(x + speed, targetX);
        } else if (x > targetX) {
            x = Math.max(x - speed, targetX);
        }

        if (y < targetY) {
            y = Math.min(y + speed, targetY);
        } else if (y > targetY) {
            y = Math.max(y - speed, targetY);
        }

        setWorldX(x);
        setWorldY(y);

        if (x == targetX && y == targetY) {
            moving = false;
        }
    }

    private boolean isBlockedTile(TileManager tileManager, int tileId) {

        if (tileManager == null || tileId < 0) {
            return true;
        }

        var tiles = tileManager.getTiles();
        if (tiles == null || tileId >= tiles.length) {
            return false;
        }

        var targetTile = tiles[tileId];
        return targetTile != null && targetTile.collision;
    }

    /**
     * 2つの矩形（X軸・Y軸に平行な長方形）が重なっているかを判定。
     * 辺同士が接しているだけの場合は重なっていないとみなす。
     *
     * @param firstX       1つ目の矩形の左端X座標
     * @param firstY       1つ目の矩形の上端Y座標
     * @param firstWidth   1つ目の矩形の幅
     * @param firstHeight  1つ目の矩形の高さ
     * @param secondX      2つ目の矩形の左端X座標
     * @param secondY      2つ目の矩形の上端Y座標
     * @param secondWidth  2つ目の矩形の幅
     * @param secondHeight 2つ目の矩形の高さ
     * @return 2つの矩形が重なっている場合は {@code true}、それ以外は {@code false}
     */

    private boolean isRectOverlap(int firstX, int firstY, int firstWidth, int firstHeight,
                                  int secondX, int secondY, int secondWidth, int secondHeight) {
        boolean overlapsHorizontally = firstX < secondX + secondWidth && firstX + firstWidth > secondX;
        boolean overlapsVertically = firstY < secondY + secondHeight && firstY + firstHeight > secondY;
        return overlapsHorizontally && overlapsVertically;
    }

    public void draw(Graphics2D g2) {
        int screenX = this.getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = this.getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }
}