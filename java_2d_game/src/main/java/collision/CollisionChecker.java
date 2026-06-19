package collision;

import entity.Entity;
import npc.NpcChicken;
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

        // ワールド座標 -> タイル座標（負座標対応）
        int entityLeftCol = Math.floorDiv(entityLeftX, tileSize);
        int entityRightCol = Math.floorDiv(entityRightX, tileSize);
        int entityTopRow = Math.floorDiv(entityTopY, tileSize);
        int entityBottomRow = Math.floorDiv(entityBottomY, tileSize);

        // マップの最大インデックス
        int maxCol = mapTileNum.length - 1;
        int maxRow = mapTileNum[0].length - 1;

        // ここで全て clamp しておく（以降は clamp済みの値を使う）
        int clampedLeftCol = Math.max(0, Math.min(maxCol, entityLeftCol));
        int clampedRightCol = Math.max(0, Math.min(maxCol, entityRightCol));
        int clampedTopRow = Math.max(0, Math.min(maxRow, entityTopRow));
        int clampedBottomRow = Math.max(0, Math.min(maxRow, entityBottomRow));

        boolean collision = false;

        // 投げられたオブジェクト（空中から着地するもの）は底面タイルを直接チェック
        boolean isThrowableCheck = (entity instanceof ObjBomb) || (entity instanceof ObjPot) || entity.isThrown();

        if (isThrowableCheck) {

            // 底面のタイル（足元）をチェック（clamp済み）
            int checkRow = Math.min(maxRow, Math.max(0, clampedBottomRow));
            int checkRowAbove = Math.max(0, checkRow - 1);

            int leftCol = Math.min(maxCol, Math.max(0, clampedLeftCol));
            int rightCol = Math.min(maxCol, Math.max(0, clampedRightCol));

            // 安全に tileId を取得（念のため bounds チェック）
            int tileNum1 = mapTileNum[leftCol][checkRow];
            int tileNum2 = mapTileNum[rightCol][checkRow];
            int tileNum3 = mapTileNum[leftCol][checkRowAbove];
            int tileNum4 = mapTileNum[rightCol][checkRowAbove];

            if (isTileIndexValid(tileNum1, tiles) && isTileIndexValid(tileNum2, tiles)
                    && isTileIndexValid(tileNum3, tiles) && isTileIndexValid(tileNum4, tiles)) {

                if (tiles[tileNum1].collision || tiles[tileNum1].bombCollision || tiles[tileNum1].potCollision
                        || tiles[tileNum2].collision || tiles[tileNum2].bombCollision || tiles[tileNum2].potCollision
                        || tiles[tileNum3].collision || tiles[tileNum3].bombCollision || tiles[tileNum3].potCollision
                        || tiles[tileNum4].collision || tiles[tileNum4].bombCollision || tiles[tileNum4].potCollision
                        || tiles[tileNum1].chickenCollision || tiles[tileNum2].chickenCollision
                        || tiles[tileNum3].chickenCollision || tiles[tileNum4].chickenCollision) {
                    collision = true;
                }
            } else {
                // tileId が不正なら安全側で衝突扱いにする
                collision = true;
            }

//            gameWindow.getUi().addMessage("[checkTile] chicken branch: tiles[tileNum1] =" + tiles[tileNum1].chickenCollision);
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
                int newTopRow = Math.floorDiv(entityTopY - entity.getSpeed(), tileSize);
                newTopRow = Math.max(0, Math.min(maxRow, newTopRow));
                int leftCol = clampedLeftCol;
                int rightCol = clampedRightCol;
                tileNum1 = mapTileNum[leftCol][newTopRow];
                tileNum2 = mapTileNum[rightCol][newTopRow];
                if (isTileIndexValid(tileNum1, tiles) && isTileIndexValid(tileNum2, tiles)) {
                    if (tiles[tileNum1].collision || tiles[tileNum2].collision)
                        collision = true;
                } else collision = true;
            }
            case "down" -> {
                int newBottomRow = Math.floorDiv(entityBottomY + entity.getSpeed(), tileSize);
                newBottomRow = Math.max(0, Math.min(maxRow, newBottomRow));
                int leftCol = clampedLeftCol;
                int rightCol = clampedRightCol;
                tileNum1 = mapTileNum[leftCol][newBottomRow];
                tileNum2 = mapTileNum[rightCol][newBottomRow];
                if (isTileIndexValid(tileNum1, tiles) && isTileIndexValid(tileNum2, tiles)) {
                    if (tiles[tileNum1].collision || tiles[tileNum2].collision)
                        collision = true;
                } else collision = true;
            }
            case "left" -> {
                int newLeftCol = Math.floorDiv(entityLeftX - entity.getSpeed(), tileSize);
                newLeftCol = Math.max(0, Math.min(maxCol, newLeftCol));
                int topRow = clampedTopRow;
                int bottomRow = clampedBottomRow;
                tileNum1 = mapTileNum[newLeftCol][topRow];
                tileNum2 = mapTileNum[newLeftCol][bottomRow];
                if (isTileIndexValid(tileNum1, tiles) && isTileIndexValid(tileNum2, tiles)) {
                    if (tiles[tileNum1].collision || tiles[tileNum2].collision)
                        collision = true;
                } else collision = true;
            }
            case "right" -> {
                int newRightCol = Math.floorDiv(entityRightX + entity.getSpeed(), tileSize);
                newRightCol = Math.max(0, Math.min(maxCol, newRightCol));
                int topRow = clampedTopRow;
                int bottomRow = clampedBottomRow;
                tileNum1 = mapTileNum[newRightCol][topRow];
                tileNum2 = mapTileNum[newRightCol][bottomRow];
                if (isTileIndexValid(tileNum1, tiles) && isTileIndexValid(tileNum2, tiles)) {
                    if (tiles[tileNum1].collision || tiles[tileNum2].collision)
                        collision = true;
                } else collision = true;
            }
            default -> {
                int col = Math.max(0, Math.min(maxCol, clampedLeftCol));
                int row = Math.max(0, Math.min(maxRow, clampedBottomRow));
                int tileNum = mapTileNum[col][row];
                if (isTileIndexValid(tileNum, tiles)) {
                    if (tiles[tileNum].collision)
                        collision = true;
                } else collision = true;
            }
        }

        entity.setCollision(collision);
        return collision;
    }

    // 補助: tiles 配列に対する tileId の妥当性チェック
    private boolean isTileIndexValid(int tileId, Tile[] tiles) {
        return tileId >= 0 && tileId < tiles.length;
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