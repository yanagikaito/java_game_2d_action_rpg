package npc;

import collision.CollisionChecker;
import entity.Entity;
import frame.FrameApp;
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
    private Random random = new Random();
    private int actionLockCounter = 0;
    private final CollisionChecker collisionChecker;

    public NpcChicken(GameWindow gameWindow) {
        super(gameWindow);
        this.collisionChecker = new CollisionChecker(gameWindow);
        setDirection("down");
        setSpeed(1);
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

    private BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }
}