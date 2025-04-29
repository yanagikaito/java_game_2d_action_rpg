package entity;

import frame.FrameApp;
import key.KeyHandler;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player extends Entity {
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final int SPRITE_COUNT = 3;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];

    private GameWindow gameWindow;
    private KeyHandler keyHandler;
    private boolean moving = false;
    private int pixelCounter = 0;

    public Player(GameWindow gameWindow, KeyHandler keyHandler) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        this.keyHandler = keyHandler;

        setDefaultValues();
        loadPlayerImages();
    }

    public void setDefaultValues() {
        x = 100;
        y = 100;
        speed = 2;
        direction = "down";
    }

    public void loadPlayerImages() {
        try {
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    sprites[dir][i] = ImageIO.read(getClass().getClassLoader()
                            .getResourceAsStream("player/image-" + DIRECTIONS[dir] + "-" + (i + 1) + ".png"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {

        if (moving == false) {

            if (keyHandler.playerUp || keyHandler.playerDown || keyHandler.playerLeft || keyHandler.playerRight) {

                if (keyHandler.playerUp) {
                    direction = "up";
                } else if (keyHandler.playerDown) {
                    direction = "down";
                } else if (keyHandler.playerRight) {
                    direction = "right";
                } else if (keyHandler.playerLeft) {
                    direction = "left";
                }

                moving = true;
            }
        }
        if (moving == true) {

            if (collision == false) {

                switch (direction) {
                    case "up" -> y -= speed;
                    case "down" -> y += speed;
                    case "right" -> x += speed;
                    case "left" -> x -= speed;
                }
            }

            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum % SPRITE_COUNT) + 1;
                spriteCounter = 0;
            }
            pixelCounter += speed;

            if (pixelCounter == FrameApp.createSize()) {
                moving = false;
                pixelCounter = 0;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        int dirIndex = java.util.Arrays.asList(DIRECTIONS).indexOf(direction);
        if (dirIndex != -1) {
            image = sprites[dirIndex][spriteNum - 1];
        }
        g2.drawImage(image, x, y, FrameApp.createSize(), FrameApp.createSize(), null);
    }
}