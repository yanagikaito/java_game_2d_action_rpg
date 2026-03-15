package entity.particle;

import window.GameWindow;
import entity.Entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

/**
 * 剣プロジェクタイルに紐づける発光＋粒子エフェクト。
 * - 剣スプライトの白ピクセルからグローを作成して重ね描画
 * - 白ピクセル位置に沿って粒子を定期的にスポーン（ランダムではなくスプライト基準）
 */
public class SwordAuraEffect {

    private final GameWindow gameWindow;
    private final Entity owner; // プロジェクタイルやプレイヤー
    private final BufferedImage sprite; // 剣のスプライト（向きに応じたもの）
    private final BufferedImage glowImage; // スプライトから作ったグロー画像（キャッシュ）
    private final java.util.List<Point> whiteCenters; // スプライト内の白ピクセル座標（キャッシュ済み）
    private final Random rnd = new Random();

    private int spawnCounter = 0;
    private final int spawnInterval; // フレーム間隔で粒子を生成（例: 4フレームに1回）
    private final Color particleColor;

    public SwordAuraEffect(GameWindow gw, Entity owner, BufferedImage sprite, BufferedImage glowImage, List<Point> whiteCenters) {
        this.gameWindow = gw;
        this.owner = owner;
        this.sprite = sprite;
        this.glowImage = glowImage;
        this.whiteCenters = whiteCenters;
        this.spawnInterval = 4;
        this.particleColor = new Color(255, 240, 200, 200);
    }

    // 毎フレーム呼ぶ（プロジェクタイルの update から）
    public void update() {
        if (!owner.getAlive()) return;

        // 粒子生成タイミング
        spawnCounter++;
        if (spawnCounter >= spawnInterval) {
            spawnCounter = 0;
            spawnParticlesFromWhitePixels();
        }
    }

    // 描画（プロジェクタイルの draw の前後どちらでも可。グローは剣の上に重ねる）
    public void draw(Graphics2D g2) {
        if (!owner.getAlive()) return;

        // プロジェクタイルのワールド座標（左上）を取得するメソッド名に合わせて調整
        int worldX = owner.getWorldX();
        int worldY = owner.getWorldY();

        // スプライト中心を基準にしているならオフセットを調整する
        int screenX = worldX - gameWindow.getPlayer().getWorldX()
                + gameWindow.getPlayer().getScreenX();
        int screenY = worldY - gameWindow.getPlayer().getWorldY()
                + gameWindow.getPlayer().getScreenY();

        // グロー画像を半透明で重ねる（Alpha は glowImage に含めておく）
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.drawImage(glowImage, screenX, screenY, null);
        g2.setComposite(old);
    }

    // 白ピクセル位置から粒子を生成（ランダムではなくスプライト上の位置を使う）
    private void spawnParticlesFromWhitePixels() {
        if (whiteCenters == null || whiteCenters.isEmpty()) return;

        // サンプリング率を低めにして負荷を抑える
        double sampleProb = 0.15; // 15% の白ピクセルから粒子を生成
        int spriteW = sprite.getWidth();
        int spriteH = sprite.getHeight();
        int centerX = spriteW / 2;
        int centerY = spriteH / 2;

        for (Point p : whiteCenters) {
            if (rnd.nextDouble() > sampleProb) continue;

            // スプライト内座標 -> ワールド座標（スプライト中心基準）
            int px = owner.getWorldX() + (p.x - centerX);
            int py = owner.getWorldY() + (p.y - centerY);

            // 粒子の初期角度は剣の向きに沿わせるかランダムに少し振る
            double angle = rnd.nextDouble() * Math.PI * 2;
            double speed = 0.6 + rnd.nextDouble() * 0.8;
            double gravity = 0.01 + rnd.nextDouble() * 0.03;
            float rot = (float) ((rnd.nextDouble() - 0.5) * 0.2f);
            int size = 2 + rnd.nextInt(3);
            int life = 12 + rnd.nextInt(12);

            SwordAuraParticle pfx = new SwordAuraParticle(
                    gameWindow,
                    owner,
                    px,
                    py,
                    particleColor,
                    size,
                    life,
                    angle,
                    speed,
                    gravity,
                    rot
            );
            gameWindow.getParticleList().add(pfx);
        }
    }
}