package object;

import entity.Entity;
import entity.type.LanternType;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjLantern extends Entity {

    private GameWindow gameWindow;
    private static final int DEFAULT_RADIUS = 250;
    private static final float DEFAULT_INTENSITY = 1.0f;

    public ObjLantern(GameWindow gameWindow) {

        super(gameWindow);
        this.gameWindow = gameWindow;

        setType(new LanternType());
        setName("ランタン");
        setDescription("[" + getName() + "]\n周囲を照らす。");

        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("object/lantern.gif"));
            setImage(raw, FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw(Graphics2D g2) {

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }

    @Override
    public boolean isLightSource() {
        return true;
    }

    @Override
    public int getLightRadius() {
        return DEFAULT_RADIUS;
    }

    @Override
    public float getLightIntensity() {
        return DEFAULT_INTENSITY;
    }
}