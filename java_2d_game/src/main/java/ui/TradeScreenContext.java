package ui;

import key.KeyHandler;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import java.awt.*;

public class TradeScreenContext {
    private TradeScreenState currentState;
    private final GameWindow gameWindow;
    private final UI ui;
    private final KeyHandler kh;

    public TradeScreenContext(@NotNull GameWindow gw, UI ui) {
        this.gameWindow = gw;
        this.ui = ui;
        this.kh = gw.getKeyHandler();
        this.currentState = new MenuState(this);
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