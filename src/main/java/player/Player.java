package player;

import entity.Entity;
import frame.FrameApp;
import key.KeyHandler;
import object.ObjRedPotion;
import object.ObjShieldWood;
import object.ObjSwordNormal;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.Timer;

public class Player extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final String[] ATTACK_DIRECTIONS = {"attackUp", "attackDown", "attackLeft", "attackRight"};
    private static final int SPRITE_COUNT = 3;
    private static final int SPRITE_ANIMATION_THRESHOLD = 10;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM1 = 5;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM2 = 15;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM3 = 25;
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
    private ArrayList<Entity> inventory = new ArrayList<>();
    private final int maxInventorySize = 20;

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
        setItems();
    }

    public void setDefaultValues() {
        setWorldX(FrameApp.getTileSize() * 23);
        setWorldY(FrameApp.getTileSize() * 21);
        setSpeed(4);
        setDirection("down");

        setLevel(1);
        setMaxLife(6);
        setLife(getMaxLife());
        setStrength(1);
        setDexterity(1);
        setExp(0);
        setNextLevelExp(5);
        setCoin(0);
        setCurrentWeapon(new ObjSwordNormal(gameWindow));
        setCurrentShield(new ObjShieldWood(gameWindow));
        setAttack(getAttack());
        setDefense(getDefense());
    }

    public void setItems() {

        inventory.add(getCurrentWeapon());
        inventory.add(getCurrentShield());
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjRedPotion(gameWindow));
    }

    @Override
    public int getAttack() {
        return setAttack(getStrength() * getCurrentWeapon().getAttackValue());
    }

    @Override
    public int getDefense() {
        return setDefense(getDexterity() * getCurrentShield().getDefenseValue());
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

    public void useRedPotion(int index) {
        System.out.println("useRedPotion が呼ばれた index=" + index
                + " invSize=" + inventory.size());

        if (index < 0 || index >= inventory.size()) {
            System.out.println("index 範囲外で return");
            return;
        }

        Entity e = inventory.get(index);

        if (e instanceof ObjRedPotion) {
            System.out.println("アイテムは RedPotion です。処理続行");
            ObjRedPotion potion = (ObjRedPotion) e;
            int heal = potion.getHealAmount();
            setLife(Math.min(getLife() + heal, getMaxLife()));
            gameWindow.getUi().addMessage("レッドポーションを使った。HPが" + heal + "回復！");
            gameWindow.getSoundmanager().redPotionWAV("res/sound/redPotion-sound.wav");
            inventory.remove(index);
            return;
        } else {
            System.out.println("選択アイテムはポーションではありません: " + e.getClass().getSimpleName());
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

        int counter = getSpriteCounter() + 1;
        setSpriteCounter(counter);

        if (counter <= SPRITE_ATTACKING_THRESHOLD_NUM1) {
            setSpriteNum(1);
//            System.out.println("counter:" + counter + " num:" + getSpriteNum());
        } else if (counter <= SPRITE_ATTACKING_THRESHOLD_NUM2) {
            setSpriteNum(2);
//            System.out.println("counter:" + counter + " num:" + getSpriteNum());
        } else if (counter <= SPRITE_ATTACKING_THRESHOLD_NUM3) {
            setSpriteNum(3);
//            System.out.println("counter:" + counter + " num:" + getSpriteNum());
        } else {
            setSpriteCounter(0);
            setAttacking(false);
            setSpriteNum(1);
            return;
        }

        final int originalWorldX = getWorldX();
        final int originalWorldY = getWorldY();
        final int originalSolidWidth = getSolidArea().width;
        final int originalSolidHeight = getSolidArea().height;

        switch (getDirection()) {
            case "up" -> setWorldY(getWorldY() - getAttackArea().height);
            case "down" -> setWorldY(getWorldY() + FrameApp.getTileSize());
            case "left" -> setWorldX(getWorldX() - getAttackArea().width);
            case "right" -> setWorldX(getWorldX() + FrameApp.getTileSize());
            default -> {
            }
        }

        getSolidArea().width = FrameApp.getTileSize();
        getSolidArea().height = FrameApp.getTileSize();

        int monsterIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        damageMonster(monsterIndex);

        setWorldX(originalWorldX);
        setWorldY(originalWorldY);
        getSolidArea().width = originalSolidWidth;
        getSolidArea().height = originalSolidHeight;
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
                gameWindow.getSoundmanager().defeatedWAV("res/sound/thrust-sound.wav");
            }
        }
        keyHandler.setPlayerEnter(false);
    }

    public void contactMonster(int i) {
        if (i != 999) {
            if (!getInvincible()) {
                gameWindow.getSoundmanager().damageWAV("res/sound/damage-sound.wav");

                // スライムのダメージ量
                int damage = setAttack(gameWindow.getMonster()[i].getAttack() - getDefense());
                if (damage < 0) {
                    damage = 0;
                }

                setLife(getLife() - damage);
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

        long start = System.nanoTime();

        if (i != 999) {
            if (!gameWindow.getMonster()[i].getInvincible()) {
                gameWindow.getSoundmanager().damageWAV("res/sound/damage-sound.wav");

                long end = System.nanoTime();

                System.out.println("サウンド再生にかかった時間: " + (end - start) + " ns");

                // プレイヤーのダメージ量
                int damage = setAttack(getAttack() - gameWindow.getMonster()[i].getDefense());
                if (damage < 0) {
                    damage = 0;
                }
                gameWindow.getMonster()[i].setLife(gameWindow.getMonster()[i].getLife() - damage);
                gameWindow.getUi().addMessage(damage + "ダメージ!");
                gameWindow.getMonster()[i].setInvincible(true);
                gameWindow.getMonster()[i].damageReaction();
                System.out.println("スライムのHP:" + gameWindow.getMonster()[i].getLife());

                if (gameWindow.getMonster()[i].getLife() <= 0) {
                    Entity monster = gameWindow.getMonster()[i];
                    gameWindow.getMonster()[i].setDying(true);
                    gameWindow.getUi().addMessage(gameWindow.getMonster()[i].getName() + "を倒した!");
                    int gainedExp = gameWindow.getMonster()[i].getExp();
                    setExp(getExp() + gainedExp);
                    gameWindow.getUi().addMessage("経験値" + gainedExp + " 入手!");
                    checkLevelUp();
                    gameWindow.getSoundmanager().defeatedWAV("res/sound/defeated-sound.wav");

                    int aliveCount = 0;
                    for (Entity m : gameWindow.getMonster()) {
                        if (m != null && !m.getDying() && m.getLife() > 0) {
                            aliveCount++;
                        }
                    }

                    if (aliveCount == 0 && !monster.isRespawning()) {
                        monster.setRespawning(true);

                        Timer respawnTimer = getTimer(monster);
                        respawnTimer.start();
                    }
                }
            }
        }
    }

    private @NotNull Timer getTimer(Entity monster) {
        Timer respawnTimer = new Timer(5000, e -> {
            gameWindow.getAssetSetter().setMonster();
            monster.setRespawning(false);
            ((Timer) e.getSource()).stop();
        });
        respawnTimer.setRepeats(false); // 1回だけ実行
        return respawnTimer;
    }

    public void checkLevelUp() {

        if (getExp() >= getNextLevelExp()) {

            setLevel(getLevel() + 1);
            setNextLevelExp(getNextLevelExp() * 2);
            setMaxLife(getMaxLife() + 2);
            setStrength(getStrength() + 1);
            setDexterity(getDexterity() + 1);
            setAttack(getAttack());
            setDefense(getDefense());
            gameWindow.getSoundmanager().levelWAV("res/sound/level-up-sound.wav");
            gameWindow.setGameState(gameWindow.getDialogueState());
            gameWindow.getUi().setCurrentDialogueMessage("プレイヤーはレベル" + gameWindow.getPlayer().getLevel() + "になった!");
            gameWindow.getPlayer().setLife(gameWindow.getPlayer().getMaxLife());
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
//                System.out.println("dirIndex:" + dirIndex);
            }
        } else {
            int attackDirIndex = Arrays.asList(ATTACK_DIRECTIONS).indexOf(getAttackDirection());
            System.out.println("attackDirIndex:" + attackDirIndex);
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
//            tempScreenX = screenX + getSolidArea().x;
//            tempScreenY = screenY + getSolidArea().y;
//            switch (getDirection()) {
//                case "up" -> tempScreenY = screenY - getAttackArea().height;
//                case "down" -> tempScreenY = screenY + FrameApp.getTileSize();
//                case "left" -> tempScreenX = screenX - getAttackArea().width;
//                case "right" -> tempScreenX = screenX + FrameApp.getTileSize();
//            }
//            g2.setColor(Color.RED);
//            g2.setStroke(new BasicStroke(1));
//            g2.drawRect(tempScreenX, tempScreenY, getAttackArea().width, getAttackArea().height);
//            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
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

    public int getMaxInventorySize() {
        return maxInventorySize;
    }

    public ArrayList<Entity> getInventory() {
        return inventory;
    }
}