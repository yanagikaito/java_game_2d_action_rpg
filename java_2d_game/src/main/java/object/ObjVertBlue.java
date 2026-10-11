package object;

import entity.Entity;
import entity.type.PickupOnlyType;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjVertBlue extends Entity {

    private GameWindow gameWindow;

    public ObjVertBlue(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setValue(5);
        setType(new PickupOnlyType());
        setName("ブルーヴェール");

        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("object/vert_blue.gif"));
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