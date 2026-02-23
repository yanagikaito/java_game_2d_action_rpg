package ui.state.trade.load;

import key.KeyHandler;
import ui.UI;
import window.GameWindow;

import java.awt.*;

public class LoadScreenContext {

    private LoadScreenState loadCurrentState;
    private final GameWindow gameWindow;
    private final UI ui;
    private final KeyHandler kh;

    public LoadScreenContext(GameWindow gw, UI ui) {
        this.gameWindow = gw;
        this.ui = ui;
        this.kh = gw.getKeyHandler();
        this.loadCurrentState = new LoadMenuState(this);
    }

    public void setState(LoadScreenState next) {
        this.loadCurrentState = next;
    }

    public void handleKey(int code) {
        // State にキー処理を集約し、必要に応じて内部で遷移
        loadCurrentState.handleKey(code);
    }

    public void draw(Graphics2D g2) {
        loadCurrentState.draw(g2);
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