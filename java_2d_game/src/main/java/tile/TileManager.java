package tile;

import db.DbManager;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TileManager {

    private GameWindow gameWindow;

    private Tile[] tiles;

    private int[][] mapTileNum;

    public TileManager(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        tiles = new Tile[6];
        mapTileNum = new int[FrameApp.getMaxWorldCol()][FrameApp.getMaxWorldRow()];
        loadTileImages();
        loadMap(1);
    }


    private void loadTileImages() {

        try {

            tiles[0] = new Tile();
            tiles[0].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/earth.png"));

            tiles[1] = new Tile();
            tiles[1].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/meadow.png"));

            tiles[2] = new Tile();
            tiles[2].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/pond.png"));
            tiles[2].collision = true;

            tiles[3] = new Tile();
            tiles[3].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/sand.png"));

            tiles[4] = new Tile();
            tiles[4].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/tree.gif"));
            tiles[4].collision = true;

            tiles[5] = new Tile();
            tiles[5].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/wall.png"));
            tiles[5].collision = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap(int mapId) {
        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT x, y, tile_id FROM map_tile WHERE map_id = ?"
             )) {
            ps.setInt(1, mapId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int tileId = rs.getInt("tile_id");
                    mapTileNum[x][y] = tileId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void draw(Graphics2D g2) {

        for (int row = 0; row < FrameApp.getMaxWorldRow(); row++) {
            for (int col = 0; col < FrameApp.getMaxWorldCol(); col++) {
                int tileNum = mapTileNum[col][row];

                int worldX = col * FrameApp.getTileSize();
                int worldY = row * FrameApp.getTileSize();

                int screenX = worldX - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
                int screenY = worldY - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

                if (worldX > gameWindow.getPlayer().getWorldX() - gameWindow.getPlayer().getScreenX() &&
                        worldX < gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX() &&
                        worldY > gameWindow.getPlayer().getWorldY() - gameWindow.getPlayer().getScreenY() &&
                        worldY < gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY()) {

                    g2.drawImage(tiles[tileNum].image, screenX, screenY,
                            FrameApp.getTileSize(), FrameApp.getTileSize(), null);
                }
                g2.drawImage(tiles[tileNum].image, screenX, screenY,
                        FrameApp.getTileSize(), FrameApp.getTileSize(), null);
            }
        }
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public int[][] getMapTileNum() {
        return mapTileNum;
    }
}