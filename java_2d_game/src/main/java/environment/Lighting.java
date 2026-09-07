package environment;

import frame.FrameApp;
import window.GameWindow;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
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
    private boolean lastHasLantern = false;
    private int lastLanternRadius = -1;
    private float lastLanternIntensity = -1f;

    public Lighting(GameWindow gameWindow, int circleSize) {
        this.gameWindow = gameWindow;
        createFilter(circleSize, 1.0f, false, 0, 0f);
    }

    public void updateAndMaybeRecreate(int circleSize, float brightness,
                                       boolean hasLantern, int lanternRadius, float lanternIntensity) {
        if (darknessFilter == null
                || lastCircleSize != circleSize
                || lastWidth != FrameApp.getScreenWidth()
                || lastHeight != FrameApp.getScreenHeight()
                || Math.abs(lastBrightness - brightness) > 0.02f
                || playerMoved()
                || lastHasLantern != hasLantern
                || lastLanternRadius != lanternRadius
                || Math.abs(lastLanternIntensity - lanternIntensity) > 0.01f) {
            createFilter(circleSize, brightness, hasLantern, lanternRadius, lanternIntensity);
        }
    }

    private void createFilter(int circleSize, float brightness,
                              boolean hasLantern, int lanternRadius, float lanternIntensity) {

        int w = FrameApp.getScreenWidth();
        int h = FrameApp.getScreenHeight();
        if (w <= 0 || h <= 0) return;

        darknessFilter = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = darknessFilter.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            float maxAlpha = 0.95f;
            float baseAlpha = maxAlpha * (1.0f - brightness);

            // 全面を塗る（暗さ）
            g2.setComposite(AlphaComposite.Src);
            g2.setColor(new Color(0, 0, 0, baseAlpha));
            g2.fillRect(0, 0, w, h);

            // プレイヤー中心（スクリーン座標）
            int tileSize = FrameApp.getTileSize();
            double centerX = gameWindow.getPlayer().getScreenX() + tileSize / 2.0;
            double centerY = gameWindow.getPlayer().getScreenY() + tileSize / 2.0;

            if (hasLantern && lanternRadius > 0 && lanternIntensity > 0f) {
                // ランタン装備時：ソフトな明るさ（RadialGradientPaint + DstOut で切り抜く）
                float radius = lanternRadius;
                Point2D center = new Point2D.Float((float) centerX, (float) centerY);
                float[] dist = {0.0f, 0.6f, 1.0f};
                float outerAlpha = baseAlpha;
                float midAlpha = outerAlpha * (1.0f - lanternIntensity * 0.5f);
                Color[] colors = {
                        new Color(0, 0, 0, 0f),
                        new Color(0, 0, 0, midAlpha),
                        new Color(0, 0, 0, outerAlpha)
                };
                RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, colors);

                BufferedImage grad = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gg = grad.createGraphics();
                try {
                    gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gg.setPaint(rgp);
                    gg.fillRect(0, 0, w, h);
                } finally {
                    gg.dispose();
                }

                g2.setComposite(AlphaComposite.DstOut);
                g2.drawImage(grad, 0, 0, null);

                // 中心は確実に視界を確保（小さな完全透明）
                g2.setComposite(AlphaComposite.Clear);
                g2.fill(new Ellipse2D.Double(centerX - circleSize / 2.0, centerY - circleSize / 2.0, circleSize, circleSize));
            } else {
                // ランタン未装備時：何もしない（＝プレイヤー中心も暗い）
            }

            // 更新履歴
            lastCenterX = (int) Math.round(centerX);
            lastCenterY = (int) Math.round(centerY);
            lastCircleSize = circleSize;
            lastWidth = w;
            lastHeight = h;
            lastBrightness = brightness;
            lastHasLantern = hasLantern;
            lastLanternRadius = lanternRadius;
            lastLanternIntensity = lanternIntensity;
        } finally {
            g2.dispose();
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