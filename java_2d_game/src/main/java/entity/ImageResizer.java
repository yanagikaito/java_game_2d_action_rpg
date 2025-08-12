package entity;

import java.awt.image.BufferedImage;

@FunctionalInterface
public interface ImageResizer {
    /**
     * 画像をリサイズして返します。
     *
     * @param src    リサイズ対象の BufferedImage（null 不可）
     * @param width  リサイズ後の幅（ピクセル）
     * @param height リサイズ後の高さ（ピクセル）
     * @return リサイズ済みの BufferedImage
     * @throws NullPointerException src が null の場合
     */
    BufferedImage resize(BufferedImage src, int width, int height);
}
