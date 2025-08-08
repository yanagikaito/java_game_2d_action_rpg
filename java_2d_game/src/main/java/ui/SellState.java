package ui;

import key.KeyHandler;

import java.awt.*;
import java.awt.event.KeyEvent;

public class SellState implements TradeScreenState {

    private final TradeScreenContext screenContext;

    public SellState(TradeScreenContext screenContext) {
        this.screenContext = screenContext;
    }

    @Override
    public void handleKey(int code) {
        KeyHandler kh = screenContext.kh();
        if (code == KeyEvent.VK_ESCAPE || (code == KeyEvent.VK_ENTER && kh.isPlayerEnter())) {
            kh.clearAllKeys();
            kh.setCommandNum(0);
            screenContext.setState(new MenuState(screenContext));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // 売却向け Inventory 描画ロジック
    }
}

