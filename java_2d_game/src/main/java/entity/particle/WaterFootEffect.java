package entity.particle;

import java.awt.*;
import java.awt.image.BufferedImage;

public class WaterFootEffect implements WaterEffect {
    double worldX, worldY;
    double life, maxLife;
    BufferedImage[] frames;
    int frameCount;
    int frameW, frameH;

    public void init(double worldX, double worldY, double life, BufferedImage[] frames) {
        System.out.println("WFE.init frames=" + (frames == null ? 0 : frames.length) + " life=" + life);
        this.worldX = worldX;
        this.worldY = worldY;
        this.life = this.maxLife = life;
        this.frames = frames;
        if (frames != null && frames.length > 0) {
            this.frameCount = frames.length;
            this.frameW = frames[0].getWidth();
            this.frameH = frames[0].getHeight();
        } else {
            this.frameCount = 0;
            this.frameW = this.frameH = 0;
        }
    }

    @Override
    public void update(double dt) {
        life -= dt;
    }

    @Override
    public void draw(Graphics2D g2, int viewX, int viewY) {
        if (life <= 0 || frames == null || frameCount == 0) return;
        float t = (float) (1.0 - (life / maxLife));
        int frame = Math.min(frameCount - 1, (int) (t * frameCount));
        int screenX = (int) Math.round(worldX - viewX) - frameW / 2;
        int screenY = (int) Math.round(worldY - viewY) - frameH;
        Composite old = g2.getComposite();
        float alpha = Math.max(0f, (float) (life / maxLife));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.drawImage(frames[frame], screenX, screenY, null);
        g2.setComposite(old);
    }

    @Override
    public boolean isAlive() {
        return life > 0;
    }

    @Override
    public void reset() {
        life = 0;
        frames = null;
    }
}