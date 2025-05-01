package tile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import frame.FrameApp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import player.Player;
import window.GameWindow;

public class TileManagerTest {

    private static class FakePlayer extends Player {
        public FakePlayer() {
            super(null, null);
        }

        @Override
        public int getWorldX() {
            return FrameApp.getTileSize() * 23;
        }

        @Override
        public int getWorldY() {
            return FrameApp.getTileSize() * 21;
        }

        @Override
        public int getScreenX() {
            return FrameApp.getScreenWidth() / 2 - (FrameApp.getTileSize() / 2);
        }

        @Override
        public int getScreenY() {
            return FrameApp.getScreenHeight() / 2 - (FrameApp.getTileSize() / 2);
        }
    }

    private static class FakeGameWindow extends GameWindow {
        private Player fakePlayer = new FakePlayer();

        @Override
        public Player getPlayer() {
            return fakePlayer;
        }
    }

    private TileManager tileManager;

    @BeforeEach
    public void setUp() {
        FakeGameWindow fakeGameWindow = new FakeGameWindow();
        tileManager = new TileManager(fakeGameWindow);
    }


    @Test
    public void testLoadMapDimensions() {
        int[][] mapTileNum = tileManager.getMapTileNum();
        assertEquals(FrameApp.getMaxWorldCol(), mapTileNum.length, "マップの列数が一致していること");
        if (mapTileNum.length > 0) {
            assertEquals(FrameApp.getMaxWorldRow(), mapTileNum[0].length, "マップの行数が一致していること");
        }
    }

    @Test
    public void testDrawMap() {
        BufferedImage dummyCanvas = new BufferedImage(FrameApp.getMaxWorldRow(), FrameApp.getMaxWorldCol(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dummyCanvas.createGraphics();
        assertDoesNotThrow(() -> tileManager.draw(g2), "マップ描画中に例外が発生してはいけない");
        g2.dispose();
    }
}