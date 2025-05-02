package ui;

import frame.FrameApp;
import window.GameWindow;

import java.awt.*;

public class UI {

    private final GameWindow gameWindow;

    // 使用するフォント
    private final Font arial40;
    private final Font arial80Bold;

    // メッセージ表示用
    private boolean messageOn;
    private String message;

    public UI(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.arial40 = new Font("エリア", Font.PLAIN, 40);
        this.arial80Bold = new Font("エリア", Font.BOLD, 80);
        this.messageOn = false;
        this.message = "";
    }


    public void showMessage(String text) {
        this.message = text;
        this.messageOn = true;
    }


    public void draw(Graphics2D g2) {

        g2.setFont(arial40);
        g2.setColor(Color.white);

        int gameState = gameWindow.getGameState();
        if (gameState == gameWindow.getPlayState()) {
        } else if (gameState == gameWindow.getPauseState()) {
            drawPauseScreen(g2);
        }

        if (messageOn == true) {
            drawMessage(g2);
        }
    }

    private void drawPauseScreen(Graphics2D g2) {
        g2.setFont(arial80Bold.deriveFont(Font.PLAIN, 80F));
        String text = "PAUSED";
        int x = getXForCenteredText(g2, text);
        int y = FrameApp.getScreenHeight() / 2;
        g2.drawString(text, x, y);
    }


    private void drawMessage(Graphics2D g2) {
        g2.setFont(arial40.deriveFont(Font.PLAIN, 40F));
        int x = getXForCenteredText(g2, message);
        int y = (int) (FrameApp.getScreenHeight() * 0.8);
        g2.drawString(message, x, y);
    }

    private int getXForCenteredText(Graphics2D g2, String text) {
        int textWidth = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return FrameApp.getScreenWidth() / 2 - textWidth / 2;
    }
}