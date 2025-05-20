package player;

import entity.Entity;
import frame.FrameApp;
import key.KeyHandler;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class Player extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final String[] ATTACK_DIRECTIONS = {"attackUp", "attackDown", "attackLeft", "attackRight"};
    private static final int ATTACK_HOLD_DELAY = 10;
    private static final int SPRITE_COUNT = 3;
    private static final int SPRITE_ANIMATION_THRESHOLD = 10;
    private static final int SPRITE_ATTACKING_THRESHOLD = 5;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] attackSprites = new BufferedImage[ATTACK_DIRECTIONS.length][SPRITE_COUNT];

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

        setAttackArea(new Rectangle());
        getAttackArea().width = 36;
        getAttackArea().height = 36;

        setDefaultValues();
        loadPlayerImages();
        loadAttackPlayerImages();
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
            int tileSize = FrameApp.getTileSize();
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream("player/image-" + DIRECTIONS[dir] + "-" + (i + 1) + ".gif"));
                    BufferedImage processed = createImage(original, tileSize, tileSize);
                    sprites[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadAttackPlayerImages() {
        try {
            int tileSize = FrameApp.getTileSize();
            for (int dir = 0; dir < ATTACK_DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    String path = "player/image-" + ATTACK_DIRECTIONS[dir] + "-" + (i + 1) + ".gif";
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader().getResourceAsStream(path));

                    BufferedImage processed = switch (ATTACK_DIRECTIONS[dir]) {
                        case "attackUp", "attackDown" -> createImage(original, tileSize, tileSize * 2);
                        case "attackLeft", "attackRight" -> createImage(original, tileSize * 2, tileSize);
                        default -> createImage(original, tileSize, tileSize);
                    };
                    attackSprites[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {

        if (getAttacking()) {
            playerAttacking();
        } else if (!moving) {
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
//        System.out.println("モンスター衝突判定: " + monsterIndex);
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
        if (getInvincible()) {
            setInvincibleCounter(getInvincibleCounter() + 1);
            if (getInvincibleCounter() > 60) {
                setInvincible(false);
                System.out.println("無敵状態が解除されました。");
                setInvincibleCounter(0);
            }
        }
    }

    public void playerAttacking() {

        setSpriteCounter(getSpriteCounter() + 1);

        if (getSpriteCounter() < 5) {
            setSpriteNum(1);
        }
        if (getSpriteCounter() > 5 && getSpriteCounter() <= 25) {
            setSpriteNum(2);
        }
        if (getSpriteCounter() > 25) {
            setSpriteNum(3);
        }
        int currentWorldX = getWorldX();
        int currentWorldY = getWorldY();
        int solidAreaWidth = getSolidArea().width;
        int solidAreaHeight = getSolidArea().height;

        switch (getDirection()) {
            case "up" -> setWorldY(getWorldY() - getAttackArea().height);
            case "down" -> setWorldY(getWorldY() + getAttackArea().height);
            case "left" -> setWorldX(getWorldX() - getAttackArea().width);
            case "right" -> setWorldX(getWorldX() + getAttackArea().width);
        }

        getSolidArea().width = getAttackArea().width;
        getSolidArea().height = getAttackArea().height;

        int monsterIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        damageMonster(monsterIndex);

        setWorldX(currentWorldX);
        setWorldY(currentWorldY);
        getSolidArea().width = solidAreaWidth;
        getSolidArea().height = solidAreaHeight;

        setSpriteCounter(0);
        setAttacking(false);
        setSpriteNum(1);
    }

    public void interactNPC(int i) {

        var keyHandler = gameWindow.getKeyHandler();

        if (keyHandler.isPlayerEnter()) {
            if (i != 999) {
                gameWindow.setGameState(gameWindow.getDialogueState());
                gameWindow.getNPC()[i].speak();
            } else {
                setAttackDirection("attack" + getDirection().substring(0, 1).toUpperCase()
                        + getDirection().substring(1));
                setAttacking(true);
            }
        }
        keyHandler.setPlayerEnter(false);
    }

    public void contactMonster(int i) {
        if (i != 999) {
            if (!getInvincible()) {
                setLife(getLife() - 1);
                setInvincible(true);
//                System.out.println("モンスター衝突: " + i);
//                System.out.println("無敵状態: " + getInvincible());
//                System.out.println(i);
//                System.out.println("衝突時の無敵状態: " + getInvincible());
//                System.out.println("プレイヤーのHP: " + getLife());
            }
        }
    }

    public void damageMonster(int i) {

        if (i != 999) {
            if (!gameWindow.getMonster()[i].getInvincible()) {
                gameWindow.getMonster()[i].setLife(gameWindow.getMonster()[i].getLife() - 1);
                gameWindow.getMonster()[i].setInvincible(true);
                System.out.println("スライムのHP:" + gameWindow.getMonster()[i].getLife());

                if (gameWindow.getMonster()[i].getLife() <= 0) {
                    gameWindow.getMonster()[i] = null;
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {

        BufferedImage image = null;
        int tempScreenX = screenX;
        int tempScreenY = screenY;

        if (!getAttacking()) {
            int dirIndex = Arrays.asList(DIRECTIONS).indexOf(getDirection());
            if (dirIndex != -1) {
                image = sprites[dirIndex][getSpriteNum() - 1];
            }
        } else {
            int attackDirIndex = Arrays.asList(ATTACK_DIRECTIONS).indexOf(getAttackDirection());
//            System.out.println("attackDirIndex:" + attackDirIndex);
            if (attackDirIndex != -1) {
                image = attackSprites[attackDirIndex][getSpriteNum() - 1];
//                System.out.println("image:" + image);
            }
        }

        if (getInvincible()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.drawImage(image, screenX, screenY, FrameApp.getTileSize(), FrameApp.getTileSize(), null);
        }
        if (!getAttacking()) {
            g2.drawImage(image, screenX, screenY, FrameApp.getTileSize(), FrameApp.getTileSize(), null);
        } else {
            if (getAttackDirection().equals("attackUp")) {
                tempScreenY = screenY - FrameApp.getTileSize();
                g2.drawImage(image, screenX, tempScreenY, FrameApp.getTileSize(), FrameApp.getTileSize() * 2, null);
            } else if (getAttackDirection().equals("attackDown")) {
                g2.drawImage(image, screenX, screenY, FrameApp.getTileSize(), FrameApp.getTileSize() * 2, null);
            } else if (getAttackDirection().equals("attackLeft")) {
                tempScreenX = screenX - FrameApp.getTileSize();
                g2.drawImage(image, tempScreenX, screenY, FrameApp.getTileSize() * 2, FrameApp.getTileSize(), null);
            } else if (getAttackDirection().equals("attackRight")) {
                g2.drawImage(image, screenX, screenY, FrameApp.getTileSize() * 2, FrameApp.getTileSize(), null);
            }

            // デバッグ
            tempScreenX = screenX + getSolidArea().x;
            tempScreenY = screenY + getSolidArea().y;
            switch (getDirection()) {
                case "up" -> tempScreenY = screenY - getAttackArea().height;
                case "down" -> tempScreenY = screenY + FrameApp.getTileSize();
                case "left" -> tempScreenX = screenX - getAttackArea().width;
                case "right" -> tempScreenX = screenX + FrameApp.getTileSize();
            }
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(tempScreenX, tempScreenY, getAttackArea().width, getAttackArea().height);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    private @NotNull BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }
}