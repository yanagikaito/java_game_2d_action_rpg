package ui;

import key.KeyHandler;

import java.awt.*;
import java.awt.event.KeyEvent;

public class DialogueState implements TradeScreenState {

    private final TradeScreenContext screenContext;
    private final String message;
    private final TradeScreenState returnState;

    public DialogueState(TradeScreenContext context,
                         String message,
                         TradeScreenState returnState) {
        this.screenContext = context;
        this.message = message;
        this.returnState = returnState;
    }

    @Override
    public void handleKey(int code) {

        // Enterで元のステートに戻す
        if (code == KeyEvent.VK_ENTER) {
            KeyHandler kh = screenContext.kh();
            kh.clearAllKeys();
            screenContext.setState(returnState);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // 完全にダイアログ画面のみ描画
        UI ui = screenContext.ui();
        ui.setCurrentDialogueMessage(message);
        ui.drawDialogueScreen(g2);
    }
}