package tileInteractive;

import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ItTrunk extends InteractiveTile {

    private GameWindow gameWindow;

    public ItTrunk(GameWindow gameWindow, int row, int col) {
        super(gameWindow, row, col);
        this.gameWindow = gameWindow;
        setWorldX(FrameApp.getTileSize() * row);
        setWorldY(FrameApp.getTileSize() * col);

        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tileInteractive/trunk.gif"));
            setImage(raw, FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }

        getSolidArea().x = 0;
        getSolidArea().y = 0;
        getSolidArea().width = 0;
        getSolidArea().height = 0;
        setSolidAreaDefaultX(getSolidArea().x);
        setSolidAreaDefaultY(getSolidArea().y);
    }

    public void draw(Graphics2D g2) {

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }
}