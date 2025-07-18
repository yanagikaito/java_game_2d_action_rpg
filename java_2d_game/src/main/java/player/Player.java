package player;

import entity.Entity;
import frame.FrameApp;
import key.KeyHandler;
import object.*;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Timer;

public class Player extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final String[] ATTACK_DIRECTIONS = {"attackUp", "attackDown", "attackLeft", "attackRight"};
    private static final String[] AXE_DIRECTIONS = {"axeUp", "axeDown", "axeLeft", "axeRight"};
    private static final int SPRITE_COUNT = 3;
    private static final int SPRITE_ANIMATION_THRESHOLD = 10;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM1 = 5;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM2 = 15;
    private static final int SPRITE_ATTACKING_THRESHOLD_NUM3 = 25;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] attackSprites = new BufferedImage[ATTACK_DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] axeSprites = new BufferedImage[AXE_DIRECTIONS.length][SPRITE_COUNT];
    private BufferedImage[][] currentAttackSprites;
    private static final long FIRE_COOLDOWN_MS = 1000;
    private long lastFireTime = 0;
    private int fireCooldown = 0;
    private int coin = 0;
    private static final int COOLDOWN_FRAMES = 60 * 3;
    private int invincibleCounter = 0;
    private final int INVINCIBLE_DURATION = 60; // 60フレーム無敵

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
    private static final int FIREBALL_MANA_COST = 20;

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

        getSolidArea().width = FrameApp.getTileSize() - 3;
        getSolidArea().height = FrameApp.getTileSize() - 3;

        setAttackArea(new Rectangle());
//        getAttackArea().width = 36;
//        getAttackArea().height = 36;

        setDefaultValues();
        loadPlayerImages();
        loadAllAttackSprites();
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
        setMaxMana(100);
        setMana(getMaxMana());
        setStrength(1);
        setDexterity(1);
        setExp(0);
        setNextLevelExp(5);
        setCoin(0);
        setCurrentWeapon(new ObjSwordNormal(gameWindow));
        setCurrentShield(new ObjShieldWood(gameWindow));
        setProjectile(new ObjFireball(gameWindow));
        setAttack(calculateBaseAttack());
        setDefense(calculateBaseDefense());
    }

    public void setItems() {

        inventory.add(getCurrentWeapon());
        inventory.add(getCurrentShield());

        inventory.add(new ObjRedPotion(gameWindow));
        inventory.add(new ObjGreenPotion(gameWindow));
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

    private void loadAllAttackSprites() {

        int tileSize = FrameApp.getTileSize();

        // 剣用
        for (int d = 0; d < ATTACK_DIRECTIONS.length; d++) {
            for (int i = 0; i < SPRITE_COUNT; i++) {
                String path = "player/image-" + ATTACK_DIRECTIONS[d] + "-" + (i + 1) + ".gif";
                try {
                    BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
                    if (d == 0 || d == 1) {
                        attackSprites[d][i] = createImage(img, tileSize, tileSize * 2);
                    } else {
                        attackSprites[d][i] = createImage(img, tileSize * 2, tileSize);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 斧用
        for (int d = 0; d < AXE_DIRECTIONS.length; d++) {
            for (int i = 0; i < SPRITE_COUNT; i++) {
                String path = "player/image-" + AXE_DIRECTIONS[d] + "-" + (i + 1) + ".gif";
                try {
                    BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
                    if (d == 0 || d == 1) {
                        axeSprites[d][i] = createImage(img, tileSize, tileSize * 2);
                    } else {
                        axeSprites[d][i] = createImage(img, tileSize * 2, tileSize);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateCurrentAttackSprites() {
        if (getCurrentWeapon().getType() == getType_axe()) {
            currentAttackSprites = axeSprites;
        } else {
            currentAttackSprites = attackSprites;
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
            gameWindow.getSoundmanager().redPotionWAV("java_2d_game/res/sound/potion-sound.wav");
            inventory.remove(index);
            return;
        } else {
            System.out.println("選択アイテムはポーションではありません: " + e.getClass().getSimpleName());
        }
    }

    public void useGreenPotion(int index) {
        System.out.println("useRedPotion が呼ばれた index=" + index
                + " invSize=" + inventory.size());

        if (index < 0 || index >= inventory.size()) {
            System.out.println("index 範囲外で return");
            return;
        }

        Entity e = inventory.get(index);

        if (e instanceof ObjGreenPotion) {
            System.out.println("アイテムは GreenPotion です。処理続行");
            ObjGreenPotion potion = (ObjGreenPotion) e;
            int heal = potion.getHealAmount();
            setMana(Math.min(getMana() + heal, getMaxMana()));
            gameWindow.getUi().addMessage("グリーンポーションを使った。魔力が" + heal + "回復！");
            gameWindow.getSoundmanager().greenPotionWAV("java_2d_game/res/sound/potion-sound.wav");
            inventory.remove(index);
            return;
        } else {
            System.out.println("選択アイテムはポーションではありません: " + e.getClass().getSimpleName());
        }
    }

    private void startAttack() {

        String base = capitalize(getDirection());
        if (getCurrentWeapon().getType() == getType_axe()) {
            setAttackDirection("axe" + base);
        } else {
            setAttackDirection("attack" + base);
        }

        updateCurrentAttackSprites();
    }

    @Override
    public void update() {

        if (getInvincible()) {
            invincibleCounter++;
            if (invincibleCounter > INVINCIBLE_DURATION) {
                setInvincible(false);
                invincibleCounter = 0;
            }
        }

        if (fireCooldown > 0) fireCooldown--;

        if (getAttacking()) {
            startAttack();
            playerAttacking();
        } else {
            if (gameWindow.getKeyHandler().isShotKeyPressed() && fireCooldown == 0) {
                fireCooldown = COOLDOWN_FRAMES;
                playerAttackingFireball();
            }
            if (!moving) {
                processInput();
            }
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

        int objIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getObj());
        pickUpObject(objIndex);

        int monsterIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        if (monsterIndex != 999 && !getInvincible()) {
            contactMonster(monsterIndex);
            setInvincible(true);
        }

        int iTileIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile());
        damageInteractiveTile(iTileIndex);
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
        if (getShotAvailableCounter() < 30) {
            setShotAvailableCounter(getShotAvailableCounter() + 1);
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
            case "down" -> setWorldY(getWorldY() + getAttackArea().height);
            case "left" -> setWorldX(getWorldX() - getAttackArea().width);
            case "right" -> setWorldX(getWorldX() + getAttackArea().width);
            default -> {
            }
        }

        getSolidArea().width = FrameApp.getTileSize();
        getSolidArea().height = FrameApp.getTileSize();

        int monsterIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        damageMonster(monsterIndex, calculateTotalAttack());

        int iTileIndex = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getItile());
        damageInteractiveTile(iTileIndex);

        setWorldX(originalWorldX);
        setWorldY(originalWorldY);
        getSolidArea().width = originalSolidWidth;
        getSolidArea().height = originalSolidHeight;
    }

    public void playerAttackingFireball() {

        KeyHandler kh = gameWindow.getKeyHandler();

        if (kh.isShotKeyPressed() &&
                !getProjectile().getAlive() &&
                getShotAvailableCounter() == 30 &&
                consumeMana(FIREBALL_MANA_COST)) {

            fireCooldown = COOLDOWN_FRAMES;

            long now = System.currentTimeMillis();

            if (now - lastFireTime >= FIRE_COOLDOWN_MS) {
                lastFireTime = now;

                System.out.println("DEBUG: Fキーが押されている");

                ObjFireball fb = new ObjFireball(gameWindow);
                fb.set(
                        getWorldX(),
                        getWorldY(),
                        getDirection(),
                        true,
                        this
                );
                fb.setLife(fb.getMaxLife());
                fb.setSpriteNum(1);
                fb.setSpriteCounter(0);

                gameWindow.getProjectileList().add(fb);
                setShotAvailableCounter(0);
                gameWindow.getSoundmanager().explosionWAV("java_2d_game/res/sound/explosion-sound.wav");
                System.out.println("DEBUG: ファイアボール発射！向き=" + getDirection());
            }
        }
    }

    public void pickUpObject(int i) {

        if (i != 999) {

            String text;

            if (inventory.size() != maxInventorySize) {

                inventory.add(gameWindow.getObj()[i]);
                text = gameWindow.getObj()[i].getName() + "を手に入れた!";
            } else {
                text = "手に入れていない!";
            }
            gameWindow.getUi().addMessage(text);
            gameWindow.getObj()[i] = null;
        }
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
                gameWindow.getSoundmanager().defeatedWAV("java_2d_game/res/sound/thrust-sound.wav");
            }
        }
        keyHandler.setPlayerEnter(false);
    }

    public void contactMonster(int i) {

        if (i != 999) {
            if (!getInvincible()) {
                gameWindow.getSoundmanager().damageWAV("java_2d_game/res/sound/damage-sound.wav");

                // スライムのダメージ量
                int damage = setAttack(gameWindow.getMonster()[i].getAttack() - calculateTotalDefense());
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

    public void damageMonster(int i, int attack) {

        long start = System.nanoTime();

        if (i != 999) {
            if (!gameWindow.getMonster()[i].getInvincible()) {
                gameWindow.getSoundmanager().damageWAV("java_2d_game/res/sound/damage-sound.wav");

                long end = System.nanoTime();

                System.out.println("サウンド再生にかかった時間: " + (end - start) + " ns");

                // プレイヤーのダメージ量
                int damage = setAttack(attack - gameWindow.getMonster()[i].getDefense());
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

                    gameWindow.getMonster()[i].setAlive(false);
                    gameWindow.getMonster()[i].setDying(true);

                    List<Supplier<Entity>> drops = Arrays.asList(
                            () -> new ObjCoinBronze(gameWindow),
                            () -> new ObjRedPotion(gameWindow),
                            () -> new ObjGreenPotion(gameWindow)
                    );

                    Collections.shuffle(drops);
                    Entity dropped = drops.get(0).get();

                    gameWindow.dropItem(dropped, monster);
                    System.out.println("drops = " + drops);

                    gameWindow.getUi().addMessage(gameWindow.getMonster()[i].getName() + "を倒した!");
                    int gainedExp = gameWindow.getMonster()[i].getExp();
                    setExp(getExp() + gainedExp);
                    gameWindow.getUi().addMessage("経験値" + gainedExp + " 入手!");
                    checkLevelUp();
                    gameWindow.getSoundmanager().defeatedWAV("java_2d_game/res/sound/defeated-sound.wav");

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

    public void damageInteractiveTile(int i) {

        if (!gameWindow.getPlayer().getAttacking()) return;

        if (i != 999 && gameWindow.getItile()[i].isDestructible()
                && gameWindow.getItile()[i].isCorrectItem(this) == true
                && gameWindow.getItile()[i].getInvincible() == false) {

            gameWindow.getSoundmanager().damageWAV("java_2d_game/res/sound/thrust-sound.wav");
            gameWindow.getItile()[i].setLife(gameWindow.getItile()[i].getLife() - 1);
            gameWindow.getItile()[i].setInvincible(true);
            generateParticle(gameWindow.getItile()[i], gameWindow.getItile()[i]);

            if (gameWindow.getItile()[i].getLife() == 0) {
                gameWindow.getItile()[i] = gameWindow.getItile()[i].createDestroyedForm();
            }
        }
    }

    private @NotNull Timer getTimer(Entity monster) {
        Timer respawnTimer = new Timer(5000, e -> {
            gameWindow.getAssetSetter().setMonster();
            monster.setRespawning(false);
            ((Timer) e.getSource()).stop();
        });
        respawnTimer.setRepeats(false);
        return respawnTimer;
    }

    public void checkLevelUp() {

        if (getExp() >= getNextLevelExp()) {

            setLevel(getLevel() + 1);
            setNextLevelExp(getNextLevelExp() * 2);
            setMaxLife(getMaxLife() + 2);
            setStrength(getStrength() + 1);
            setDexterity(getDexterity() + 1);
            setAttack(calculateBaseAttack());
            setDefense(calculateBaseDefense());
            gameWindow.getSoundmanager().levelWAV("java_2d_game/res/sound/level-up-sound.wav");
            gameWindow.setGameState(gameWindow.getDialogueState());
            gameWindow.getUi().setCurrentDialogueMessage("プレイヤーはレベル" + gameWindow.getPlayer().getLevel() + "になった!");
            gameWindow.getPlayer().setLife(gameWindow.getPlayer().getMaxLife());
        }
    }

    public void selectItem(int index) {

        int itemIndex = gameWindow.getUi().getItemIndexOnSlot();

        if (itemIndex < inventory.size()) {

            Entity selectedItem = inventory.get(itemIndex);
            System.out.println("selectedItem = " + selectedItem);

            if (selectedItem.getType() == getType_sword() || selectedItem.getType() == getType_axe()) {

                setCurrentWeapon(selectedItem);
                setAttack(calculateTotalAttack());
                loadAllAttackSprites();
            }
            if (selectedItem.getType() == getType_shield()) {

                setCurrentShield(selectedItem);
                setDefense(calculateTotalDefense());
            }
            if (selectedItem.getType() == getType_redPotion()) {
                useRedPotion(index);
            }
            if (selectedItem.getType() == getType_greenPotion()) {
                useGreenPotion(index);
            }
        }
    }

    @Override
    public void draw(@NotNull Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

        int screenX = getWorldX()
                - gameWindow.getPlayer().getWorldX()
                + gameWindow.getPlayer().getScreenX();

        int screenY = getWorldY()
                - gameWindow.getPlayer().getWorldY()
                + gameWindow.getPlayer().getScreenY();

        Composite original = g2.getComposite();
        if (getInvincible()) {
            g2.setComposite(
                    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
            );
        }

        BufferedImage img;
        if (!getAttacking()) {
            int walkDirIndex = Arrays.asList(DIRECTIONS)
                    .indexOf(getDirection());
            if (walkDirIndex >= 0) {
                img = sprites[walkDirIndex][getSpriteNum() - 1];
                g2.drawImage(img, screenX, screenY, tileSize, tileSize, null);
            }
        } else {
            boolean isAxe = getCurrentWeapon().getType() == getType_axe();
            String[] animationKeys = isAxe
                    ? AXE_DIRECTIONS
                    : ATTACK_DIRECTIONS;
            BufferedImage[][] spriteSet = isAxe
                    ? axeSprites
                    : attackSprites;

            int directionIndex = Arrays.asList(animationKeys)
                    .indexOf(getAttackDirection());
            if (directionIndex >= 0) {
                int frameIndex = Math.max(0,
                        Math.min(getSpriteNum() - 1, SPRITE_COUNT - 1)
                );
                img = spriteSet[directionIndex][frameIndex];

                int drawWidth = (directionIndex == 2 || directionIndex == 3)
                        ? tileSize * 2 : tileSize;
                int drawHeight = (directionIndex == 0 || directionIndex == 1)
                        ? tileSize * 2 : tileSize;
                int drawX = animationKeys[directionIndex]
                        .endsWith("Left") ? screenX - tileSize : screenX;
                int drawY = animationKeys[directionIndex]
                        .endsWith("Up") ? screenY - tileSize : screenY;

                g2.drawImage(img, drawX, drawY, drawWidth, drawHeight, null);
            }
        }

        g2.setComposite(original);
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

    public ArrayList<Entity> getInventory() {
        return inventory;
    }

    public void calculateBaseAttackArea() {
        setAttackArea(getCurrentWeapon().getAttackArea());
    }

    public int calculateBaseAttack() {
        return getStrength();
    }

    public int calculateWeaponBonus() {
        return getCurrentWeapon() != null
                ? getCurrentWeapon().getAttackValue()
                : 1;
    }

    public int calculateTotalAttack() {
        calculateBaseAttackArea();
        return calculateBaseAttack()
                * calculateWeaponBonus();
    }

    public int calculateBaseDefense() {
        return getDexterity();
    }

    public int calculateShieldBonus() {
        return getCurrentShield() != null
                ? getCurrentShield().getDefenseValue()
                : 1;
    }

    public int calculateTotalDefense() {
        return calculateBaseDefense()
                * calculateShieldBonus();
    }

    public boolean consumeMana(int cost) {
        if (getMana() < cost) return false;
        setMana(getMana() - cost);
        return true;
    }


    public void addCoin(@NotNull ObjCoinBronze coin) {
        int value = coin.getValue();
        setCoin(getCoin() + value);
        gameWindow.getUi().addMessage("コインを拾った。所持金が" + value + "獲得！");
    }

    public void healRedPotion(@NotNull ObjRedPotion potion) {
        int amount = potion.getHealAmount();
        setLife(Math.min(getLife() + amount, getMaxLife()));
        gameWindow.getUi().addMessage("レッドポーションを拾った。HPが" + amount + "回復！");
    }

    public void healGreenPotion(@NotNull ObjGreenPotion potion) {
        int amount = potion.getHealAmount();
        setMana(Math.min(getMana() + amount, getMaxMana()));
        gameWindow.getUi().addMessage("グリーンポーションを拾った。MPが" + amount + "回復！");
    }

    private @NotNull String capitalize(@NotNull String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}