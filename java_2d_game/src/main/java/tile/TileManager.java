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

    public static final int MEADOW_TILE_ID = 1;
    public static final int TREE_TILE_ID = 4;
    public static final int HUT_TILE_ID = 6;
    public static final int FOREST_TILE_ID = 9;
    public static final int STAIRS_DOWN_TILE_ID = 12;
    public static final int STAIRS_UP_TILE_ID = 13;

    public TileManager(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        tiles = new Tile[15];
        mapTileNum = new int[FrameApp.getMaxWorldCol()][FrameApp.getMaxWorldRow()];
        loadTileImages();
        loadMap(1);
    }

    /**
     * 指定されたタイルが障害物かどうかを返す
     *
     * @param x タイルのX座標
     * @param y タイルのY座標
     * @return 障害物なら true
     */

    public boolean isObstacle(int x, int y) {
        if (x < 0 || x >= FrameApp.getMaxWorldRow() || y < 0 || y >= FrameApp.getMaxWorldCol()) {
            return true; // マップ外は通行不可（壁扱い）
        }
        int tileId = mapTileNum[y][x];
        return tileId == TREE_TILE_ID; // 壁のIDと一致すれば障害物
    }

    private void loadTileImages() {

        try {

            tiles[0] = new Tile();
            tiles[0].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/earth.png"));
            tiles[0].bombCollision = true;
            tiles[0].potCollision = true;

            tiles[1] = new Tile();
            tiles[1].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/meadow.png"));
            tiles[1].chickenCollision = true;

            tiles[2] = new Tile();
            tiles[2].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/pond.png"));
            tiles[2].collision = true;

            tiles[3] = new Tile();
            tiles[3].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/sand.png"));

            tiles[4] = new Tile();
            tiles[4].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/tree.png"));
            tiles[4].collision = true;
            tiles[4].chickenCollision = true;

            tiles[5] = new Tile();
            tiles[5].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/wall.png"));
            tiles[5].collision = true;

            tiles[6] = new Tile();
            tiles[6].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/hut.png"));

            tiles[7] = new Tile();
            tiles[7].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/floor.png"));

            tiles[8] = new Tile();
            tiles[8].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/table.png"));
            tiles[8].collision = true;

            tiles[9] = new Tile();
            tiles[9].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/forest.png"));

            tiles[10] = new Tile();
            tiles[10].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/tree‑stump.png"));
            tiles[10].collision = true;
            tiles[10].chickenCollision = true;

            tiles[11] = new Tile();
            tiles[11].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-floor.png"));

            tiles[12] = new Tile();
            tiles[12].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/stairs-down.png"));

            tiles[13] = new Tile();
            tiles[13].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/stairs-up.png"));

            tiles[14] = new Tile();
            tiles[14].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/rock.png"));
            tiles[14].collision = true;
            tiles[14].rockCollision = true;

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

    // マップ読み込み後のタイルIDを取得
    public int getTileIdAt(int col, int row) {
        // 範囲外チェックを入れても安全
        if (col < 0 || col >= mapTileNum.length ||
                row < 0 || row >= mapTileNum[0].length) {
            return -1;
        }
        return mapTileNum[col][row];
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public int[][] getMapTileNum() {
        return mapTileNum;
    }
}