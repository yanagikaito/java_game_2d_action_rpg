package entity.particle;

import java.awt.*;

public class WaterSplash {
    public double x, y;
    public double vx, vy;
    public double life, maxLife;

    public WaterSplash() {
        this(0, 0, 0, 0, 0);
    }

    public WaterSplash(double x, double y, double vx, double vy, double life) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
    }

    public void update(double dt) {
        life -= dt;
        x += vx * dt;
        y += vy * dt;
        vy += 300 * dt; // 重力風
    }

    public void draw(Graphics2D g2, int viewX, int viewY) {
        if (life <= 0) return;
        float alpha = (float) Math.max(0, life / maxLife);
        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int size = (int) (3 + (1 - alpha) * 8);
        g2.setColor(new Color(180, 220, 255));
        g2.fillOval((int) (x - viewX) - size / 2, (int) (y - viewY) - size / 2, size, size);
        g2.setComposite(oldComp);
    }

    public boolean isAlive() {
        return life > 0;
    }
}