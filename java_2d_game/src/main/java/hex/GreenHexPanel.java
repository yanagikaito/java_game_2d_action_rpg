package hex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * GreenHexPanel
 *
 * <p>6 個の三角ピースを管理し、外周から中央へ合体するアニメーションを行う Swing コンポーネント。</p>
 *
 * <p>このコンポーネントは自分で Timer を持ち、状態を更新して repaint() を呼びます。
 * UI 側はこのコンポーネントをフレームに追加するだけでよいです。</p>
 */

public class GreenHexPanel extends JPanel {
    // 基準値（必ず Renderer と整合させること）
    private static final double FACET_RADIUS = 0.6;
    private static final double BASE_PIECE_SIZE = 1.5 * 0.28;

    private final SmallHex[] tris = new SmallHex[6];
    private final Timer timer;
    private boolean forming = false;
    private final double formDuration = 1.0;
    private double formTime = 0.0;
    private final int fpsDelay = 16;
    private final List<Particle> particles = new ArrayList<>();
    private boolean assembled = false;

    private static final int PARTICLE_POOL = 90;
    private final Random rnd = new Random();

    // animation timing
    private long lastTimeNanos = System.nanoTime();

    public GreenHexPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(360, 360)); // 任意の推奨サイズ（UI 側で調整可）
        initPieces();
        initParticles();

        timer = new Timer(fpsDelay, e -> {
            long now = System.nanoTime();
            double dt = (now - lastTimeNanos) / 1_000_000_000.0;
            if (dt > 0.1) dt = 0.1;
            lastTimeNanos = now;

            update(dt);
            repaint();
        });
    }

    /**
     * 初期ピース配置：各ファセットの外側（重心方向へオフセット）に配置します。
     */

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

            // ← ここで displayAngle を初期化する（ラジアン）
            tris[i].displayAngle = Math.toRadians(startAngle);

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
        if (!timer.isRunning()) {
            lastTimeNanos = System.nanoTime();
            timer.start();
        }
    }

    /**
     * 合体後の各ピースの中心位置（正規化座標）を返す。
     */

    private Point2D.Double[] finalPositions() {
        Point2D.Double[] pts = new Point2D.Double[6];
        double r = FACET_RADIUS;
        for (int i = 0; i < 6; i++) {
            double ang = Math.toRadians(-90 + i * 60.0);
            pts[i] = new Point2D.Double(r * Math.cos(ang), r * Math.sin(ang));
        }
        return pts;
    }

    private void update(double dt) {
        if (!forming) {
            // 初期のゆらぎ（任意）
            for (SmallHex t : tris) {
                t.y += t.vy * dt;
                t.angle += t.angularVel * dt;
            }
            // 合体開始判定（全ピースがある閾値を超えたら）
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
                    // 合体後のサイズは BASE_PIECE_SIZE に合わせる
                    tris[i].setTarget(targets[i].x, targets[i].y, BASE_PIECE_SIZE);
                }
                // burst particles
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
                // 回転は滑らかに補間（displayAngle はラジアン）
                double target = Math.atan2(-st.y, -st.x);
                st.displayAngle = lerpAngle(st.displayAngle, target, 0.14);
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
        double angle = rnd.nextDouble() * Math.PI * 2;
        double dist = background ? (0.6 + rnd.nextDouble() * 1.6) : (0.1 + rnd.nextDouble() * 0.8);
        double x = Math.cos(angle) * dist;
        double y = Math.sin(angle) * dist;
        double speed = background ? (0.01 + rnd.nextDouble() * 0.04) : (0.6 + rnd.nextDouble() * 1.6);
        double vx = Math.cos(angle) * speed;
        double vy = Math.sin(angle) * speed;
        float alpha = (float) (0.15 + rnd.nextDouble() * 0.85);
        int size = background ? (1 + (int) (rnd.nextDouble() * 2)) : (2 + (int) (rnd.nextDouble() * 3));
        double life = background ? (2.0 + rnd.nextDouble() * 6.0) : (0.6 + rnd.nextDouble() * 1.2);
        return new Particle(x, y, vx, vy, size, alpha, life);
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private double lerpAngle(double a, double b, double t) {
        double diff = b - a;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI) diff -= Math.PI * 2;
        return a + diff * t;
    }

    // 内部パーティクルクラス
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