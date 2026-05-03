package object;

import entity.Entity;
import entity.PotType;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjPot extends Projectile {

    private GameWindow gameWindow;
    private static final String[] DIRS = {"Up", "Down", "Left", "Right"};
    private static final int OBJ_POT_SPRITE_COUNT = 1;
    private static final int STONE_SCATTER_SPRITE_COUNT = 3;
    private BufferedImage[][] potSprites;
    private BufferedImage[][] fragmentSprites;

    // 投げられたときの物理挙動用
    private boolean hasShadow = false;
    private double vx = 0;
    private double vy = 0;
    // 高さ方向速度（上向き正）
    private double vz = 0.0;
    // 地面からの高さ（ピクセル）
    private double z = 0.0;
    // 毎フレーム vy に加算する重力
    private double gravity = 0.3;
    // 垂直重力（正、調整可）
    private double verticalGravity = 0.45;
    // 投げられて飛んでいる状態
    private boolean thrown = false;
    private Entity user = null;
    // 置かれていて拾える状態
    private boolean pickable = false;

    // 新規フィールド（着地→タイマー）
    // 地面に着地しているか
    private boolean landed = false;

    // 割れ管理
    private boolean shattering = false;
    private int shatterFrame = 0;
    private boolean damageGiven = false;

    public ObjPot(GameWindow gameWindow) {
        super(gameWindow, DIRS.length, OBJ_POT_SPRITE_COUNT);
        this.gameWindow = gameWindow;

        setType(new PotType());
        setName("壺");
        setSpeed(1);
        setMaxLife(80);
        setAttack(1);
        setKnockBackPower(0);
        setUseCost(5);
        setAlive(false);
        loadSprites();
    }

    @Override
    protected void loadSprites() {

        try {

            int ts = FrameApp.getTileSize();

            potSprites = new BufferedImage[DIRS.length][OBJ_POT_SPRITE_COUNT];
            fragmentSprites = new BufferedImage[DIRS.length][STONE_SCATTER_SPRITE_COUNT];

            for (int i = 0; i < DIRS.length; i++) {
                for (int j = 0; j < OBJ_POT_SPRITE_COUNT; j++) {
                    String path = String.format("object/image-pot%s-%d.gif", DIRS[i].toLowerCase(), j + 1);
                    BufferedImage ori = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
                    BufferedImage buf = new BufferedImage(ts, ts, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = buf.createGraphics();
                    g.drawImage(ori, 0, 0, ts, ts, null);
                    g.dispose();
                    potSprites[i][j] = buf;
                }
            }

            for (int i = 0; i < DIRS.length; i++) {
                for (int j = 0; j < STONE_SCATTER_SPRITE_COUNT; j++) {
                    String path = String.format("object/image-stonescatter%s-%d.gif", DIRS[i], j + 1);
                    BufferedImage ori = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
                    BufferedImage buf = new BufferedImage(ts, ts, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = buf.createGraphics();
                    g.drawImage(ori, 0, 0, ts, ts, null);
                    g.dispose();
                    fragmentSprites[i][j] = buf;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // update() の実装（放物線・着地・割れ）
    @Override
    public void update() {

        if (!getAlive()) return;

        // 割れ中はアニメ進行のみ
        if (shattering) {
            // ダメージや破片生成は所定フレームで一度だけ
            if (!damageGiven) {
                damageGiven = true;
                giveShatterDamageAndSpawnFragments();
            } else {
                shatterFrame++;
            }
            int maxIndex = Math.max(0, Math.min(STONE_SCATTER_SPRITE_COUNT - 1,
                    fragmentSprites != null && fragmentSprites[0] != null ?
                            fragmentSprites[0].length - 1 : STONE_SCATTER_SPRITE_COUNT - 1));
            if (shatterFrame > maxIndex) {
                setAlive(false);
            }
            return;
        }

        // 投げられているときの物理更新
        if (thrown) {
            // 水平移動（ワールド座標）
            setWorldX(getWorldX() + (int) Math.round(vx));
            setWorldY(getWorldY() + (int) Math.round(vy));

            // 画面Y方向の重力（既存の vy に加算）
            vy += gravity;

            // 高さ方向の更新（z, vz）
            z += vz;
            vz -= verticalGravity;

            // 低空での当たり判定（モンスター等） — 衝突で割る
            double hitThreshold = FrameApp.getTileSize() * 0.5;
            if (z <= hitThreshold) {
                if (user == gameWindow.getPlayer()) {
                    int monsterHit = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
                    if (monsterHit != -1 && monsterHit != 999) {
                        startShatter();
                    }
                } else {
                    boolean hitPlayer = gameWindow.getCollisionChecker().checkPlayer(this);
                    if (hitPlayer) {
                        startShatter();
                    }
                }
            }

            // 着地判定
            if (z <= 0) {
                z = 0;
                vz = 0;
                boolean tileCollision = gameWindow.getCollisionChecker().checkTile(this);
                if (tileCollision) {
                    // 着地で割る
                    startShatter();
                    this.setCollision(false);
                    if (this.getSolidArea() != null) {
                        this.getSolidArea().width = 0;
                        this.getSolidArea().height = 0;
                    }
                } else {
                    // 地面に置く（拾える）
                    thrown = false;
                    pickable = true;
                    hasShadow = false;
                }
            }

            // 寿命管理（任意）
            setLife(getLife() - 1);
            if (getLife() <= 0) startShatter();

            updateAnimation();
        } else {
            // 地面に置かれている状態の処理（拾える等）
            if (!pickable && !landed) {
                setLife(getLife() - 1);
                if (getLife() <= 0) startShatter();
            }
        }
    }

    private void updateAnimation() {

        if (shattering) return;

        setSpriteCounter(getSpriteCounter() + 1);
        if (getSpriteCounter() > 12) {
            setSpriteNum((getSpriteNum() % STONE_SCATTER_SPRITE_COUNT) + 1);
            setSpriteCounter(0);
        }
    }

    public void startShatter() {
        if (shattering) return;
        shattering = true;
        shatterFrame = 0;
        damageGiven = false;
        thrown = false;
        pickable = false;
        hasShadow = false;
        vx = vy = vz = 0;
        setAlive(true);
    }

    private void giveShatterDamageAndSpawnFragments() {

        // 1) ダメージ判定（必要なら）
        int monsterHit = gameWindow.getCollisionChecker().checkEntity(this, gameWindow.getMonster());
        if (monsterHit != 999 && monsterHit != -1) {
            gameWindow.getPlayer().damageMonster(monsterHit, getAttack(), getKnockBackPower());
        }

        // 3) 当たり判定を無効化して以降の判定を防ぐ
        this.setCollision(false);
        if (this.getSolidArea() != null) {
            this.getSolidArea().width = 0;
            this.getSolidArea().height = 0;
        }
    }

    public void onPickedUp() {
        setPickable(false);
        setAlive(false);
        setThrown(false);
        setLife(getMaxLife());
    }

    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
        this.thrown = true;
        setAlive(true);
    }

    /**
     * 所持中に Player が頭上に描画するためのスプライトを返す。
     * デフォルトは Down 方向の最初のフレームを使い、タイルサイズに合わせてリサイズする。
     */

    public BufferedImage getHeldSprite() {
        int ts = FrameApp.getTileSize();
        int downIndex = 1; // DIRS = {"Up","Down","Left","Right"} の Down
        int frameIndex = 0; // 所持時は最初のフレームを使う
        if (potSprites != null && potSprites.length > downIndex && potSprites[downIndex].length > frameIndex) {
            BufferedImage src = potSprites[downIndex][frameIndex];
            // 小さめにリサイズして返す
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
        int frameIndex = Math.max(0, Math.min(frame, potSprites[dirIndex].length - 1));
        return potSprites[dirIndex][frameIndex];
    }

    @Override
    public void draw(Graphics2D g2) {

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        int dirIndex = 1;
        if (Math.abs(vx) > Math.abs(vy)) dirIndex = vx < 0 ? 2 : 3;
        else dirIndex = vy < 0 ? 0 : 1;

        BufferedImage[][] useSprites = shattering ? fragmentSprites : potSprites;
        if (useSprites == null) return;
        if (dirIndex < 0 || dirIndex >= useSprites.length) return;
        if (useSprites[dirIndex] == null) return;

        int maxIndex = useSprites[dirIndex].length - 1;
        int spriteIndex;
        if (shattering) {
            if (shatterFrame >= STONE_SCATTER_SPRITE_COUNT) return;
            spriteIndex = Math.max(0, Math.min(shatterFrame, maxIndex));
        } else {
            spriteIndex = Math.max(0, Math.min(getSpriteNum(), maxIndex));
        }

        BufferedImage img = useSprites[dirIndex][spriteIndex];
        if (img == null) return;

        // スプライトサイズを img から取得
        int spriteW = img.getWidth();
        int spriteH = img.getHeight();

        // 描画位置（スプライト基準を中心にしている想定）
        int drawX = screenX;
        int drawY = (screenY - spriteH / 2) - (int) Math.round(z);
        System.out.println("DRAW screenY=" + screenY + " drawY=" + drawY + " z=" + z + " vz=" + vz + " vy=" + vy);

        g2.drawImage(img, drawX, drawY, null);

        // 影は先に描くと自然（地面→本体の順）
        if (hasShadow && !shattering) {
            int baseTile = FrameApp.getTileSize();
            float alpha = Math.max(0.15f, 1.0f - (float) (z / (baseTile * 2.0)));
            double scale = Math.max(0.4, 1.0 - (z / (baseTile * 3.0)));

            int shadowW = (int) (spriteW * scale);
            int shadowH = Math.max(2, shadowW / 4);

            // 影の位置は本体の地面投影（z を考慮しない screenY）を基準にする
            int shadowX = screenX - shadowW / 2;
            int shadowY = screenY - shadowH / 2 + baseTile / 3;

            Composite oldComp = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(new Color(0, 0, 0, 255));
            g2.fillOval(shadowX, shadowY, shadowW, shadowH);
            g2.setComposite(oldComp);
        }

        // 本体を描画
        g2.drawImage(img, drawX, drawY, null);
    }

    public boolean isPickable() {
        return pickable;
    }

    public double getGravity() {
        return gravity;
    }

    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }

    public void setHasShadow(boolean v) {
        this.hasShadow = v;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setVerticalVelocity(double vz) {
        this.vz = Math.max(-40.0, Math.min(40.0, vz));
    }
}