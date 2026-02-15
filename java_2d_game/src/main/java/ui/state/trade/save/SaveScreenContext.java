package ui.state.trade.save;

import key.KeyHandler;
import ui.UI;
import window.GameWindow;

import java.awt.*;

public class SaveScreenContext {

    private SaveScreenState saveCurrentState;
    private final GameWindow gameWindow;
    private final UI ui;
    private final KeyHandler kh;

    public SaveScreenContext(GameWindow gw, UI ui) {
        this.gameWindow = gw;
        this.ui = ui;
        this.kh = gw.getKeyHandler();
        this.saveCurrentState = new SaveMenuState(this);
    }

    public void setState(SaveScreenState next) {
        this.saveCurrentState = next;
    }

    public void handleKey(int code) {
        // State にキー処理を集約し、必要に応じて内部で遷移
        saveCurrentState.handleKey(code);
    }

    public void draw(Graphics2D g2) {
        saveCurrentState.draw(g2);
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