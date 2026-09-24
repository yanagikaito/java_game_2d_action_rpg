package player;

import frame.FrameApp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpriteManager {

    // classpath 上のディレクトリ（resources 内）
    private static final String RESOURCE_DIR = "/player/";
    private final Map<String, BufferedImage> cache = new ConcurrentHashMap<>();

    /**
     * key/facing/frame は将来の拡張用に受け取るが、
     * 現状は固定ファイル "player/image-down-2.gif" を読み込み、
     * タイルサイズに合わせてリサイズして返す実装。
     */
    public BufferedImage getSprite(String key, String facing, int frame) {
        // キャッシュキーは key/facing/frame を組み合わせる
        String cacheKey = (key == null ? "entity_default" : key)
                + "_" + (facing == null ? "down" : facing)
                + "_" + frame;

        return cache.computeIfAbsent(cacheKey, k -> {

            String fileName = "image-down-2.gif";
            String resourcePath = RESOURCE_DIR + fileName;
            System.out.println("SpriteManager: loading resource: " + resourcePath);

            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    System.err.println("SpriteManager: resource not found: " + resourcePath);
                    return null;
                }
                BufferedImage raw = ImageIO.read(is);
                if (raw == null) {
                    System.err.println("SpriteManager: ImageIO.read returned null for " + resourcePath);
                    return null;
                }
                // タイルサイズに合わせてリサイズして返す
                int tile = FrameApp.getTileSize();
                return resizeToTile(raw, tile, tile);
            } catch (Exception e) {
                System.err.println("SpriteManager: failed to load " + resourcePath);
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * 画像を指定幅高さに高品質でリサイズするユーティリティ
     */

    private static BufferedImage resizeToTile(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dst.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(src, 0, 0, w, h, null);
        } finally {
            g2.dispose();
        }
        return dst;
    }

    /**
     * 指定ベース名の連番スプライトを読み込み、タイルサイズにリサイズして返す。
     * 例: resourceBaseName="death_" count=9 -> "/player/death_0.png" ... "/player/death_8.png"
     */

    public BufferedImage[] getAnimationFrames(String resourceBaseName, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            String cacheKey = "anim_" + resourceBaseName + i;
            // 既にキャッシュにあれば使う
            BufferedImage cached = cache.get(cacheKey);
            if (cached != null) {
                frames[i] = cached;
                continue;
            }

            String fileName = resourceBaseName + i + ".png";
            String resourcePath = RESOURCE_DIR + fileName;
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    System.err.println("SpriteManager: resource not found: " + resourcePath);
                    frames[i] = null;
                    continue;
                }
                BufferedImage raw = ImageIO.read(is);
                if (raw == null) {
                    System.err.println("SpriteManager: ImageIO.read returned null for " + resourcePath);
                    frames[i] = null;
                    continue;
                }
                int tile = FrameApp.getTileSize();
                BufferedImage resized = resizeToTile(raw, tile, tile);
                cache.put(cacheKey, resized);
                frames[i] = resized;
            } catch (Exception e) {
                System.err.println("SpriteManager: failed to load " + resourcePath);
                e.printStackTrace();
                frames[i] = null;
            }
        }
        return frames;
    }
}