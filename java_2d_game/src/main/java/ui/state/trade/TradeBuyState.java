package ui.state.trade;

import entity.Entity;
import frame.FrameApp;
import key.KeyHandler;
import npc.NpcMerChant;
import object.*;
import player.Player;
import ui.UI;
import window.GameWindow;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public final class TradeBuyState implements TradeScreenState {

    private final TradeScreenContext tradeScreenContext;
    private Entity shopEntity;

    public TradeBuyState(TradeScreenContext tradeScreenContext) {
        this.tradeScreenContext = tradeScreenContext;
    }

    @Override
    public void handleKey(int code) {

        GameWindow gameWindow = tradeScreenContext.gw();
        KeyHandler keyHandler = tradeScreenContext.kh();

        // 1) カーソル移動
        gameWindow.getKeyHandler().npcInventory(code);

        // 2) ENTER → 購入判定
        if (code == KeyEvent.VK_ENTER) {
            attemptPurchase();
            return;
        }

        // 3) ESC → メニューに戻る
        if (code == KeyEvent.VK_ESCAPE) {
            keyHandler.clearAllKeys();
            keyHandler.setCommandNum(0);
            tradeScreenContext.setState(new TradeMenuState(tradeScreenContext));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        UI ui = tradeScreenContext.ui();
        GameWindow gameWindow = tradeScreenContext.gw();
        int tileSize = FrameApp.getTileSize();
        Entity npc = ui.getNpc();

        if (npc == null) return;

        ArrayList<Entity> shopItems;

        if (npc instanceof NpcMerChant) {
            shopItems = npc.getShopItems();
        } else {
            shopItems = createDefaultShopItems();
        }

        this.shopEntity = new Entity(tradeScreenContext.gw()) {
            @Override
            public ArrayList<Entity> getInventory() {
                return shopItems;
            }
        };

        ui.drawInventory(g2, tradeScreenContext.gw().getPlayer(), false);
        ui.drawInventory(g2, this.shopEntity, true);

        // メッセージキーウィンドウ
        drawMessageWindow(g2, tileSize);

        // 所持金ウィンドウ
        drawPlayerCoinWindow(g2, tileSize, gameWindow.getPlayer().getCoin());

        // 選択中アイテムの価格ウィンドウ
        drawPriceWindow(g2, tileSize, ui, createDefaultShopItems());
    }

    private ArrayList<Entity> createDefaultShopItems() {

        GameWindow gw = tradeScreenContext.gw();
        ArrayList<Entity> items = new ArrayList<>();
        items.add(new ObjSwordNormal(gw));
        items.add(new ObjShieldWood(gw));
        items.add(new ObjRedPotion(gw));
        items.add(new ObjGreenPotion(gw));
        items.add(new ObjBluePotion(gw));
        return items;
    }

    private void attemptPurchase() {

        UI ui = tradeScreenContext.ui();
        GameWindow gameWindow = tradeScreenContext.gw();
        KeyHandler keyHandler = tradeScreenContext.kh();
        Player player = gameWindow.getPlayer();

        if (shopEntity == null) return;
        System.out.println("shopEntity = " + shopEntity);

        // shopEntity のインベントリを使う
        List<Entity> shopInv = shopEntity.getInventory();

        // スロット位置 → インデックス取得
        int row = ui.getNpcSlotRow();
        int col = ui.getNpcSlotCol();
        int idx = ui.getItemIndexOnSlot(row, col);

        // 範囲外チェック
        if (idx < 0 || idx >= shopInv.size()) {
            System.err.println("attemptPurchase: item is null at index " + idx);
            return;
        }

        Entity item = shopInv.get(idx);
        int price = item.getPrice();

        // 購入判定
        keyHandler.clearAllKeys();
        keyHandler.setCommandNum(0);
        if (price > player.getCoin()) {
            tradeScreenContext.setState(
                    new TradeDialogueState(tradeScreenContext,
                            "所持金が不足している!",
                            this  // BuyState に戻る
                    )
            );
        } else if (player.getInventory().size() >= player.getMaxInventorySize()) {
            tradeScreenContext.setState(
                    new TradeDialogueState(tradeScreenContext,
                            "これ以上購入できない!!",
                            this // BuyState に戻る
                    )
            );
        } else {
            player.setCoin(player.getCoin() - price);
            player.getInventory().add(item.copy());
        }
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

        int row = ui.getNpcSlotRow();
        int col = ui.getNpcSlotCol();
        int idx = ui.getItemIndexOnSlot(row, col);
        if (idx < 0 || idx >= inv.size()) return;

        int x = (int) (tileSize * 5.5);
        int y = (int) (tileSize * 5.5);
        int width = (int) (tileSize * 2.5);
        int height = tileSize;
        ui.drawSubWindow(g2, x, y, width, height);
        g2.drawImage(ui.getCoin(), x + 10, y + 8, 32, 32, null);

        String text = String.valueOf(inv.get(idx).getPrice());
        int tx = ui.getXForAlignToRightText(g2, text, tileSize * 8) - 20;
        g2.drawString(text, tx, y + 32);
    }
}