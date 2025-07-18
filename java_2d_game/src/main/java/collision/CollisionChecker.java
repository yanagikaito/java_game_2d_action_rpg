package collision;

import entity.Entity;
import frame.FrameApp;
import org.jetbrains.annotations.NotNull;
import tile.Tile;
import tile.TileManager;
import window.GameWindow;

public class CollisionChecker {

    private final GameWindow gameWindow;

    public CollisionChecker(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void checkTile(@NotNull Entity entity) {

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

    public int checkEntity(@NotNull Entity entity, Entity @NotNull [] targets) {

        int index = 999;

        for (int i = 0; i < targets.length; i++) {

            if (targets[i] != null) {
                updateSolidArea(entity);
                updateSolidArea(targets[i]);

                int offsetX = 0;
                int offsetY = 0;
                switch (entity.getDirection()) {
                    case "up" -> offsetY = -entity.getSpeed();
                    case "down" -> offsetY = entity.getSpeed();
                    case "left" -> offsetX = -entity.getSpeed();
                    case "right" -> offsetX = entity.getSpeed();
                }

                entity.getSolidArea().x += offsetX;
                entity.getSolidArea().y += offsetY;

                if (entity.getSolidArea().intersects(targets[i].getSolidArea())) {
                    // 自分自身との衝突ではないことを確認
                    if (targets[i] != entity) {
                        entity.setCollision(true);
                        index = i;
                    }
                }

                resetSolidArea(entity);
                resetSolidArea(targets[i]);
            }
        }
        return index;
    }

    public boolean checkPlayer(Entity entity) {

        boolean contactPlayer = false;

        updateSolidArea(entity);
        updateSolidArea(gameWindow.getPlayer());

        int offsetX = 0;
        int offsetY = 0;
        switch (entity.getDirection()) {
            case "up" -> offsetY = -entity.getSpeed();
            case "down" -> offsetY = entity.getSpeed();
            case "left" -> offsetX = -entity.getSpeed();
            case "right" -> offsetX = entity.getSpeed();
        }

        entity.getSolidArea().x += offsetX;
        entity.getSolidArea().y += offsetY;

        if (entity.getSolidArea().intersects(gameWindow.getPlayer().getSolidArea())) {
            entity.setCollision(true);
            contactPlayer = true;
        }

        resetSolidArea(entity);
        resetSolidArea(gameWindow.getPlayer());

        return contactPlayer;
    }

    private void updateSolidArea(@NotNull Entity entity) {
        entity.getSolidArea().x = entity.getWorldX() + entity.getSolidArea().x;
        entity.getSolidArea().y = entity.getWorldY() + entity.getSolidArea().y;
    }

    private void resetSolidArea(@NotNull Entity entity) {
        entity.getSolidArea().x = entity.getSolidAreaDefaultX();
        entity.getSolidArea().y = entity.getSolidAreaDefaultY();
    }
}