package ui.state.trade;

import key.KeyHandler;
import ui.UI;

import java.awt.*;
import java.awt.event.KeyEvent;

public final class TradeDialogueState implements TradeScreenState {

    private final TradeScreenContext tradeScreenContext;
    private final String message;
    private final TradeScreenState returnState;

    public TradeDialogueState(TradeScreenContext context,
                              String message,
                              TradeScreenState returnState) {
        this.tradeScreenContext = context;
        this.message = message;
        this.returnState = returnState;
    }

    @Override
    public void handleKey(int code) {

        // Enterで元のステートに戻す
        if (code == KeyEvent.VK_ENTER) {
            KeyHandler kh = tradeScreenContext.kh();
            kh.clearAllKeys();
            tradeScreenContext.setState(returnState);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // 完全にダイアログ画面のみ描画
        UI ui = tradeScreenContext.ui();
        ui.setCurrentDialogueMessage(message);
        ui.drawDialogueScreen(g2);
    }
}