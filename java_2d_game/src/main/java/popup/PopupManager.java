package popup;

import entity.Entity;
import frame.FrameApp;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PopupManager {
    private final Deque<Popup> pool = new ArrayDeque<>();
    private final List<Popup> active = new ArrayList<>();
    private final int poolSize;

    public PopupManager(int poolSize) {
        this.poolSize = poolSize;
        for (int i = 0; i < poolSize; i++) pool.push(new Popup());
    }

    public synchronized void pop(String text, int screenX, int screenY, PopupVariant variant, int lifeFrames) {
        Popup p = pool.isEmpty() ? new Popup() : pool.pop();
        p.init(text, null, screenX, screenY, variant, lifeFrames);
        active.add(p);
    }

    // アイテム表示用（Entity を直接渡す）
    public synchronized void popItem(Entity item, int screenX, int screenY, PopupVariant variant, int lifeFrames) {
        if (item == null) return;
        String name = item.getName(); // 表示名
        Image icon = item.getImage();  // アイテム画像（null でも可）
        Popup p = pool.isEmpty() ? new Popup() : pool.pop();
        p.init(name, icon, screenX, screenY, variant, lifeFrames);
        active.add(p);
    }

    public synchronized void updateAll() {
        Iterator<Popup> it = active.iterator();
        while (it.hasNext()) {
            Popup p = it.next();
            p.update();
            if (!p.isAlive()) {
                it.remove();
                pool.push(p);
            }
        }
    }

    public synchronized void drawAll(Graphics2D g2) {
        for (Popup p : active) {
            float alpha = p.alpha;
            Color color;
            switch (p.variant) {
                case PLAYER_DAMAGE:
                    color = new Color(180, 180, 255);
                    break;
                case MONSTER_DAMAGE:
                    color = new Color(255, 120, 120);
                    break;
                case HEAL:
                    color = new Color(100, 255, 150);
                    break;
                case XP:
                    color = new Color(255, 215, 0);
                    break;
                case DROP:
                    color = new Color(255, 240, 160);
                    break;
                default:
                    color = new Color(0, 0, 0);
                    break;
            }

            int fontSize = (p.variant == PopupVariant.PLAYER_DAMAGE) ? 28 : 20;
            int tileSize = FrameApp.getTileSize();
            Font font = new Font("SansSerif", Font.BOLD, fontSize);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics(font);

            String display = p.text;

            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            if (p.icon != null && p.variant == PopupVariant.DROP) {
                int iconW = tileSize - 16;
                int iconH = tileSize - 16;
                Image scaled = p.icon.getScaledInstance(iconW, iconH, Image.SCALE_SMOOTH);
                int iconX = p.screenX - iconW / 2;
                int iconY = p.screenY - iconH - 8;
                g2.drawImage(scaled, iconX, iconY, null);

                // デバッグ
//                int textY = iconY + iconH + fm.getAscent();
//                // shadow
//                g.setColor(new Color(0, 0, 0, (int) (200 * alpha)));
//                g.drawString(display, p.screenX - fm.stringWidth(display) / 2 + 2, textY + 2);
//                // main
//                g.setColor(color);
//                g.drawString(display, p.screenX - fm.stringWidth(display) / 2, textY);
            } else {
                int w = fm.stringWidth(display);
                int h = fm.getAscent();
                // shadow
                g2.setColor(new Color(0, 0, 0, (int) (200 * alpha)));
                g2.drawString(display, p.screenX - w / 2 + 2, p.screenY - h / 2 + 2);
                // main
                g2.setColor(color);
                g2.drawString(display, p.screenX - w / 2, p.screenY - h / 2);
            }

            g2.setComposite(old);
        }
    }
}