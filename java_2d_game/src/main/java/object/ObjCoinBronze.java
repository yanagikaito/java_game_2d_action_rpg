package object;

import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjCoinBronze extends Entity {

    private GameWindow gameWindow;

    public ObjCoinBronze(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setValue(1);
        setType(getType_pickupOnly());
        setName("ブロンズコイン");

        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("object/coin-bronze.gif"));
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