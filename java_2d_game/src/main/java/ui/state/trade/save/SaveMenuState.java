package ui.state.trade.save;

import game.GameState;
import key.KeyHandler;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static ui.state.trade.save.SaveOptionConstants.*;

public class SaveMenuState implements SaveScreenState {
    private final SaveScreenContext saveScreenContext;
    private final List<String> options = List.of("slot0", "slot1", "slot2");
    private boolean skipNextEnter = true;

    public SaveMenuState(SaveScreenContext saveScreenContext) {
        this.saveScreenContext = saveScreenContext;
    }

    @Override
    public void handleKey(int code) {

        KeyHandler keyHandler = saveScreenContext.kh();

        System.out.println("DBG: SaveMenuState.handleKey called code=" + code + " cmd=" + saveScreenContext.kh().getCommandNum());


        if (skipNextEnter && code == KeyEvent.VK_ENTER) {
            skipNextEnter = false;
            keyHandler.clearAllKeys();
            return;
        }
        skipNextEnter = true;

        // カーソル移動（上下のみ）
        if (code == KeyEvent.VK_W && keyHandler.getCommandNum() > 0) {
            keyHandler.setCommandNum(keyHandler.getCommandNum() - 1);
            return;
        }
        if (code == KeyEvent.VK_S && keyHandler.getCommandNum() < options.size() - 1) {
            keyHandler.setCommandNum(keyHandler.getCommandNum() + 1);
            return;
        }

        // ESC 押下でメニューを閉じて Play に戻す
        if (code == KeyEvent.VK_ESCAPE) {
            System.out.println("DBG: SaveMenuState ESC pressed - closing save menu");
            try {
                saveScreenContext.ui().closeSaveMenuUI();
            } catch (Throwable ignored) {
            }
            saveScreenContext.kh().clearAllKeys();
            saveScreenContext.gw().setGameState(GameState.PLAY);
            return;
        }

        // 決定
        if (code == KeyEvent.VK_ENTER) {

            int slot = keyHandler.getCommandNum();

            if (slot == SLOT0) {
                System.out.println("DBG: SaveMenuState ENTER pressed, cmd=" + keyHandler.getCommandNum());
                saveScreenContext.setState(new SaveConfirmState(saveScreenContext, slot));
                return;
            }
            if (slot == SLOT1) {
                System.out.println("DBG: SaveMenuState ENTER pressed, cmd=" + keyHandler.getCommandNum());
                saveScreenContext.setState(new SaveConfirmState(saveScreenContext, slot));
                return;
            }
            if (slot == SLOT2) {
                System.out.println("DBG: SaveMenuState ENTER pressed, cmd=" + keyHandler.getCommandNum());
                saveScreenContext.setState(new SaveConfirmState(saveScreenContext, slot));
                return;
            }
        }
        System.out.println("DBG: SaveMenuState ENTER pressed, cmd=" + keyHandler.getCommandNum());
        saveScreenContext.setState(new SaveConfirmState(saveScreenContext, keyHandler.getCommandNum()));
        System.out.println("DBG: SaveMenuState setState -> SaveConfirmState done");
    }

    @Override
    public void draw(Graphics2D g2) {
        saveScreenContext.ui().drawDialogueSaveScreen(g2);
    }
}