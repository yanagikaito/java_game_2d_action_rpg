package player;

import key.KeyHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import window.GameWindow;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerTest {

    private FakeKeyHandler keyHandler;
    private FakeGameWindow gameWindow;
    private Player player;

    private static class FakeKeyHandler extends KeyHandler {

        public boolean playerUp = false;
        public boolean playerDown = false;
        public boolean playerLeft = false;
        public boolean playerRight = false;

        public FakeKeyHandler(GameWindow gameWindow) {
            super(gameWindow);
        }
    }

    private static class FakeGameWindow extends GameWindow {
        public FakeGameWindow() {
            super();
        }
    }

    @BeforeEach
    public void setUp() {
        keyHandler = new FakeKeyHandler(gameWindow);
        gameWindow = new FakeGameWindow();
        player = new Player(gameWindow, keyHandler);
    }

    @Test
    public void testMoveDown() {
        int initialY = player.getWorldY();
        keyHandler.playerDown = true;
        player.update();
        assertEquals(initialY + player.getSpeed(), player.getWorldY() + player.getSpeed(),
                "プレイヤーはスピード分だけ下に移動する");
    }

    @Test
    public void testMoveUp() {
        int initialY = player.getWorldY();
        keyHandler.playerUp = true;
        player.update();
        assertEquals(initialY - player.getSpeed(), player.getWorldY() - player.getSpeed(),
                "プレイヤーはスピード分だけ上に移動する");
    }

    @Test
    public void testMoveRight() {
        int initialX = player.getWorldX();
        keyHandler.playerRight = true;
        player.update();
        assertEquals(initialX + player.getSpeed(), player.getWorldX() + player.getSpeed(),
                "プレイヤーはスピード分だけ右に移動する");
    }

    @Test
    public void testMoveLeft() {
        int initialX = player.getWorldX();
        keyHandler.playerLeft = true;
        player.update();
        assertEquals(initialX - player.getSpeed(), player.getWorldX() - player.getSpeed(),
                "プレイヤーはスピード分だけ左に移動する");
    }
}