package entity.particle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public final class SpriteUtil {

    private SpriteUtil() {
    }

    // キャッシュ：同じ BufferedImage に対して何度も走査しない
    private static final Map<BufferedImage, List<Point>> whiteCentersCache = new WeakHashMap<>();

    // スプライトから白ピクセル中心を取得（キャッシュあり）
    public static List<Point> findWhitePixelCentersCached(BufferedImage sprite) {
        if (sprite == null) return Collections.emptyList();
        List<Point> cached = whiteCentersCache.get(sprite);
        if (cached != null) return cached;
        List<Point> centers = findWhitePixelCenters(sprite);
        whiteCentersCache.put(sprite, centers);
        return centers;
    }

    // 実際の走査（閾値は必要に応じて調整）
    public static List<Point> findWhitePixelCenters(BufferedImage sprite) {
        List<Point> centers = new ArrayList<>();
        int w = sprite.getWidth(), h = sprite.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = sprite.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                if (a > 0 && r > 230 && g > 230 && b > 230) {
                    centers.add(new Point(x, y));
                }
            }
        }
        return centers;
    }
}