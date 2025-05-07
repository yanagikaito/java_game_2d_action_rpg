package key;

import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    private final GameWindow gameWindow;
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
    public void keyPressed(@NotNull KeyEvent e) {

        int code = e.getKeyCode();

        switch (code) {
            case KeyEvent.VK_W -> setPlayerUp(true);
            case KeyEvent.VK_S -> setPlayerDown(true);
            case KeyEvent.VK_A -> setPlayerLeft(true);
            case KeyEvent.VK_D -> setPlayerRight(true);
            case KeyEvent.VK_P -> togglePause();
            case KeyEvent.VK_ENTER -> speakDialogue();
            default -> {
            }
        }
    }

    @Override
    public void keyReleased(@NotNull KeyEvent e) {

        int code = e.getKeyCode();

        switch (code) {
            case KeyEvent.VK_W -> setPlayerUp(false);
            case KeyEvent.VK_S -> setPlayerDown(false);
            case KeyEvent.VK_A -> setPlayerLeft(false);
            case KeyEvent.VK_D -> setPlayerRight(false);
            default -> {
            }
        }
    }


    private void togglePause() {
        if (gameWindow.getGameState() == gameWindow.getPlayState()) {
            gameWindow.setGameState(gameWindow.getPauseState());
        } else if (gameWindow.getGameState() == gameWindow.getPauseState()) {
            gameWindow.setGameState(gameWindow.getPlayState());
        }
    }

    private void speakDialogue() {
        if (gameWindow.getGameState() == gameWindow.getDialogueState()) {
            gameWindow.setGameState(gameWindow.getPlayState());
        }
    }
}