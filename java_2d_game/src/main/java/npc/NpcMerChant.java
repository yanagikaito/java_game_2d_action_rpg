package npc;

import entity.Entity;
import frame.FrameApp;
import game.GameState;
import object.*;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class NpcMerChant extends Entity {

    private GameWindow gameWindow;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final int SPRITE_COUNT = 3;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private int dialogueIndex = 0;

    public NpcMerChant(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setDirection("down");
        setSpeed(0);
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

        System.out.println("🔧 NpcMerChant: setItems() called");

        ArrayList<Entity> items = new ArrayList<>();

        System.out.println("gameWindow = " + gameWindow);

        items.add(new ObjSwordNormal(gameWindow));
        items.add(new ObjShieldWood(gameWindow));
        items.add(new ObjRedPotion(gameWindow));
        items.add(new ObjGreenPotion(gameWindow));
        items.add(new ObjBluePotion(gameWindow));

        System.out.println("Items created: " + items.size());
        for (Entity item : items) {
            System.out.println(" - " + item + " | type=" + item.getType());
        }

        setInventory(items);
    }

    @Override
    public void speak() {

        GameWindow gameWindow = getGameWindow();
        super.speak();
        gameWindow.setGameState(GameState.TRADE);

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

    @Override
    public ArrayList<Entity> getShopItems() {
        ArrayList<Entity> items = new ArrayList<>();
        items.add(new ObjSwordNormal(gameWindow));
        items.add(new ObjShieldWood(gameWindow));
        items.add(new ObjRedPotion(gameWindow));
        items.add(new ObjGreenPotion(gameWindow));
        items.add(new ObjBluePotion(gameWindow));
        return items;
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