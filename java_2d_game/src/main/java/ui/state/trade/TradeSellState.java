package ui.state.trade;

import entity.Entity;
import frame.FrameApp;
import key.KeyHandler;
import player.Player;
import ui.UI;
import window.GameWindow;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public final class TradeSellState implements TradeScreenState {

    private final TradeScreenContext tradeScreenContext;

    public TradeSellState(TradeScreenContext tradeScreenContext) {
        this.tradeScreenContext = tradeScreenContext;
    }

    @Override
    public void handleKey(int code) {

        GameWindow gameWindow = tradeScreenContext.gw();
        KeyHandler keyHandler = tradeScreenContext.kh();

        // 1) カーソル移動
        gameWindow.getKeyHandler().playerInventory(code);

        // 2) ENTER → 売却判定
        if (code == KeyEvent.VK_ENTER) {
            attemptSell();
            return;
        }

        if (code == KeyEvent.VK_ESCAPE || (code == KeyEvent.VK_ENTER && keyHandler.isPlayerEnter())) {
            keyHandler.clearAllKeys();
            keyHandler.setCommandNum(0);
            tradeScreenContext.setState(new TradeMenuState(tradeScreenContext));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        UI ui = tradeScreenContext.ui();
        GameWindow gw = tradeScreenContext.gw();
        int tileSize = FrameApp.getTileSize();

        // インベントリ描画
        ui.drawInventory(g2, gw.getPlayer(), true);

        // メッセージキーウィンドウ
        drawMessageWindow(g2, tileSize);

        // 所持金ウィンドウ
        drawPlayerCoinWindow(g2, tileSize, gw.getPlayer().getCoin());

        // 選択中アイテムの価格ウィンドウ
        drawPriceWindow(g2, tileSize, ui, gw.getPlayer().getInventory());
    }

    private void attemptSell() {

        UI ui = tradeScreenContext.ui();
        GameWindow gameWindow = tradeScreenContext.gw();
        KeyHandler keyHandler = tradeScreenContext.kh();
        Player player = gameWindow.getPlayer();
        List<Entity> playerInv = player.getInventory();

        // 1) 在庫がそもそも空なら「売却するアイテムがない！」
        if (playerInv.isEmpty()) {
            showNoItemDialogue();
            return;
        }

        // スロット位置 → インデックス取得
        int row = ui.getPlayerSlotRow();
        int col = ui.getPlayerSlotCol();
        int idx = ui.getItemIndexOnSlot(row, col);

        // 範囲外チェック
        if (idx < 0 || idx >= playerInv.size()) {
            return;
        }

        // 装備判定の追加
        Entity item = playerInv.get(idx);
        if (player.isEquipped(item)) {
            showCannotSellEquippedDialogue();
            return;
        }

        // 4) 正常売却処理
        int price = item.getPrice();

        keyHandler.clearAllKeys();
        keyHandler.setCommandNum(0);
        player.setCoin(player.getCoin() + price);
        playerInv.remove(idx);
    }

    private void showNoItemDialogue() {
        tradeScreenContext.setState(
                new TradeDialogueState(
                        tradeScreenContext,
                        "売却するアイテムがない!",
                        this   // SellState に戻る
                )
        );
    }

    private void showCannotSellEquippedDialogue() {
        tradeScreenContext.setState(
                new TradeDialogueState(
                        tradeScreenContext,
                        "装備中のアイテムは売却できない!",
                        this  // SellState に戻る
                )
        );
    }

    private void drawMessageWindow(Graphics2D g2, int tileSize) {

        int x = tileSize * 2;
        int y = tileSize * 9;
        int width = tileSize * 6 + tileSize / 2;
        int height = tileSize * 2;
        tradeScreenContext.ui().drawSubWindow(g2, x, y, width, height);
        g2.drawString("[ESC] 戻る", x + 50, y + 50);
    }

    private void drawPlayerCoinWindow(Graphics2D g2, int tileSize, int coin) {

        int x = tileSize * 9;
        int y = tileSize * 9;
        int width = tileSize * 6 + tileSize / 2;
        int height = tileSize * 2;
        tradeScreenContext.ui().drawSubWindow(g2, x, y, width, height);
        g2.drawString("所持金: " + coin, x + 50, y + 50);
    }

    private void drawPriceWindow(Graphics2D g2, int tileSize, UI ui, List<Entity> inv) {

        int row = ui.getPlayerSlotRow();
        int col = ui.getPlayerSlotCol();
        int idx = ui.getItemIndexOnSlot(row, col);
        if (idx < 0 || idx >= inv.size()) return;

        int x = (int) (tileSize * 12.5);
        int y = (int) (tileSize * 5.5);
        int width = (int) (tileSize * 2.5);
        int height = tileSize;
        ui.drawSubWindow(g2, x, y, width, height);
        g2.drawImage(ui.getCoin(), x + 10, y + 8, 32, 32, null);

        String text = String.valueOf(inv.get(idx).getPrice());
        int tx = ui.getXForAlignToRightText(g2, text, tileSize * 15) - 20;
        g2.drawString(text, tx, y + 32);
    }
}