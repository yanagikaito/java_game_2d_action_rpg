package environment;

import frame.FrameApp;
import window.GameWindow;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class Lighting {

    GameWindow gameWindow;
    BufferedImage darknessFilter;

    public Lighting(GameWindow gameWindow, int circleSize) {

        darknessFilter = new BufferedImage
                (FrameApp.getScreenWidth(), FrameApp.getScreenHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) darknessFilter.getGraphics();

        // 全画面を半透明黒で塗る
        g2.setComposite(AlphaComposite.Src);
        g2.setColor(new Color(0, 0, 0, 0.95f));
        g2.fillRect(0, 0, darknessFilter.getWidth(), darknessFilter.getHeight());

        // プレイヤー中心を計算
        int tileSize = FrameApp.getTileSize();
        double centerX = gameWindow.getPlayer().getScreenX() + tileSize / 2.0;
        double centerY = gameWindow.getPlayer().getScreenY() + tileSize / 2.0;

        double x = centerX - circleSize / 2.0;
        double y = centerY - circleSize / 2.0;

        // 透明にくり抜く（AlphaComposite.Clear）
        g2.setComposite(AlphaComposite.Clear);
        g2.fill(new Ellipse2D.Double(x, y, circleSize, circleSize));

        g2.dispose();
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(darknessFilter, 0, 0, null);
    }
}