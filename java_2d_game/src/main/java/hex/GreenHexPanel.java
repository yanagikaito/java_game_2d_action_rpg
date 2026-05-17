package hex;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 統合版 GreenHexPanel
 * - SmallHex[] (6 個) を管理し、外周→中央へ合体するアニメーション
 * - EmblemPanel の要素（fragments 相当の動き、グロー、パーティクル、低解像度ブラー）を内包
 */

public class GreenHexPanel extends JPanel {

    private final SmallHex[] tris = new SmallHex[6];
    private final Timer timer;
    private boolean forming = false;
    private final double formDuration = 1.0;
    private double formTime = 0.0;
    private final int fpsDelay = 16;
    private final List<Particle> particles = new ArrayList<>();
    private boolean assembled = false;

    private static final int PARTICLE_POOL = 90;

    public GreenHexPanel() {
        setOpaque(false);
        initPieces();
        initParticles();

        timer = new Timer(fpsDelay, e -> {
            double dt = fpsDelay / 1000.0;
            update(dt);
            repaint();
        });
    }

    private void initPieces() {
        double finalSize = 1.5 * 0.28;
        double outerRadius = 1.8;
        for (int i = 0; i < tris.length; i++) {
            double ang = Math.toRadians(-90 + i * 60.0);
            double x = Math.cos(ang) * outerRadius;
            double y = Math.sin(ang) * outerRadius;
            double startSize = finalSize * (0.7 + (i % 3) * 0.08);
            double startAngle = (i % 2 == 0) ? 60 + i * 10 : -60 - i * 8;
            tris[i] = new SmallHex(x, y, finalSize, startSize, startAngle);
            tris[i].vy = 0.6 + (i * 0.03);
            tris[i].angularVel = (i % 2 == 0) ? 40 : -30;
        }
        forming = false;
        formTime = 0.0;
        assembled = false;
    }

    private void initParticles() {
        particles.clear();
        for (int i = 0; i < PARTICLE_POOL; i++) particles.add(createParticle(true));
    }

    public void resetAnimation() {
        initPieces();
        initParticles();
        repaint();
    }

    public void startAnimation() {
        if (!timer.isRunning()) timer.start();
    }

    private Point2D.Double[] finalPositions() {
        Point2D.Double[] pts = new Point2D.Double[6];
        double r = 0.6;
        for (int i = 0; i < 6; i++) {
            double ang = Math.toRadians(-90 + i * 60.0);
            pts[i] = new Point2D.Double(r * Math.cos(ang), r * Math.sin(ang));
        }
        return pts;
    }

    private void update(double dt) {
        if (!forming) {
            for (SmallHex t : tris) {
                t.y += t.vy * dt;
                t.angle += t.angularVel * dt;
            }
            boolean allAbove = true;
            for (SmallHex t : tris) {
                if (t.y < -0.05) {
                    allAbove = false;
                    break;
                }
            }
            if (allAbove) {
                forming = true;
                formTime = 0.0;
                Point2D.Double[] targets = finalPositions();
                for (int i = 0; i < tris.length; i++) {
                    tris[i].setTarget(targets[i].x, targets[i].y, tris[i].size);
                }
                // burst particles at start of forming
                for (int i = 0; i < 30; i++) particles.add(createParticle(false));
            }
        } else {
            formTime += dt;
            double t = Math.min(1.0, formTime / formDuration);
            double ease = t * t * (3 - 2 * t);
            for (SmallHex st : tris) {
                st.x = lerp(st.startX, st.targetX, ease);
                st.y = lerp(st.startY, st.targetY, ease);
                st.size = lerp(st.startSize, st.targetSize, ease);
                st.angle = lerp(st.startAngle, 0.0, ease);
            }
            if (formTime >= formDuration && !assembled) {
                assembled = true;
                for (int i = 0; i < 60; i++) particles.add(createParticle(false));
            }
        }

        // update particles
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.life -= dt;
            if (p.life <= 0) it.remove();
        }
        while (particles.size() < PARTICLE_POOL) particles.add(createParticle(true));
    }

    private Particle createParticle(boolean background) {
        double angle = Math.random() * Math.PI * 2;
        double dist = background ? (0.6 + Math.random() * 1.6) : (0.1 + Math.random() * 0.8);
        double x = Math.cos(angle) * dist;
        double y = Math.sin(angle) * dist;
        double speed = background ? (0.01 + Math.random() * 0.04) : (0.6 + Math.random() * 1.6);
        double vx = Math.cos(angle) * speed;
        double vy = Math.sin(angle) * speed;
        float alpha = (float) (0.15 + Math.random() * 0.85);
        int size = background ? (1 + (int) (Math.random() * 2)) : (2 + (int) (Math.random() * 3));
        double life = background ? (2.0 + Math.random() * 6.0) : (0.6 + Math.random() * 1.2);
        return new Particle(x, y, vx, vy, size, alpha, life);
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    // particle
    private static class Particle {
        double x, y, vx, vy;
        int size;
        float alpha;
        double life;

        Particle(double x, double y, double vx, double vy, int size, float alpha, double life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.alpha = alpha;
            this.life = life;
        }
    }

    // getters used by UI
    public SmallHex[] getTris() {
        return tris;
    }

    public boolean isForming() {
        return forming;
    }

    public double getFormTime() {
        return formTime;
    }

    public double getFormDuration() {
        return formDuration;
    }
}