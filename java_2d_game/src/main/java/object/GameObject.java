package object;

import java.awt.image.BufferedImage;

public class GameObject {

    protected int tileX;
    protected int tileY;
    protected BufferedImage image;
    protected boolean collision = false;

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public void setTilePosition(int x, int y) {
        this.tileX = x;
        this.tileY = y;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage img) {
        this.image = img;
    }

    public boolean hasCollision() {
        return collision;
    }

    public void setCollision(boolean c) {
        this.collision = c;
    }
}