package object;

import entity.particle.SpriteUtil;
import entity.particle.SwordAuraEffect;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ObjSwordProjectile extends Projectile {

    private static final String[] DIRS = {"Up", "Down", "Left", "Right"};
    private static final int SPRITE_COUNT = 3;
    private static ObjSwordProjectile prototype = null;
    private SwordAuraEffect auraEffect;
    private BufferedImage representativeSprite;
    private GameWindow gameWindow;
    private static final Map<String, BufferedImage> glowCache = new HashMap<>(); // 方向キー -> glow image キャッシュ

    public ObjSwordProjectile(GameWindow gw) {
        super(gw, DIRS.length, SPRITE_COUNT);
        this.gameWindow = gw;
        setName("普通の剣");
        setSpeed(5);
        setMaxLife(80);
        setAttack(2);
        setKnockBackPower(5);
        setUseCost(1);
        setAlive(false);
        loadSprites();
    }

    @Override
    protected void loadSprites() {
        try {
            int ts = FrameApp.getTileSize();
            for (int i = 0; i < DIRS.length; i++) {
                for (int j = 0; j < SPRITE_COUNT; j++) {
                    String path = String.format(
                            "projectile/image-sword-normal%s-%d.gif",
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
        super.update(); // 既存処理
        if (auraEffect != null) auraEffect.update();
    }

    @Override
    public void draw(Graphics2D g2) {

        // 画面座標変換（共通関数を使うのが理想）
        int screenX = getWorldX() - getGameWindow().getPlayer().getWorldX()
                + getGameWindow().getPlayer().getScreenX();
        int screenY = getWorldY() - getGameWindow().getPlayer().getWorldY()
                + getGameWindow().getPlayer().getScreenY();

        // 実際の剣描画（既存の描画処理に合わせてください）
        g2.drawImage(representativeSprite, screenX, screenY, null);

        // エフェクト描画は同じ world->screen 式を使う
        if (auraEffect != null) auraEffect.draw(g2);
    }

    // 発射時に呼ぶメソッド（shootSword() から呼ぶ）
    public void onFire(String direction) {
        // 方向に応じたスプライトを取得
        representativeSprite = getSprite(direction);

        if (representativeSprite == null) return;

        // glow をスプライトごとにキャッシュ（キーは方向付きの簡易キー）
        String key = "sword_glow_" + direction;
        BufferedImage glow = glowCache.get(key);
        if (glow == null) {
            glow = SpriteGlowUtil.createGlowFromSprite(representativeSprite, new Color(255, 240, 200), 6);
            glowCache.put(key, glow);
        }

        // 白ピクセル座標を取得（キャッシュ付きユーティリティ）
        List<Point> whiteCenters = SpriteUtil.findWhitePixelCentersCached(representativeSprite);

        // SwordAuraEffect を生成して保持（update/draw で使う）
        auraEffect = new SwordAuraEffect(gameWindow, this, representativeSprite, glow, whiteCenters);
    }

    public static ObjSwordProjectile getPrototype(GameWindow gw) {
        if (prototype == null) {
            prototype = new ObjSwordProjectile(gw);
        }
        return prototype;
    }

    /**
     * 方向文字列 ("up","down","left","right") を受け取り、
     * その方向の代表スプライトを返す（フレームは0を返す）。
     */

    public BufferedImage getSprite(String dir) {
        if (dir == null) return sprites[1][0];
        String d = dir.toLowerCase();
        int idx;
        switch (d) {
            case "up":
                idx = 0;
                break;
            case "down":
                idx = 1;
                break;
            case "left":
                idx = 2;
                break;
            case "right":
                idx = 3;
                break;
            default:
                idx = 1;
                break;
        }
        return sprites[idx][0];
    }
}