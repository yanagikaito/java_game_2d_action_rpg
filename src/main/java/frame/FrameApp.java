package frame;

public class FrameApp {

    private static final int maxScreenRow = 16;
    private static final int maxScreenCol = 12;
    private static final int tileSize = createSize();
    private static final int screenWidth = tileSize * maxScreenRow;
    private static final int screenHeight = tileSize * maxScreenCol;
    private static final int maxWorldRow = 50;
    private static final int maxWorldCol = 50;

    public static FrameSize baseDisplay() {

        return new FrameSize(screenWidth, screenHeight);
    }

    public static int createSize() {
        int originalTileSize = 16;
        int scale = 3;
        return originalTileSize * scale;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static int getMaxScreenCol() {
        return maxScreenCol;
    }

    public static int getMaxScreenRow() {
        return maxScreenRow;
    }

    public static int getMaxWorldCol() {
        return maxWorldCol;
    }

    public static int getMaxWorldRow() {
        return maxWorldRow;
    }

    public static int getTileSize() {
        return tileSize;
    }
}