package environment;

import frame.FrameApp;
import window.GameWindow;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class Lighting {

    private final GameWindow gameWindow;
    private BufferedImage darknessFilter;
    private int lastCenterX = Integer.MIN_VALUE;
    private int lastCenterY = Integer.MIN_VALUE;
    private int lastCircleSize = -1;
    private int lastWidth = -1;
    private int lastHeight = -1;
    private float lastBrightness = -1f;

    public Lighting(GameWindow gameWindow, int circleSize) {
        this.gameWindow = gameWindow;
        // 初期生成（brightness は 1.0 = 明るめに仮設定）
        createFilter(circleSize, 1.0f);
    }

    /**
     * フィルタを生成／再生成する。
     * brightness: 0.0 (dark) 〜 1.0 (bright) の値を受け取り、暗転の強さに反映する。
     */
    private void createFilter(int circleSize, float brightness) {
        int w = FrameApp.getScreenWidth();
        int h = FrameApp.getScreenHeight();
        if (w <= 0 || h <= 0) return;

        darknessFilter = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = darknessFilter.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // brightness を暗さに変換（例: brightness=1 -> alpha 0.0, brightness=0 -> alpha 0.95）
            // 調整しやすいようにマッピング
            float maxAlpha = 0.95f; // 完全暗転時のアルファ
            float alpha = maxAlpha * (1.0f - brightness);

            // 全画面を半透明黒で塗る（AlphaComposite.Src）
            g2.setComposite(AlphaComposite.Src);
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRect(0, 0, w, h);

            // プレイヤー中心を計算
            int tileSize = FrameApp.getTileSize();
            double centerX = gameWindow.getPlayer().getScreenX() + tileSize / 2.0;
            double centerY = gameWindow.getPlayer().getScreenY() + tileSize / 2.0;

            double x = centerX - circleSize / 2.0;
            double y = centerY - circleSize / 2.0;

            // 透明にくり抜く（AlphaComposite.Clear）
            g2.setComposite(AlphaComposite.Clear);
            g2.fill(new Ellipse2D.Double(x, y, circleSize, circleSize));

            // 必要ならソフトエッジを追加（例: 半透明の縁）
            // g2.setComposite(AlphaComposite.SrcOver);
            // g2.setColor(new Color(0,0,0,alpha * 0.6f));
            // g2.fill(new Ellipse2D.Double(x - 12, y - 12, circleSize + 24, circleSize + 24));

            // 更新履歴
            lastCenterX = (int) Math.round(centerX);
            lastCenterY = (int) Math.round(centerY);
            lastCircleSize = circleSize;
            lastWidth = w;
            lastHeight = h;
            lastBrightness = brightness;
        } finally {
            g2.dispose();
        }
    }

    /**
     * 毎フレーム呼ぶ。必要ならフィルタを再生成する。
     * deltaBrightnessThreshold は小さな変化で毎フレーム再生成しないための閾値。
     */

    public void updateAndMaybeRecreate(int circleSize, float brightness) {
        if (darknessFilter == null
                || lastCircleSize != circleSize
                || lastWidth != FrameApp.getScreenWidth()
                || lastHeight != FrameApp.getScreenHeight()
                || Math.abs(lastBrightness - brightness) > 0.02f
                || playerMoved()) {
            createFilter(circleSize, brightness);
        }
    }

    private boolean playerMoved() {
        int tileSize = FrameApp.getTileSize();
        int centerX = (int) Math.round(gameWindow.getPlayer().getScreenX() + tileSize / 2.0);
        int centerY = (int) Math.round(gameWindow.getPlayer().getScreenY() + tileSize / 2.0);
        return centerX != lastCenterX || centerY != lastCenterY;
    }

    public void draw(Graphics2D g2) {
        if (darknessFilter != null) {
            g2.drawImage(darknessFilter, 0, 0, null);
        }
    }
}