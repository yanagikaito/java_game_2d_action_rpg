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
    private static final int SPRITE_ANIMATION_THRESHOLD = 10;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];

    private GameWindow gameWindow;
    private KeyHandler keyHandler;
    private final int screenX;
    private final int screenY;
    private boolean moving = false;
    private int pixelCounter = 0;
    private final int playerSolidAreaX = 1;
    private final int playerSolidAreaY = 1;

    public Player(GameWindow gameWindow, KeyHandler keyHandler) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        this.keyHandler = keyHandler;

        screenX = FrameApp.getScreenWidth() / 2 - (FrameApp.getTileSize() / 2);
        screenY = FrameApp.getScreenHeight() / 2 - (FrameApp.getTileSize() / 2);

        setSolidArea(new Rectangle());
        getSolidArea().x = playerSolidAreaX;
        getSolidArea().y = playerSolidAreaY;

        setSolidAreaDefaultX(getSolidArea().x);
        setSolidAreaDefaultY(getSolidArea().y);

        getSolidArea().width = FrameApp.getTileSize() - 6;
        getSolidArea().height = FrameApp.getTileSize() - 4;

        setDefaultValues();
        loadPlayerImages();
    }

    public void setDefaultValues() {
        setWorldX(FrameApp.getTileSize() * 23);
        setWorldY(FrameApp.getTileSize() * 21);
        setSpeed(4);
        setDirection("down");

        setMaxLife(6);
        setLife(getMaxLife());
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
            processInput();
        }

        if (moving) {
            updateMovement();
            updateCollision();
            updateAnimation();
            updateTileMovement();
        }

        updateInvincibility();
    }

    private void processInput() {
        var keyHandler = gameWindow.getKeyHandler();
        if (keyHandler.isPlayerUp() || keyHandler.isPlayerDown()
                || keyHandler.isPlayerLeft() || keyHandler.isPlayerRight()) {
            if (keyHandler.isPlayerUp()) {
                setDirection("up");
            } else if (keyHandler.isPlayerDown()) {
                setDirection("down");
            } else if (keyHandler.isPlayerRight()) {
                setDirection("right");
            } else if (keyHandler.isPlayerLeft()) {
                setDirection("left");
            }
            moving = true;
        }
    }

    private void updateMovement() {
        if (!isCollision()) {
            switch (getDirection()) {
                case "up" -> setWorldY(getWorldY() - getSpeed());
                case "down" -> setWorldY(getWorldY() + getSpeed());
                case "right" -> setWorldX(getWorldX() + getSpeed());
                case "left" -> setWorldX(getWorldX() - getSpeed());
            }
        }
    }

    private void updateCollision() {
        setCollision(false);
        gameWindow.getCollisionChecker().checkTile(this);

        int npcIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC());
        interactNPC(npcIndex);

        int monsterIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        contactMonster(monsterIndex);
    }

    private void updateAnimation() {
        setSpriteCounter(getSpriteCounter() + 1);
        if (getSpriteCounter() > SPRITE_ANIMATION_THRESHOLD) {
            setSpriteNum((getSpriteNum() % SPRITE_COUNT) + 1);
            setSpriteCounter(0);
        }
    }

    private void updateTileMovement() {
        pixelCounter += getSpeed();
        if (pixelCounter >= FrameApp.getTileSize()) {
            moving = false;
            pixelCounter = 0;
        }
    }

    private void updateInvincibility() {
        // 無敵状態の更新
        if (getInvincible()) {
            setInvincibleCounter(getInvincibleCounter() + 1);
            if (getInvincibleCounter() > 60) {
                setInvincible(false);
                setInvincibleCounter(0);
            }
        }
    }

    public void interactNPC(int i) {

        if (i != 999) {
            if (gameWindow.getKeyHandler().isPlayerEnter() == true) {
                gameWindow.setGameState(gameWindow.getDialogueState());
                gameWindow.getNPC()[i].speak();
            }
        }
        gameWindow.getKeyHandler().setPlayerEnter(false);
    }

    public void contactMonster(int i) {

        if (i != 999) {
            if (getInvincible() == false) {
                int life = getLife() - 1;
                setLife(life);
                setInvincible(true);
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
        g2.drawImage(image, screenX, screenY, FrameApp.getTileSize(), FrameApp.getTileSize(), null);

//        // デバッグ
//        g2.setColor(Color.RED);
//        g2.drawRect(screenX + getSolidArea().x, screenY + getSolidArea().y, getSolidArea().width, getSolidArea().height);

    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }
}