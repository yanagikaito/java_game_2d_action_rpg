package object;

import collision.CollisionChecker;
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

        GameWindow gw = getGameWindow();
        CollisionChecker cc = gw.getCollisionChecker();

        move();

        // Entity間とPlayer間の衝突チェック
        if (this instanceof Projectile proj) {

            // 発射元によって判定先を切り分け
            if (proj.user == getGameWindow().getPlayer()) {
                int mi = cc.checkEntity(proj, getGameWindow().getMonster());
                if (mi != 999) {
                    getGameWindow().getPlayer().damageMonster(mi, proj.getAttack());
                    setAlive(false);
                }
            } else {
                boolean hit = cc.checkPlayer(proj);
                if (hit && !getGameWindow().getPlayer().getInvincible()) {
                    getGameWindow().getPlayer().damagePlayer(proj.getAttack());
                    setAlive(false);
                }
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
}