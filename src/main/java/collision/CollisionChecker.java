package collision;

import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

public class CollisionChecker {

    private GameWindow gameWindow;

    public CollisionChecker(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void checkTile(Entity entity) {

        int entityLeftX = entity.getWorldX() + entity.getSolidArea().x;
        int entityRightX = entity.getWorldX() + entity.getSolidArea().x + entity.getSolidArea().width;
        int entityTopY = entity.getWorldY() + entity.getSolidArea().y;
        int entityBottomY = entity.getWorldY() + entity.getSolidArea().y + entity.getSolidArea().height;

        int entityLeftCol = entityLeftX / FrameApp.createSize();
        int entityRightCol = entityRightX / FrameApp.createSize();
        int entityTopRow = entityTopY / FrameApp.createSize();
        int entityBottomRow = entityBottomY / FrameApp.createSize();

        int tileNum1, tileNum2;

        switch (entity.getDirection()) {
            case "up" -> {
                entityTopRow = (entityTopY - entity.getSpeed()) / FrameApp.createSize();
                tileNum1 = gameWindow.getTileManager().getMapTileNum()[entityLeftCol][entityTopRow];
                tileNum2 = gameWindow.getTileManager().getMapTileNum()[entityRightCol][entityTopRow];
                if (gameWindow.getTileManager().getTiles()[tileNum1].collision
                        || gameWindow.getTileManager().getTiles()[tileNum2].collision) {
                    entity.setCollision(true);
                }
            }
            case "down" -> {
                entityBottomRow = (entityBottomY + entity.getSpeed()) / FrameApp.createSize();
                tileNum1 = gameWindow.getTileManager().getMapTileNum()[entityLeftCol][entityBottomRow];
                tileNum2 = gameWindow.getTileManager().getMapTileNum()[entityRightCol][entityBottomRow];
                if (gameWindow.getTileManager().getTiles()[tileNum1].collision
                        || gameWindow.getTileManager().getTiles()[tileNum2].collision) {
                    entity.setCollision(true);
                }
            }
            case "left" -> {
                entityLeftCol = (entityLeftX - entity.getSpeed()) / FrameApp.createSize();
                tileNum1 = gameWindow.getTileManager().getMapTileNum()[entityLeftCol][entityTopRow];
                tileNum2 = gameWindow.getTileManager().getMapTileNum()[entityLeftCol][entityBottomRow];
                if (gameWindow.getTileManager().getTiles()[tileNum1].collision
                        || gameWindow.getTileManager().getTiles()[tileNum2].collision) {
                    entity.setCollision(true);
                }
            }
            case "right" -> {
                entityRightCol = (entityRightX + entity.getSpeed()) / FrameApp.createSize();
                tileNum1 = gameWindow.getTileManager().getMapTileNum()[entityRightCol][entityTopRow];
                tileNum2 = gameWindow.getTileManager().getMapTileNum()[entityRightCol][entityBottomRow];
                if (gameWindow.getTileManager().getTiles()[tileNum1].collision
                        || gameWindow.getTileManager().getTiles()[tileNum2].collision) {
                    entity.setCollision(true);
                }
            }
        }
    }
}