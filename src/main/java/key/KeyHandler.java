package key;

import window.GameWindow;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    private GameWindow gameWindow;
    private boolean playerUp;
    private boolean playerDown;
    private boolean playerLeft;
    private boolean playerRight;

    public KeyHandler(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }


    public boolean isPlayerUp() {
        return playerUp;
    }

    public boolean isPlayerDown() {
        return playerDown;
    }

    public boolean isPlayerLeft() {
        return playerLeft;
    }

    public boolean isPlayerRight() {
        return playerRight;
    }


    public void setPlayerUp(boolean playerUp) {
        this.playerUp = playerUp;
    }

    public void setPlayerDown(boolean playerDown) {
        this.playerDown = playerDown;
    }

    public void setPlayerLeft(boolean playerLeft) {
        this.playerLeft = playerLeft;
    }

    public void setPlayerRight(boolean playerRight) {
        this.playerRight = playerRight;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            setPlayerUp(true);
        }
        if (code == KeyEvent.VK_S) {
            setPlayerDown(true);
        }
        if (code == KeyEvent.VK_A) {
            setPlayerLeft(true);
        }
        if (code == KeyEvent.VK_D) {
            setPlayerRight(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            setPlayerUp(false);
        }
        if (code == KeyEvent.VK_S) {
            setPlayerDown(false);
        }
        if (code == KeyEvent.VK_A) {
            setPlayerLeft(false);
        }
        if (code == KeyEvent.VK_D) {
            setPlayerRight(false);
        }
    }
}