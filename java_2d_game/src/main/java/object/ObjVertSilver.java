package object;

import entity.Entity;
import entity.type.PickupOnlyType;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjVertSilver extends Entity {

    private GameWindow gameWindow;

    public ObjVertSilver(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setValue(100);
        setType(new PickupOnlyType());
        setName("ヴェールシルバー");

        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("object/vert_silver.gif"));
            setImage(raw, FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }
}