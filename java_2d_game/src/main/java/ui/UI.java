package ui;

import entity.Entity;
import frame.FrameApp;
import object.ObjHeart;
import org.jetbrains.annotations.NotNull;
import player.Player;
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
    private List<String> currentDialogueList = List.of();
    private int currentDialogueIndex = 0;

    private BufferedImage heartFull;
    private BufferedImage heartHalf;
    private BufferedImage heartBlank;

    private int slotCol = 0;
    private int slotRow = 0;

    public UI(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.arial40 = new Font("エリア", Font.PLAIN, 40);
        this.arial80Bold = new Font("エリア", Font.BOLD, 80);
        this.messageOn = false;
        this.currentDialogueMessage = "";

        Entity heart = new ObjHeart(gameWindow);
        heartFull = heart.getImage();
        heartHalf = heart.getImage2();
        heartBlank = heart.getImage3();
    }

    public void addMessage(String text) {
        message.add(text);
        messageCounter.add(0);
    }

    public void draw(@NotNull Graphics2D g2) {

        g2.setFont(arial40);
        g2.setColor(Color.white);

        int gameState = gameWindow.getGameState();
        int playState = gameWindow.getPlayState();
        int pauseState = gameWindow.getPauseState();
        int dialogueState = gameWindow.getDialogueState();
        int charState = gameWindow.getCharacterState();
        int gameOverState = gameWindow.getGameOverState();

        if (gameState == playState) {

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
            drawInventory(g2);

        } else if (gameState == gameOverState) {
            drawGameOverScreen(g2);
        }


        if (messageOn) {
            drawMessage(g2);
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

    private void drawManaBar(@NotNull Graphics2D g2) {

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

    private void drawPauseScreen(@NotNull Graphics2D g2) {
        g2.setFont(arial80Bold.deriveFont(Font.PLAIN, 80F));
        String text = "PAUSED";
        int x = getXForCenteredText(g2, text);
        int y = FrameApp.getScreenHeight() / 2;
        g2.drawString(text, x, y);
    }

    private void drawDialogueScreen(@NotNull Graphics2D g2) {

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

    private void drawCharacterScreen(@NotNull Graphics2D g2) {

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

    private void drawBattleLogMessage(@NotNull Graphics2D g2) {

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

    private void drawInventory(@NotNull Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        int frameX = tileSize * 9;
        int frameY = tileSize;
        int frameWidth = (tileSize * 6) + tileSize / 2;
        int frameHeight = tileSize * 5;
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

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(cursorX, cursorY - 2, cursorWidth, cursorHeight, 10, 10);

        for (int i = 0; i < gameWindow.getPlayer().getInventory().size(); i++) {
            if (gameWindow.getPlayer().getInventory().get(i) == gameWindow.getPlayer().getCurrentWeapon()
                    || gameWindow.getPlayer().getInventory().get(i) == gameWindow.getPlayer().getCurrentShield()) {
                g2.setColor(new Color(240, 190, 90));
                g2.fillRoundRect(slotX, slotY, FrameApp.getTileSize(), FrameApp.getTileSize(), 10, 10);
            }

            g2.drawImage(gameWindow.getPlayer().getInventory().get(i).getImage(), slotX, slotY, null);

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

        int itemIndex = getItemIndexOnSlot();

        if (itemIndex < gameWindow.getPlayer().getInventory().size()) {
            drawSubWindow(g2, dFrameX, dFrameY, dFrameWidth, dFrameHeight);
            for (String line : gameWindow.getPlayer().getInventory().get(itemIndex).getDescription().split("\n")) {
                g2.drawString(line, textX, textY);
                textY += 32;
            }
        }
    }

    public void drawGameOverScreen(@NotNull Graphics2D g2) {

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

        text = "ゲーム終了";
        x = getXForCenteredText(g2, text);
        y += 55;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 1) {
            g2.drawString(">", x - 40, y);
        }
    }

    public int getItemIndexOnSlot() {
        int itemIndex = slotRow + (slotCol * 5);
        return itemIndex;
    }

    private void drawSubWindow(@NotNull Graphics2D g2, int x, int y, int width, int height) {

        Color color = new Color(0, 0, 0, 210);
        g2.setColor(color);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        color = new Color(255, 255, 255);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }


    private void drawMessage(@NotNull Graphics2D g2) {
        g2.setFont(arial40.deriveFont(Font.PLAIN, 40F));
        int x = getXForCenteredText(g2, currentDialogueMessage);
        int y = (int) (FrameApp.getScreenHeight() * 0.8);
        g2.drawString(currentDialogueMessage, x, y);
    }

    private int getXForCenteredText(@NotNull Graphics2D g2, String text) {
        int textWidth = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return FrameApp.getScreenWidth() / 2 - textWidth / 2;
    }

    private int getXForAlignToRightText(@NotNull Graphics2D g2, String text, int tailX) {
        int textWidth = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = tailX - textWidth;
        return x;
    }

    public void setCurrentDialogueMessage(String currentDialogueMessage) {
        this.currentDialogueMessage = currentDialogueMessage;
    }

    public int getSlotRow() {
        return slotRow;
    }

    public void setSlotRow(int slotRow) {
        this.slotRow = slotRow;
    }

    public int getSlotCol() {
        return slotCol;
    }

    public void setSlotCol(int slotCol) {
        this.slotCol = slotCol;
    }
}