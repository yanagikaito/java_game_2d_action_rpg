package entity;

import frame.FrameApp;
import window.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

public class Entity {

    private GameWindow gameWindow;
    private int worldX;
    private int worldY;
    private int width;
    private int height;
    private int speed;
    private String direction;
    private BufferedImage[][] sprites;
    private BufferedImage image;
    private BufferedImage image2;
    private boolean collision;
    private int spriteCounter;
    private int spriteNum;
    private Rectangle solidArea;
    private int solidAreaDefaultX;
    private int solidAreaDefaultY;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private String[] dialogue = new String[20];
    private static final int SPRITE_COUNT = 3;
    private static final int SPRITE_ANIMATION_THRESHOLD = 10;
    private static final int MAX_RANDOM_VALUE = 100;
    private static final int THRESHOLD_UP = 25;
    private static final int THRESHOLD_DOWN = 50;
    private static final int THRESHOLD_LEFT = 75;
    private int pixelCounter = 0;
    private int actionLockCounter = 0;
    private static final int COLLISION_COOLDOWN_FRAMES = 30;
    private int collisionCooldown = 0;
    private int dialogueIndex = 0;
    private int maxLife;
    private int life;
    private String name;

    public Entity(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.worldX = 0;
        this.worldY = 0;
        this.speed = 0;
        this.direction = "";
        this.name = "";
        this.collision = false;
        this.spriteCounter = 0;
        this.spriteNum = 1;
        this.solidArea = new Rectangle(0, 0, FrameApp.getTileSize(), FrameApp.getTileSize());
        this.solidAreaDefaultX = 0;
        this.solidAreaDefaultY = 0;
        this.sprites = new BufferedImage[4][3];
    }

    public void setAction() {

    }


    public void update() {

        setAction();

        setCollision(false);
        gameWindow.getCollisionChecker().checkTile(this);
        gameWindow.getCollisionChecker().checkPlayer(this);

        if (!isCollision()) {
            move();
            collisionCooldown = 0;
        } else {
            if (collisionCooldown == 0) {
                chooseNewDirection();
                collisionCooldown = COLLISION_COOLDOWN_FRAMES;
            } else {
                collisionCooldown--;
            }
        }

        setSpriteCounter(getSpriteCounter() + 1);
        if (getSpriteCounter() > SPRITE_ANIMATION_THRESHOLD) {
            setSpriteNum((getSpriteNum() % SPRITE_COUNT) + 1);
            setSpriteCounter(0);
        }

        pixelCounter += getSpeed();
        if (pixelCounter == FrameApp.getTileSize()) {
            pixelCounter = 0;
        }
    }

    public void speak() {
    }

    private void move() {
        switch (getDirection()) {
            case "up":
                setWorldY(getWorldY() - getSpeed());
                break;
            case "down":
                setWorldY(getWorldY() + getSpeed());
                break;
            case "left":
                setWorldX(getWorldX() - getSpeed());
                break;
            case "right":
                setWorldX(getWorldX() + getSpeed());
                break;
        }
    }

    private void chooseNewDirection() {

        actionLockCounter++;

        int i = (new Random()).nextInt(MAX_RANDOM_VALUE) + 1;
        if (i <= THRESHOLD_UP) {
            setDirection("up");
        } else if (i <= THRESHOLD_DOWN) {
            setDirection("down");
        } else if (i <= THRESHOLD_LEFT) {
            setDirection("left");
        } else {
            setDirection("right");
        }
        actionLockCounter = 0;
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

    public BufferedImage[][] getSprites() {
        return sprites;
    }

    public BufferedImage[][] setSprites(BufferedImage[][] sprites) {
        this.sprites = sprites;
        return this.sprites;
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

    public int getSolidAreaDefaultX() {
        return solidAreaDefaultX;
    }

    public int getSolidAreaDefaultY() {
        return solidAreaDefaultY;
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

    public String[] getDialogue() {
        return dialogue;
    }

    public GameWindow getGameWindow() {
        return gameWindow;
    }

    public int getDialogueIndex() {
        return dialogueIndex;
    }

    public void setDialogueIndex(int dialogueIndex) {
        this.dialogueIndex = dialogueIndex;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public void setMaxLife(int maxLife) {
        this.maxLife = maxLife;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void draw(Graphics2D g2) {

        BufferedImage image = null;

        int dirIndex = Arrays.asList(DIRECTIONS).indexOf(getDirection());
        if (dirIndex != -1) {
            image = sprites[dirIndex][getSpriteNum() - 1];
        }

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
}