package tile;

import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TileManager {

    private GameWindow gameWindow;

    private Tile[] tiles;

    private int[][] mapTileNum;

    public TileManager(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        tiles = new Tile[6];
        mapTileNum = new int[FrameApp.getMaxWorldCol()][FrameApp.getMaxWorldRow()];
        loadTileImages();
        loadMap("map/world01.txt");
    }


    private void loadTileImages() {

        try {

            tiles[0] = new Tile();
            tiles[0].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/meadow.png"));

            tiles[1] = new Tile();
            tiles[1].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/wall.png"));
            tiles[1].collision = true;

            tiles[2] = new Tile();
            tiles[2].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/pond.png"));
            tiles[2].collision = true;

            tiles[3] = new Tile();
            tiles[3].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/sand.png"));

            tiles[4] = new Tile();
            tiles[4].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/tree.gif"));
            tiles[4].collision = true;

            tiles[5] = new Tile();
            tiles[5].image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tile/earth.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMap(String filePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            for (int row = 0; row < FrameApp.getMaxWorldRow(); row++) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                String[] numbers = line.split(" ");
                for (int col = 0; col < FrameApp.getMaxWorldCol(); col++) {
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
                }
            }
        } catch (Exception e) {
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