package entity;

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

    public Entity(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.worldX = 0;
        this.worldY = 0;
        this.speed = 0;
        this.direction = "";
        this.collision = false;
        this.spriteCounter = 0;
        this.spriteNum = 1;
        this.sprites = new BufferedImage[4][3];
    }

    public GameWindow getGameWindow() {
        return gameWindow;
    }

    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
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

    public BufferedImage[][] getSprites() {
        return sprites;
    }

    public void setSprites(BufferedImage[][] sprites) {
        this.sprites = sprites;
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

    public void draw(Graphics2D g2) {

    }
}