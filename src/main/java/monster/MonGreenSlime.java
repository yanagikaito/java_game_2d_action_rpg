package monster;

import entity.Entity;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class MonGreenSlime extends Entity {

    private static final int ACTION_LOCK_THRESHOLD = 120;
    private static final int MAX_RANDOM_VALUE = 100;
    private static final int THRESHOLD_UP = 25;
    private static final int THRESHOLD_DOWN = 50;
    private static final int THRESHOLD_LEFT = 75;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final int SPRITE_COUNT = 3;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private Random random = new Random();
    private int actionLockCounter = 0;

    public MonGreenSlime(GameWindow gameWindow) {
        super(gameWindow);

        setType(2);
        setName("Green Slime");
        setSpeed(1);
        setMaxLife(4);
        setLife(getMaxLife());

        getSolidArea().x = 1;
        getSolidArea().y = 1;
        getSolidArea().width = 46;
        getSolidArea().height = 46;
        setSolidAreaDefaultX(getSolidArea().x);
        setSolidAreaDefaultY(getSolidArea().y);

        loadMonsterImages();
    }

    public void loadMonsterImages() {
        try {
            setSprites(new BufferedImage[DIRECTIONS.length][SPRITE_COUNT]);
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    String path = "monster/greenSlime-" + DIRECTIONS[dir] + "-" + (i + 1) + ".gif";
                    InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                    getSprites()[dir][i] = ImageIO.read(is);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAction() {

        actionLockCounter++;

        if (actionLockCounter >= ACTION_LOCK_THRESHOLD) {
            int i = random.nextInt(MAX_RANDOM_VALUE) + 1;
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
    }
}