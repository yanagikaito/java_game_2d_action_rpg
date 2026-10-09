package ui.state.sleep;

import frame.FrameApp;
import game.GameState;
import player.Player;

import java.awt.*;

public final class SleepingState implements SleepScreenState {

    private final SleepScreenContext sleepScreenContext;
    private double sleepTimer = 3.0;

    public SleepingState(SleepScreenContext sleepScreenContext) {
        this.sleepScreenContext = sleepScreenContext;

        Player player = sleepScreenContext.gw().getPlayer();
        int tileSize = FrameApp.getTileSize();

        player.setWorldX(23 * tileSize);
        player.setWorldY(13 * tileSize);
        player.setDirection("down");

        player.startSleeping();
    }

    @Override
    public void update(double deltaSeconds) {

        sleepTimer -= deltaSeconds;

        // 3秒経過したら通常プレイ画面に戻る
        if (sleepTimer <= 0) {
            Player player = sleepScreenContext.gw().getPlayer();
            int tileSize = FrameApp.getTileSize();

            player.setSleeping(false);

            player.setWorldY(14 * tileSize);
            player.setDirection("down");

            sleepScreenContext.setState(new SleepMenuState(sleepScreenContext));

            sleepScreenContext.kh().clearAllKeys();
            sleepScreenContext.gw().setGameState(GameState.PLAY);
        }
    }

    @Override
    public void handleKey(int code) {
        // 睡眠中はプレイヤーのキー操作を受け付けない
    }

    @Override
    public void draw(Graphics2D g2) {

        Player player = sleepScreenContext.gw().getPlayer();
        int bedScreenX = player.getScreenX();
        int bedScreenY = player.getScreenY();

        g2.setColor(Color.WHITE);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 28F));
        g2.drawString("Z", bedScreenX + 28, bedScreenY - 20);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20F));
        g2.drawString("z", bedScreenX + 18, bedScreenY - 8);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14F));
        g2.drawString("z", bedScreenX + 10, bedScreenY + 2);
    }
}