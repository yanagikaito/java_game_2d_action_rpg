package object;

import frame.FrameApp;
import window.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SuperObject {

    private BufferedImage image;
    private BufferedImage image2;
    private BufferedImage image3;
    private String name;
    private boolean collision = false;
    private int worldX;
    private int worldY;
    private Rectangle solidArea;
    private int solidAreaDefaultX = 0;
    private int solidAreaDefaultY = 0;
    private int width;
    private int height;

    public void draw(Graphics2D g2, GameWindow gameWindow) {

        int screenX = worldX - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = worldY - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        if (worldX > gameWindow.getPlayer().getWorldX() - gameWindow.getPlayer().getScreenX() &&
                worldX < gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX() &&
                worldY > gameWindow.getPlayer().getWorldY() - gameWindow.getPlayer().getScreenY() &&
                worldY < gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY()) {

            g2.drawImage(image, screenX, screenY,
                    FrameApp.getTileSize(), FrameApp.getTileSize(), null);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public BufferedImage getImage2() {
        return image2;
    }

    public BufferedImage getImage3() {
        return image3;
    }

    public void setImage(BufferedImage image, int width, int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.width = width;
        this.height = height;
        Graphics2D graphics2D = this.image.createGraphics();
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
    }

    public void setImage2(BufferedImage image2, int width, int height) {
        this.image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.width = width;
        this.height = height;
        Graphics2D graphics2D = this.image2.createGraphics();
        graphics2D.drawImage(image2, 0, 0, width, height, null);
        graphics2D.dispose();
    }

    public void setImage3(BufferedImage image3, int width, int height) {
        this.image3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.width = width;
        this.height = height;
        Graphics2D graphics2D = this.image3.createGraphics();
        graphics2D.drawImage(image3, 0, 0, width, height, null);
        graphics2D.dispose();
    }
}