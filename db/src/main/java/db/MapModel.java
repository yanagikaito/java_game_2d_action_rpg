package db;

public class MapModel {

    private final int width, height;
    private final int[][] tiles;

    public MapModel(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new int[height][width];
    }

    public void setTile(int x, int y, int id) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        tiles[y][x] = id;
    }

    public int getTile(int x, int y) {
        return tiles[y][x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}