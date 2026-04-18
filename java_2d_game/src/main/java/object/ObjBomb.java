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

    // 投げられたときの物理挙動用
    private boolean hasShadow = false;
    private double vx = 0;
    private double vy = 0;
    // 毎フレーム vy に加算する重力
    private double gravity = 0.3;
    // 投げられて飛んでいる状態
    private boolean thrown = false;
    private Entity user = null;
    // 置かれていて拾える状態
    private boolean pickable = false;


    public ObjBomb(GameWindow gameWindow) {
        super(gameWindow, DIRS.length, SPRITE_COUNT);
        this.gameWindow = gameWindow;

        setType(new BombType());
        setName("爆弾");
        setSpeed(1);
        setMaxLife(80);
        setAttack(5);
        setKnockBackPower(0);
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

            if (thrown) {
                // 投げられているときの既存処理（移動・重力・衝突判定）
                setWorldX(getWorldX() + (int) Math.round(vx));
                setWorldY(getWorldY() + (int) Math.round(vy));
                vy += gravity;

                gameWindow.getCollisionChecker().checkTile(this);
                if (this.isCollision()) {
                    startExplosion();
                } else {
                    if (user == gameWindow.getPlayer()) {
                        int monsterHit = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
                        if (monsterHit != -1 && monsterHit != 999) startExplosion();
                    } else {
                        boolean hitPlayer = gameWindow.getCollisionChecker().checkPlayer(this);
                        if (hitPlayer) startExplosion();
                    }
                }

                setLife(getLife() - 1);
                if (getLife() <= 0) startExplosion();

            } else {
                // 地面に置かれている状態
                if (!pickable) {
                    setLife(getLife() - 1);
                    if (getLife() <= 0) startExplosion();
                } else {
                }
            }

        } else {
            // 爆発中の処理は既存のまま
            if (!damageGiven) {
                damageGiven = true;
                giveExplosionDamage();
            }
            explosionFrame++;
            if (explosionFrame >= SPRITE_COUNT) {
                setAlive(false);
                thrown = false;
                explosionFrame = SPRITE_COUNT - 1;
            }
        }

        if (thrown) {
            updateAnimation();
        }
    }

    public void startExplosion() {

        if (exploding) return;

        exploding = true;
        explosionFrame = 0;
        damageGiven = false;
    }

    private void updateAnimation() {

        if (exploding) return;

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
                getGameWindow().getPlayer().damageMonster(monsterHit, getAttack(), this.getKnockBackPower());
                System.out.println("プレイヤ-が爆弾の衝突判定がありダメージを与えた!");
                generateParticle(this, target);
                setAlive(false);
            }

        } else {


            boolean hit = getGameWindow().getCollisionChecker().checkPlayer(this);

            if (getGameWindow().getPlayer().getInvincible() == false && hit == true) {

                damagePlayer(getAttack(), getKnockBackPower());
                setAlive(false);
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        int dirIndex = 1;
        if (Math.abs(vx) > Math.abs(vy)) dirIndex = vx < 0 ? 2 : 3;
        else dirIndex = vy < 0 ? 0 : 1;

        if (sprites == null || dirIndex < 0 || dirIndex >= sprites.length || sprites[dirIndex] == null) return;

        int maxIndex = sprites[dirIndex].length - 1;
        int spriteIndex;
        if (exploding) {
            spriteIndex = Math.max(0, Math.min(explosionFrame, maxIndex));
        } else {
            spriteIndex = Math.max(0, getSpriteNum());
            if (spriteIndex > maxIndex) spriteIndex = maxIndex;
        }

        BufferedImage img = sprites[dirIndex][spriteIndex];
        if (img != null) g2.drawImage(img, screenX, screenY, null);
    }

    // 拾ったときに呼ぶ
    public void onPickedUp() {
        setPickable(false);
        setAlive(false);
        setThrown(false);
        setLife(getMaxLife());
    }

    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }

    public boolean isPickable() {
        return pickable;
    }

    @Override
    public void setUser(Entity user) {
        this.user = user;
    }

    @Override
    public Entity getUser() {
        return this.user;
    }

    public void setThrown(boolean thrown) {
        this.thrown = thrown;
    }

    public boolean isThrown() {
        return this.thrown;
    }

    public double getGravity() {
        return gravity;
    }

    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
        this.thrown = true;
        setAlive(true);
    }

    public void setHasShadow(boolean v) {
        this.hasShadow = v;
    }

    public boolean hasShadow() {
        return this.hasShadow;
    }

    /**
     * 所持中に Player が頭上に描画するためのスプライトを返す。
     * デフォルトは Down 方向の最初のフレームを使い、タイルサイズに合わせてリサイズする。
     */

    // ObjBomb.java に追加

// 所持時に使う1枚を返す
    public BufferedImage getHeldSprite() {
        int ts = FrameApp.getTileSize();
        int downIndex = 1; // DIRS = {"Up","Down","Left","Right"} の Down
        int frameIndex = 0; // 所持時は最初のフレームを使う
        if (sprites != null && sprites.length > downIndex && sprites[downIndex].length > frameIndex) {
            BufferedImage src = sprites[downIndex][frameIndex];
            // 小さめにリサイズして返す（任意）
            int w = ts * 3 / 4;
            int h = ts * 3 / 4;
            BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = buf.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, w, h, null);
            g.dispose();
            return buf;
        }
        return null;
    }

    // 投げるときの向き・フレーム別スプライトを返す（存在しなければ null を返す）
    public BufferedImage getThrowSprite(String dir, int frame) {
        // ここでは sprites の向きインデックスとフレームをそのまま使う例
        int dirIndex = 1; // default Down
        switch (dir) {
            case "up" -> dirIndex = 0;
            case "down" -> dirIndex = 1;
            case "left" -> dirIndex = 2;
            case "right" -> dirIndex = 3;
        }
        int frameIndex = Math.max(0, Math.min(frame, sprites[dirIndex].length - 1));
        return sprites[dirIndex][frameIndex];
    }
}