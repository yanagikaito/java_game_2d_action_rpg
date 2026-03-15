package entity.particle;

import entity.Entity;
import window.GameWindow;

import java.awt.*;

public class SwordAuraParticle extends Particle {

    private final GameWindow gameWindow;
    private final double vx;
    private double vy;
    private final double gravity;
    private final float initLife;
    private final int size;
    private final Color color;
    private final float rotationSpeed;
    private float angle;

    public SwordAuraParticle(
            GameWindow gameWindow,
            Entity generator,
            int originX,
            int originY,
            Color color,
            int size,
            int maxLife,
            double angleRad,
            double speed,
            double gravity,
            float rotationSpeed) {

        super(gameWindow, generator, color, size, 1, maxLife, 0, 0);
        this.gameWindow = gameWindow;
        this.vx = Math.cos(angleRad) * speed;
        this.vy = Math.sin(angleRad) * speed;
        this.gravity = gravity;
        this.initLife = maxLife;
        this.size = size;
        this.color = color;
        this.rotationSpeed = rotationSpeed;
        this.angle = (float) angleRad;

        setLife(maxLife);
        setWorldX(originX - size / 2);
        setWorldY(originY - size / 2);
        setAlive(true);
    }

    @Override
    public void update() {
        // 物理と寿命
        vy += gravity;
        setWorldX((int) (getWorldX() + vx));
        setWorldY((int) (getWorldY() + vy));
        angle += rotationSpeed;
        setLife(getLife() - 1);
        if (getLife() <= 0) {
            setAlive(false);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        float alpha = Math.max(0f, getLife() / initLife);
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        Composite oldComp = g2.getComposite();
        g2.setComposite(ac);

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        // 回転やグロー表現
        g2.setColor(color);
        g2.fillOval(screenX, screenY, size, size);

        // optional: 輪郭や光彩
        g2.setComposite(oldComp);
    }
}