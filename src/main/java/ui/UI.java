package ui;

import frame.FrameApp;
import object.ObjHeart;
import object.SuperObject;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;

public class UI {

    private final GameWindow gameWindow;

    // 使用するフォント
    private final Font arial40;
    private final Font arial80Bold;

    // メッセージ表示用
    private boolean messageOn;
    private String currentDialogueMessage;

    private BufferedImage heartFull;
    private BufferedImage heartHalf;
    private BufferedImage heartBlank;

    public UI(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.arial40 = new Font("エリア", Font.PLAIN, 40);
        this.arial80Bold = new Font("エリア", Font.BOLD, 80);
        this.messageOn = false;
        this.currentDialogueMessage = "";

        SuperObject heart = new ObjHeart(gameWindow);
        heartFull = heart.getImage();
        heartHalf = heart.getImage2();
        heartBlank = heart.getImage3();
    }

    public void draw(@NotNull Graphics2D g2) {

        g2.setFont(arial40);
        g2.setColor(Color.white);

        int gameState = gameWindow.getGameState();

        if (gameState == gameWindow.getPlayState()) {
            drawPlayerLife(g2);
        } else if (gameState == gameWindow.getPauseState()) {
            drawPlayerLife(g2);
            drawPauseScreen(g2);
        } else if (gameState == gameWindow.getDialogueState()) {
            drawPlayerLife(g2);
            drawDialogueScreen(g2);
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

    public String getCurrentDialogueMessage() {
        return currentDialogueMessage;
    }

    public void setCurrentDialogueMessage(String currentDialogueMessage) {
        this.currentDialogueMessage = currentDialogueMessage;
    }
}