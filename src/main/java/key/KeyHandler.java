package key;

import window.GameWindow;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

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