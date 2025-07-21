package npc;

import db.PathManager;
import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class NpcOldMan extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private static final int SPRITE_COUNT = 3;
    private static final int ACTION_LOCK_THRESHOLD = 120;
    private static final int MAX_RANDOM_VALUE = 100;
    private static final int THRESHOLD_UP = 25;
    private static final int THRESHOLD_DOWN = 50;
    private static final int THRESHOLD_LEFT = 75;
    private Random random = new Random();
    private int actionLockCounter = 0;
    private List<Point> route = List.of();
    private int routeIndex = 0;
    private boolean following = false;

    public NpcOldMan(GameWindow gameWindow) {
        super(gameWindow);
        setDirection("down");
        setSpeed(1);
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
                                    .getResourceAsStream("npc/oldman-" + DIRECTIONS[dir] + "-" + (i + 1) + ".png"));
                    BufferedImage processed = createImage(original, tileSize, tileSize);
                    sprites[dir][i] = processed;
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

    @Override
    public void setAction() {
        if (following) {
            followRouteStep();
        } else {
            randomWalkStep();
        }
    }

    private void randomWalkStep() {
        actionLockCounter++;
        if (actionLockCounter < ACTION_LOCK_THRESHOLD) return;

        int i = random.nextInt(MAX_RANDOM_VALUE) + 1;
        if (i <= THRESHOLD_UP) setDirection("up");
        else if (i <= THRESHOLD_DOWN) setDirection("down");
        else if (i <= THRESHOLD_LEFT) setDirection("left");
        else setDirection("right");

        actionLockCounter = 0;
    }

    private void followRouteStep() {
        if (routeIndex >= route.size()) {
            following = false;
            return;
        }

        Point tile = route.get(routeIndex);
        int ts = FrameApp.getTileSize();
        int targetX = tile.x * ts;
        int targetY = tile.y * ts;

        int wx = getWorldX();
        int wy = getWorldY();
        int dx = targetX - wx;
        int dy = targetY - wy;

        if (dx == 0 && dy == 0) {
            routeIndex++;
            return;
        }

        int sp = getSpeed();

        if (dx != 0) {
            int step = Math.min(Math.abs(dx), sp);
            int newX = wx + (dx > 0 ? step : -step);
            setWorldX(newX);
            setDirection(dx > 0 ? "right" : "left");
        } else {
            int step = Math.min(Math.abs(dy), sp);
            int newY = wy + (dy > 0 ? step : -step);
            setWorldY(newY);
            setDirection(dy > 0 ? "down" : "up");
        }
    }

    public void startRouteFollow(int mapId, int pathId) {

        List<Point> raw = PathManager.loadPath(mapId, pathId);
        System.out.println("Loaded raw path: " + raw);
        if (raw.isEmpty()) {
            following = false;
            return;
        }

        Point startTile = raw.get(0);
        int ts = FrameApp.getTileSize();
        setWorldX(startTile.x * ts);
        setWorldY(startTile.y * ts);

        raw.remove(0);

        this.route = raw;
        this.routeIndex = 0;
        this.following = true;
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

    private BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }
}