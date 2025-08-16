package model;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Emitter {
    private Point2D position;
    private Color startColor, endColor;
    private double emitRate;
    private double velMin, velMax;
    private double lifeMin, lifeMax;
    private float startSize, endSize;

    // internal state
    private final List<Particle> particles = new ArrayList<>();
    private double buffer = 0;
    private final Random rnd = new Random();

    /**
     * デフォルトコンストラクタ.
     * 位置は (0,0)、色は白→黒、発生率100、速度50–150、寿命1–3秒、
     * サイズ5→1を初期値とする
     */
    public Emitter() {
        this.position   = new Point2D.Double(0, 0);
        this.startColor = Color.WHITE;
        this.endColor   = Color.BLACK;
        this.emitRate   = 100;
        this.velMin     = 50;
        this.velMax     = 150;
        this.lifeMin    = 1;
        this.lifeMax    = 3;
        this.startSize  = 5f;
        this.endSize    = 1f;
    }

    /**
     * コピー用コンストラクタ.
     *
     * @param x          初期X座標
     * @param y          初期Y座標
     * @param startColor 開始色
     * @param endColor   終了色
     * @param emitRate   発生レート (particles/sec)
     * @param velMin     最小速度
     * @param velMax     最大速度
     * @param lifeMin    最小寿命 (秒)
     * @param lifeMax    最大寿命 (秒)
     * @param startSize  開始サイズ
     * @param endSize    終了サイズ
     */
    public Emitter(double x,
                   double y,
                   Color startColor,
                   Color endColor,
                   double emitRate,
                   double velMin,
                   double velMax,
                   double lifeMin,
                   double lifeMax,
                   float startSize,
                   float endSize) {
        this.position   = new Point2D.Double(x, y);
        this.startColor = startColor;
        this.endColor   = endColor;
        this.emitRate   = emitRate;
        this.velMin     = velMin;
        this.velMax     = velMax;
        this.lifeMin    = lifeMin;
        this.lifeMax    = lifeMax;
        this.startSize  = startSize;
        this.endSize    = endSize;
    }

    /**
     * 通常のフレーム更新:
     * 1. バッファリング発生
     * 2. 既存粒子の移動＋重力適用
     * 3. 寿命切れ粒子の除去
     *
     * @param dt      前フレームからの経過秒数
     * @param gravity 重力ベクトル (px/sec^2)
     */
    public void update(double dt, Point2D gravity) {
        // emitRate に従いバッファ→整数部を発生数に
        buffer += emitRate * dt;
        int toEmit = (int) buffer;
        buffer -= toEmit;
        for (int i = 0; i < toEmit; i++) {
            particles.add(createParticle());
        }

        // 各粒子を更新、死んだらIteratorで除去
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update(dt, gravity);
            if (!p.isAlive()) {
                it.remove();
            }
        }
    }

    /**
     * マウスドラッグなどの即時発射.
     *
     * @param count 一度に発生させる粒子数
     */
    public void emitNow(int count) {
        for (int i = 0; i < count; i++) {
            particles.add(createParticle());
        }
    }

    /** 粒子生成の共通ロジック */
    private Particle createParticle() {
        Point2D posCopy = new Point2D.Double(
                position.getX(), position.getY()
        );
        return new Particle(
                posCopy,
                randomVelocity(),
                randomLifetime(),
                startColor,
                endColor,
                startSize,
                endSize
        );
    }

    /** 速度ベクトルをランダム生成 */
    private Point2D randomVelocity() {
        double speed = velMin + rnd.nextDouble() * (velMax - velMin);
        double angle = rnd.nextDouble() * Math.PI * 2;
        return new Point2D.Double(
                Math.cos(angle) * speed,
                Math.sin(angle) * speed
        );
    }

    /** 寿命をランダム生成 */
    private double randomLifetime() {
        return lifeMin + rnd.nextDouble() * (lifeMax - lifeMin);
    }

    // === Getters & Setters ===

    public Point2D getPosition() {
        return position;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public Color getStartColor() {
        return startColor;
    }

    public void setStartColor(Color startColor) {
        this.startColor = startColor;
    }

    public Color getEndColor() {
        return endColor;
    }

    public void setEndColor(Color endColor) {
        this.endColor = endColor;
    }

    public double getEmitRate() {
        return emitRate;
    }

    public void setEmitRate(double emitRate) {
        this.emitRate = emitRate;
    }

    public double getVelMin() {
        return velMin;
    }

    public void setVelMin(double velMin) {
        this.velMin = velMin;
    }

    public double getVelMax() {
        return velMax;
    }

    public void setVelMax(double velMax) {
        this.velMax = velMax;
    }

    public double getLifeMin() {
        return lifeMin;
    }

    public void setLifeMin(double lifeMin) {
        this.lifeMin = lifeMin;
    }

    public double getLifeMax() {
        return lifeMax;
    }

    public void setLifeMax(double lifeMax) {
        this.lifeMax = lifeMax;
    }

    public float getStartSize() {
        return startSize;
    }

    public void setStartSize(float startSize) {
        this.startSize = startSize;
    }

    public float getEndSize() {
        return endSize;
    }

    public void setEndSize(float endSize) {
        this.endSize = endSize;
    }

    /**
     * 描画・外部参照用に読み取り専用リストを返す
     */
    public List<Particle> getParticles() {
        return Collections.unmodifiableList(particles);
    }

    /** 全粒子とバッファをクリア */
    public void clearParticles() {
        particles.clear();
        buffer = 0;
    }
}