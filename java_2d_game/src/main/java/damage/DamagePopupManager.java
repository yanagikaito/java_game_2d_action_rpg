package damage;// DamagePopupManager.java (UI 側に置く)

import java.awt.*;
import java.util.*;
import java.util.List;

public class DamagePopupManager {
    private final Deque<DamagePopup> pool = new ArrayDeque<>();
    private final List<DamagePopup> active = new ArrayList<>();
    private final int poolSize;

    public DamagePopupManager(int poolSize) {
        this.poolSize = poolSize;
        for (int i = 0; i < poolSize; i++) pool.push(new DamagePopup());
    }

    public synchronized void pop(String text, int screenX, int screenY, DamagePopup.PopupVariant variant, int lifeFrames) {
        DamagePopup p = pool.isEmpty() ? new DamagePopup() : pool.pop();
        p.init(text, screenX, screenY, variant, lifeFrames);
        active.add(p);
    }

    public synchronized void updateAll() {
        Iterator<DamagePopup> it = active.iterator();
        while (it.hasNext()) {
            DamagePopup p = it.next();
            p.update();
            if (!p.isAlive()) {
                it.remove();
                pool.push(p);
            }
        }
    }

    public synchronized void drawAll(Graphics2D g) {
        for (DamagePopup p : active) {
            float alpha = p.alpha;
            Color color;
            switch (p.variant) {
                case DAMAGE:
                    color = new Color(180, 180, 255);
                    break;
                default:
                    color = new Color(255, 120, 120);
                    break;
            }
            Font font = new Font("SansSerif", Font.BOLD, (p.variant == DamagePopup.PopupVariant.DAMAGE) ? 28 : 20);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics(font);
            int w = fm.stringWidth(p.text);
            int h = fm.getAscent();

            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            // shadow
            g.setColor(new Color(0, 0, 0, (int) (200 * alpha)));
            g.drawString(p.text, p.screenX - w / 2 + 2, p.screenY - h / 2 + 2);
            // main
            g.setColor(color);
            g.drawString(p.text, p.screenX - w / 2, p.screenY - h / 2);
            g.setComposite(old);
        }
    }

    public synchronized int getLiveCount() {
        return active.size();
    }
}