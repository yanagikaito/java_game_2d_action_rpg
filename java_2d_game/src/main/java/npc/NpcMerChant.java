package npc;

import entity.Entity;
import frame.FrameApp;
import object.*;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class NpcMerChant extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final int SPRITE_COUNT = 3;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private int dialogueIndex = 0;

    public NpcMerChant(GameWindow gameWindow) {
        super(gameWindow);
        setDirection("down");
        setSpeed(1);
        loadNPCImages();
        setDialogue();
        setItems();
    }

    public void loadNPCImages() {

        setSprites(sprites);
        try {
            int tileSize = FrameApp.getTileSize();
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream("npc/merchant-" + DIRECTIONS[dir] + "-" + (i + 1) + ".png"));
                    BufferedImage processed = createImage(original, tileSize, tileSize);
                    sprites[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDialogue() {

        getDialogue()[0] = "いらっしゃい。何買う?";
    }

    public void setItems() {

        ArrayList<Entity> items = new ArrayList<>();
        items.add(getCurrentWeapon());
        items.add(getCurrentShield());
        items.add(new ObjRedPotion(getGameWindow()));
        items.add(new ObjGreenPotion(getGameWindow()));

        setInventory(items);
    }

    @Override
    public void speak() {

        GameWindow gameWindow = getGameWindow();
        int tradeState = gameWindow.getTradeState();
        super.speak();
        gameWindow.setGameState(tradeState);

        gameWindow.getUi().setNpc(this);

    }

    public String getNextDialogue() {
        if (dialogueIndex < getDialogue().length) {
            return getDialogue()[dialogueIndex++];
        } else {
            resetDialogue();
            return null;
        }
    }

    public void resetDialogue() {
        dialogueIndex = 0;
    }

    private @NotNull BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }
}