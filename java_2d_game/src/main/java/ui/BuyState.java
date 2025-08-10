package ui;

import entity.Entity;
import frame.FrameApp;
import key.KeyHandler;
import org.jetbrains.annotations.NotNull;
import player.Player;
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
        GameWindow gw = screenContext.gw();
        KeyHandler kh = screenContext.kh();

        // 1) カーソル移動
        gw.getKeyHandler().npcInventory(code);

        // 2) ENTER → 購入判定
        if (code == KeyEvent.VK_ENTER) {
            attemptPurchase();
            return;
        }

        // 3) ESC → メニューに戻る
        if (code == KeyEvent.VK_ESCAPE) {
            kh.clearAllKeys();
            kh.setCommandNum(0);
            screenContext.setState(new MenuState(screenContext));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        UI ui = screenContext.ui();
        GameWindow gw = screenContext.gw();
        int tileSize = FrameApp.getTileSize();

        // インベントリ描画
        ui.drawInventory(g2, gw.getPlayer(), false);
        ui.drawInventory(g2, ui.getNpc(), true);

        // メッセージキーウィンドウ
        drawMessageWindow(g2, tileSize);

        // 所持金ウィンドウ
        drawPlayerCoinWindow(g2, tileSize, gw.getPlayer().getCoin());

        // 選択中アイテムの価格ウィンドウ
        drawPriceWindow(g2, tileSize, ui, gw.getPlayer().getInventory());
    }

    private void attemptPurchase() {
        UI ui = screenContext.ui();
        GameWindow gameWindow = screenContext.gw();
        KeyHandler keyHandler = screenContext.kh();
        Player player = gameWindow.getPlayer();

        // スロット位置 → インデックス取得
        int row = ui.getNpcSlotRow();
        int col = ui.getNpcSlotCol();
        int idx = ui.getItemIndexOnSlot(row, col);
        List<Entity> shopInv = ui.getNpc().getInventory();

        // 範囲外チェック
        if (idx < 0 || idx >= shopInv.size()) {
            return;
        }

        Entity item = shopInv.get(idx);
        int price = item.getPrice();

        // 購入判定
        keyHandler.clearAllKeys();
        keyHandler.setCommandNum(0);
        if (price > player.getCoin()) {
            screenContext.setState(
                    new DialogueState(screenContext,
                            "所持金が不足している!",
                            this  // BuyState に戻る
                    )
            );
        } else if (player.getInventory().size() >= player.getMaxInventorySize()) {
            screenContext.setState(
                    new DialogueState(screenContext,
                            "これ以上購入できない!!",
                            this
                    )
            );
        } else {
            player.setCoin(player.getCoin() - price);
            player.getInventory().add(item);
        }
    }

    private void drawMessageWindow(Graphics2D g2, int tileSize) {
        int x = tileSize * 2;
        int y = tileSize * 9;
        int width = tileSize * 6 + tileSize / 2;
        int height = tileSize * 2;
        screenContext.ui().drawSubWindow(g2, x, y, width, height);
        g2.drawString("[ESC] 戻る", x + 50, y + 50);
    }

    private void drawPlayerCoinWindow(Graphics2D g2, int tileSize, int coin) {
        int x = tileSize * 9;
        int y = tileSize * 9;
        int width = tileSize * 6 + tileSize / 2;
        int height = tileSize * 2;
        screenContext.ui().drawSubWindow(g2, x, y, width, height);
        g2.drawString("所持金: " + coin, x + 50, y + 50);
    }

    private void drawPriceWindow(Graphics2D g2, int tileSize, @NotNull UI ui, List<Entity> inv) {
        int row = ui.getNpcSlotRow();
        int col = ui.getNpcSlotCol();
        int idx = ui.getItemIndexOnSlot(row, col);
        if (idx < 0 || idx >= inv.size()) return;

        int x = (int) (tileSize * 5.5), y = (int) (tileSize * 5.5);
        int width = (int) (tileSize * 2.5);
        int height = tileSize;
        ui.drawSubWindow(g2, x, y, width, height);
        g2.drawImage(ui.getCoin(), x + 10, y + 8, 32, 32, null);

        String text = String.valueOf(inv.get(idx).getPrice());
        int tx = ui.getXForAlignToRightText(g2, text, tileSize * 8) - 20;
        g2.drawString(text, tx, y + 32);
    }
}