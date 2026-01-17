package object;

import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ObjAura extends Entity {

    private GameWindow gameWindow;
    private static final String[] DIRECTIONS = {"Up", "Down", "Left", "Right"};
    private static final int FRAMES_PER_DIRECTION = 3;

    // アニメ制御
    private long elapsedMilliseconds = 0L;
    private int currentFrameIndexOneBased = 1;
    private final int totalFrames = FRAMES_PER_DIRECTION;

    // 1フレームを1.5秒にする（0.66fps）
    private final long FRAME_INTERVAL_MS = 1500L;

    public ObjAura(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setName("オーラ");

        // sprites 配列を確実に初期化（Entity 側で未初期化ならここで）
        sprites = new BufferedImage[DIRECTIONS.length][FRAMES_PER_DIRECTION];

        // 画像はコンストラクタで一度だけ読み込む
        loadSpriteFrames();
    }

    // コンストラクタで一度だけ呼ぶ（名前を分かりやすく）
    private void loadSpriteFrames() {
        int tileSize = FrameApp.getTileSize();
        for (int dirIndex = 0; dirIndex < DIRECTIONS.length; dirIndex++) {
            for (int frameIndex = 0; frameIndex < FRAMES_PER_DIRECTION; frameIndex++) {
                String path = String.format("player/aura-image%s-%d.gif", DIRECTIONS[dirIndex], frameIndex + 1);
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                    if (is == null) {
                        System.err.println("ObjAura: resource not found -> " + path);
                        sprites[dirIndex][frameIndex] = null;
                        continue;
                    }
                    // 読み込み直後に黒を透過にする処理を行う
                    BufferedImage originalImage = ImageIO.read(is);
                    if (originalImage == null) {
                        System.err.println("ObjAura: ImageIO.read returned null -> " + path);
                        sprites[dirIndex][frameIndex] = null;
                        continue;
                    }

                    // 透過処理のために ARGB 形式のバッファを作る
                    BufferedImage resizedImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = resizedImage.createGraphics();
                    // 最近傍補間（ピクセルアート向け）
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    // 元画像をリサイズして resizedImage に描画
                    g.drawImage(originalImage, 0, 0, tileSize, tileSize, null);
                    g.dispose();

                    // 黒を透過にする（閾値で近似黒も透過可能）
                    int tolerance = 16; // 0 = 完全な黒のみ、数値を上げると近似黒も透過
                    int w = resizedImage.getWidth();
                    int h = resizedImage.getHeight();
                    int[] pixels = new int[w * h];
                    resizedImage.getRGB(0, 0, w, h, pixels, 0, w);

                    for (int pixelIndex = 0; pixelIndex < pixels.length; pixelIndex++) {
                        int argb = pixels[pixelIndex];
                        int red = (argb >> 16) & 0xFF;
                        int green = (argb >> 8) & 0xFF;
                        int blue = argb & 0xFF;
                        // 完全な黒または近似黒を透過にする判定
                        if (red <= tolerance && green <= tolerance && blue <= tolerance) {
                            // alpha を 0 にする（上位バイトを 0 にする）
                            pixels[pixelIndex] = (argb & 0x00FFFFFF);
                        } else {
                            // そのまま（保持）
                            pixels[pixelIndex] = argb;
                        }
                    }
                    resizedImage.setRGB(0, 0, w, h, pixels, 0, w);

                    // 最終的に sprites に格納
                    sprites[dirIndex][frameIndex] = resizedImage;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 現在のフレーム画像を返す
     * ここでは向きに依存しないオーラを想定（必要なら向きに応じた index を渡すよう拡張）
     */

    public BufferedImage getAnimationFrame() {
        // Down を使う（必要ならプレイヤーの向きに合わせて index を変える）
        int useDirectionIndex = 1; // 0:Up, 1:Down, 2:Left, 3:Right
        // currentFrameIndexOneBased は 1..totalFrames なので -1 してアクセス
        int frameIndex = Math.max(0, Math.min(currentFrameIndexOneBased - 1, FRAMES_PER_DIRECTION - 1));
        if (sprites == null) return null;
        return sprites[useDirectionIndex][frameIndex];
    }

    /**
     * アニメ更新（deltaMs はミリ秒）
     */

    public void updateAnimation(long deltaMs) {
        elapsedMilliseconds += deltaMs;
        while (elapsedMilliseconds >= FRAME_INTERVAL_MS) {
            elapsedMilliseconds -= FRAME_INTERVAL_MS;
            currentFrameIndexOneBased = currentFrameIndexOneBased % totalFrames + 1;
        }
    }

    /**
     * 指定スクリーン座標に拡大して描画する
     */

    public void drawAt(Graphics2D g2, int screenX, int screenY, float scale, float alpha) {

        BufferedImage frame = getAnimationFrame();
        // フォールバック: frame が null なら透明画像を作る（描画で落ちないように）
        if (frame == null) {
            int ts = FrameApp.getTileSize();
            frame = new BufferedImage(ts, ts, BufferedImage.TYPE_INT_ARGB);
        }

        // 現在の合成とレンダリングヒントを丸ごと保存
        Composite originalComposite = g2.getComposite();
        RenderingHints savedHints = (RenderingHints) g2.getRenderingHints().clone();

        try {
            // 合成と補間を設定
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            int baseSize = FrameApp.getTileSize();
            int drawWidth = Math.round(baseSize * scale);
            int drawHeight = Math.round(baseSize * scale);

            // 中心合わせ
            int drawX = screenX + (baseSize / 2) - (drawWidth / 2);
            int drawY = screenY + (baseSize / 2) - (drawHeight / 2);

            g2.drawImage(frame, drawX, drawY, drawWidth, drawHeight, null);
        } finally {
            // 復帰（丸ごと戻すので null チェック不要）
            g2.setRenderingHints(savedHints);
            g2.setComposite(originalComposite);
        }
    }
}