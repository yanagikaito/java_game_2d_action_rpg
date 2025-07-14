package entity;

import frame.FrameApp;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import java.awt.*;

public class Particle extends Entity {

    private GameWindow gameWindow;
    private Entity generator;
    private Color color;
    private int size;
    private int speed;
    private int xd;
    private int yd;
    private final int startY;
    private final int tileSize;
    private final double gravity = 1.0;

    public Particle(GameWindow gameWindow,
                    @NotNull Entity generator,
                    Color color,
                    int size,
                    int speed,
                    int maxLife,
                    int xd,
                    int yd) {

        super(gameWindow);
        this.gameWindow = gameWindow;
        this.generator = generator;
        this.color = color;
        this.size = size;
        this.speed = speed;
        this.xd = xd;
        this.yd = yd;
        this.tileSize = FrameApp.getTileSize();

        int offset = (FrameApp.getTileSize() / 2) - (size / 2);
        setLife(maxLife);
        setWorldX(generator.getWorldX() + offset);
        setWorldY(generator.getWorldY() + offset);
        this.startY = getWorldY();
    }

    @Override
    public void update() {

        setLife(getLife() - 1);

        if (getLife() < getMaxLife() / 3) {
            yd += gravity;
        }

        setWorldX(getWorldX() + xd * speed);
        setWorldY(getWorldY() + yd * speed);

        if (getWorldY() >= startY + tileSize) {
            setAlive(false);
        }
    }

    @Override
    public void draw(@NotNull Graphics2D g2) {

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.setColor(color);
        g2.fillRect(screenX, screenY, size, size);
    }
}