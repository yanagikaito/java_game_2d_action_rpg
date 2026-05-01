package collision;

import entity.Entity;
import object.ObjBomb;
import frame.FrameApp;
import object.ObjPot;
import tile.Tile;
import tile.TileManager;
import window.GameWindow;

import java.awt.Rectangle;

public class CollisionChecker {

    private final GameWindow gameWindow;

    public CollisionChecker(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    /**
     * タイル衝突判定（エンティティの solidArea を直接変更しない）
     * 戻り値: 衝突があれば true を返す
     */

    public boolean checkTile(Entity entity) {

        try {
            System.out.println("[DBG-CHECKTILE-ENTRY] this=" + this.getClass().getName() + "@" + this.hashCode()
                    + " gameWindow=" + (gameWindow != null ? gameWindow.getClass().getName() + "@" + gameWindow.hashCode() : "null")
                    + " callerCollisionCheckerHash=" + (gameWindow != null && gameWindow.getCollisionChecker() != null ? gameWindow.getCollisionChecker().hashCode() : "null")
                    + " entityClass=" + entity.getClass().getName()
                    + " thrown=" + entity.isThrown()
                    + " worldY=" + entity.getWorldY());
        } catch (Throwable t) {
            System.out.println("[DBG-CHECKTILE-ENTRY-ERR] " + t);
        }

        int tileSize = FrameApp.getTileSize();
        TileManager tileManager = gameWindow.getTileManager();
        int[][] mapTileNum = tileManager.getMapTileNum();
        Tile[] tiles = tileManager.getTiles();

        // 衝突判定に使う矩形（ワールド座標）
        Rectangle r = worldSolid(entity);

        int entityLeftX = r.x;
        int entityRightX = r.x + r.width;
        int entityTopY = r.y;
        int entityBottomY = r.y + r.height;

        int entityLeftCol = Math.max(0, entityLeftX / tileSize);
        int entityRightCol = Math.max(0, entityRightX / tileSize);
        int entityTopRow = Math.max(0, entityTopY / tileSize);
        int entityBottomRow = Math.max(0, entityBottomY / tileSize);

        int maxCol = mapTileNum.length - 1;
        int maxRow = mapTileNum[0].length - 1;

        boolean collision = false;

        // 投げられたオブジェクト（空中から着地するもの）は底面タイルを直接チェック
        // 着地時に確実に地面タイルに当たっているかを判定
        if (entity instanceof ObjBomb || entity instanceof ObjPot || entity.isThrown()) {

            // 底面のタイル（足元）をチェック
            int checkRow = Math.min(maxRow, Math.max(0, entityBottomRow));

            // 投擲物は底面の1行上もチェックする
            int checkRowAbove = Math.max(0, checkRow - 1);

            int leftCol = Math.min(maxCol, Math.max(0, entityLeftCol));
            int rightCol = Math.min(maxCol, Math.max(0, entityRightCol));

            int tileNum1 = mapTileNum[leftCol][checkRow];
            int tileNum2 = mapTileNum[rightCol][checkRow];
            int tileNum3 = mapTileNum[leftCol][checkRowAbove];
            int tileNum4 = mapTileNum[rightCol][checkRowAbove];

            // collisionとbombCollisionの衝突判定をチェックする
            if (tiles[tileNum1].collision || tiles[tileNum1].bombCollision || tiles[tileNum1].potCollision
                    || tiles[tileNum2].collision || tiles[tileNum2].bombCollision || tiles[tileNum2].potCollision
                    || tiles[tileNum3].collision || tiles[tileNum3].bombCollision || tiles[tileNum3].potCollision
                    || tiles[tileNum4].collision || tiles[tileNum4].bombCollision || tiles[tileNum4].potCollision) {
                collision = true;
            }

            gameWindow.getUi().addMessage("[checkTile] bomb branch: collision=" + collision +
                    " checkRow=" + checkRow + " checkRowAbove=" + checkRowAbove);
            entity.setCollision(collision);
            return collision;
        }

        // --- 通常の移動ベースの判定 ---
        int tileNum1, tileNum2;
        String direction = entity.getDirection();
        if (entity.isInKnockBack()) {
            direction = entity.getKnockBackDirection();
        }

        switch (direction) {
            case "up" -> {
                int newTopRow = (entityTopY - entity.getSpeed()) / tileSize;
                newTopRow = Math.max(0, Math.min(maxRow, newTopRow));
                tileNum1 = mapTileNum[entityLeftCol][newTopRow];
                tileNum2 = mapTileNum[entityRightCol][newTopRow];
                if (tiles[tileNum1].collision || tiles[tileNum2].collision) collision = true;
            }
            case "down" -> {
                int newBottomRow = (entityBottomY + entity.getSpeed()) / tileSize;
                newBottomRow = Math.max(0, Math.min(maxRow, newBottomRow));
                tileNum1 = mapTileNum[entityLeftCol][newBottomRow];
                tileNum2 = mapTileNum[entityRightCol][newBottomRow];
                if (tiles[tileNum1].collision || tiles[tileNum2].collision) collision = true;
            }
            case "left" -> {
                int newLeftCol = (entityLeftX - entity.getSpeed()) / tileSize;
                newLeftCol = Math.max(0, Math.min(maxCol, newLeftCol));
                tileNum1 = mapTileNum[newLeftCol][entityTopRow];
                tileNum2 = mapTileNum[newLeftCol][entityBottomRow];
                if (tiles[tileNum1].collision || tiles[tileNum2].collision) collision = true;
            }
            case "right" -> {
                int newRightCol = (entityRightX + entity.getSpeed()) / tileSize;
                newRightCol = Math.max(0, Math.min(maxCol, newRightCol));
                tileNum1 = mapTileNum[newRightCol][entityTopRow];
                tileNum2 = mapTileNum[newRightCol][entityBottomRow];
                if (tiles[tileNum1].collision || tiles[tileNum2].collision) collision = true;
            }
            default -> {
                // 方向不明時は現在の底面タイルをチェックしておく
                int col = Math.max(0, Math.min(maxCol, entityLeftCol));
                int row = Math.max(0, Math.min(maxRow, entityBottomRow));
                int tileNum = mapTileNum[col][row];
                if (tiles[tileNum].collision) collision = true;
            }
        }

        entity.setCollision(collision);
        return collision;
    }

    /**
     * エンティティ同士の衝突判定（targets 配列）
     * ObjBomb の「設置状態」は衝突判定から除外するガードを入れている
     */

    public int checkEntity(Entity entity, Entity[] targets) {

        int index = 999;

        String direction = entity.getDirection();
        if (entity.isInKnockBack()) {
            direction = entity.getKnockBackDirection();
        }

        for (int i = 0; i < targets.length; i++) {

            if (targets[i] == null) continue;

            if (targets[i] instanceof ObjBomb) {
                ObjBomb tb = (ObjBomb) targets[i];
                if (!tb.isThrown() && tb.isPickable()) {
                    continue;
                }
            }
            if (targets[i] instanceof ObjPot) {
                ObjPot tp = (ObjPot) targets[i];
                if (!tp.isThrown() && tp.isPickable()) {
                    continue;
                }
            }


            Rectangle rEntity = worldSolid(entity);
            Rectangle rTarget = worldSolid(targets[i]);

            int offsetX = 0;
            int offsetY = 0;
            switch (direction) {
                case "up" -> offsetY = -entity.getSpeed();
                case "down" -> offsetY = entity.getSpeed();
                case "left" -> offsetX = -entity.getSpeed();
                case "right" -> offsetX = entity.getSpeed();
            }

            // コピーにオフセットを適用して判定
            Rectangle moved = new Rectangle(rEntity);
            moved.translate(offsetX, offsetY);

            if (moved.intersects(rTarget)) {
                if (targets[i] != entity) {
                    entity.setCollision(true);
                    index = i;
                }
            }
        }
        return index;
    }

    /**
     * プレイヤーとの接触判定（地面設置の爆弾は除外）
     */

    public boolean checkPlayer(Entity entity) {

        // 設置中の爆弾はプレイヤー接触で即爆発させない（拾う処理で扱う）
        if (entity instanceof ObjBomb) {
            ObjBomb bomb = (ObjBomb) entity;
            if (!bomb.isThrown() && bomb.isPickable()) {
                return false;
            }
        }

        if (entity instanceof ObjPot) {
            ObjPot pot = (ObjPot) entity;
            if (!pot.isThrown() && pot.isPickable()) {
                return false;
            }
        }

        boolean contactPlayer = false;

        Rectangle rEntity = worldSolid(entity);
        Rectangle rPlayer = worldSolid(gameWindow.getPlayer());

        int offsetX = 0;
        int offsetY = 0;
        switch (entity.getDirection()) {
            case "up" -> offsetY = -entity.getSpeed();
            case "down" -> offsetY = entity.getSpeed();
            case "left" -> offsetX = -entity.getSpeed();
            case "right" -> offsetX = entity.getSpeed();
        }

        Rectangle moved = new Rectangle(rEntity);
        moved.translate(offsetX, offsetY);

        if (moved.intersects(rPlayer)) {
            entity.setCollision(true);
            contactPlayer = true;
        }

        return contactPlayer;
    }

    /**
     * エンティティの solidArea をワールド座標に変換したコピーを返す（元のオブジェクトは変更しない）
     */

    private Rectangle worldSolid(Entity e) {
        Rectangle sa = e.getSolidArea();
        int x = e.getWorldX() + e.getSolidAreaDefaultX();
        int y = e.getWorldY() + e.getSolidAreaDefaultY();
        return new Rectangle(x, y, sa.width, sa.height);
    }
}