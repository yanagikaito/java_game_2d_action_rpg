package entity;

import frame.FrameApp;
import org.jetbrains.annotations.NotNull;
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
    private String attackDirection;
    private BufferedImage[][] sprites;
    private BufferedImage[][] attackSprites;
    private BufferedImage image;
    private BufferedImage image2;
    private BufferedImage image3;
    private boolean collision;
    private int spriteCounter;
    private int spriteNum;
    private Rectangle solidArea;
    private Rectangle attackArea;
    private int solidAreaDefaultX;
    private int solidAreaDefaultY;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final String[] ATTACK_DIRECTIONS = {"attackUp", "attackDown", "attackLeft", "attackRight"};
    private String[] dialogue = new String[20];
    private static final int SPRITE_COUNT = 3;
    private static final int SPRITE_ANIMATION_THRESHOLD = 10;
    private static final int SPRITE_DYING_COUNT = 5;
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
    private boolean invincible = false;
    private int invincibleCounter = 0;
    private int type;
    private boolean attacking = false;
    private boolean alive = true;
    private boolean dying = false;
    private boolean hpBarOn = false;
    private int dyingCounter = 0;
    private int hpBarCounter = 0;
    private int level;
    private int strength;
    private int dexterity;
    private int attack;
    private int defense;
    private int exp;
    private int nextLevelExp;
    private int coin;
    private Entity currentWeapon;
    private Entity currentShield;
    private int attackValue;
    private int defenseValue;

    public Entity(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.worldX = 0;
        this.worldY = 0;
        this.speed = 0;
        this.direction = "";
        this.attackDirection = "";
        this.name = "";
        this.collision = false;
        this.spriteCounter = 0;
        this.spriteNum = 1;
        this.solidArea = new Rectangle(0, 0, FrameApp.getTileSize(), FrameApp.getTileSize());
        this.attackArea = new Rectangle(0, 0, 0, 0);
        this.solidAreaDefaultX = 0;
        this.solidAreaDefaultY = 0;
        this.sprites = new BufferedImage[4][3];
        this.attackSprites = new BufferedImage[4][3];
        this.attackValue = 0;
        this.defenseValue = 0;
        this.level = 0;
        this.strength = 0;
        this.dexterity = 0;
        this.attack = 0;
        this.defense = 0;
        this.exp = 0;
        this.nextLevelExp = 0;
        this.coin = 0;
    }

    public void setAction() {

    }

    public void damageReaction() {

    }

    public void update() {

        setAction();

        setCollision(false);
        gameWindow.getCollisionChecker().checkTile(this);
        gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getNPC());
        gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        boolean contactPlayer = gameWindow.getCollisionChecker().checkPlayer(this);

        if (this.type == 2 && contactPlayer == true) {
            if (gameWindow.getPlayer().getInvincible() == false) {
                gameWindow.getSoundmanager().damageWAV("res/sound/damage-sound.wav");

                int damage = setAttack(getAttack() - gameWindow.getPlayer().getDefense());
                if (damage < 0) {
                    damage = 0;
                }

                gameWindow.getPlayer().setLife(gameWindow.getPlayer().getLife() - damage);
                gameWindow.getPlayer().setInvincible(true);
            }
        }

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

    public void setSprites(BufferedImage[][] sprites) {
        this.sprites = sprites;
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

    public BufferedImage getImage() {
        return image;
    }

    public BufferedImage getImage2() {
        return image2;
    }

    public BufferedImage getImage3() {
        return image3;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getInvincible() {
        return invincible;
    }

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    public int getInvincibleCounter() {
        return invincibleCounter;
    }

    public void setInvincibleCounter(int invincibleCounter) {
        this.invincibleCounter = invincibleCounter;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getAttacking() {
        return attacking;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public String[] getAttackDirections() {
        return ATTACK_DIRECTIONS;
    }

    public String getAttackDirection() {
        return attackDirection;
    }

    public void setAttackDirection(String attackDirection) {
        this.attackDirection = attackDirection;
    }

    public Rectangle getAttackArea() {
        return attackArea;
    }

    public void setAttackArea(Rectangle attackArea) {
        this.attackArea = attackArea;
    }

    public boolean getDying() {
        return dying;
    }

    public void setDying(boolean dying) {
        this.dying = dying;
    }

    public boolean getAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getAttackValue() {
        return attackValue;
    }

    public void setAttackValue(int attackValue) {
        this.attackValue = attackValue;
    }

    public int getDefenseValue() {
        return defenseValue;
    }

    public void setDefenseValue(int defenseValue) {
        this.defenseValue = defenseValue;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getAttack() {
        return attack;
    }

    public int setAttack(int attack) {
        return this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public int setDefense(int defense) {
        return this.defense = defense;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getNextLevelExp() {
        return nextLevelExp;
    }

    public void setNextLevelExp(int nextLevelExp) {
        this.nextLevelExp = nextLevelExp;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public Entity getCurrentWeapon() {
        return currentWeapon;
    }

    public void setCurrentWeapon(Entity currentWeapon) {
        this.currentWeapon = currentWeapon;
    }

    public Entity getCurrentShield() {
        return currentShield;
    }

    public void setCurrentShield(Entity currentShield) {
        this.currentShield = currentShield;
    }

    public void setImage(BufferedImage image, int width, int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.width = width;
        this.height = height;
        Graphics2D graphics2D = this.image.createGraphics();
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
    }

    public void setImage2(BufferedImage image2, int width, int height) {
        this.image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.width = width;
        this.height = height;
        Graphics2D graphics2D = this.image2.createGraphics();
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        graphics2D.drawImage(image2, 0, 0, width, height, null);
        graphics2D.dispose();
    }

    public void setImage3(BufferedImage image3, int width, int height) {
        this.image3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.width = width;
        this.height = height;
        Graphics2D graphics2D = this.image3.createGraphics();
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        System.out.println("Composite状態: " + graphics2D.getComposite());
        graphics2D.drawImage(image3, 0, 0, width, height, null);
        graphics2D.dispose();
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

            if (type == 2 && hpBarOn) {

                double oneScale = (double) FrameApp.getTileSize() / maxLife;
                double hpBarValue = oneScale * life;

                g2.setColor(new Color(35, 35, 35));
                g2.fillRect(screenX + 4, screenY - 21, FrameApp.getTileSize(), 12);

                g2.setColor(new Color(255, 0, 30));
                g2.fillRect(screenX + 5, screenY - 20, (int) hpBarValue, 10);

                hpBarCounter++;

                if (hpBarCounter > 600) {
                    hpBarCounter = 0;
                    hpBarOn = false;
                }
            }

            if (getInvincible()) {
                hpBarOn = true;
                hpBarCounter = 0;
                changeAlpha(g2, 0.4f);
            } else {
                changeAlpha(g2, 1F);
            }

            if (getDying()) {
                dyingAnimation(g2);
            }

            g2.drawImage(image, screenX, screenY, FrameApp.getTileSize(), FrameApp.getTileSize(), null);

            changeAlpha(g2, 1F);
        }
    }

    public void dyingAnimation(Graphics2D g2) {
        dyingCounter++;

        if (dyingCounter <= SPRITE_DYING_COUNT) {
            changeAlpha(g2, 0f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 2) {
            changeAlpha(g2, 1f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 3) {
            changeAlpha(g2, 0f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 4) {
            changeAlpha(g2, 1f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 5) {
            changeAlpha(g2, 0f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 6) {
            changeAlpha(g2, 1f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 7) {
            changeAlpha(g2, 0f);
        } else if (dyingCounter <= SPRITE_DYING_COUNT * 8) {
            changeAlpha(g2, 1f);
        } else {
            dying = false;
            alive = false;
        }
    }

    public void changeAlpha(@NotNull Graphics2D g2, float alphaValue) {

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue));

    }
}