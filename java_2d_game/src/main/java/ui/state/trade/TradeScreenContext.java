package ui.state.trade;

import key.KeyHandler;
import ui.UI;
import window.GameWindow;

import java.awt.*;

public final class TradeScreenContext {

    private TradeScreenState currentState;
    private final GameWindow gameWindow;
    private final UI ui;
    private final KeyHandler kh;

    public TradeScreenContext(GameWindow gw, UI ui) {
        this.gameWindow = gw;
        this.ui = ui;
        this.kh = gw.getKeyHandler();
        this.currentState = new TradeMenuState(this);
    }

    public void setState(TradeScreenState next) {
        this.currentState = next;
    }

    public void handleKey(int code) {
        // State にキー処理を集約し、必要に応じて内部で遷移
        currentState.handleKey(code);
    }

    public void draw(Graphics2D g2) {
        currentState.draw(g2);
        // ENTER フラグは描画後に必ずクリア
        kh.setPlayerEnter(false);
    }

    // Context 内から使いたい共通ユーティリティ
    public GameWindow gw() {
        return gameWindow;
    }

    public UI ui() {
        return ui;
    }

    public KeyHandler kh() {
        return kh;
    }
}