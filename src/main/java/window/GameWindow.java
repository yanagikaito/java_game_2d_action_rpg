package window;

import asset.AssetSetter;
import collision.CollisionChecker;
import entity.Entity;
import frame.FrameApp;
import object.Projectile;
import player.Player;
import factory.FrameFactory;
import frame.GameFrame;
import key.KeyHandler;
import sound.SoundManager;
import tile.TileManager;
import ui.UI;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static frame.FrameApp.baseDisplay;

public class GameWindow extends JPanel implements Window, Runnable {

    private GameFrame gameFrame = FrameFactory.createFrame(baseDisplay(), this);
    private KeyHandler keyHandler = new KeyHandler(this);
    private Player player = new Player(this, keyHandler);
    private TileManager tileManager = new TileManager(this);
    private CollisionChecker collisionChecker = new CollisionChecker(this);
    private AssetSetter assetSetter = new AssetSetter(this);
    private SoundManager soundManager = new SoundManager(this);
    private Entity[] npc = new Entity[10];
    private Entity[] monster = new Entity[20];
    private UI ui = new UI(this);
    private ArrayList<Entity> projectileList = new ArrayList<>();
    private ArrayList<Entity> entityList = new ArrayList<>();
    private static GameWindow instance;
    private Thread gameThread;
    private int gameState;
    private final int playState = 1;
    private final int pauseState = 2;
    private final int dialogueState = 3;
    private final int characterState = 4;
    private final int debugState = 5;

    protected GameWindow() {
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.startThread();
        this.setFocusable(true);
        this.setLayout(null);
        this.addKeyListener(keyHandler);
        this.setUpGame();
    }

    public void setUpGame() {

        assetSetter.setNPC();
        assetSetter.setMonster();
        gameState = playState;
    }

    public static synchronized GameWindow getInstance() {
        if (instance == null) {
            instance = new GameWindow();
        }
        return instance;
    }

    @Override
    public void run() {
        int fps = 60;
        int nanosecond = 1000000000;
        double drawInterval = (double) nanosecond / fps;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }

            if (timer >= nanosecond) {
                System.out.println("FPS :" + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    @Override
    public void frame() {
        gameFrame.createFrame();
    }

    public void startThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void update() {

        if (gameState == playState) {
            player.update();

            for (Entity entity : npc) {
                if (entity != null) {
                    entity.update();
                }
            }
            for (int i = 0; i < monster.length; i++) {
                if (monster[i] != null) {
                    if (monster[i].getAlive() && !monster[i].getDying()) {
                        monster[i].update();
                    }
                    if (!monster[i].getAlive()) {
                        monster[i] = null;
                    }
                }
            }
            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null) {
                    if (projectileList.get(i).getAlive()) {
                        projectileList.get(i).update();
                    }
                    if (!projectileList.get(i).getAlive()) {
                        projectileList.remove(i--);
                    }
                }
            }
            if (gameState == pauseState) {

            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        long drawStart = System.nanoTime();

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        tileManager.draw(g2);

        entityList.add(player);

        for (Entity entity : npc) {
            if (entity != null) {
                entityList.add(entity);
            }
        }
        for (Entity entity : monster) {
            if (entity != null) {
                entityList.add(entity);
            }
        }
        for (Entity p : projectileList) {
            if (p != null && p.getAlive()) {
                entityList.add(p);
            }
        }

        entityList.sort(
                Comparator.comparingInt(Entity::getWorldY)
                        .thenComparingInt(Entity::getWorldX)
        );

        for (Entity entity : entityList) {
            entity.draw(g2);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        entityList.clear();

        ui.draw(g2);

        if (keyHandler.isShowDebugText()) {

            int tileSize = FrameApp.getTileSize();
            long drawEnd = System.nanoTime();
            long passed = drawEnd - drawStart;

            g2.setFont(new Font("アリアル", Font.PLAIN, 20));
            g2.setColor(Color.WHITE);

            int debugX = 10;
            int debugY = 400;
            int lineHeight = 20;
            double nanosecond = 1000000000.0;

            g2.drawString("描画時間: " + passed / nanosecond + "秒", debugX, debugY);
            debugY += lineHeight;

            g2.drawString("WorldX: " + player.getWorldX(), debugX, debugY);
            debugY += lineHeight;
            g2.drawString("WorldY: " + player.getWorldY(), debugX, debugY);
            debugY += lineHeight;

            int row = player.getWorldX() / tileSize;
            int col = player.getWorldY() / tileSize;

            g2.drawString("Row  : " + row, debugX, debugY);
            debugY += lineHeight;
            g2.drawString("Col    : " + col, debugX, debugY);
        }

        g2.dispose();
    }

    public Player getPlayer() {
        return player;
    }

    public KeyHandler getKeyHandler() {
        return keyHandler;
    }

    public TileManager getTileManager() {
        return tileManager;
    }

    public CollisionChecker getCollisionChecker() {
        return collisionChecker;
    }

    public int getGameState() {
        return gameState;
    }

    public int getPauseState() {
        return pauseState;
    }

    public int getPlayState() {
        return playState;
    }

    public void setGameState(int gameState) {
        this.gameState = gameState;
    }

    public Entity[] getNPC() {
        return npc;
    }

    public Entity[] getMonster() {
        return monster;
    }

    public void setNPC(Entity[] npc) {
        this.npc = npc;
    }

    public void setMonster(Entity[] monster) {
        this.monster = monster;
    }

    public int getDialogueState() {
        return dialogueState;
    }

    public UI getUi() {
        return ui;
    }

    public SoundManager getSoundmanager() {
        return soundManager;
    }

    public int getCharacterState() {
        return characterState;
    }

    public void setCharacterState(int characterState) {
        this.gameState = characterState;
    }

    public int getDebugState() {
        return debugState;
    }

    public void setDebugState(int debugState) {
        this.gameState = debugState;
    }

    public AssetSetter getAssetSetter() {
        return assetSetter;
    }

    public void setAssetSetter(AssetSetter assetSetter) {
        this.assetSetter = assetSetter;
    }

    public ArrayList<Entity> getProjectileList() {
        return projectileList;
    }

    public void setProjectileList(ArrayList<Entity> projectileList) {
        this.projectileList = projectileList;
    }
}