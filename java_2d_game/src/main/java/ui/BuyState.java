package ui;

import entity.Entity;
import frame.FrameApp;
import key.KeyHandler;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class BuyState implements TradeScreenState {

    private final TradeScreenContext screenContext;

    public BuyState(TradeScreenContext screenContext) {
        this.screenContext = screenContext;
    }

    @Override
    public void handleKey(int code) {

        GameWindow gameWindow = screenContext.gw();
        KeyHandler keyHandler = screenContext.kh();

        gameWindow.getKeyHandler().npcInventory(code);

        // ESC や ENTER でメニューに戻す
        if (code == KeyEvent.VK_ESCAPE || (code == KeyEvent.VK_ENTER && keyHandler.isPlayerEnter())) {
            keyHandler.clearAllKeys();
            keyHandler.setCommandNum(0);
            screenContext.setState(new MenuState(screenContext));
        }
    }

    @Override
    public void draw(Graphics2D g2) {

        screenContext.ui().drawInventory(g2, screenContext.gw().getPlayer(), false);
        screenContext.ui().drawInventory(g2, screenContext.ui().getNpc(), true);

        // メッセージキーウィンドウ
        int tileSize = FrameApp.getTileSize();
        int x = tileSize * 2;
        int y = tileSize * 9;
        int width = (tileSize * 6) + tileSize / 2;
        int height = tileSize * 2;
        screenContext.ui().drawSubWindow(g2, x, y, width, height);
        messageKeyString(g2, x + 50, y + 50);

        // プレイヤー所持金ウィンドウ
        x = tileSize * 9;
        y = tileSize * 9;
        width = (tileSize * 6) + tileSize / 2;
        height = tileSize * 2;
        screenContext.ui().drawSubWindow(g2, x, y, width, height);
        playerCoinString(g2, x + 50, y + 50);

        // アイテム価格ウィンドウ
        int npcSlotRow = screenContext.ui().getNpcSlotRow();
        int npcSlotCol = screenContext.ui().getNpcSlotCol();
        int itemIndex = screenContext.ui().getItemIndexOnSlot(npcSlotRow, npcSlotCol);
        List<Entity> npcInv = screenContext.gw().getPlayer().getInventory();
        if (itemIndex < npcInv.size()) {
            x = (int) (tileSize * 5.5);
            y = (int) (tileSize * 5.5);
            width = (int) (tileSize * 2.5);
            height = tileSize;
            screenContext.ui().drawSubWindow(g2, x, y, width, height);
            g2.drawImage(screenContext.ui().getCoin(), x + 10, y + 8, 32, 32, null);

            int price = npcInv.get(itemIndex).getPrice();
            String text = "" + price;
            x = screenContext.ui().getXForAlignToRightText(g2, text, tileSize * 8);
            priceString(g2, text, x - 20, y + 32);
        }
    }

    private void messageKeyString(@NotNull Graphics2D g2, int x, int y) {
        g2.drawString("[ESC] 戻る", x, y);
    }

    private void playerCoinString(@NotNull Graphics2D g2, int x, int y) {
        g2.drawString("所持金: " + screenContext.gw().getPlayer().getCoin(), x, y);
    }

    private void priceString(@NotNull Graphics2D g2, String text, int x, int y) {
        g2.drawString(text, x, y);
    }
}