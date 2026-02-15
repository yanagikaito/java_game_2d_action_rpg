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
        }
        if (code == KeyEvent.VK_S && keyHandler.getCommandNum() < options.size() - 1) {
            keyHandler.setCommandNum(keyHandler.getCommandNum() + 1);
        }

        // 決定
        if (code == KeyEvent.VK_ENTER) {
            System.out.println("DBG: SaveMenuState ENTER pressed, cmd=" + keyHandler.getCommandNum());
            saveScreenContext.setState(new SaveConfirmState(saveScreenContext, keyHandler.getCommandNum()));
            System.out.println("DBG: SaveMenuState setState -> SaveConfirmState done");

            switch (keyHandler.getCommandNum()) {

                case SLOT0 -> {
                    saveScreenContext.setState(new SaveConfirmState(saveScreenContext, SLOT0));
                    saveScreenContext.gw().setGameState(GameState.PLAY);
                }
                case SLOT1 -> {
                    saveScreenContext.setState(new SaveConfirmState(saveScreenContext, SLOT1));
                    saveScreenContext.gw().setGameState(GameState.PLAY);
                }
                case SLOT2 -> {
                    saveScreenContext.setState(new SaveConfirmState(saveScreenContext, SLOT2));
                    saveScreenContext.gw().setGameState(GameState.PLAY);
                }
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