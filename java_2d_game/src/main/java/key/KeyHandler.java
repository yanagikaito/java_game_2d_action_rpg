package key;

import entity.Entity;
import game.GameState;
import npc.NpcMalonyChicken;
import npc.NpcMerChant;
import npc.NpcSave;
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
    private boolean playerSpace;
    private boolean showDebugText;
    private boolean shotKeyPressed;
    private boolean bombKeyPressed;
    private boolean throwKeyPressed;
    private int commandNum;
    private static final int MAX_COL = 3;
    private static final int MAX_ROW = 4;
    private boolean enterJustPressed = false;

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

    public boolean isPlayerSpace() {
        return playerSpace;
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

    public boolean isPlayerEnterJustPressed() {
        return enterJustPressed;
    }

    public boolean isThrowKeyPressed() {
        return throwKeyPressed;
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

    public void setPlayerSpace(boolean playerSpace) {
        this.playerSpace = playerSpace;
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

    public void setThrowKeyPressed(boolean throwKeyPressed) {
        this.throwKeyPressed = throwKeyPressed;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();

        if (gameWindow.getGameState() == GameState.CHARACTER) {

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
                    gameWindow.setGameState(GameState.PLAY);
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

        if (gameWindow.getUi().isAwaitingChoice()) {
            // 左右で選択移動
            if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                gameWindow.getUi().moveChoiceLeft();
                gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                return;
            }
            if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                gameWindow.getUi().moveChoiceRight();
                gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                return;
            }

            // 決定（Enter または専用キー）
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_Z) {
                Entity npc = gameWindow.getUi().getCurrentChoiceNpc();
                int choice = gameWindow.getUi().getSelectedOption();
                if (npc != null) {
                    try {
                        npc.getClass().getMethod("onPlayerChoice", int.class).invoke(npc, choice);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                gameWindow.getUi().clearChoice();
                gameWindow.getSoundmanager().cursorWAV("sound/confirm.wav");
                return;
            }

            // キャンセル（Esc）
            if (code == KeyEvent.VK_ESCAPE) {
                Entity npc = gameWindow.getUi().getCurrentChoiceNpc();
                if (npc instanceof NpcMalonyChicken malony) {
                    malony.onPlayerChoice(1); // 断る
                }
                gameWindow.getUi().clearChoice();
                gameWindow.getSoundmanager().cursorWAV("sound/cancel.wav");
                return;
            }

            // 選択肢表示中は他のキーを無視
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
            case KeyEvent.VK_C -> {
                gameWindow.setGameState(GameState.CHARACTER);
                clearAllKeys();
            }
            case KeyEvent.VK_ENTER -> {
                int npcIdx = gameWindow.getPlayer().checkNpcInFront(gameWindow.getNPC(), 2);
                if (npcIdx != -1 && gameWindow.getNPC()[npcIdx] instanceof NpcMerChant) {
                    startMerConversation(npcIdx);
                    gameWindow.setGameState(GameState.TRADE);
                } else if (npcIdx != -1 && gameWindow.getNPC()[npcIdx] instanceof NpcSave) {
                    gameWindow.setGameState(GameState.SAVE);
                } else if (gameWindow.getGameState() == GameState.TRADE) {
                    npcMerChantSpeak();
                    gameWindow.getUi().setSubState(0);
                } else if (gameWindow.getGameState() == GameState.SAVE) {
                    npcSaveSpeak();
                } else {
                    speakDialogue();
                    clearAllKeys();
                }
            }
            case KeyEvent.VK_SPACE -> {
                setPlayerSpace(true);

                if (gameWindow.getPlayer().hasLeftShield()) {
                    gameWindow.getPlayer().startBlockingLeft();
                }
            }
            case KeyEvent.VK_F -> {
                setShotKeyPressed(true);
            }
            case KeyEvent.VK_B -> {
                setBombKeyPressed(true);
            }
            case KeyEvent.VK_G -> {
                setThrowKeyPressed(true);
            }
        }

        if (gameWindow.getGameState() == GameState.TITLE) {

            switch (code) {
                case KeyEvent.VK_W -> {
                    setCommandNum(getCommandNum() - 1);
                    if (getCommandNum() < 0) {
                        setCommandNum(2);
                    }
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                }
                case KeyEvent.VK_S -> {
                    setCommandNum(getCommandNum() + 1);
                    if (getCommandNum() > 2) {
                        setCommandNum(0);
                    }
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                }
                case KeyEvent.VK_ENTER -> {
                    int selected = getCommandNum();
                    if (selected == 0) {
                        gameWindow.setGameState(GameState.LOAD);
                        gameWindow.getUi().initLoadScreen();
                        clearAllKeys();
                    } else if (selected == 1) {
                        gameWindow.setGameState(GameState.PLAY);
                        gameWindow.restart();
                    } else if (selected == 2) {
                        System.exit(0);
                    }
                    gameWindow.getSoundmanager().cursorWAV("sound/cursor-sound.wav");
                }
            }
        }

        if (gameWindow.getGameState() == GameState.LOAD) {

            gameWindow.getUi().updateLoad(code);

        }

        if (gameWindow.getGameState() == GameState.GAME_OVER) {

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
                        gameWindow.setGameState(GameState.PLAY);
                        gameWindow.retry();
                    }
                    if (getCommandNum() == 1) {
                        gameWindow.setGameState(GameState.TITLE);
                        gameWindow.getUi().returnToTitleFromGameOver();
                    }
                }
            }
        }

        if (gameWindow.getGameState() == GameState.TRADE) {

            gameWindow.getUi().updateTrade(code);
        }

        if (gameWindow.getGameState() == GameState.SAVE) {

            gameWindow.getUi().updateSave(code);
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

    private void startMerConversation(int npcIdx) {

        NpcMerChant mer = (NpcMerChant) gameWindow.getNPC()[npcIdx];

        // インデックスをリセット
        mer.resetDialogue();

        // 会話状態へ移行
        gameWindow.getPlayer().setTalkNpcIndex(npcIdx);
        gameWindow.setGameState(GameState.DIALOGUE);

        // 最初のセリフを出す
        npcMerChantSpeak();
    }

    private void startSaveConversation(int npcIdx) {

        NpcSave save = (NpcSave) gameWindow.getNPC()[npcIdx];

        // インデックスをリセット
        save.resetDialogue();

        // 会話状態へ移行
        gameWindow.getPlayer().setTalkNpcIndex(npcIdx);
        gameWindow.setGameState(GameState.DIALOGUE);

        // 最初のセリフを出す
        npcSaveSpeak();
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getKeyCode();

        if (gameWindow.getGameState() == GameState.PLAY) {

            switch (code) {
                case KeyEvent.VK_W -> setPlayerUp(false);
                case KeyEvent.VK_S -> setPlayerDown(false);
                case KeyEvent.VK_A -> setPlayerLeft(false);
                case KeyEvent.VK_D -> setPlayerRight(false);
                case KeyEvent.VK_F -> setShotKeyPressed(false);
                case KeyEvent.VK_B -> setBombKeyPressed(false);
                case KeyEvent.VK_G -> setThrowKeyPressed(false);
                case KeyEvent.VK_SPACE -> {
                    gameWindow.getPlayer().stopBlockingLeft();
                }
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
        setThrowKeyPressed(false);
    }

    private void togglePause() {
        if (gameWindow.getGameState() == GameState.PLAY) {
            gameWindow.setGameState(GameState.PAUSE);
        } else if (gameWindow.getGameState() == GameState.PAUSE) {
            gameWindow.setGameState(GameState.PLAY);
        }
    }

    private void speakDialogue() {

        if (gameWindow.getGameState() == GameState.PLAY) {
            setPlayerEnter(true);
            clearAllKeys();
        } else if (gameWindow.getGameState() == GameState.DIALOGUE) {

            gameWindow.setGameState(GameState.PLAY);
            gameWindow.startNpcRoute(0, 1, 0);
            gameWindow.getPlayer().setInvincible(false);
        }
    }

    public void npcSaveSpeak() {

        int idx = gameWindow.getPlayer().getTalkNpcIndex();
        if (idx < 0) return;

        NpcSave npcSave = (NpcSave) gameWindow.getNPC()[idx];
        String text = npcSave.getNextDialogue();

        // セリフを UI にセット
        gameWindow.getUi().addDialogue(text);

        if (text == null) {
            // 会話終了：状態リセット
            gameWindow.setGameState(GameState.PLAY);
            gameWindow.getPlayer().setTalkNpcIndex(idx);

            // ついでにもう一度リセットしておく
            npcSave.resetDialogue();
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
            gameWindow.setGameState(GameState.PLAY);
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

    public void consumeThrowOnce() {
        this.throwKeyPressed = false;
    }

    public int getCommandNum() {
        return commandNum;
    }

    public void setCommandNum(int commandNum) {
        this.commandNum = commandNum;
    }

    public void consumeEnterOnce() {
        this.playerEnter = false;
    }
}