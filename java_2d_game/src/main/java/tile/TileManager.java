package tile;

import db.DbManager;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
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

    // パーティクル（しぶき）
    private final List<WaterSplash> waterParticles = new ArrayList<>();
    private final int maxParticles = 120; // 上限

    public TileManager(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        tiles = new Tile[15];
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
            tiles[14].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/cave-wall.png"));
            tiles[14].collision = true;

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
        if (waterFrames != null && waterFrames.length > 0) {
            waterAnimTime += deltaSeconds;
            int frameIndex = (int) (waterAnimTime / waterFrameTime) % waterFrames.length;
            waterTexture = waterFrames[frameIndex];
        }
        if (waterTexture != null) {
            waterOffsetX = (waterOffsetX + waterSpeed * deltaSeconds) % waterTexture.getWidth();
        }

        // パーティクル更新
        for (int i = 0; i < waterParticles.size(); i++) {
            WaterSplash p = waterParticles.get(i);
            p.update(deltaSeconds);
            if (!p.isAlive()) {
                waterParticles.remove(i--);
            }
        }
        // パーティクル上限を維持
        while (waterParticles.size() > maxParticles) {
            waterParticles.remove(0);
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

        int viewX = gameWindow.getPlayer().getWorldX() - gameWindow.getPlayer().getScreenX();
        int viewY = gameWindow.getPlayer().getWorldY() - gameWindow.getPlayer().getScreenY();
        int viewW = gameWindow.getWidth();
        int viewH = gameWindow.getHeight();

        // waterTexture が null でなければ、水タイル領域だけに描画する
        if (waterTexture != null) {
            int texW = waterTexture.getWidth();
            int texH = waterTexture.getHeight();

            // テクスチャのスクロール基準をワールド座標に合わせる
            // こうすると隣接タイルで同じ基準が使われ、継ぎ目が出にくい
            double baseOffset = waterOffsetX;

            // 各可視タイルを走査して、水タイルだけクリップして描画する
            Shape oldClip = g2.getClip();
            for (int row = 0; row < FrameApp.getMaxWorldRow(); row++) {
                for (int col = 0; col < FrameApp.getMaxWorldCol(); col++) {
                    int tileNum = mapTileNum[col][row];
                    if (tileNum != WATER_TILE_ID) continue;

                    int worldX = col * FrameApp.getTileSize();
                    int worldY = row * FrameApp.getTileSize();

                    // 画面外チェック（タイル全体が画面外ならスキップ）
                    if (worldX + FrameApp.getTileSize() < viewX || worldX > viewX + viewW ||
                            worldY + FrameApp.getTileSize() < viewY || worldY > viewY + viewH) {
                        continue;
                    }

                    // タイルのスクリーン座標
                    int screenX = worldX - viewX;
                    int screenY = worldY - viewY;

                    // クリップをタイル矩形に設定
                    g2.setClip(screenX, screenY, FrameApp.getTileSize(), FrameApp.getTileSize());

                    // テクスチャをタイル矩形を覆うように繰り返し描画する
                    // テクスチャの原点をワールド基準で揃えるため、描画開始Xを計算
                    // world 基準のオフセット = (worldX + baseOffset) % texW を使うと継ぎ目が揃う
                    int alignedX = (int) ((worldX + baseOffset) % texW);
                    // 描画ループはスクリーン座標で行う。startX は screenX - alignedX から始める
                    int startX = screenX - alignedX - texW;
                    for (int x = startX; x < screenX + FrameApp.getTileSize(); x += texW) {
                        for (int y = screenY - texH; y < screenY + FrameApp.getTileSize(); y += texH) {
                            g2.drawImage(waterTexture, x, y, null);
                        }
                    }

                    // クリップを元に戻さず次ループで上書きする（最後に復元）
                }
            }
            // クリップを元に戻す
            g2.setClip(oldClip);
        }

        // パーティクル描画（しぶき）: ワールド→スクリーン変換して描画
        for (WaterSplash p : waterParticles) {
            p.draw(g2, viewX, viewY);
        }
    }

    // 水しぶきをワールド座標で発生させる
    public void spawnSplashAt(double worldX, double worldY, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(60 + Math.random() * 60 - 30);
            double speed = 40 + Math.random() * 120;
            double vx = Math.cos(angle) * speed * (Math.random() < 0.5 ? -1 : 1);
            double vy = -Math.sin(angle) * speed;
            waterParticles.add(new WaterSplash(worldX + Math.random() * FrameApp.getTileSize(),
                    worldY + Math.random() * FrameApp.getTileSize(),
                    vx, vy, 0.4 + Math.random() * 0.6));
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

    // WaterSplash 内部クラス
    public static class WaterSplash {
        double x, y;
        double vx, vy;
        double life, maxLife;

        public WaterSplash(double x, double y, double vx, double vy, double life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
        }

        public void update(double dt) {
            life -= dt;
            x += vx * dt;
            y += vy * dt;
            vy += 300 * dt; // 重力風
        }

        public void draw(Graphics2D g2, int viewX, int viewY) {
            if (life <= 0) return;
            float alpha = (float) Math.max(0, life / maxLife);
            Composite oldComp = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int size = (int) (3 + (1 - alpha) * 8);
            g2.setColor(new Color(180, 220, 255));
            g2.fillOval((int) (x - viewX) - size / 2, (int) (y - viewY) - size / 2, size, size);
            g2.setComposite(oldComp);
        }

        public boolean isAlive() {
            return life > 0;
        }
    }
}