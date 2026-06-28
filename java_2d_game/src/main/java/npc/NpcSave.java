package npc;

import entity.Entity;
import frame.FrameApp;
import game.GameState;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class NpcSave extends Entity {

    private GameWindow gameWindow;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final int SPRITE_COUNT = 3;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private int dialogueIndex = 0;
    private String eventId;

    public NpcSave(GameWindow gameWindow, db.MapEvent ev) {
        super(gameWindow);
        if (ev != null) {
            this.eventId = String.valueOf(ev.getId());
            applyMapEvent(ev);
        }
        this.gameWindow = gameWindow;
        setDirection("down");
        setSpeed(0);
        loadNPCImages();
    }

    public void loadNPCImages() {

        setSprites(sprites);
        try {
            int tileSize = FrameApp.getTileSize();
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream("npc/savenpc-" + DIRECTIONS[dir] + "-" + (i + 1) + ".png"));
                    BufferedImage processed = createImage(original, tileSize, tileSize);
                    sprites[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void applyMapEvent(db.MapEvent ev) {
        if (ev == null) return;

        // eventId 等の基本情報
        if (ev.getId() != null) this.eventId = ev.getId();
        this.setName(ev.getName());
        this.setTrigger(ev.getTrigger());

        // dialogues を配列にコピー
        java.util.List<String> dlg = ev.getDialogues();
        if (dlg == null || dlg.isEmpty()) {
            // 空なら空配列をセットしておく（null 回避）
            setDialogueArray(new String[0]);
        } else {
            String[] arr = new String[dlg.size()];
            for (int i = 0; i < dlg.size(); i++) {
                arr[i] = dlg.get(i);
            }
            setDialogueArray(arr);
        }
    }

    @Override
    public void speak() {

        GameWindow gameWindow = getGameWindow();
        super.speak();
        gameWindow.setGameState(GameState.SAVE);
        System.out.println(gameWindow.getGameState());
        gameWindow.getUi().setNpc(this);

    }

    public String getNextDialogue() {
        if (dialogueIndex < getDialogue().length) {
            return getDialogue()[dialogueIndex];
        } else {
            resetDialogue();
            return null;
        }
    }

    public void resetDialogue() {
        dialogueIndex = 0;
    }

    private BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}