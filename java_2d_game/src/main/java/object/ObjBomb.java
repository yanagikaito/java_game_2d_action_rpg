package object;

import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjBomb extends Projectile {

    private GameWindow gameWindow;
    private static final String[] DIRS = {"Up", "Down", "Left", "Right"};
    private static final int SPRITE_COUNT = 11;

    public ObjBomb(GameWindow gameWindow) {
        super(gameWindow, DIRS.length, SPRITE_COUNT);
        this.gameWindow = gameWindow;

        setType(getType_axe());
        setName("爆弾");
        setSpeed(1);
        setMaxLife(80);
        setAttack(5);
        setUseCost(1);
        setAlive(false);
        setDescription("[" + getName() + "]\n一定時間経過すると爆発する。");
        loadSprites();
    }

    @Override
    protected void loadSprites() {

        try {
            int ts = FrameApp.getTileSize();
            for (int i = 0; i < DIRS.length; i++) {
                for (int j = 0; j < SPRITE_COUNT; j++) {
                    String path = String.format(
                            "bomb/image-bomb%s-%d.gif",
                            DIRS[i], j + 1);
                    BufferedImage ori = ImageIO.read(
                            getClass().getClassLoader().getResourceAsStream(path)
                    );
                    BufferedImage buf = new BufferedImage(ts, ts, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = buf.createGraphics();
                    g.drawImage(ori, 0, 0, ts, ts, null);
                    g.dispose();
                    sprites[i][j] = buf;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}