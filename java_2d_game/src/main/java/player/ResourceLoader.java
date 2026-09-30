package player;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public final class ResourceLoader {
    private ResourceLoader() {
    }

    /**
     * 足元エフェクト用スプライトを読み込んで配列で返す
     * リソースパスはプロジェクト構成に合わせて変更してください
     */

    public static BufferedImage[] loadFootFrames() {
        String[] paths = {
                "player/water-foot-fx_1.png",
                "player/water-foot-fx_2.png",
                "player/water-foot-fx_3.png",
                "player/water-foot-fx_4.png"
        };

        BufferedImage[] frames = new BufferedImage[paths.length];
        for (int i = 0; i < paths.length; i++) {
            try (InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream(paths[i])) {
                if (is == null) {
                    System.err.println("Resource not found: " + paths[i]);
                    frames[i] = null;
                } else {
                    frames[i] = ImageIO.read(is);
                }
            } catch (IOException e) {
                System.err.println("Failed to load image: " + paths[i] + " -> " + e.getMessage());
                frames[i] = null;
            }
        }
        return frames;
    }
}