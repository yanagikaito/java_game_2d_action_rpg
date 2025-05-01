package key;

import org.junit.jupiter.api.Test;
import window.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyHandlerTest {


    @Test
    public void testKeyPressW() {

        KeyHandlerTest container = new KeyHandlerTest();

        KeyHandler keyHandler = container.new KeyHandler(null);

        Component source = new JPanel();

        // int modifiers は、修飾キー（Shift、Ctrl、Alt など）が押されているかどうかを示す
        // 押されていない場合はmodifiersの値は通常 0
        KeyEvent pressW = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W');

        keyHandler.keyPressed(pressW);

        assertTrue(keyHandler.playerUp, "Wキーを押すと playerUp が true");
    }

    @Test
    public void testKeyPressS() {

        KeyHandlerTest container = new KeyHandlerTest();

        KeyHandler keyHandler = container.new KeyHandler(null);

        Component source = new JPanel();

        // int modifiers は、修飾キー（Shift、Ctrl、Alt など）が押されているかどうかを示す
        // 押されていない場合はmodifiersの値は通常 0
        KeyEvent pressS = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_S, 'S');

        keyHandler.keyPressed(pressS);

        assertTrue(keyHandler.playerDown, "Sキーを押すと playerDown が true");
    }

    @Test
    public void testKeyPressA() {

        KeyHandlerTest container = new KeyHandlerTest();

        KeyHandler keyHandler = container.new KeyHandler(null);

        Component source = new JPanel();

        // int modifiers は、修飾キー（Shift、Ctrl、Alt など）が押されているかどうかを示す
        // 押されていない場合はmodifiersの値は通常 0
        KeyEvent pressA = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'A');

        keyHandler.keyPressed(pressA);

        assertTrue(keyHandler.playerLeft, "Aキーを押すと playerLeft が true");
    }

    @Test
    public void testKeyPressD() {

        KeyHandlerTest container = new KeyHandlerTest();

        KeyHandler keyHandler = container.new KeyHandler(null);

        Component source = new JPanel();

        // int modifiers は、修飾キー（Shift、Ctrl、Alt など）が押されているかどうかを示す
        // 押されていない場合はmodifiersの値は通常 0
        KeyEvent pressD = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_D, 'D');

        keyHandler.keyPressed(pressD);

        assertTrue(keyHandler.playerRight, "Dキーを押すと  playerRight が true");
    }

    @Test
    public void testKeyReleaseW() {

        KeyHandlerTest container = new KeyHandlerTest();

        KeyHandler keyHandler = container.new KeyHandler(null);

        Component source = new JPanel();

        // int modifiers は、修飾キー（Shift、Ctrl、Alt など）が押されているかどうかを示す
        // 押されていない場合はmodifiersの値は通常 0
        KeyEvent releaseW = new KeyEvent(source, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W');

        keyHandler.keyReleased(releaseW);

        assertFalse(keyHandler.playerUp, "Wキーを離すと playerUp が false");
    }

    @Test
    public void testKeyReleaseS() {

        KeyHandlerTest container = new KeyHandlerTest();

        KeyHandler keyHandler = container.new KeyHandler(null);

        Component source = new JPanel();

        // int modifiers は、修飾キー（Shift、Ctrl、Alt など）が押されているかどうかを示す
        // 押されていない場合はmodifiersの値は通常 0
        KeyEvent releaseS = new KeyEvent(source, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_S, 'S');

        keyHandler.keyReleased(releaseS);

        assertFalse(keyHandler.playerDown, "Sキーを離すと playerDown が false");
    }

    @Test
    public void testKeyReleaseA() {

        KeyHandlerTest container = new KeyHandlerTest();

        KeyHandler keyHandler = container.new KeyHandler(null);

        Component source = new JPanel();

        // int modifiers は、修飾キー（Shift、Ctrl、Alt など）が押されているかどうかを示す
        // 押されていない場合はmodifiersの値は通常 0
        KeyEvent releaseA = new KeyEvent(source, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'A');

        keyHandler.keyReleased(releaseA);

        assertFalse(keyHandler.playerLeft, "Aキーを離すと playerLeft が false");
    }

    @Test
    public void testKeyReleaseD() {

        KeyHandlerTest container = new KeyHandlerTest();

        KeyHandler keyHandler = container.new KeyHandler(null);

        Component source = new JPanel();

        // int modifiers は、修飾キー（Shift、Ctrl、Alt など）が押されているかどうかを示す
        // 押されていない場合はmodifiersの値は通常 0
        KeyEvent releaseD = new KeyEvent(source, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_D, 'D');

        keyHandler.keyReleased(releaseD);

        assertFalse(keyHandler.playerRight, "Sキーを離すと playerRight が false");
    }

    private class KeyHandler implements KeyListener {

        private GameWindow gameWindow;
        public boolean playerUp;
        public boolean playerDown;
        public boolean playerLeft;
        public boolean playerRight;

        public KeyHandler(GameWindow gameWindow) {
            this.gameWindow = gameWindow;
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            int code = e.getKeyCode();

            if (code == KeyEvent.VK_W) {
                playerUp = true;
            }
            if (code == KeyEvent.VK_S) {
                playerDown = true;
            }
            if (code == KeyEvent.VK_A) {
                playerLeft = true;
            }
            if (code == KeyEvent.VK_D) {
                playerRight = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_W) {
                playerUp = false;
            }
            if (code == KeyEvent.VK_S) {
                playerDown = false;
            }
            if (code == KeyEvent.VK_A) {
                playerLeft = false;
            }
            if (code == KeyEvent.VK_D) {
                playerRight = false;
            }
        }
    }
}