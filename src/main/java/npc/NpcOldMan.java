package npc;

import entity.Entity;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class NpcOldMan extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final int SPRITE_COUNT = 3;
    private static final int ACTION_LOCK_THRESHOLD = 120;
    private static final int MAX_RANDOM_VALUE = 100;
    private static final int THRESHOLD_UP = 25;
    private static final int THRESHOLD_DOWN = 50;
    private static final int THRESHOLD_LEFT = 75;
    private Random random = new Random();
    private int actionLockCounter = 0;

    public NpcOldMan(GameWindow gameWindow) {
        super(gameWindow);
        setDirection("down");
        setSpeed(1);
        loadNPCImages();
        setDialogue();
    }

    public void loadNPCImages() {

        try {

            setSprites(new BufferedImage[DIRECTIONS.length][SPRITE_COUNT]);
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    String path = "npc/oldman-" + DIRECTIONS[dir] + "-" + (i + 1) + ".png";
                    InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                    getSprites()[dir][i] = ImageIO.read(is);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDialogue() {

        getDialogue()[0] = "ここに何しに来た";
        getDialogue()[1] = "お前は誰だ?";
        getDialogue()[2] = "ここで何をしておる";
        getDialogue()[3] = "モンスターでも倒してみろ";
        getDialogue()[4] = "たぶん無理だがな";

    }

    public void setAction() {

        actionLockCounter++;

        if (actionLockCounter >= ACTION_LOCK_THRESHOLD) {
            int i = random.nextInt(MAX_RANDOM_VALUE) + 1;
            if (i <= THRESHOLD_UP) {
                setDirection("up");
            } else if (i <= THRESHOLD_DOWN) {
                setDirection("down");
            } else if (i <= THRESHOLD_LEFT) {
                setDirection("left");
            } else {
                setDirection("right");
            }
            actionLockCounter = 0;
        }
    }

    @Override
    public void speak() {

        String[] dialogues = getDialogue();

        int dialogueIndex = getDialogueIndex();

        if (dialogues[dialogueIndex] == null) {
            dialogueIndex = 0;
        }

        getGameWindow().getUi().setCurrentDialogueMessage(dialogues[dialogueIndex]);
        dialogueIndex++;
        setDialogueIndex(dialogueIndex);

        switch (getGameWindow().getPlayer().getDirection()) {
            case "up" -> setDirection("down");
            case "down" -> setDirection("up");
            case "left" -> setDirection("right");
            case "right" -> setDirection("left");
        }
    }
}