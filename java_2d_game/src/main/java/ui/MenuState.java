package ui;

import frame.FrameApp;
import key.KeyHandler;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static ui.TradeOptionConstants.*;

public class MenuState implements TradeScreenState {

    private final TradeScreenContext screenContext;
    private final List<String> options = List.of("買う", "売る", "去る");
    private boolean skipNextEnter = true;

    public MenuState(TradeScreenContext screenContext) {
        this.screenContext = screenContext;
    }

    @Override
    public void handleKey(int code) {

        KeyHandler keyHandler = screenContext.kh();

        if (skipNextEnter && code == KeyEvent.VK_ENTER) {
            skipNextEnter = false;
            keyHandler.clearAllKeys();
            return;
        }
        skipNextEnter = true;

        // カーソル移動（上下のみ）
        if (code == KeyEvent.VK_W && keyHandler.getCommandNum() > 0) {
            keyHandler.setCommandNum(keyHandler.getCommandNum() - 1);
        }
        if (code == KeyEvent.VK_S && keyHandler.getCommandNum() < options.size() - 1) {
            keyHandler.setCommandNum(keyHandler.getCommandNum() + 1);
        }

        // 決定
        if (code == KeyEvent.VK_ENTER) {
            switch (keyHandler.getCommandNum()) {
                case BUY -> screenContext.setState(new BuyState(screenContext));
                case SELL -> screenContext.setState(new SellState(screenContext));
                case EXIT -> {
                    keyHandler.clearAllKeys();
                    keyHandler.setCommandNum(0);
                    screenContext.gw().setGameState(screenContext.gw().getPlayState());
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int ts = FrameApp.getTileSize();
        int cmd = screenContext.kh().getCommandNum();

        screenContext.ui().drawDialogueScreen(g2);
        screenContext.ui().drawSubWindow(g2, ts * 12, ts * 5, ts * 3, (int) (ts * 3.5));

        int x = ts * 13, y = ts * 6;
        for (int i = 0; i < options.size(); i++) {
            g2.drawString(options.get(i), x, y + ts * i);
            if (i == cmd) {
                g2.drawString(">", x - 24, y + ts * i);
            }
        }
    }
}