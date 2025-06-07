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
    private boolean playerEnter;
    private boolean playerStatus;
    private boolean showDebugText;

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

    public boolean isPlayerEnter() {
        return playerEnter;
    }

    public boolean isShowDebugText() {
        return showDebugText;
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

    public void setPlayerEnter(boolean playerEnter) {
        this.playerEnter = playerEnter;
    }

    public void setShowDebugText(boolean showDebugText) {
        this.showDebugText = showDebugText;
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
            case KeyEvent.VK_C -> characterStatus(true);
            case KeyEvent.VK_T -> debugText();
            case KeyEvent.VK_ENTER -> speakDialogue(true);
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

    public void speakDialogue(boolean playerEnter) {
        if (gameWindow.getGameState() == gameWindow.getPlayState()) {
            this.playerEnter = playerEnter;
        } else if (gameWindow.getGameState() == gameWindow.getDialogueState()) {
            gameWindow.setGameState(gameWindow.getPlayState());
        }
        if (this.playerEnter == false) {
            gameWindow.setGameState(gameWindow.getPlayState());
        }
    }

    public void characterStatus(boolean playerStatus) {
        this.playerStatus = playerStatus;
        if (gameWindow.getGameState() == gameWindow.getPlayState()) {
            gameWindow.setCharacterState(gameWindow.getCharacterState());
        } else if (gameWindow.getGameState() == gameWindow.getCharacterState()) {
            gameWindow.setGameState(gameWindow.getPlayState());
        }
    }

    public void debugText() {
        if (this.showDebugText == false) {
            setShowDebugText(true);
        } else if (this.showDebugText == true) {
            setShowDebugText(false);
        }
    }
}