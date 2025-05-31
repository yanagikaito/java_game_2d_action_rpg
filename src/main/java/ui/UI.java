package ui;

import entity.Entity;
import frame.FrameApp;
import object.ObjHeart;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class UI {

    private final GameWindow gameWindow;

    // 使用するフォント
    private final Font arial40;
    private final Font arial80Bold;

    // メッセージ表示用
    private boolean messageOn;
    private String currentDialogueMessage;
    private ArrayList<String> message = new ArrayList<>();
    private ArrayList<Integer> messageCounter = new ArrayList<>();

    private BufferedImage heartFull;
    private BufferedImage heartHalf;
    private BufferedImage heartBlank;

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

        if (gameState == gameWindow.getPlayState()) {
            drawPlayerLife(g2);
            drawBattleLogMessage(g2);
        } else if (gameState == gameWindow.getPauseState()) {
            drawPlayerLife(g2);
            drawPauseScreen(g2);
        } else if (gameState == gameWindow.getDialogueState()) {
            drawPlayerLife(g2);
            drawDialogueScreen(g2);
        } else if (gameState == gameWindow.getCharacterState()) {
            drawCharacterScreen(g2);
        }

        if (messageOn == true) {
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

    public void drawBattleLogMessage(@NotNull Graphics2D g2) {

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

    public String getCurrentDialogueMessage() {
        return currentDialogueMessage;
    }

    public void setCurrentDialogueMessage(String currentDialogueMessage) {
        this.currentDialogueMessage = currentDialogueMessage;
    }
}