package entity;

import window.GameWindow;

import java.awt.*;

public class FireworkParticle extends Particle {

    private GameWindow gameWindow;
    private final double vx;
    private double vy;
    private final double gravity;
    private final float initLife;
    private final int size;
    private final Color color;

    public FireworkParticle(
            GameWindow gameWindow,
            Entity generator,
            int originX,
            int originY,
            Color color,
            int size,
            int maxLife,
            double angle,
            double speed,
            double gravity) {

        super(gameWindow, generator, color, size, 1, maxLife, 0, 0);
        this.gameWindow = gameWindow;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.gravity = gravity;
        this.initLife = maxLife;
        this.size = size;
        this.color = color;

        setLife(maxLife);
        setWorldX(originX - size / 2);
        setWorldY(originY - size / 2);
    }

    @Override
    public void update() {

        vy += gravity;

        setWorldX((int) (getWorldX() + vx));
        setWorldY((int) (getWorldY() + vy));

        setLife(getLife() - 1);
        if (getLife() <= 0) {
            setAlive(false);
        }
    }

    @Override
    public void draw(Graphics2D g2) {

        float alpha = getLife() / initLife;
        AlphaComposite ac = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha
        );
        Composite oldComp = g2.getComposite();
        g2.setComposite(ac);

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.setColor(color);
        g2.fillOval(screenX, screenY, size, size);

        g2.setComposite(oldComp);
    }
}