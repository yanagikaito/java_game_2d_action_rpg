package entity;

import java.awt.image.BufferedImage;

@FunctionalInterface
public interface ImageResizer {
    BufferedImage resize(BufferedImage src, int width, int height);
}
