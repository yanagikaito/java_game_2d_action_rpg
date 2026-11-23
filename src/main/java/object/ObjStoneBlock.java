package object;

import entity.Entity;
import entity.type.StoneBlockType;
import frame.FrameApp;
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
        int ts = FrameApp.getTileSize();
        return true;
    }

    // update() の実装（押し出し）
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

    public void draw(Graphics2D g2) {
        int screenX = this.getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = this.getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }
}