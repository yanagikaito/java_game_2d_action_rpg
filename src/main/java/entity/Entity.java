package entity;

import window.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Entity {

    private GameWindow gameWindow;
    public int x;
    public int y;
    public int speed;
    public String direction;
    public BufferedImage[][] sprites = new BufferedImage[4][3];
    public boolean collision = false;
    public int spriteCounter = 0;
    public int spriteNum = 1;

    public Entity(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void draw(Graphics2D g2) {
    }
}