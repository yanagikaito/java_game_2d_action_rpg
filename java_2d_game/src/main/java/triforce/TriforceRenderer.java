package triforce;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

/**
 * トライフォース描画ユーティリティ。
 * <p>
 * drawTriforce は g2 が既に正規化座標系（中心原点、Y反転、適切な scale）に変換済みであることを前提に描画。
 * 各種描画パラメータはクラス定数として定義してあり、見た目の調整は定数を書き換えるだけで行う。
 * </p>
 */

public final class TriforceRenderer {

    // 三角形形状係数
    private static final double TRI_TOP_Y = 0.0;           // 三角形の頂点 X（基準）
    private static final double TRI_TOP_OFFSET_Y = 0.8;    // 大トライフォース上部頂点の y（正規化）
    private static final double TRI_MID_X = 0.4;           // 大トライフォース中間頂点の x（正規化）
    private static final double TRI_MID_Y = 0.0;           // 大トライフォース中間頂点の y（正規化）
    private static final double TRI_BOTTOM_X = 0.8;        // 大トライフォース底辺の x（正規化）
    private static final double TRI_BOTTOM_Y = -0.8;       // 大トライフォース底辺の y（正規化）

    // 小三角形の頂点比率（size に対する頂点オフセット比）
    private static final double SMALL_TRI_HALF_WIDTH_FACTOR = 0.8;  // 横幅係数
    private static final double SMALL_TRI_HALF_HEIGHT_FACTOR = 0.9; // 底辺の高さ係数

    // 描画色（fill / stroke）およびアルファ
    private static final Color SMALL_TRI_FILL = new Color(1.0f, 0.95f, 0.2f);
    private static final Color SMALL_TRI_STROKE = new Color(0.5f, 0.35f, 0.0f);
    private static final float SMALL_TRI_STROKE_WIDTH = 0.006f;

    private static final Color BIG_TRIFORCE_FILL = new Color(1.0f, 0.9f, 0.2f, 0.25f);

    private TriforceRenderer() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    /**
     * トライフォース（小三角形3つ＋合体後の薄い大トライフォース）を描画。
     *
     * @param g2           正規化座標系に変換済みの Graphics2D
     * @param tris         描画対象の SmallTri 配列（3 要素を想定）
     * @param forming      合体フェーズ中かどうか
     * @param formTime     合体フェーズの経過時間（秒）
     * @param formDuration 合体にかける総時間（秒）
     */

    public static void drawTriforce(Graphics2D g2, SmallTri[] tris, boolean forming, double formTime, double formDuration) {
        // 小三角形を描画
        for (SmallTri t : tris) {
            AffineTransform old = g2.getTransform();
            try {
                g2.translate(t.x, t.y);
                g2.rotate(Math.toRadians(t.angle));
                double s = t.size;

                Path2D tri = new Path2D.Double();
                tri.moveTo(0.0, s);
                tri.lineTo(-s * SMALL_TRI_HALF_WIDTH_FACTOR, -s * SMALL_TRI_HALF_HEIGHT_FACTOR);
                tri.lineTo(s * SMALL_TRI_HALF_WIDTH_FACTOR, -s * SMALL_TRI_HALF_HEIGHT_FACTOR);
                tri.closePath();

                g2.setColor(SMALL_TRI_FILL);
                g2.fill(tri);

                g2.setColor(SMALL_TRI_STROKE);
                g2.setStroke(new BasicStroke(SMALL_TRI_STROKE_WIDTH));
                g2.draw(tri);
            } finally {
                g2.setTransform(old);
            }
        }

        // 合体完了後に薄い大トライフォースを描画（オプション）
        if (forming && formTime >= formDuration) {
            g2.setColor(BIG_TRIFORCE_FILL);

            Path2D up = new Path2D.Double();
            up.moveTo(TRI_TOP_Y, TRI_TOP_OFFSET_Y);
            up.lineTo(-TRI_MID_X, TRI_MID_Y);
            up.lineTo(TRI_MID_X, TRI_MID_Y);
            up.closePath();
            g2.fill(up);

            Path2D left = new Path2D.Double();
            left.moveTo(-TRI_MID_X, TRI_MID_Y);
            left.lineTo(-TRI_BOTTOM_X, TRI_BOTTOM_Y);
            left.lineTo(0.0, TRI_BOTTOM_Y);
            left.closePath();
            g2.fill(left);

            Path2D right = new Path2D.Double();
            right.moveTo(TRI_MID_X, TRI_MID_Y);
            right.lineTo(0.0, TRI_BOTTOM_Y);
            right.lineTo(TRI_BOTTOM_X, TRI_BOTTOM_Y);
            right.closePath();
            g2.fill(right);
        }
    }
}