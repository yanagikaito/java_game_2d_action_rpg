package event;

import frame.FrameApp;
import player.Player;
import window.GameWindow;

import static frame.FrameApp.getMaxWorldCol;
import static frame.FrameApp.getMaxWorldRow;

public class EventHandler {

    GameWindow gameWindow;

    // 2次元の配列として使用し、コンストラクタの中でインスタンス化します。
    EventRect eventRect[][];

    boolean canTouchEvent = true;

    // 以前のイベント

    int previousEventX;
    int previousEventY;

    public EventHandler(GameWindow gameWindow) {
        this.gameWindow = gameWindow;

        // マップ上のすべてのタイルにイベント矩形ができる。
        // このソリッドエリアをeventRect[][]に設定。
        eventRect = new EventRect[getMaxWorldCol()][getMaxWorldRow()];

        int col = 0;
        int row = 0;
        while (col < getMaxWorldCol() && row < getMaxWorldRow()) {

            eventRect[col][row] = new EventRect();
            eventRect[col][row].x = 23;
            eventRect[col][row].y = 23;
            eventRect[col][row].width = 2;
            eventRect[col][row].height = 2;
            eventRect[col][row].eventRectDefaultX = eventRect[col][row].x;
            eventRect[col][row].eventRectDefaultY = eventRect[col][row].y;

            col++;
            if (col == getMaxWorldRow()) {
                col = 0;
                row++;
            }
        }
    }

    public void checkEvent() {

        int tileSize = FrameApp.getTileSize();
        int hitRow = 26;
        int hitCol = 15;

        // プレイヤーキャラクターが最後のイベントから1タイル以上離れているかチェック。
        // mass absはこの絶対値を返す
        // この計算により、たとえ負の数であっても正の数として返される。
        int xDistance = Math.abs(gameWindow.getPlayer().getWorldX() - previousEventX);
        int yDistance = Math.abs(gameWindow.getPlayer().getWorldY() - previousEventY);
        int distance = Math.max(xDistance, yDistance);
        if (distance > tileSize) {
            canTouchEvent = true;
        }

        if (hit(hitRow, hitCol, "どれか") == true) {
            applyDamageToPlayer(3, hitRow, hitCol);
        }
    }


    // イベントの衝突をチェックするメソッドで、オブジェクトと似たような働きをする。
    public boolean hit(int row, int col, String reqDirection) {

        int tileSize = FrameApp.getTileSize();
        boolean hit = false;

        gameWindow.getPlayer().getSolidArea().x = gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getSolidArea().x;
        gameWindow.getPlayer().getSolidArea().y = gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getSolidArea().y;
        eventRect[row][col].x = row * tileSize + eventRect[row][col].x;
        eventRect[row][col].y = col * tileSize + eventRect[row][col].y;

        // intersexメソッドを使い、プレイヤーが衝突しているかどうかをチェック。
        if (gameWindow.getPlayer().getSolidArea().intersects(eventRect[row][col]) && eventRect[row][col].eventDone == false) {
            // プレーヤーの方向をチェックできて,どちらのイベントが起こるかを選択。
            if (gameWindow.getPlayer().getDirection().contentEquals(reqDirection) || reqDirection.contentEquals("どれか")) {
                hit = true;
                // プレイヤーキャラクターがイベントの矩形から1タイル分離れるまで
                // 二度と起きないようにすれば、イベントが繰り返し起きるのを防げる
                previousEventX = gameWindow.getPlayer().getWorldX();
                previousEventY = gameWindow.getPlayer().getWorldY();
            }
        }
        // 最後に、プレーヤーとイベント矩形をリセット。
        gameWindow.getPlayer().getSolidArea().x = gameWindow.getPlayer().getSolidAreaDefaultX();
        gameWindow.getPlayer().getSolidArea().y = gameWindow.getPlayer().getSolidAreaDefaultY();
        eventRect[row][col].x = eventRect[row][col].eventRectDefaultX;
        eventRect[row][col].y = eventRect[row][col].eventRectDefaultY;
        // 衝突している場合はtrueを返す。
        return hit;
    }

    private void applyDamageToPlayer(int dmg, int col, int row) {
        Player p = gameWindow.getPlayer();
        if (!p.getInvincible()) {
            p.takeDamage(dmg);
            gameWindow.getSoundmanager().damageWAV("sound/damage-sound.wav");
            p.setInvincible(true);
        }
        eventRect[row][col].eventDone = true;
        canTouchEvent = false;
    }
}