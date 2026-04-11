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

        move();

        if (user == getGameWindow().getPlayer()) {

            int monsterHit = getGameWindow().getCollisionChecker().checkEntity(this, getGameWindow().getMonster());

            if (monsterHit != 999) {

                Entity target = getGameWindow().getMonster()[monsterHit];
                getGameWindow().getPlayer().damageMonster(monsterHit, getAttack(), this.getKnockBackPower());

                generateParticle(this, target);
                spawnFireworkParticles(target);
                setAlive(false);
            }

        } else {

            boolean hit = getGameWindow().getCollisionChecker().checkPlayer(this);

            if (getGameWindow().getPlayer().getInvincible() == false && hit == true) {

                damagePlayer(getAttack(), getKnockBackPower());
                // 衝突判定を起こした時の Projectile インスタンスを渡す
                generateParticle(this, getGameWindow().getPlayer());
                System.out.println("プレイヤ-に石ころの衝突判定がありダメージを与えた!");
                setAlive(false);
            }
        }
        updateAnimation();
    }

    protected void move() {
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
            int screenX = getWorldX() - getGameWindow().getPlayer().getWorldX()
                    + getGameWindow().getPlayer().getScreenX();
            int screenY = getWorldY() - getGameWindow().getPlayer().getWorldY()
                    + getGameWindow().getPlayer().getScreenY();
            g2.drawImage(image, screenX, screenY, null);

            // デバッグ
//            if (getGameWindow().isShowHitBoxes()) {
//                // 当たり判定領域をスクリーン座標に変換
//                int x = screenX + getSolidArea().x;
//                int y = screenY + getSolidArea().y;
//                int w = getSolidArea().width;
//                int h = getSolidArea().height;
//
//                // 色や線幅を設定して描画
//                g2.setColor(new Color(255, 0, 0, 180));
//                g2.setStroke(new BasicStroke(2));
//                g2.drawRect(x, y, w, h);
//
//                g2.drawImage(image, screenX, screenY, FrameApp.getTileSize(), FrameApp.getTileSize(), null);
//            }
//        }
        }
    }

    public void setUser(Entity user) {
        this.user = user;
    }

    public Entity getUser() {
        return user;
    }
}