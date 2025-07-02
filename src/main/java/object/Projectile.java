package object;

import collision.CollisionChecker;
import entity.Entity;
import frame.FrameApp;
import player.Player;
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
        Player p = gw.getPlayer();

        // 1) プレイヤーの当たり判定矩形（ワールド座標）
        Rectangle playerArea = new Rectangle(
                p.getWorldX() + p.getSolidArea().x,
                p.getWorldY() + p.getSolidArea().y,
                p.getSolidArea().width,
                p.getSolidArea().height

        );

        // 2) この石ころ（Projectile）の当たり判定矩形
        Rectangle stoneArea = new Rectangle(
                getWorldX() + getSolidArea().x,
                getWorldY() + getSolidArea().y,
                getSolidArea().width,
                getSolidArea().height
        );

        // 3) 重なりチェック
        boolean overlap = playerArea.intersects(stoneArea);
        System.out.printf(
                "[DBG] playerArea=%s, stoneArea=%s, overlap=%b%n",
                playerArea, stoneArea, overlap
        );

        move();

        if (user == getGameWindow().getPlayer()) {
            int monsterIndex = getGameWindow().getCollisionChecker().checkEntity(this, getGameWindow().getMonster());
            if (monsterIndex != 999) {
                getGameWindow().getPlayer().damageMonster(monsterIndex, getAttack());
                setAlive(false);
            }
        } else {
            boolean contactPlayer = getGameWindow().getCollisionChecker().checkPlayer(this);
            System.out.printf("[DBG] cc.checkPlayer() → %b%n", contactPlayer);
            System.out.println("Invincible = " + getInvincible());
            System.out.println("contactPlayer = " + contactPlayer);
            if (getGameWindow().getPlayer().getInvincible() == false && contactPlayer == true) {
                damagePlayer(getAttack());
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

            // --- ここからデバッグ描画 ---
            if (getGameWindow().isShowHitBoxes()) {
                // 当たり判定領域をスクリーン座標に変換
                int x = screenX + getSolidArea().x;
                int y = screenY + getSolidArea().y;
                int w = getSolidArea().width;
                int h = getSolidArea().height;

                // 色や線幅を設定して描画
                g2.setColor(new Color(255, 0, 0, 180));      // 半透明の赤
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(x, y, w, h);

                g2.drawImage(image, screenX, screenY, FrameApp.getTileSize(), FrameApp.getTileSize(), null);
            }
        }
    }
}