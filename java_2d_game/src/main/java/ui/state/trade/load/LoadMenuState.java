package ui.state.trade.load;

import game.GameState;
import key.KeyHandler;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static ui.state.trade.load.LoadOptionConstants.*;

public class LoadMenuState implements LoadScreenState {

    private final LoadScreenContext loadScreenContext;
    private final List<String> options = List.of("slot0", "slot1", "slot2");
    private boolean skipNextEnter = true;

    public LoadMenuState(LoadScreenContext loadScreenContext) {
        this.loadScreenContext = loadScreenContext;
    }

    @Override
    public void handleKey(int code) {

        KeyHandler keyHandler = loadScreenContext.kh();

        System.out.println("DBG: LoadMenuState.handleKey called code=" + code + " cmd=" + loadScreenContext.kh().getCommandNum());


        if (skipNextEnter && code == KeyEvent.VK_ENTER) {
            skipNextEnter = false;
            keyHandler.clearAllKeys();
            return;
        }
        skipNextEnter = true;

        // カーソル移動（上下のみ）
        if (code == KeyEvent.VK_W && keyHandler.getCommandNum() > 0) {
            keyHandler.setCommandNum(keyHandler.getCommandNum() - 1);
            loadScreenContext.kh().clearAllKeys();
            return;
        }
        if (code == KeyEvent.VK_S && keyHandler.getCommandNum() < options.size() - 1) {
            keyHandler.setCommandNum(keyHandler.getCommandNum() + 1);
            loadScreenContext.kh().clearAllKeys();
            return;
        }

        // ESC 押下でメニューを閉じて TITLE に戻す
        if (code == KeyEvent.VK_ESCAPE) {
            System.out.println("DBG: SaveMenuState ESC pressed - closing save menu");
            try {
                loadScreenContext.ui().closeSaveMenuUI();
            } catch (Throwable ignored) {
            }
            loadScreenContext.kh().clearAllKeys();
            loadScreenContext.gw().setGameState(GameState.TITLE);
            return;
        }

        // 決定
        if (code == KeyEvent.VK_ENTER) {

            int slot = keyHandler.getCommandNum();

            if (slot == LOAD_SLOT0) {
                System.out.println("DBG: LoadMenuState ENTER pressed, cmd=" + keyHandler.getCommandNum());
                loadScreenContext.setState(new LoadConfirmState(loadScreenContext, slot));
                loadScreenContext.ui().confirmLoadSelectedSlot(slot);
                return;
            }
            if (slot == LOAD_SLOT1) {
                System.out.println("DBG: LoadMenuState ENTER pressed, cmd=" + keyHandler.getCommandNum());
                loadScreenContext.setState(new LoadConfirmState(loadScreenContext, slot));
                loadScreenContext.ui().confirmLoadSelectedSlot(slot);
                return;
            }
            if (slot == LOAD_SLOT2) {
                System.out.println("DBG: LoadMenuState ENTER pressed, cmd=" + keyHandler.getCommandNum());
                loadScreenContext.setState(new LoadConfirmState(loadScreenContext, slot));
                loadScreenContext.ui().confirmLoadSelectedSlot(slot);
                return;
            }
        }
        System.out.println("DBG: LoadMenuState ENTER pressed, cmd=" + keyHandler.getCommandNum());
        loadScreenContext.setState(new LoadConfirmState(loadScreenContext, keyHandler.getCommandNum()));
        System.out.println("DBG: LoadMenuState setState -> LoadConfirmState done");
    }

    @Override
    public void draw(Graphics2D g2) {
        loadScreenContext.ui().drawDialogueLoadScreen(g2);
    }
}