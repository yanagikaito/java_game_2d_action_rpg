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
    private boolean showDebugText;
    private boolean shotKeyPressed;
    private int commandNum;
    private static final int MAX_COL = 3;
    private static final int MAX_ROW = 4;

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

    public boolean isShotKeyPressed() {
        return shotKeyPressed;
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

    public void setShotKeyPressed(boolean shotKeyPressed) {
        this.shotKeyPressed = shotKeyPressed;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();

        if (gameWindow.getGameState() == gameWindow.getCharacterState()) {

            int row = gameWindow.getUi().getSlotRow();
            int col = gameWindow.getUi().getSlotCol();
            int maxCol = MAX_COL;
            int maxRow = (gameWindow.getPlayer().getInventory().size() + maxCol - 1) / maxCol;

            switch (code) {
                case KeyEvent.VK_W -> {
                    if (col > 0) gameWindow.getUi().setSlotCol(col - 1);
                    gameWindow.getSoundmanager().cursorWAV("java_2d_game/res/sound/cursor-sound.wav");
                }
                case KeyEvent.VK_S -> {
                    if (col < MAX_COL) gameWindow.getUi().setSlotCol(col + 1);
                    gameWindow.getSoundmanager().cursorWAV("java_2d_game/res/sound/cursor-sound.wav");
                }
                case KeyEvent.VK_A -> {
                    if (row > 0) gameWindow.getUi().setSlotRow(row - 1);
                    gameWindow.getSoundmanager().cursorWAV("java_2d_game/res/sound/cursor-sound.wav");
                }
                case KeyEvent.VK_D -> {
                    if (row < MAX_ROW) gameWindow.getUi().setSlotRow(row + 1);
                    gameWindow.getSoundmanager().cursorWAV("java_2d_game/res/sound/cursor-sound.wav");
                }
                case KeyEvent.VK_C -> {
                    gameWindow.setGameState(gameWindow.getPlayState());
                }
                case KeyEvent.VK_ENTER -> {
                    int index = col * maxRow + row;
                    System.out.println("useRedPotion index=" + index
                            + " (row=" + row + ", col=" + col + ")"
                            + " inventorySize=" + gameWindow.getPlayer().getInventory().size()
                    );
                    gameWindow.getPlayer().selectItem(index);
                }
            }
            return;
        }

        switch (code) {
            case KeyEvent.VK_W -> setPlayerUp(true);
            case KeyEvent.VK_S -> setPlayerDown(true);
            case KeyEvent.VK_A -> setPlayerLeft(true);
            case KeyEvent.VK_D -> setPlayerRight(true);
            case KeyEvent.VK_P -> togglePause();
            case KeyEvent.VK_T -> debugText();
            case KeyEvent.VK_R -> gameWindow.toggleHitBoxDebug();
            case KeyEvent.VK_C -> gameWindow.setGameState(gameWindow.getCharacterState());
            case KeyEvent.VK_ENTER -> {
                speakDialogue(true);
                clearAllKeys();
            }
            case KeyEvent.VK_F -> setShotKeyPressed(true);
        }

        if (gameWindow.getGameState() == gameWindow.getTitleState()) {

            switch (code) {
                case KeyEvent.VK_W -> {
                    setCommandNum(getCommandNum() - 1);
                    if (getCommandNum() < 0) {
                        setCommandNum(1);
                    }
                    gameWindow.getSoundmanager().cursorWAV("java_2d_game/res/sound/cursor-sound.wav");
                }
                case KeyEvent.VK_S -> {
                    setCommandNum(getCommandNum() + 1);
                    if (getCommandNum() > 1) {
                        setCommandNum(0);
                    }
                    gameWindow.getSoundmanager().cursorWAV("java_2d_game/res/sound/cursor-sound.wav");
                }
                case KeyEvent.VK_ENTER -> {
                    if (getCommandNum() == 0) {
                        gameWindow.setGameState(gameWindow.getPlayState());
                        gameWindow.restart();
                        clearAllKeys();
                    } else {
                        System.exit(0);
                    }
                    gameWindow.getSoundmanager().cursorWAV("java_2d_game/res/sound/cursor-sound.wav");
                }
            }
        }

        if (gameWindow.getGameState() == gameWindow.getGameOverState()) {

            switch (code) {
                case KeyEvent.VK_W -> {
                    setCommandNum(getCommandNum() - 1);
                    if (getCommandNum() < 0) {
                        setCommandNum(1);
                    }
                    gameWindow.getSoundmanager().cursorWAV("java_2d_game/res/sound/cursor-sound.wav");
                }
                case KeyEvent.VK_S -> {
                    setCommandNum(getCommandNum() + 1);
                    if (getCommandNum() > 1) {
                        setCommandNum(0);
                    }
                    gameWindow.getSoundmanager().cursorWAV("java_2d_game/res/sound/cursor-sound.wav");
                }
                case KeyEvent.VK_ENTER -> {
                    if (getCommandNum() == 0) {
                        gameWindow.setGameState(gameWindow.getPlayState());
                        gameWindow.retry();
                    }
                    if (getCommandNum() == 1) {
                        gameWindow.setGameState(gameWindow.getTitleState());
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(@NotNull KeyEvent e) {

        int code = e.getKeyCode();

        if (gameWindow.getGameState() == gameWindow.getPlayState()) {

            switch (code) {
                case KeyEvent.VK_W -> setPlayerUp(false);
                case KeyEvent.VK_S -> setPlayerDown(false);
                case KeyEvent.VK_A -> setPlayerLeft(false);
                case KeyEvent.VK_D -> setPlayerRight(false);
                case KeyEvent.VK_F -> setShotKeyPressed(false);
            }
        }
    }

    public void clearAllKeys() {
        setPlayerUp(false);
        setPlayerDown(false);
        setPlayerLeft(false);
        setPlayerRight(false);
        setShotKeyPressed(false);
    }

    private void togglePause() {
        if (gameWindow.getGameState() == gameWindow.getPlayState()) {
            gameWindow.setGameState(gameWindow.getPauseState());
        } else if (gameWindow.getGameState() == gameWindow.getPauseState()) {
            gameWindow.setGameState(gameWindow.getPlayState());
        }
    }

    public void speakDialogue(boolean playerEnter) {
        if (gameWindow.getGameState() == gameWindow.getPlayState() && playerEnter) {
            this.playerEnter = true;
            clearAllKeys();
        } else if (gameWindow.getGameState() == gameWindow.getDialogueState()) {
            gameWindow.setGameState(gameWindow.getPlayState());
            gameWindow.startNpcRoute(0, 1, 0);
            gameWindow.getPlayer().setInvincible(false);
        }
    }

    public void debugText() {
        if (this.showDebugText == false) {
            setShowDebugText(true);
        } else if (this.showDebugText == true) {
            setShowDebugText(false);
        }
    }

    public int getCommandNum() {
        return commandNum;
    }

    public void setCommandNum(int commandNum) {
        this.commandNum = commandNum;
    }
}