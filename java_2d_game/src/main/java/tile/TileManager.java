package tile;

import db.DbManager;
import entity.particle.WaterEffectManager;
import entity.particle.WaterSplash;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    public static final int WATER_TILE_ID = 2;

    // 水アニメ関連
    private BufferedImage[] waterFrames;
    private BufferedImage waterTexture; // 繰り返し描画用（フレーム0をベース）
    private double waterAnimTime = 0.0;
    private final double waterFrameTime = 0.12; // 秒
    private double waterOffsetX = 0.0;
    private double waterSpeed = 30.0; // px/sec（調整可）

    private WaterEffectManager waterEffectManager;


    // パーティクル（しぶき）
    private final List<WaterSplash> waterParticles = new ArrayList<>();
    private final int maxParticles = 120; // 上限

    public TileManager(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        tiles = new Tile[22];
        mapTileNum = new int[FrameApp.getMaxWorldCol()][FrameApp.getMaxWorldRow()];
        loadTileImages();
        loadMap(1);
    }

    public boolean isObstacle(int x, int y) {
        if (x < 0 || x >= FrameApp.getMaxWorldRow() || y < 0 || y >= FrameApp.getMaxWorldCol()) {
            return true;
        }
        int tileId = mapTileNum[y][x];
        return tileId == TREE_TILE_ID;
    }

    private void loadTileImages() {
        try {
            tiles[0] = new Tile();
            tiles[0].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/earth.png"));
            tiles[0].bombCollision = true;
            tiles[0].potCollision = true;
            tiles[0].rockCollision = true;

            tiles[1] = new Tile();
            tiles[1].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/meadow.png"));
            tiles[1].chickenCollision = true;

            // 水アニメフレーム読み込み（4フレーム）
            tiles[2] = new Tile();
            waterFrames = new BufferedImage[4];
            waterFrames[0] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/water_0.png"));
            waterFrames[1] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/water_1.png"));
            waterFrames[2] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/water_2.png"));
            waterFrames[3] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/water_3.png"));
            waterTexture = waterFrames[0]; // ベーステクスチャ

            tiles[3] = new Tile();
            tiles[3].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/sand.png"));
            tiles[3].bombCollision = true;
            tiles[3].potCollision = true;
            tiles[3].rockCollision = true;

            tiles[4] = new Tile();
            tiles[4].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/tree.png"));
            tiles[4].collision = true;
            tiles[4].bombCollision = true;
            tiles[4].potCollision = true;
            tiles[4].rockCollision = true;
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
            tiles[10].bombCollision = true;
            tiles[10].potCollision = true;
            tiles[10].rockCollision = true;
            tiles[10].chickenCollision = true;

            tiles[11] = new Tile();
            tiles[11].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-floor.png"));

            tiles[12] = new Tile();
            tiles[12].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/stairs-down.png"));

            tiles[13] = new Tile();
            tiles[13].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/stairs-up.png"));

            tiles[14] = new Tile();
            tiles[14].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-wall_down.png"));
            tiles[14].collision = true;

            tiles[15] = new Tile();
            tiles[15].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-wall-corner-slanted-1.png"));
            tiles[15].collision = true;

            tiles[16] = new Tile();
            tiles[16].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-wall-corner-slanted-2.png"));
            tiles[16].collision = true;

            tiles[17] = new Tile();
            tiles[17].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-wall-corner-slanted-3.png"));
            tiles[17].collision = true;

            tiles[18] = new Tile();
            tiles[18].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-wall-corner-slanted-4.png"));
            tiles[18].collision = true;

            tiles[19] = new Tile();
            tiles[19].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-wall_left.png"));
            tiles[19].collision = true;

            tiles[20] = new Tile();
            tiles[20].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-wall_right.png"));
            tiles[20].collision = true;

            tiles[21] = new Tile();
            tiles[21].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-wall_up.png"));
            tiles[21].collision = true;

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

    /**
     * 毎フレーム呼び出してタイルアニメ等を更新する
     *
     * @param deltaSeconds 前フレームからの経過時間（秒）
     */

    public void update(double deltaSeconds) {
        // 水アニメ更新
        if (waterFrames != null && waterFrames.length > 0) {
            waterAnimTime += deltaSeconds;
            int frameIndex = (int) (waterAnimTime / waterFrameTime) % waterFrames.length;
            waterTexture = waterFrames[frameIndex];
        }
        if (waterTexture != null) {
            waterOffsetX = (waterOffsetX + waterSpeed * deltaSeconds) % waterTexture.getWidth();
        }

        // パーティクル更新（後方から削除）
        for (int i = waterParticles.size() - 1; i >= 0; i--) {
            WaterSplash p = waterParticles.get(i);
            p.update(deltaSeconds);
            if (!p.isAlive()) waterParticles.remove(i);
        }
        while (waterParticles.size() > maxParticles) waterParticles.remove(0);

        // WaterEffectManager があれば更新（foot エフェクト等）
        if (waterEffectManager != null) {
            waterEffectManager.update(deltaSeconds);
        }
    }

    public void draw(Graphics2D g2) {

        // タイル描画（通常）
        for (int row = 0; row < FrameApp.getMaxWorldRow(); row++) {
            for (int col = 0; col < FrameApp.getMaxWorldCol(); col++) {
                int tileNum = mapTileNum[col][row];
                if (tileNum < 0 || tileNum >= tiles.length || tiles[tileNum] == null) continue;

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

        int tileSize = FrameApp.getTileSize();
        int viewX = gameWindow.getPlayer().getWorldX() - gameWindow.getPlayer().getScreenX();
        int viewY = gameWindow.getPlayer().getWorldY() - gameWindow.getPlayer().getScreenY();
        int viewW = gameWindow.getWidth();
        int viewH = gameWindow.getHeight();


        // waterTexture があれば水領域だけにテクスチャを敷く
        if (waterTexture != null) {
            int texW = waterTexture.getWidth();
            int texH = waterTexture.getHeight();
            double baseOffset = waterOffsetX;

            Shape oldClip = g2.getClip();
            for (int row = 0; row < FrameApp.getMaxWorldRow(); row++) {
                for (int col = 0; col < FrameApp.getMaxWorldCol(); col++) {
                    int tileNum = mapTileNum[col][row];
                    if (tileNum != WATER_TILE_ID) continue;

                    int worldX = col * tileSize;
                    int worldY = row * tileSize;

                    if (worldX + tileSize < viewX || worldX > viewX + viewW ||
                            worldY + tileSize < viewY || worldY > viewY + viewH) continue;

                    int screenX = worldX - viewX;
                    int screenY = worldY - viewY;

                    g2.setClip(screenX, screenY, tileSize, tileSize);

                    int alignedX = Math.floorMod((int) Math.round(worldX + baseOffset), texW);
                    int startX = screenX - alignedX - texW;
                    for (int x = startX; x < screenX + tileSize; x += texW) {
                        for (int y = screenY - texH; y < screenY + tileSize; y += texH) {
                            g2.drawImage(waterTexture, x, y, null);
                        }
                    }
                }
            }
            g2.setClip(oldClip);
        }

        // パーティクル描画（しぶき）
        for (WaterSplash p : waterParticles) {
            p.draw(g2, viewX, viewY);
        }

        if (waterEffectManager != null) {
            waterEffectManager.draw(g2, viewX, viewY);
        }
    }

    public void spawnSplashAt(double worldX, double worldY, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(60 + Math.random() * 60 - 30);
            double speed = 40 + Math.random() * 120;
            double vx = Math.cos(angle) * speed * (Math.random() < 0.5 ? -1 : 1);
            double vy = -Math.sin(angle) * speed;
            double px = worldX + (Math.random() - 0.5) * FrameApp.getTileSize() * 0.5;
            double py = worldY + (Math.random() - 0.5) * FrameApp.getTileSize() * 0.5;
            double life = 0.4 + Math.random() * 0.6;

            WaterSplash s = new WaterSplash(px, py, vx, vy, life);
            waterParticles.add(s);

            // デバッグログ
            System.out.println("TileManager.spawnSplashAt added splash px=" + px + " py=" + py + " vx=" + vx + " vy=" + vy + " life=" + life);
        }
    }

    // マップ読み込み後のタイルIDを取得
    public int getTileIdAt(int col, int row) {
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

    public void setWaterEffectManager(WaterEffectManager manager) {
        this.waterEffectManager = manager;
    }

    public WaterEffectManager getWaterEffectManager() {
        return this.waterEffectManager;
    }
}