package tileInteractive;

import entity.Entity;
import frame.FrameApp;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;

public class ItDryTree extends InteractiveTile {
    public ItDryTree(GameWindow gw, int row, int col) {
        super(gw, row, col);
        setDestructible(true);
        setLife(6);

        try {
            var raw = ImageIO.read(
                    getClass().getResourceAsStream("/tileInteractive/drytree.gif"));
            setImage(raw, FrameApp.getTileSize());
        } catch (Exception e) {
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
        InteractiveTile trunk = new ItTrunk(getGameWindow(), getWorldX() / tileSize, getWorldY() / tileSize);
        return trunk;
    }

    @Override
    public Color getParticleColor() {
        Color color = new Color(65, 50, 30);
        return color;
    }

    @Override
    public int getParticleSize() {
        int size = 6;
        return size;
    }

    @Override
    public int getParticleSpeed() {
        int speed = 1;
        return speed;
    }

    @Override
    public int getParticleMaxLife() {
        int maxLife = 20;
        return maxLife;
    }

    @Override
    public void draw(@NotNull Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        int screenX = getWorldX() - getGameWindow().getPlayer().getWorldX() + getGameWindow().getPlayer().getScreenX();
        int screenY = getWorldY() - getGameWindow().getPlayer().getWorldY() + getGameWindow().getPlayer().getScreenY();

        g2.drawImage(getImage(), screenX, screenY, tileSize, tileSize, null);
    }
}