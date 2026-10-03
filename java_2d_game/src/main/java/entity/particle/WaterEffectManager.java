package entity.particle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;

public class WaterEffectManager {
    private final BufferedImage[] footFrames;
    private final Deque<WaterSplash> splashPool = new ArrayDeque<>();
    private final Deque<WaterFootEffect> footPool = new ArrayDeque<>();
    private final List<WaterSplash> activeSplashes = new ArrayList<>();
    private final List<WaterFootEffect> activeFeet = new ArrayList<>();

    public WaterEffectManager(BufferedImage[] footFrames) {
        this.footFrames = footFrames;
        for (int i = 0; i < 32; i++) splashPool.add(new WaterSplash());
        for (int i = 0; i < 16; i++) footPool.add(new WaterFootEffect());
    }

    public void spawnFoot(double x, double y, double life) {
        System.out.println("WEM.spawnFoot called x=" + x + " y=" + y + " life=" + life + " pool=" + footPool.size() + " activeBefore=" + activeFeet.size());
        if (activeFeet.size() > 300) {
            System.out.println("WEM.spawnFoot blocked: activeFeet limit");
            return;
        }
        WaterFootEffect f = footPool.pollFirst();
        if (f == null) f = new WaterFootEffect();
        double jitterX = (Math.random() - 0.5) * 6.0;
        double jitterY = (Math.random() - 0.5) * 2.0;
        f.init(x + jitterX, y + jitterY, life, footFrames);
        activeFeet.add(f);
        System.out.println("WEM.spawnFoot added activeAfter=" + activeFeet.size());
    }


    public void update(double dt) {
        // スプラッシュ更新
        Iterator<WaterSplash> itS = activeSplashes.iterator();
        while (itS.hasNext()) {
            WaterSplash s = itS.next();
            s.update(dt);
            if (!s.isAlive()) {
                itS.remove();
                splashPool.addLast(s);
            }
        }
        // 足元エフェクト更新
        Iterator<WaterFootEffect> itF = activeFeet.iterator();
        while (itF.hasNext()) {
            WaterFootEffect f = itF.next();
            f.update(dt);
            if (!f.isAlive()) {
                itF.remove();
                f.reset();
                footPool.addLast(f);
            }
        }
    }

    public void draw(Graphics2D g2, int viewX, int viewY) {
        for (WaterSplash s : activeSplashes) s.draw(g2, viewX, viewY);
        for (WaterFootEffect f : activeFeet) f.draw(g2, viewX, viewY);
    }
}