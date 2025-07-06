package tileInteractive;

import entity.Entity;
import frame.FrameApp;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ItDryTree extends InteractiveTile {

    private GameWindow gameWindow;

    public ItDryTree(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;

        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tileInteractive/drytree.gif"));
            setImage(raw, FrameApp.getTileSize());

            setDestructible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCorrectItem(@NotNull Entity entity) {

        boolean isCorrectItem = false;

        if (entity.getCurrentWeapon().getType() == getType_axe()) {
            isCorrectItem = true;
        }
        return isCorrectItem;
    }

    public void draw(@NotNull Graphics2D g2) {

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }
}