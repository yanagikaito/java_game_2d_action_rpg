package object;

import entity.Entity;
import window.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public abstract class Projectile extends Entity {

    protected BufferedImage[][] sprites;
    private static final int SPRITE_COUNT = 3;
    private static final int SPRITE_ANIMATION_THRESHOLD = 12;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private Entity user;

    public Projectile(GameWindow gw, int dirCount, int spriteCount) {
        super(gw);
        sprites = new BufferedImage[dirCount][spriteCount];
    }

    protected abstract void loadSprites();

    public void set(int worldX, int worldY, String direction, boolean alive, Entity user) {
        setWorldX(worldX);
        setWorldY(worldY);
        setDirection(direction);
        setAlive(alive);
        this.user = user;
        setLife(getMaxLife());
        setSpriteNum(1);
        setSpriteCounter(0);
    }

    @Override
    public void update() {

        if (!getAlive()) return;

        if (user == getGameWindow().getPlayer()) {
            int monsterIndex = getGameWindow().getCollisionChecker().checkEntity(this, getGameWindow().getMonster());
            if (monsterIndex != 999) {
                getGameWindow().getPlayer().damageMonster(monsterIndex, getAttack());
                setAlive(false);
            }
        }
        if (user != getGameWindow().getPlayer()) {

        }
        updateAnimation();
        move();
    }

    private void move() {
        switch (getDirection()) {
            case "up" -> setWorldY(getWorldY() - getSpeed());
            case "down" -> setWorldY(getWorldY() + getSpeed());
            case "left" -> setWorldX(getWorldX() - getSpeed());
            case "right" -> setWorldX(getWorldX() + getSpeed());
        }
    }

    private void updateAnimation() {
        setSpriteCounter(getSpriteCounter() + 1);
        if (getSpriteCounter() > SPRITE_ANIMATION_THRESHOLD) {
            setSpriteNum((getSpriteNum() % SPRITE_COUNT) + 1);
            setSpriteCounter(0);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        if (!getAttacking()) {
            int dirIndex = Arrays.asList(DIRECTIONS).indexOf(getDirection());
            if (dirIndex != -1) {
                image = sprites[dirIndex][getSpriteNum() - 1];
//                System.out.println("dirIndex:" + dirIndex);
            }
            BufferedImage img = sprites[dirIndex][getSpriteNum() - 1];
            int sx = getWorldX() - getGameWindow().getPlayer().getWorldX()
                    + getGameWindow().getPlayer().getScreenX();
            int sy = getWorldY() - getGameWindow().getPlayer().getWorldY()
                    + getGameWindow().getPlayer().getScreenY();
            g2.drawImage(image, sx, sy, null);
        }
    }
}