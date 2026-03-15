package object;

import java.awt.*;
import java.awt.image.*;

/**
 * スプライト画像から「グロー（発光）画像」を生成するユーティリティクラス。
 *
 * <p>処理の流れ:
 * <ol>
 *   <li>ソーススプライトの白（またはほぼ白）ピクセルをマスクとして抽出する。</li>
 *   <li>抽出したマスクを指定した発光色で塗る。</li>
 *   <li>小さなボックスブラーを複数回適用してソフトな発光（グロー）を近似する。</li>
 * </ol>
 *
 * <p>使用例:
 * <pre>
 * BufferedImage sword = ...; // スプライト
 * Color glowColor = new Color(255, 240, 200); // 暖色のグロー
 * BufferedImage glow = SpriteGlowUtil.createGlowFromSprite(sword, glowColor, 6);
 * // 描画時に剣の上または下に重ねて使う
 * </pre>
 *
 * <p>注意:
 * <ul>
 *   <li>この実装は軽量で高速化を優先しています。より高品質なブラーが必要ならガウスブラー等を検討してください。</li>
 *   <li>「白」の判定閾値は現在 RGB &gt; 230 にしています。スプライトの色味に応じて調整してください。</li>
 *   <li>同じスプライトに対して毎フレーム再生成すると重いので、生成結果はキャッシュして使うことを推奨します。</li>
 * </ul>
 */

public final class SpriteGlowUtil {

    private SpriteGlowUtil() {
    }

    /**
     * スプライトの白い部分からグロー画像を作成します。
     *
     * <p>戻り値は {@code src} と同じ幅・高さの {@link BufferedImage} で、
     * 白ピクセル領域が指定色で塗られ、ブラーが適用された画像になります。
     * グロー画像は透明部分を含むため、描画時に {@link Graphics2D#drawImage(BufferedImage, int, int, java.awt.image.ImageObserver)}
     * で重ねるのに適している。
     *
     * @param src        元のスプライト画像（null不可）
     * @param glowColor  グローに使う色（RGB成分を使用、アルファはマスクで不透明に設定）
     * @param blurRadius ブラーの強さの目安。値が大きいほど広がりのあるソフトなグローになる。
     *                   内部では小さなボックスブラーを複数回適用して近似する。
     * @return 同サイズのブラー済みグロー画像（グローがない部分は透明）
     * @throws NullPointerException {@code src} または {@code glowColor} が null の場合
     */

    public static BufferedImage createGlowFromSprite(BufferedImage src, Color glowColor, int blurRadius) {
        if (src == null) throw new NullPointerException("src must not be null");
        if (glowColor == null) throw new NullPointerException("glowColor must not be null");

        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // マスク作成: ほぼ白のピクセルを glowColor で不透明に塗る
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                // 白判定の閾値（必要に応じて調整）
                if (a > 0 && r > 230 && g > 230 && b > 230) {
                    int colorRgb = (0xff << 24) | (glowColor.getRed() << 16) | (glowColor.getGreen() << 8) | glowColor.getBlue();
                    mask.setRGB(x, y, colorRgb);
                } else {
                    mask.setRGB(x, y, 0x00000000);
                }
            }
        }

        // 軽量ブラーを繰り返してソフトなグローを作る
        BufferedImage blurred = mask;
        for (int i = 0; i < Math.max(1, blurRadius / 2); i++) {
            blurred = boxBlur(blurred);
        }
        return blurred;
    }

    /**
     * 簡易な 3x3 ボックスブラー（横方向パスと縦方向パスの分離）を適用。
     *
     * <p>内部で {@link ConvolveOp} を使い、画像サイズとアルファを保持したままブラーを行う。
     *
     * @param src ブラーを適用する ARGB 画像（null不可）
     * @return ブラー後の新しい {@link BufferedImage}
     * @throws NullPointerException {@code src} が null の場合
     */

    private static BufferedImage boxBlur(BufferedImage src) {
        if (src == null) throw new NullPointerException("src must not be null");

        float[] kernel = new float[3];
        kernel[0] = kernel[1] = kernel[2] = 1f / 3f;

        // 横方向パス
        Kernel kH = new Kernel(3, 1, kernel);
        ConvolveOp opH = new ConvolveOp(kH, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage tmp = opH.filter(src, null);

        // 縦方向パス
        Kernel kV = new Kernel(1, 3, kernel);
        ConvolveOp opV = new ConvolveOp(kV, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage dst = opV.filter(tmp, null);

        return dst;
    }
}