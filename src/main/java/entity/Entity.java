package entity;

import frame.FrameApp;
import window.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Entity {

    private GameWindow gameWindow;
    private int worldX;
    private int worldY;
    private int speed;
    private String direction;
    private BufferedImage[][] sprites;
    private boolean collision;
    private int spriteCounter;
    private int spriteNum;
    private Rectangle solidArea;
    private int solidAreaDefaultX;
    private int solidAreaDefaultY;

    public Entity(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.worldX = 0;
        this.worldY = 0;
        this.speed = 0;
        this.direction = "";
        this.collision = false;
        this.spriteCounter = 0;
        this.spriteNum = 1;
        this.solidArea = new Rectangle(0, 0, FrameApp.getTileSize(), FrameApp.getTileSize());
        this.solidAreaDefaultX = 0;
        this.solidAreaDefaultY = 0;
        this.sprites = new BufferedImage[4][3];
    }

    public int getWorldX() {
        return worldX;
    }

    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }

    public int getWorldY() {
        return worldY;
    }

    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isCollision() {
        return collision;
    }

    public void setCollision(boolean collision) {
        this.collision = collision;
    }

    public int getSpriteCounter() {
        return spriteCounter;
    }

    public void setSpriteCounter(int spriteCounter) {
        this.spriteCounter = spriteCounter;
    }

    public int getSpriteNum() {
        return spriteNum;
    }

    public void setSpriteNum(int spriteNum) {
        this.spriteNum = spriteNum;
    }

    public Rectangle getSolidArea() {
        return solidArea;
    }

    public void setSolidArea(Rectangle solidArea) {
        this.solidArea = solidArea;
    }

    public void setSolidAreaDefaultX(int solidAreaDefaultX) {
        this.solidAreaDefaultX = solidAreaDefaultX;
    }

    public void setSolidAreaDefaultY(int solidAreaDefaultY) {
        this.solidAreaDefaultY = solidAreaDefaultY;
    }

    public void draw(Graphics2D g2) {

    }
}