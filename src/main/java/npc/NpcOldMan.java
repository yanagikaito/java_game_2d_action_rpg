package npc;

import entity.Entity;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class NpcOldMan extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final int SPRITE_COUNT = 3;

    public NpcOldMan(GameWindow gameWindow) {
        super(gameWindow);
        setDirection("down");
        setSpeed(1);
        loadNPCImages();
    }

    public void loadNPCImages() {

        try {

            setSprites(new BufferedImage[DIRECTIONS.length][SPRITE_COUNT]);
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    String path = "npc/oldman-" + DIRECTIONS[dir] + "-" + (i + 1) + ".png";
                    InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                    getSprites()[dir][i] = ImageIO.read(is);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}