package ui;

import key.KeyHandler;
import window.GameWindow;

import java.awt.*;
import java.awt.event.KeyEvent;

public class BuyState implements TradeScreenState {

    private final TradeScreenContext screenContext;

    public BuyState(TradeScreenContext screenContext) {
        this.screenContext = screenContext;
    }

    @Override
    public void handleKey(int code) {

        GameWindow gameWindow = screenContext.gw();
        KeyHandler keyHandler = screenContext.kh();

        gameWindow.getKeyHandler().npcInventory(code);

        // ESC や ENTER でメニューに戻す
        if (code == KeyEvent.VK_ESCAPE || (code == KeyEvent.VK_ENTER && keyHandler.isPlayerEnter())) {
            keyHandler.clearAllKeys();
            keyHandler.setCommandNum(0);
            screenContext.setState(new MenuState(screenContext));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        screenContext.ui().drawInventory(g2, screenContext.gw().getPlayer(), false);
        screenContext.ui().drawInventory(g2, screenContext.ui().getNpc(), true);
    }
}