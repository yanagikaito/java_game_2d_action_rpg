package object;

import entity.Entity;
import window.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Projectile extends Entity {

    protected BufferedImage[][] sprites;
    private static final int SPRITE_COUNT = 3;
    private static final int SPRITE_ANIMATION_THRESHOLD = 12;
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
        updateAnimation();
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
        if (!getAlive()) return;
        int idx;
        switch (getDirection()) {
            case "up":
                idx = 0;
                break;
            case "down":
                idx = 1;
                break;
            case "left":
                idx = 2;
                break;
            default:
                idx = 3;
                break;
        }
        BufferedImage img = sprites[idx][getSpriteNum() - 1];
        int screenX = getWorldX() - getGameWindow().getPlayer().getWorldX()
                + getGameWindow().getPlayer().getScreenX();
        int screenY = getWorldY() - getGameWindow().getPlayer().getWorldY()
                + getGameWindow().getPlayer().getScreenY();
        g2.drawImage(img, screenX, screenY, null);
    }
}