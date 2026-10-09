package ui.state.sleep;

import frame.FrameApp;
import game.GameState;
import key.KeyHandler;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public final class SleepMenuState implements SleepScreenState {

    private final SleepScreenContext sleepScreenContext;
    private final List<String> options = List.of("休む", "去る");

    private static final int OPTION_REST = 0;
    private static final int OPTION_EXIT = 1;

    private boolean skipNextEnter = true;

    public SleepMenuState(SleepScreenContext sleepScreenContext) {
        this.sleepScreenContext = sleepScreenContext;
    }

    @Override
    public void handleKey(int code) {
        KeyHandler keyHandler = sleepScreenContext.kh();

        // ダイアログ開いた直後のEnter連続暴発防止
        if (skipNextEnter && code == KeyEvent.VK_ENTER) {
            skipNextEnter = false;
            keyHandler.clearAllKeys();
            return;
        }

        // カーソル移動（WASD / 矢印キー対応）
        if ((code == KeyEvent.VK_W || code == KeyEvent.VK_UP) && keyHandler.getCommandNum() > 0) {
            keyHandler.setCommandNum(keyHandler.getCommandNum() - 1);
        }
        if ((code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) && keyHandler.getCommandNum() < options.size() - 1) {
            keyHandler.setCommandNum(keyHandler.getCommandNum() + 1);
        }

        // 決定処理
        if (code == KeyEvent.VK_ENTER) {
            switch (keyHandler.getCommandNum()) {
                case OPTION_REST -> {
                    keyHandler.clearAllKeys();
                    keyHandler.setCommandNum(0);
                    // ★ 睡眠実行Stateへ切り替える
                    sleepScreenContext.setState(new SleepingState(sleepScreenContext));
                }
                case OPTION_EXIT -> {
                    keyHandler.clearAllKeys();
                    keyHandler.setCommandNum(0);
                    sleepScreenContext.gw().setGameState(GameState.PLAY);
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        int cmd = sleepScreenContext.kh().getCommandNum();

        sleepScreenContext.ui().drawDialogueScreen(g2);
        sleepScreenContext.ui().drawSubWindow(g2, tileSize * 12, tileSize * 5, tileSize * 3, (int) (tileSize * 2.5));

        int x = tileSize * 13;
        int y = tileSize * 6;
        for (int i = 0; i < options.size(); i++) {
            g2.drawString(options.get(i), x, y + tileSize * i);
            if (i == cmd) {
                g2.drawString(">", x - 24, y + tileSize * i);
            }
        }
    }
}