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

    public ItDryTree(GameWindow gameWindow, int row, int col) {
        super(gameWindow, row, col);
        this.gameWindow = gameWindow;
        setWorldX(FrameApp.getTileSize() * row);
        setWorldY(FrameApp.getTileSize() * col);

        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tileInteractive/drytree.gif"));
            setImage(raw, FrameApp.getTileSize());

            setDestructible(true);
            setLife(6);

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

    public InteractiveTile createDestroyedForm() {
        int tileSize = FrameApp.getTileSize();
        InteractiveTile trunk = new ItTrunk(gameWindow, getWorldX() / tileSize, getWorldY() / tileSize);
        return trunk;
    }

    @Override
    public void draw(@NotNull Graphics2D g2) {

        int ts = FrameApp.getTileSize();
        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        Composite original = g2.getComposite();

        if (getInvincible()) {
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.5f
            ));
        }

        g2.drawImage(getImage(), screenX, screenY, ts, ts, null);

        g2.setComposite(original);
    }
}