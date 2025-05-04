package window;

import asset.AssetSetter;
import collision.CollisionChecker;
import entity.Entity;
import player.Player;
import factory.FrameFactory;
import frame.GameFrame;
import key.KeyHandler;
import tile.TileManager;
import ui.UI;

import javax.swing.*;

import java.awt.*;

import static frame.FrameApp.baseDisplay;

public class GameWindow extends JPanel implements Window, Runnable {

    private GameFrame gameFrame = FrameFactory.createFrame(baseDisplay(), this);
    private KeyHandler keyHandler = new KeyHandler(this);
    private Player player = new Player(this, keyHandler);
    private TileManager tileManager = new TileManager(this);
    private CollisionChecker collisionChecker = new CollisionChecker(this);
    private AssetSetter assetSetter = new AssetSetter(this);
    private Entity[] npc = new Entity[10];
    private UI ui = new UI(this);
    private static GameWindow instance;
    private Thread gameThread;
    private int gameState;
    private final int playState = 1;
    private final int pauseState = 2;

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
            if (gameState == pauseState) {

            }
        }
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        tileManager.draw(g2);

        for (Entity entity : npc) {
            if (entity != null) {
                entity.draw(g2);
            }
        }
        player.draw(g2);

        ui.draw(g2);

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

    public void setNPC(Entity[] npc) {
        this.npc = npc;
    }
}