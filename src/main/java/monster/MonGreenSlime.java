package monster;

import entity.Entity;
import frame.FrameApp;
import object.ObjStone;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
    private static final int ROCK_COOLDOWN_FRAMES = 180;
    private int shotAvailableCounter = ROCK_COOLDOWN_FRAMES;

    public MonGreenSlime(GameWindow gameWindow) {

        super(gameWindow);
        setType(2);
        setName("Green Slime");
        setSpeed(1);
        setMaxLife(4);
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

    public void loadMonsterImages() {
        try {
            int tileSize = FrameApp.getTileSize();
            setSprites(new BufferedImage[DIRECTIONS.length][SPRITE_COUNT]);
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream("monster/greenSlime-" + DIRECTIONS[dir] + "-" + (i + 1) + ".gif"));
                    BufferedImage processed = createImage(original, tileSize, tileSize);
                    getSprites()[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private @NotNull BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    public void setAction() {

        updateMonsterInvincibility();

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

        int i = random.nextInt(MAX_RANDOM_VALUE) + 1;

        if (i < 99 && getShotAvailableCounter() == 30) {

            getProjectile().set(getWorldX(), getWorldY(), getDirection(), true, this);
            getGameWindow().getProjectileList().add(getProjectile());

            if (shotAvailableCounter < ROCK_COOLDOWN_FRAMES) {
                shotAvailableCounter++;
            }
            if (shotAvailableCounter >= ROCK_COOLDOWN_FRAMES) {
                shootRandomStone();
                shotAvailableCounter = 0;
            }
        }
    }

    private void shootRandomStone() {

        int idx = new java.util.Random().nextInt(DIRECTIONS.length);
        String dir = DIRECTIONS[idx];

        ObjStone rock = new ObjStone(getGameWindow());
        rock.set(
                getWorldX(),
                getWorldY(),
                dir,
                true,
                this
        );
        getGameWindow().getProjectileList().add(rock);
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
                System.out.println("スライムの無敵状態が解除されました。");
                System.out.println("getInvincible() :" + getInvincible());
                setInvincibleCounter(0);
            }
        }
    }
}