package key;

import npc.NpcMerChant;
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
    private boolean bombKeyPressed;
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

    public boolean isBombKeyPressed() {
        return bombKeyPressed;
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

    public void setBombKeyPressed(boolean bombKeyPressed) {
        this.bombKeyPressed = bombKeyPressed;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();

        if (gameWindow.getGameState() == gameWindow.getCharacterState()) {

            int playerSlotRow = gameWindow.getUi().getPlayerSlotRow();
            int playerSlotCol = gameWindow.getUi().getPlayerSlotCol();
            int maxCol = MAX_COL;
            int maxRow = (gameWindow.getPlayer().getInventory().size() + maxCol - 1) / maxCol;

            switch (code) {
                case KeyEvent.VK_W -> {
                    if (playerSlotCol > 0) gameWindow.getUi().setPlayerSlotCol(playerSlotCol - 1);
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                }
                case KeyEvent.VK_S -> {
                    if (playerSlotCol < MAX_COL) gameWindow.getUi().setPlayerSlotCol(playerSlotCol + 1);
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                }
                case KeyEvent.VK_A -> {
                    if (playerSlotRow > 0) gameWindow.getUi().setPlayerSlotRow(playerSlotRow - 1);
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                }
                case KeyEvent.VK_D -> {
                    if (playerSlotRow < MAX_ROW) gameWindow.getUi().setPlayerSlotRow(playerSlotRow + 1);
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                }
                case KeyEvent.VK_C -> {
                    gameWindow.setGameState(gameWindow.getPlayState());
                }
                case KeyEvent.VK_ENTER -> {
                    int index = playerSlotCol * maxRow + playerSlotRow;
                    System.out.println("useRedPotion index=" + index
                            + " (playerSlotRow =" + playerSlotRow + ", playerSlotCol =" + playerSlotCol + ")"
                            + " inventorySize =" + gameWindow.getPlayer().getInventory().size()
                    );
                    gameWindow.getPlayer().selectItem(index);
                }
            }
            return;
        }

        if (gameWindow.isOnTransition()) return;

        switch (code) {
            case KeyEvent.VK_W -> setPlayerUp(true);
            case KeyEvent.VK_S -> setPlayerDown(true);
            case KeyEvent.VK_A -> setPlayerLeft(true);
            case KeyEvent.VK_D -> setPlayerRight(true);
            case KeyEvent.VK_P -> togglePause();
            case KeyEvent.VK_T -> debugText();
            case KeyEvent.VK_R -> gameWindow.toggleHitBoxDebug();
            case KeyEvent.VK_C -> {
                gameWindow.setGameState(gameWindow.getCharacterState());
                clearAllKeys();
            }
            case KeyEvent.VK_ENTER -> {
                int npcIdx = gameWindow.getPlayer().checkNpcInFront(gameWindow.getNPC(), 2);
                if (npcIdx != -1 && gameWindow.getNPC()[npcIdx] instanceof NpcMerChant) {
                    startConversation(npcIdx);
                    gameWindow.setGameState(gameWindow.getTradeState());
                } else if (gameWindow.getGameState() == gameWindow.getTradeState()) {
                    npcMerChantSpeak();
                    gameWindow.getUi().setSubState(0);
                } else {
                    speakDialogue();
                    clearAllKeys();
                }
            }
            case KeyEvent.VK_F -> {
                setShotKeyPressed(true);
            }
            case KeyEvent.VK_B -> {
                setBombKeyPressed(true);
            }
        }

        if (gameWindow.getGameState() == gameWindow.getTitleState()) {

            switch (code) {
                case KeyEvent.VK_W -> {
                    setCommandNum(getCommandNum() - 1);
                    if (getCommandNum() < 0) {
                        setCommandNum(1);
                    }
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                }
                case KeyEvent.VK_S -> {
                    setCommandNum(getCommandNum() + 1);
                    if (getCommandNum() > 1) {
                        setCommandNum(0);
                    }
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                }
                case KeyEvent.VK_ENTER -> {
                    if (getCommandNum() == 0) {
                        gameWindow.setGameState(gameWindow.getPlayState());
                        gameWindow.restart();
                        clearAllKeys();
                    } else {
                        System.exit(0);
                    }
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
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
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                }
                case KeyEvent.VK_S -> {
                    setCommandNum(getCommandNum() + 1);
                    if (getCommandNum() > 1) {
                        setCommandNum(0);
                    }
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                    System.out.println("working dir: " + System.getProperty("user.dir"));

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

        if (gameWindow.getGameState() == gameWindow.getTradeState()) {

            gameWindow.getUi().updateTrade(code);
        }
    }

    public void playerInventory(int code) {

        int playerSlotRow = gameWindow.getUi().getPlayerSlotRow();
        int playerSlotCol = gameWindow.getUi().getPlayerSlotCol();
        int maxCol = MAX_COL;
        int maxRow = (gameWindow.getPlayer().getInventory().size() + maxCol - 1) / maxCol;

        switch (code) {
            case KeyEvent.VK_W -> {
                if (playerSlotCol > 0) gameWindow.getUi().setPlayerSlotCol(playerSlotCol - 1);
                gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
            }
            case KeyEvent.VK_S -> {
                if (playerSlotCol < MAX_COL) gameWindow.getUi().setPlayerSlotCol(playerSlotCol + 1);
                gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
            }
            case KeyEvent.VK_A -> {
                if (playerSlotRow > 0) gameWindow.getUi().setPlayerSlotRow(playerSlotRow - 1);
                gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
            }
            case KeyEvent.VK_D -> {
                if (playerSlotRow < MAX_ROW) gameWindow.getUi().setPlayerSlotRow(playerSlotRow + 1);
                gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
            }
        }
    }

    public void npcInventory(int code) {

        int npcSlotRow = gameWindow.getUi().getNpcSlotRow();
        int npcSlotCol = gameWindow.getUi().getNpcSlotCol();
        int maxCol = MAX_COL;
        int maxRow = (gameWindow.getUi().getNpc().getInventory().size() + maxCol - 1) / maxCol;

        switch (code) {
            case KeyEvent.VK_W -> {
                if (npcSlotCol > 0) gameWindow.getUi().setNpcSlotCol(npcSlotCol - 1);
                gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
            }
            case KeyEvent.VK_S -> {
                if (npcSlotCol < MAX_COL) gameWindow.getUi().setNpcSlotCol(npcSlotCol + 1);
                gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
            }
            case KeyEvent.VK_A -> {
                if (npcSlotRow > 0) gameWindow.getUi().setNpcSlotRow(npcSlotRow - 1);
                gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
            }
            case KeyEvent.VK_D -> {
                if (npcSlotRow < MAX_ROW) gameWindow.getUi().setNpcSlotRow(npcSlotRow + 1);
                gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
            }
        }
    }

    private void startConversation(int npcIdx) {

        NpcMerChant mer = (NpcMerChant) gameWindow.getNPC()[npcIdx];

        // インデックスをリセット
        mer.resetDialogue();

        // 会話状態へ移行
        gameWindow.getPlayer().setTalkNpcIndex(npcIdx);
        gameWindow.setGameState(gameWindow.getDialogueState());

        // 最初のセリフを出す
        npcMerChantSpeak();
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getKeyCode();

        if (gameWindow.getGameState() == gameWindow.getPlayState()) {

            switch (code) {
                case KeyEvent.VK_W -> setPlayerUp(false);
                case KeyEvent.VK_S -> setPlayerDown(false);
                case KeyEvent.VK_A -> setPlayerLeft(false);
                case KeyEvent.VK_D -> setPlayerRight(false);
                case KeyEvent.VK_F -> setShotKeyPressed(false);
                case KeyEvent.VK_B -> setBombKeyPressed(false);
            }
        }
    }

    public void clearAllKeys() {
        setPlayerUp(false);
        setPlayerDown(false);
        setPlayerLeft(false);
        setPlayerRight(false);
        setShotKeyPressed(false);
        setBombKeyPressed(false);
    }

    private void togglePause() {
        if (gameWindow.getGameState() == gameWindow.getPlayState()) {
            gameWindow.setGameState(gameWindow.getPauseState());
        } else if (gameWindow.getGameState() == gameWindow.getPauseState()) {
            gameWindow.setGameState(gameWindow.getPlayState());
        }
    }

    private void speakDialogue() {

        if (gameWindow.getGameState() == gameWindow.getPlayState()) {
            setPlayerEnter(true);
            clearAllKeys();
        } else if (gameWindow.getGameState() == gameWindow.getDialogueState()) {

            gameWindow.setGameState(gameWindow.getPlayState());
            gameWindow.startNpcRoute(0, 1, 0);
            gameWindow.getPlayer().setInvincible(false);
        }
    }

    public void npcMerChantSpeak() {

        int idx = gameWindow.getPlayer().getTalkNpcIndex();
        if (idx < 0) return;

        NpcMerChant mer = (NpcMerChant) gameWindow.getNPC()[idx];
        String text = mer.getNextDialogue();

        // セリフを UI にセット
        gameWindow.getUi().addDialogue(text);

        if (text == null) {
            // 会話終了：状態リセット
            gameWindow.setGameState(gameWindow.getPlayState());
            gameWindow.getPlayer().setTalkNpcIndex(idx);

            // ついでにもう一度リセットしておく
            mer.resetDialogue();
        }

        gameWindow.getSoundmanager().cursorWAV(
                "sound/cursor-sound.wav"
        );
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