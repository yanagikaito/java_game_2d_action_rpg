package object;

import entity.BombType;
import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjBomb extends Projectile {

    private GameWindow gameWindow;
    private static final String[] DIRS = {"Up", "Down", "Left", "Right"};
    private static final int SPRITE_COUNT = 11;
    private int explosionFrame = 0;
    private boolean exploding = false;
    private boolean damageGiven = false;

    public ObjBomb(GameWindow gameWindow) {
        super(gameWindow, DIRS.length, SPRITE_COUNT);
        this.gameWindow = gameWindow;

        setType(new BombType());
        setName("爆弾");
        setSpeed(1);
        setMaxLife(80);
        setAttack(5);
        setUseCost(5);
        setAlive(false);
        setDescription("[" + getName() + "]\n一定時間経過すると爆発する。");
        loadSprites();
    }

    @Override
    protected void loadSprites() {

        try {
            int ts = FrameApp.getTileSize();
            for (int i = 0; i < DIRS.length; i++) {
                for (int j = 0; j < SPRITE_COUNT; j++) {
                    String path = String.format(
                            "bomb/image-bomb%s-%d.gif",
                            DIRS[i], j + 1);
                    BufferedImage ori = ImageIO.read(
                            getClass().getClassLoader().getResourceAsStream(path)
                    );
                    BufferedImage buf = new BufferedImage(ts, ts, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = buf.createGraphics();
                    g.drawImage(ori, 0, 0, ts, ts, null);
                    g.dispose();
                    sprites[i][j] = buf;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {

        if (!getAlive()) return;

        if (!exploding) {

            // タイマー減算
            setLife(getLife() - 1);
            System.out.println("getLife() = " + getLife());
            if (getLife() <= 0) {
                startExplosion();
            }
        } else {

            // 爆発中：最初のフレームで一度だけダメージ
            if (!damageGiven) {
                damageGiven = true;
            }
            explosionFrame++;
            if (explosionFrame >= SPRITE_COUNT) {
                giveExplosionDamage();
                setAlive(false);
            }
        }
        updateAnimation();
    }

    private void startExplosion() {
        exploding = true;
        explosionFrame = 0;
    }

    private void updateAnimation() {
        setSpriteCounter(getSpriteCounter() + 1);
        if (getSpriteCounter() > 12) {
            setSpriteNum((getSpriteNum() % SPRITE_COUNT) + 1);
            setSpriteCounter(0);
        }
    }

    private void giveExplosionDamage() {

        if (getUser() == getGameWindow().getPlayer()) {

            int monsterHit = getGameWindow().getCollisionChecker().checkEntity(this, getGameWindow().getMonster());

            if (monsterHit != 999) {

                Entity target = getGameWindow().getMonster()[monsterHit];
                getGameWindow().getPlayer().damageMonster(monsterHit, getAttack());
                System.out.println("プレイヤ-が爆弾の衝突判定がありダメージを与えた!");
                generateParticle(this, target);
                setAlive(false);
            }

        } else {


            boolean hit = getGameWindow().getCollisionChecker().checkPlayer(this);

            if (getGameWindow().getPlayer().getInvincible() == false && hit == true) {

                damagePlayer(getAttack());
                setAlive(false);
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();
        int dirIndex = 1; // 例: 下向き固定
        int spriteIndex = exploding ? explosionFrame : 0; // 爆発中はフレーム進行
        g2.drawImage(sprites[dirIndex][spriteIndex], screenX, screenY, null);
    }
}