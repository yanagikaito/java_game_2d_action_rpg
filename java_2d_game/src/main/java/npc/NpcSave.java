package npc;

import entity.Entity;
import frame.FrameApp;
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

    public NpcSave(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setDirection("down");
        setSpeed(0);
        loadNPCImages();
        setDialogue();
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

    public void setDialogue() {

        getDialogue()[0] = "セーブしますか？\"";
    }

    @Override
    public void speak() {

        GameWindow gameWindow = getGameWindow();
        int saveState = gameWindow.getSaveState();
        gameWindow.setGameState(saveState);
        System.out.println(gameWindow.getGameState());
        super.speak();
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
}