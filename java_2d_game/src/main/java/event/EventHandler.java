package event;

import frame.FrameApp;
import game.GameState;
import player.Player;
import tile.TileManager;
import window.GameWindow;

import static frame.FrameApp.getMaxWorldCol;
import static frame.FrameApp.getMaxWorldRow;

public class EventHandler {

    private GameWindow gameWindow;
    private EventRect eventRect[][];
    private boolean canTouchEvent = true;
    private int previousEventX;
    private int previousEventY;

    public EventHandler(GameWindow gameWindow) {
        this.gameWindow = gameWindow;

        eventRect = new EventRect[getMaxWorldCol()][getMaxWorldRow()];

        // バグを防ぐため for文 で安全に初期化
        for (int col = 0; col < getMaxWorldCol(); col++) {
            for (int row = 0; row < getMaxWorldRow(); row++) {
                eventRect[col][row] = new EventRect();
                // 当たり判定を少し広めに設定
                eventRect[col][row].x = 10;
                eventRect[col][row].y = 10;
                eventRect[col][row].width = 28;
                eventRect[col][row].height = 28;
                eventRect[col][row].eventRectDefaultX = eventRect[col][row].x;
                eventRect[col][row].eventRectDefaultY = eventRect[col][row].y;
            }
        }
    }

    public void checkEvent() {

        int tileSize = FrameApp.getTileSize();

        // イベント発生後の距離チェック
        int xDistance = Math.abs(gameWindow.getPlayer().getWorldX() - previousEventX);
        int yDistance = Math.abs(gameWindow.getPlayer().getWorldY() - previousEventY);
        if (Math.max(xDistance, yDistance) > tileSize) {
            canTouchEvent = true;
        }

        if (canTouchEvent) {
            if (hit(25, 13, "up")) {
//                gameWindow.getPlayer().startSleeping();
            }
        }
    }

    public boolean hit(int col, int row, String reqDirection) {
        int tileSize = FrameApp.getTileSize();
        boolean hit = false;
        Player player = gameWindow.getPlayer();

        player.getSolidArea().x = player.getWorldX() + player.getSolidArea().x;
        player.getSolidArea().y = player.getWorldY() + player.getSolidArea().y;
        eventRect[col][row].x = col * tileSize + eventRect[col][row].x;
        eventRect[col][row].y = row * tileSize + eventRect[col][row].y;

        if (player.getSolidArea().intersects(eventRect[col][row]) && !eventRect[col][row].eventDone) {
            if (player.getDirection().contentEquals(reqDirection) || reqDirection.contentEquals("どれか")) {
                hit = true;
                previousEventX = player.getWorldX();
                previousEventY = player.getWorldY();
            }
        }

        player.getSolidArea().x = player.getSolidAreaDefaultX();
        player.getSolidArea().y = player.getSolidAreaDefaultY();
        eventRect[col][row].x = eventRect[col][row].eventRectDefaultX;
        eventRect[col][row].y = eventRect[col][row].eventRectDefaultY;

        return hit;
    }

    public void setPreviousEventX(int previousEventX) {
        this.previousEventX = previousEventX;
    }

    public void setPreviousEventY(int previousEventY) {
        this.previousEventY = previousEventY;
    }

    public void setCanTouchEvent(boolean canTouchEvent) {
        this.canTouchEvent = canTouchEvent;
    }
}