package player;

import entity.Entity;
import frame.FrameApp;
import key.KeyHandler;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

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
        setWorldX(100);
        setWorldY(100);
        setSpeed(2);
        setDirection("down");
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

        if (!moving) {
            if (keyHandler.playerUp || keyHandler.playerDown || keyHandler.playerLeft || keyHandler.playerRight) {
                if (keyHandler.playerUp) {
                    setDirection("up");
                } else if (keyHandler.playerDown) {
                    setDirection("down");
                } else if (keyHandler.playerRight) {
                    setDirection("right");
                } else if (keyHandler.playerLeft) {
                    setDirection("left");
                }
                moving = true;
            }
        }

        if (moving) {
            if (!isCollision()) {
                switch (getDirection()) {
                    case "up":
                        setWorldY(getWorldY() - getSpeed());
                        break;
                    case "down":
                        setWorldY(getWorldY() + getSpeed());
                        break;
                    case "right":
                        setWorldX(getWorldX() + getSpeed());
                        break;
                    case "left":
                        setWorldX(getWorldX() - getSpeed());
                        break;
                }
            }

            setSpriteCounter(getSpriteCounter() + 1);
            if (getSpriteCounter() > 10) {
                setSpriteNum((getSpriteNum() % SPRITE_COUNT) + 1);
                setSpriteCounter(0);
            }

            pixelCounter += getSpeed();
            if (pixelCounter == FrameApp.getTileSize()) {
                moving = false;
                pixelCounter = 0;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        int dirIndex = Arrays.asList(DIRECTIONS).indexOf(getDirection());
        if (dirIndex != -1) {
            image = sprites[dirIndex][getSpriteNum() - 1];
        }
        g2.drawImage(image, getWorldX(), getWorldY(), FrameApp.getTileSize(), FrameApp.getTileSize(), null);
    }
}