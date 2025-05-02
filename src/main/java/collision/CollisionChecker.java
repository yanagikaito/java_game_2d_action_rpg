package collision;

import entity.Entity;
import frame.FrameApp;
import tile.Tile;
import tile.TileManager;
import window.GameWindow;

public class CollisionChecker {

    private GameWindow gameWindow;

    public CollisionChecker(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void checkTile(Entity entity) {

        int tileSize = FrameApp.getTileSize();

        TileManager tileManager = gameWindow.getTileManager();
        int[][] mapTileNum = tileManager.getMapTileNum();
        Tile[] tiles = tileManager.getTiles();

        int entityLeftX = entity.getWorldX() + entity.getSolidArea().x;
        int entityRightX = entity.getWorldX() + entity.getSolidArea().x + entity.getSolidArea().width;
        int entityTopY = entity.getWorldY() + entity.getSolidArea().y;
        int entityBottomY = entity.getWorldY() + entity.getSolidArea().y + entity.getSolidArea().height;

        int entityLeftCol = entityLeftX / tileSize;
        int entityRightCol = entityRightX / tileSize;
        int entityTopRow = entityTopY / tileSize;
        int entityBottomRow = entityBottomY / tileSize;

        int tileNum1, tileNum2;

        switch (entity.getDirection()) {
            case "up" -> {
                int newTopRow = (entityTopY - entity.getSpeed()) / tileSize;
                tileNum1 = mapTileNum[entityLeftCol][newTopRow];
                tileNum2 = mapTileNum[entityRightCol][newTopRow];
                if (tiles[tileNum1].collision || tiles[tileNum2].collision) {
                    entity.setCollision(true);
                }
            }
            case "down" -> {
                int newBottomRow = (entityBottomY + entity.getSpeed()) / tileSize;
                tileNum1 = mapTileNum[entityLeftCol][newBottomRow];
                tileNum2 = mapTileNum[entityRightCol][newBottomRow];
                if (tiles[tileNum1].collision || tiles[tileNum2].collision) {
                    entity.setCollision(true);
                }
            }
            case "left" -> {
                int newLeftCol = (entityLeftX - entity.getSpeed()) / tileSize;
                tileNum1 = mapTileNum[newLeftCol][entityTopRow];
                tileNum2 = mapTileNum[newLeftCol][entityBottomRow];
                if (tiles[tileNum1].collision || tiles[tileNum2].collision) {
                    entity.setCollision(true);
                }
            }
            case "right" -> {
                int newRightCol = (entityRightX + entity.getSpeed()) / tileSize;
                tileNum1 = mapTileNum[newRightCol][entityTopRow];
                tileNum2 = mapTileNum[newRightCol][entityBottomRow];
                if (tiles[tileNum1].collision || tiles[tileNum2].collision) {
                    entity.setCollision(true);
                }
            }
            default -> {
            }
        }
    }
}