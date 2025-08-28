package ui;

import entity.Entity;
import frame.FrameApp;
import npc.NpcMerChant;
import object.*;
import player.Player;
import save.SaveManager;
import window.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class UI {

    private final GameWindow gameWindow;

    // 使用するフォント
    private final Font arial40;
    private final Font arial80Bold;

    private final int MANA_BAR_X = 5;
    private final int MANA_BAR_Y = 60;
    private final int MANA_BAR_WIDTH = 20;
    private final int MANA_BAR_HEIGHT = 200;

    // メッセージ表示用
    private boolean messageOn;
    private String currentDialogueMessage;
    private ArrayList<String> message = new ArrayList<>();
    private ArrayList<Integer> messageCounter = new ArrayList<>();

    private BufferedImage heartFull;
    private BufferedImage heartHalf;
    private BufferedImage heartBlank;
    private BufferedImage coin;

    private int playerSlotCol = 0;
    private int playerSlotRow = 0;

    private int npcSlotCol = 0;
    private int npcSlotRow = 0;

    private boolean dialogueOn = false;
    private Entity npc;
    private int subState;

    private final ScreenContext tradeCtx;

    public UI(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.tradeCtx = new ScreenContext(gameWindow, this);
        this.arial40 = new Font("エリア", Font.PLAIN, 40);
        this.arial80Bold = new Font("エリア", Font.BOLD, 80);
        this.messageOn = false;
        this.currentDialogueMessage = "";
        this.npc = new NpcMerChant(gameWindow);

        Entity heart = new ObjHeart(gameWindow);
        heartFull = heart.getImage();
        heartHalf = heart.getImage2();
        heartBlank = heart.getImage3();

        Entity bronzeCoin = new ObjCoinBronze(gameWindow);
        coin = bronzeCoin.getImage();
    }

    public void addMessage(String text) {
        message.add(text);
        messageCounter.add(0);
    }

    public void addDialogue(String text) {
        if (text == null || text.isEmpty()) {
            dialogueOn = false;
            currentDialogueMessage = "";
        } else {
            dialogueOn = true;
            currentDialogueMessage = text;
        }
    }

    public void draw(Graphics2D g2) {

        g2.setFont(arial40);
        g2.setColor(Color.white);

        int gameState = gameWindow.getGameState();
        int titleState = gameWindow.getTitleState();
        int playState = gameWindow.getPlayState();
        int pauseState = gameWindow.getPauseState();
        int dialogueState = gameWindow.getDialogueState();
        int charState = gameWindow.getCharacterState();
        int gameOverState = gameWindow.getGameOverState();
        int tradeState = gameWindow.getTradeState();
        int saveState = gameWindow.getSaveState();

        if (gameState == titleState) {

            drawTitleScreen(g2);

        } else if (gameState == playState) {

            gameWindow.setDialogueActive(false);
            drawPlayerLife(g2);
            drawBattleLogMessage(g2);
            drawManaBar(g2);

        } else if (gameState == pauseState) {

            gameWindow.setDialogueActive(false);
            drawPlayerLife(g2);
            drawPauseScreen(g2);

        } else if (gameState == dialogueState) {

            gameWindow.setDialogueActive(true);
            drawPlayerLife(g2);
            drawDialogueScreen(g2);

        } else if (gameState == charState) {

            gameWindow.setDialogueActive(false);
            drawCharacterScreen(g2);
            drawInventory(g2, gameWindow.getPlayer(), true);

        } else if (gameState == gameOverState) {
            drawGameOverScreen(g2);

        } else if (gameState == tradeState) {
            drawTradeScreen(g2);

        } else if (gameState == saveState) {
            drawSaveScreen(g2);
        }


        if (messageOn) {
            drawMessage(g2);
        }
    }

    private void drawSaveScreen(Graphics2D g2) {

        // 背景を暗く
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, FrameApp.getScreenWidth(), FrameApp.getScreenHeight());

        // セーブ中テキスト
        g2.setFont(arial40);
        g2.setColor(Color.white);
        String text = "ゲームをセーブしますか？";
        int x = getXForCenteredText(g2, text);
        int y = FrameApp.getScreenHeight() / 2 - 40;

        g2.drawString(text, x, y);

        // オプション表示
        g2.setFont(arial40);
        text = "はい";
        x = getXForCenteredText(g2, text);
        y += 60;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 0) {
            g2.drawString(">", x - 40, y);
            if (gameWindow.getKeyHandler().isPlayerEnter() == true) {
                SaveManager.saveGame(1, gameWindow.getPlayer());
                addMessage("セーブしました。");
                subState = 1;
                gameWindow.setGameState(gameWindow.getPlayState());
            }
        }

        text = "いいえ";
        x = getXForCenteredText(g2, text);
        y += 45;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 1) {
            g2.drawString(">", x - 40, y);
        }
    }

    private void drawTitleScreen(Graphics2D g2) {

        String text;
        int x;
        int y;
        int tileSize = FrameApp.getTileSize();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.setFont(arial80Bold.deriveFont(Font.PLAIN, 80F));

        text = "GreenFantasy";
        g2.setColor(Color.GREEN);
        x = getXForCenteredText(g2, text);
        y = tileSize * 3;
        g2.drawString(text, x, y);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x - 4, y - 4);

        g2.setFont(g2.getFont().deriveFont(50f));

        text = "ニューゲーム";
        x = getXForCenteredText(g2, text);
        y += tileSize * 4;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 0) {
            g2.drawString(">", x - 40, y);
        }

        text = "ゲームロード";
        x = getXForCenteredText(g2, text);
        y += 55;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 1) {
            g2.drawString(">", x - 40, y);
        }

        text = "ゲーム終了";
        x = getXForCenteredText(g2, text);
        y += 55;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 2) {
            g2.drawString(">", x - 40, y);
        }
    }

    public void drawGameOverScreen(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, FrameApp.getScreenWidth(), FrameApp.getScreenHeight());

        int x;
        int y;
        String text;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 80f));

        text = "ゲームオーバー";
        g2.setColor(Color.BLACK);
        x = getXForCenteredText(g2, text);
        y = tileSize * 4;
        g2.drawString(text, x, y);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x - 4, y - 4);

        g2.setFont(g2.getFont().deriveFont(50f));
        text = "リトライ";
        x = getXForCenteredText(g2, text);
        y += tileSize * 4;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 0) {
            g2.drawString(">", x - 40, y);
        }

        text = "タイトルに戻る";
        x = getXForCenteredText(g2, text);
        y += 55;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 1) {
            g2.drawString(">", x - 40, y);
        }
    }

    public void drawPlayerLife(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        int startX = tileSize / 2;
        int y = tileSize / 2;

        var player = gameWindow.getPlayer();

        int maxHearts = player.getMaxLife() / 2;
        int life = player.getLife();
        int fullHearts = life / 2;
        int halfHearts = life % 2;

        for (int i = 0; i < maxHearts; i++) {
            int x = startX + i * tileSize;
            if (i < fullHearts) {
                g2.drawImage(heartFull, x, y, tileSize, tileSize, null);
            } else if (i == fullHearts && halfHearts == 1) {
                g2.drawImage(heartHalf, x, y, tileSize, tileSize, null);
            } else {
                g2.drawImage(heartBlank, x, y, tileSize, tileSize, null);
            }
        }
    }

    private void drawManaBar(Graphics2D g2) {

        Player p = gameWindow.getPlayer();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int x = MANA_BAR_X;
        int y = MANA_BAR_Y;
        int w = MANA_BAR_WIDTH;
        int h = MANA_BAR_HEIGHT;
        int arc = 20;

        g2.setColor(Color.BLACK);
        g2.fillRoundRect(x, y, w, h, arc, arc);

        double ratio = (double) p.getMana() / p.getMaxMana();
        int filledH = (int) (h * ratio);
        int filledY = y + (h - filledH);
        g2.setColor(Color.GREEN);
        g2.fillRoundRect(x, filledY, w, filledH, arc, arc);

        Color offWhite = new Color(255, 255, 255);
        g2.setColor(offWhite);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, w, h, arc, arc);
    }

    private void drawPauseScreen(Graphics2D g2) {
        g2.setFont(arial80Bold.deriveFont(Font.PLAIN, 80F));
        String text = "PAUSED";
        int x = getXForCenteredText(g2, text);
        int y = FrameApp.getScreenHeight() / 2;
        g2.drawString(text, x, y);
    }

    void drawDialogueScreen(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

        int x = tileSize * 2;
        int y = tileSize / 2;
        int width = FrameApp.getScreenWidth() - (FrameApp.getTileSize() * 4);
        int height = tileSize * 4;

        drawSubWindow(g2, x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 32F));
        x += tileSize;
        y += tileSize;
        g2.drawString(currentDialogueMessage, x, y);
    }

    private void drawCharacterScreen(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

        final int frameX = tileSize / 2;
        final int frameY = tileSize / 2;
        final int frameWidth = tileSize * 5;
        final int frameHeight = tileSize * 10;
        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));

        int textX = frameX + 20;
        int textY = frameY + tileSize;
        final int lineHeight = 35;

        g2.drawString("レベル", textX, textY);
        textY += lineHeight;
        g2.drawString("体力", textX, textY);
        textY += lineHeight;
        g2.drawString("力", textX, textY);
        textY += lineHeight;
        g2.drawString("器用さ", textX, textY);
        textY += lineHeight;
        g2.drawString("攻撃力", textX, textY);
        textY += lineHeight;
        g2.drawString("防御力", textX, textY);
        textY += lineHeight;
        g2.drawString("経験値", textX, textY);
        textY += lineHeight;
        g2.drawString("次のレベル", textX, textY);
        textY += lineHeight;
        g2.drawString("所持金", textX, textY);
        textY += lineHeight;
        g2.drawString("武器", textX, textY + 20);
        textY += lineHeight;
        g2.drawString("盾", textX, textY + 38);

        int tailX = (frameX + frameWidth) - 30;
        textY = frameY + tileSize;
        String value;

        value = String.valueOf(gameWindow.getPlayer().getLevel());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = gameWindow.getPlayer().getLife() +
                "/" + gameWindow.getPlayer().getMaxLife();
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getStrength());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getDexterity());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getAttack());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getDefense());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getExp());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getNextLevelExp());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getCoin());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        g2.drawImage(gameWindow.getPlayer().getCurrentWeapon().getImage(), tailX - tileSize, textY - 14, null);
        textY += tileSize;

        g2.drawImage(gameWindow.getPlayer().getCurrentShield().getImage(), tailX - tileSize, textY - 10, null);
    }

    private void drawBattleLogMessage(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        int messageX = tileSize;
        int messageY = tileSize * 4;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));

        for (int i = 0; i < message.size(); i++) {

            if (message.get(i) != null) {

                g2.setColor(Color.BLACK);
                g2.drawString(message.get(i), messageX + 2, messageY + 2);

                g2.setColor(Color.white);
                g2.drawString(message.get(i), messageX, messageY);

                int counter = messageCounter.get(i) + 1;
                messageCounter.set(i, counter);
                messageY += 50;

                if (messageCounter.get(i) > 180) {
                    message.remove(i);
                    messageCounter.remove(i);
                }
            }
        }
    }

    public void drawInventory(Graphics2D g2, Entity entity, boolean cursor) {

        if (entity == null || entity.getInventory() == null) return;

        int frameX = 0;
        int frameY = 0;
        int frameWidth = 0;
        int frameHeight = 0;
        int slotCol = 0;
        int slotRow = 0;
        int tileSize = FrameApp.getTileSize();

        if (entity == gameWindow.getPlayer()) {
            frameX = tileSize * 9;
            frameY = tileSize;
            frameWidth = (tileSize * 6) + tileSize / 2;
            frameHeight = tileSize * 5;
            slotRow = playerSlotRow;
            slotCol = playerSlotCol;
        } else {
            frameX = tileSize * 2;
            frameY = tileSize;
            frameWidth = (tileSize * 6) + tileSize / 2;
            frameHeight = tileSize * 5;
            slotRow = npcSlotRow;
            slotCol = npcSlotCol;
        }

        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        final int slotXstart = frameX + (tileSize / 2) - 4;
        final int slotYstart = frameY + (tileSize / 2) - 4;
        int slotX = slotXstart;
        int slotY = slotYstart;

        int slotSize = tileSize + 3;
        int cursorX = slotXstart + (slotSize * slotRow);
        int cursorY = slotYstart + (slotSize * slotCol);
        int cursorWidth = tileSize;
        int cursorHeight = tileSize;

        if (cursor) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(cursorX, cursorY - 2, cursorWidth, cursorHeight, 10, 10);
        }

        List<Entity> inv = entity.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            Entity item = inv.get(i);

            // 装備中のアイテムは背景色を変える
            if (item == entity.getCurrentWeapon()
                    || item == entity.getCurrentShield()) {
                g2.setColor(new Color(240, 190, 90));
                g2.fillRoundRect(slotX, slotY, tileSize, tileSize, 10, 10);
            }

            g2.drawImage(item.getImage(), slotX, slotY, tileSize, tileSize, null);

            slotX += slotSize;

            if (i == 4 || i == 9 || i == 14) {
                slotX = slotXstart;
                slotY += slotSize;
            }
        }

        int dFrameX = frameX;
        int dFrameY = frameY + frameHeight;
        int dFrameWidth = frameWidth;
        int dFrameHeight = tileSize * 3;

        int textX = dFrameX + 20;
        int textY = dFrameY + tileSize;
        g2.setFont(g2.getFont().deriveFont(28F));

        if (cursor) {

            int itemIndex = getItemIndexOnSlot(slotRow, slotCol);

            if (0 <= itemIndex && itemIndex < inv.size()) {
                Entity item = inv.get(itemIndex);
                if (item != null) {
                    String desc = item.getDescription();
                    if (desc != null && !desc.isEmpty()) {
                        drawSubWindow(g2, dFrameX, dFrameY, dFrameWidth, dFrameHeight);
                        int ty = textY;
                        for (String line : desc.split("\n")) {
                            g2.drawString(line, textX, ty);
                            ty += 32;
                        }
                    }
                }
            }
        }
    }

    public void drawTradeScreen(Graphics2D g2) {
        tradeCtx.draw(g2);
    }

    public void updateTrade(int keyCode) {
        tradeCtx.handleKey(keyCode);
    }

    public int getItemIndexOnSlot(int slotRow, int slotCol) {
        int itemIndex = slotRow + (slotCol * 5);
        return itemIndex;
    }

    void drawSubWindow(Graphics2D g2, int x, int y, int width, int height) {

        Color color = new Color(0, 0, 0, 210);
        g2.setColor(color);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        color = new Color(255, 255, 255);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }


    private void drawMessage(Graphics2D g2) {
        g2.setFont(arial40.deriveFont(Font.PLAIN, 40F));
        int x = getXForCenteredText(g2, currentDialogueMessage);
        int y = (int) (FrameApp.getScreenHeight() * 0.8);
        g2.drawString(currentDialogueMessage, x, y);
    }

    private int getXForCenteredText(Graphics2D g2, String text) {
        int textWidth = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return FrameApp.getScreenWidth() / 2 - textWidth / 2;
    }

    int getXForAlignToRightText(Graphics2D g2, String text, int tailX) {
        int textWidth = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = tailX - textWidth;
        return x;
    }

    public void setCurrentDialogueMessage(String currentDialogueMessage) {
        this.currentDialogueMessage = currentDialogueMessage;
    }

    public int getPlayerSlotRow() {
        return playerSlotRow;
    }

    public void setPlayerSlotRow(int playerSlotRow) {
        this.playerSlotRow = playerSlotRow;
    }

    public int getPlayerSlotCol() {
        return playerSlotCol;
    }

    public void setPlayerSlotCol(int playerSlotCol) {
        this.playerSlotCol = playerSlotCol;
    }

    public int getNpcSlotRow() {
        return npcSlotRow;
    }

    public void setNpcSlotRow(int npcSlotRow) {
        this.npcSlotRow = npcSlotRow;
    }

    public int getNpcSlotCol() {
        return npcSlotCol;
    }

    public void setNpcSlotCol(int npcSlotCol) {
        this.npcSlotCol = npcSlotCol;
    }

    public boolean isDialogueOn() {
        return dialogueOn;
    }

    public void setDialogueOn(boolean dialogueOn) {
        this.dialogueOn = dialogueOn;
    }

    public Entity getNpc() {
        return npc;
    }

    public void setNpc(Entity npc) {
        this.npc = npc;
    }

    public int getSubState() {
        return subState;
    }

    public void setSubState(int subState) {
        this.subState = subState;
    }

    public BufferedImage getCoin() {
        return coin;
    }

    public void setCoin(BufferedImage coin) {
        this.coin = coin;
    }
}