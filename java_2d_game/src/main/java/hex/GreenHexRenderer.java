package hex;

import java.awt.*;
import java.awt.geom.*;

/**
 * GreenHexRenderer
 *
 * <p>
 * タイトル画面や UI 上で描画する「緑の六角エンブレム（六芒星＋6ピース）」を描画するユーティリティクラスです。
 * このクラスはインスタンス化せずに静的メソッド {@link #drawGreenHex(Graphics2D, SmallHex[], boolean, double, double)}
 * を呼び出して使用します。
 * </p>
 *
 * <p>
 * 描画は「正規化座標系（中心が (0,0)、Y 軸が上向き）」を前提としています。呼び出し側（UI）は
 * {@code Graphics2D} に対して適切な {@code translate} / {@code scale(..., -...)} を行ってから本メソッドを呼んでください。
 * </p>
 *
 * <p><b>主な機能</b></p>
 * <ul>
 *   <li>中央のグロー（RadialGradient）</li>
 *   <li>6つのピース（三角形）を描画</li>
 *   <li>六角フレームと六芒星（2つの三角形）を描画</li>
 *   <li>周囲の小さな光点（ルーン風）を描画</li>
 * </ul>
 *
 * @see #drawGreenHex(Graphics2D, SmallHex[], boolean, double, double)
 */

public final class GreenHexRenderer {
    private GreenHexRenderer() {
    }

    /**
     * 緑の六角エンブレムを描画します。
     *
     * <p>
     * 呼び出し側は {@code Graphics2D} の座標系をエンブレム表示用に整えておく必要があります（例：中心へ translate、Y 反転 scale）。
     * {@code tris} 配列には各ピースの現在の位置・角度・サイズを表す {@link SmallHex} オブジェクトを渡してください。
     * </p>
     *
     * @param g2           描画先の {@link Graphics2D}。呼び出し側で適切に transform を設定しておくこと。
     * @param tris         描画するピース情報の配列。null の場合は何も描画しません。
     * @param forming      合体アニメーション中かどうか（true のときは合体進行に応じた描画効果を適用する場合があります）。
     * @param formTime     合体アニメーションの経過時間（秒）。合体演出の進行度計算に使用されます。
     * @param formDuration 合体アニメーションの総時間（秒）。{@code formTime/formDuration} で進行度を算出します。
     * @throws IllegalArgumentException 引数に不正な値（NaN や無限大）が含まれる場合は呼び出し側で検査してください。
     * @implNote <ul>
     * <li>メソッド内部で {@link Graphics2D#setTransform(AffineTransform)} 等を用いて一時的に transform を変更しますが、
     * 最後に元の状態へ復帰します。</li>
     * <li>描画は複数のパート（グロー、ピース、フレーム、六芒星、光点）に分かれています。</li>
     * </ul>
     */
    public static void drawGreenHex(Graphics2D g2, SmallHex[] tris, boolean forming, double formTime, double formDuration) {
        if (tris == null) return;

        // 保存
        AffineTransform old = g2.getTransform();
        Composite oldComp = g2.getComposite();
        Stroke oldStroke = g2.getStroke();
        Paint oldPaint = g2.getPaint();
        Color oldColor = g2.getColor();

        // 基本パラメータ（正規化）
        double hexRadius = 1.0; // UI 側の scale で見た目を調整
        // 背景の薄いグロー（中心）
        if (forming && formTime >= formDuration) {
            float alpha = 0.6f;
            RadialGradientPaint rgp = new RadialGradientPaint(
                    new Point2D.Double(0, 0),
                    (float) (hexRadius * 1.6f),
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{new Color(200, 255, 220, Math.round(alpha * 255)), new Color(120, 255, 160, Math.round(alpha * 128)), new Color(0, 0, 0, 0)}
            );
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2.setPaint(rgp);
            g2.fill(new Ellipse2D.Double(-hexRadius * 1.6, -hexRadius * 1.6, hexRadius * 3.2, hexRadius * 3.2));
            g2.setComposite(AlphaComposite.SrcOver);
        }

        // draw small pieces (triangles) — each SmallHex is drawn as an isosceles triangle
        for (SmallHex s : tris) {
            AffineTransform t = g2.getTransform();
            g2.translate(s.x, s.y);
            g2.rotate(Math.toRadians(s.angle));
            double sz = s.size;
            Path2D tri = new Path2D.Double();
            tri.moveTo(0, sz);
            tri.lineTo(-sz * 0.7, -sz * 0.6);
            tri.lineTo(sz * 0.7, -sz * 0.6);
            tri.closePath();

            // fill with radial-ish gradient (center bright)
            Point2D center = new Point2D.Double(0, -sz * 0.1);
            float radius = (float) (sz * 1.2);
            Color cCenter = new Color(207, 255, 224); // highlight
            Color cEdge = new Color(26, 168, 75); // base
            RadialGradientPaint gp = new RadialGradientPaint(center, radius, new float[]{0f, 1f}, new Color[]{cCenter, cEdge});
            g2.setPaint(gp);
            g2.fill(tri);

            // stroke
            g2.setStroke(new BasicStroke((float) (0.02f * sz)));
            g2.setColor(new Color(0, 0, 0, 80));
            g2.draw(tri);

            g2.setTransform(t);
        }

        // draw hex frame
        Path2D hex = createRegularPolygonPath(6, hexRadius + 0.28, Math.toRadians(-30));
        g2.setStroke(new BasicStroke(0.04f));
        g2.setColor(new Color(107, 107, 107)); // frame dark
        g2.draw(hex);

        // inner hex fill (subtle)
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(26, 168, 75)); // GEM_BASE
        g2.fill(hex);
        g2.setComposite(AlphaComposite.SrcOver);

        // draw hexagram (two triangles)
        Path2D up = new Path2D.Double();
        up.moveTo(0, hexRadius * 0.9);
        up.lineTo(-hexRadius * 0.78, -hexRadius * 0.45);
        up.lineTo(hexRadius * 0.78, -hexRadius * 0.45);
        up.closePath();

        Path2D down = new Path2D.Double();
        down.moveTo(0, -hexRadius * 0.9);
        down.lineTo(-hexRadius * 0.78, hexRadius * 0.45);
        down.lineTo(hexRadius * 0.78, hexRadius * 0.45);
        down.closePath();

        float starAlpha = (float) Math.min(1.0, formDuration <= 0 ? 1.0 : formTime / formDuration);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, starAlpha * 0.95f));
        g2.setPaint(new GradientPaint(0f, (float) (-hexRadius * 0.2), new Color(207, 255, 224), 0f, (float) (hexRadius * 0.6), new Color(0, 42, 16)));
        g2.fill(up);
        g2.fill(down);
        g2.setComposite(AlphaComposite.SrcOver);

        // small rune-like glow dots around frame
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(-90 + i * 60);
            double rx = (hexRadius + 0.45) * Math.cos(a);
            double ry = (hexRadius + 0.45) * Math.sin(a);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f * starAlpha));
            g2.setColor(new Color(40, 220, 120, Math.round(180 * starAlpha)));
            g2.fill(new Ellipse2D.Double(rx - 0.03, ry - 0.03, 0.06, 0.06));
            g2.setComposite(AlphaComposite.SrcOver);
        }

        // restore
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
        g2.setComposite(oldComp);
        g2.setTransform(old);
        g2.setColor(oldColor);
    }

    /**
     * 正多角形のパスを作成。
     *
     * <p>
     * 指定した辺数・半径・回転角に基づいて {@link Path2D} を生成します。
     * 最初の頂点は必ず {@code moveTo} で設定されるため、空のパスで {@code closePath()} が呼ばれて
     * {@link java.awt.geom.IllegalPathStateException} が発生することはありません。
     * </p>
     *
     * @param sides  多角形の辺数（3 以上を想定）
     * @param radius 多角形の外接半径（正の有限値）
     * @param rotRad 回転角（ラジアン）
     * @return 指定条件の {@link Path2D}。引数が不正な場合は空のパスを返す。
     */

    private static Path2D createRegularPolygonPath(int sides, double radius, double rotRad) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < sides; i++) {
            double angle = rotRad + i * (2.0 * Math.PI / sides);
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.closePath();
        return path;
    }
}