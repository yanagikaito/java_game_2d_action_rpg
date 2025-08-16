// model/Particle.java
package model;

import java.awt.*;
import java.awt.geom.Point2D;

public class Particle {
    private final double initialLife;
    private double life;
    private double x, y;
    private final Point2D velocity;
    private final Color startColor, endColor;
    private final float startSize, endSize;

    /**
     * コンストラクタ.
     *
     * @param position   初期位置
     * @param velocity   初速度ベクトル
     * @param life       初期寿命（秒）
     * @param startColor 寿命開始時の色
     * @param endColor   寿命終了時の色
     * @param startSize  寿命開始時のサイズ
     * @param endSize    寿命終了時のサイズ
     */
    public Particle(Point2D position,
                    Point2D velocity,
                    double life,
                    Color startColor,
                    Color endColor,
                    float startSize,
                    float endSize) {
        this.x = position.getX();
        this.y = position.getY();
        this.velocity = new Point2D.Double(
                velocity.getX(), velocity.getY()
        );
        this.initialLife = life;
        this.life = life;
        this.startColor = startColor;
        this.endColor = endColor;
        this.startSize = startSize;
        this.endSize = endSize;
    }

    /**
     * フレームごとの状態更新.
     *
     * @param dt      前フレームからの経過秒数
     * @param gravity 重力ベクトル（px/sec^2）
     */
    public void update(double dt, Point2D gravity) {
        // 重力を速度に加算
        velocity.setLocation(
                velocity.getX() + gravity.getX() * dt,
                velocity.getY() + gravity.getY() * dt
        );
        // 位置を更新
        x += velocity.getX() * dt;
        y += velocity.getY() * dt;
        // 寿命を減らす
        life -= dt;
    }

    /**
     * 粒子がまだ生存中か判定.
     */
    public boolean isAlive() {
        return life > 0;
    }

    /**
     * 粒子の描画.
     *
     * @param g Graphics2D コンテキスト
     */
    public void draw(Graphics2D g) {
        // 生存率 0→1 の逆 t: 1→0
        float t = (float) (life / initialLife);
        t = Math.max(0f, Math.min(1f, t));

        // 色を線形補間
        int r = (int) (startColor.getRed() * t + endColor.getRed() * (1 - t));
        int gg = (int) (startColor.getGreen() * t + endColor.getGreen() * (1 - t));
        int b = (int) (startColor.getBlue() * t + endColor.getBlue() * (1 - t));
        int alpha = (int) (255 * t);

        g.setColor(new Color(r, gg, b, alpha));

        // サイズを線形補間
        int size = (int) (startSize * t + endSize * (1 - t));

        // 中心揃えで描画
        g.fillOval((int) (x - size / 2), (int) (y - size / 2), size, size);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLife() {
        return life;
    }

    public double getInitialLife() {
        return initialLife;
    }

    public Point2D getVelocity() {
        return (Point2D) velocity.clone();
    }
}