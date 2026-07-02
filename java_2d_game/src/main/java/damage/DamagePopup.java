package damage;

import ui.PopupVariant;

public class DamagePopup {
    public String text;
    public int screenX;
    public int screenY;
    public float vy;        // 上方向速度（負の値）
    public int life;        // 残フレーム
    public int maxLife;
    public float alpha;
    public PopupVariant variant;

    public void init(String text, int sx, int sy, PopupVariant variant, int lifeFrames) {
        this.text = text;
        this.screenX = sx;
        this.screenY = sy;
        this.variant = variant;
        this.maxLife = lifeFrames;
        this.life = lifeFrames;
        this.vy = -1.2f - (float) (Math.random() * 0.6);
        this.alpha = 1f;
    }

    public void update() {
        if (life <= 0) return;
        screenY += vy;
        vy *= 0.98f; // 減速（イージング）
        life--;
        alpha = Math.max(0f, (float) life / maxLife);
    }

    public boolean isAlive() {
        return life > 0;
    }
}