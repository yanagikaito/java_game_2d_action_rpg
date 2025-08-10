package window;

import asset.AssetSetter;
import collision.CollisionChecker;
import entity.Entity;
import frame.FrameApp;
import npc.NpcOldMan;
import object.ObjCoinBronze;
import object.ObjGreenPotion;
import object.ObjRedPotion;
import object.Projectile;
import org.jetbrains.annotations.NotNull;
import player.Player;
import factory.FrameFactory;
import frame.GameFrame;
import key.KeyHandler;
import sound.SoundManager;
import tile.TileManager;
import tileInteractive.InteractiveTile;
import ui.UI;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
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
    private Entity[] obj = new Entity[10];
    private InteractiveTile[] iTile = new InteractiveTile[50];
    private UI ui = new UI(this);
    private ArrayList<Projectile> projectileList = new ArrayList<>();
    private ArrayList<Entity> particleList = new ArrayList<>();
    public ArrayList<Entity> itemList = new ArrayList<>();
    private static GameWindow instance;
    private Thread gameThread;
    private int gameState;
    private final int titleState = 0;
    private final int playState = 1;
    private final int pauseState = 2;
    private final int dialogueState = 3;
    private final int characterState = 4;
    private final int debugState = 5;
    private final int gameOverState = 6;
    private final int tradeState = 7;
    private boolean onTransition = false;
    private boolean fadingOut = true;
    private float alpha = 0f;
    private int frameCount = 0;
    private final int TRANSITION_DURATION = 30;
    private int pendingMapId;
    private int currentMap = 1;
    private boolean showHitBoxes = false;
    private boolean dialogueActive = false;

    protected GameWindow() {
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.startThread();
        this.setLayout(null);
        this.addKeyListener(keyHandler);
        this.setUpGame();
    }

    public void setUpGame() {

        assetSetter.setNPC();
        assetSetter.setMonster();
        assetSetter.setInteractiveTile();
        assetSetter.setObjAxe();
        gameState = titleState;
    }

    public static synchronized GameWindow getInstance() {
        if (instance == null) {
            instance = new GameWindow();
        }
        return instance;
    }

    public void retry() {

        getKeyHandler().clearAllKeys();
        getPlayer().setDefaultPositions();
        getPlayer().restoreLifeAndMan();
        getPlayer().setItems();
        getPlayer().setCoin(500);
        getPlayer().setMoving(false);
        assetSetter.setNPC();
        assetSetter.setMonster();
    }

    public void restart() {

        getPlayer().setDefaultValues();
        getPlayer().setDefaultPositions();
        getPlayer().restoreLifeAndMan();
        getPlayer().setItems();
        getPlayer().setCoin(500);
        assetSetter.setNPC();
        assetSetter.setMonster();
        assetSetter.setInteractiveTile();
        assetSetter.setObjAxe();
    }

    public void startMapTransition(int newMapId) {
        this.pendingMapId = newMapId;
        this.onTransition = true;
        this.fadingOut = true;
        this.frameCount = 0;
        this.alpha = 0f;

        keyHandler.clearAllKeys();
        getPlayer().setMoving(false);
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

    private void updateTransition() {

        frameCount++;

        if (fadingOut) {
            alpha = frameCount / (float) TRANSITION_DURATION;

            if (frameCount >= TRANSITION_DURATION) {
                changeMap(pendingMapId);
                fadingOut = false;
                frameCount = 0;
            }
        } else {
            alpha = 1f - frameCount / (float) TRANSITION_DURATION;

            if (frameCount >= TRANSITION_DURATION) {
                alpha = 0f;
                onTransition = false;
                frameCount = 0;
            }
        }
    }

    public void update() {

        if (gameState == playState) {

            if (onTransition) {
                updateTransition();
                return;
            }

            player.update();

            for (Entity entity : npc) {
                if (entity != null) {
                    if (entity.getMapId() != currentMap) continue;
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
            for (int i = 0; i < itemList.size(); i++) {
                Entity item = itemList.get(i);
                if (item == null) continue;

                if (item.getAlive() && collisionChecker.checkPlayer(item)) {
                    if (item instanceof ObjCoinBronze) {
                        player.addCoin(((ObjCoinBronze) item));
                    } else if (item instanceof ObjRedPotion) {
                        player.healRedPotion((ObjRedPotion) item);
                    } else if (item instanceof ObjGreenPotion) {
                        player.healGreenPotion((ObjGreenPotion) item);
                    }
                    item.setAlive(false);
                }

                if (!item.getAlive()) {
                    itemList.remove(i--);
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
            for (int i = 0; i < particleList.size(); i++) {
                if (particleList.get(i) != null) {
                    if (particleList.get(i).getAlive()) {
                        particleList.get(i).update();
                    }
                    if (!particleList.get(i).getAlive()) {
                        particleList.remove(i--);
                    }
                }
            }
            for (int i = 0; i < iTile.length; i++) {
                if (iTile[i] != null) {
                    iTile[i].update();
                }
            }
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    obj[i].update();
                }
            }
        }
        if (gameState == pauseState) {

        }
    }

    public void changeMap(int newMap) {

        int tileSize = FrameApp.getTileSize();
        currentMap = newMap;

        if (currentMap == TileManager.HUT_TILE_ID) {

            Arrays.fill(npc, null);
            Arrays.fill(monster, null);
            Arrays.fill(obj, null);
            Arrays.fill(iTile, null);
            itemList.clear();
            projectileList.clear();
            particleList.clear();

            getKeyHandler().clearAllKeys();
            tileManager.loadMap(2);
            startMapTransition(2);
            getPlayer().setWorldX(tileSize * 29);
            getPlayer().setWorldY(tileSize * 16);
            assetSetter.setMerchant();

            repaint();

        } else if (currentMap == TileManager.MEADOW_TILE_ID) {

            getKeyHandler().clearAllKeys();
            tileManager.loadMap(1);
            startMapTransition(2);
            getPlayer().setWorldX(tileSize * 23);
            getPlayer().setWorldY(tileSize * 16);

            assetSetter.setNPC();
            assetSetter.setMonster();
            assetSetter.setInteractiveTile();
            assetSetter.setObjAxe();

            repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        long drawStart = System.nanoTime();

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == titleState) {

            getUi().draw(g2);

        } else {

            tileManager.draw(g2);

            for (int i = 0; i < iTile.length; i++) {
                if (iTile[i] != null) {
                    iTile[i].draw(g2);
                }
            }

            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    obj[i].draw(g2);
                }
            }

            for (Entity item : itemList) {
                if (item != null && item.getAlive()) {
                    item.draw(g2);
                }
            }

            for (Entity paList : particleList) {
                if (paList != null) {
                    paList.draw(g2);
                }
            }

            List<Entity> entityList = new ArrayList<>();

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
            for (Entity proList : projectileList) {
                if (proList != null && proList.getAlive()) {
                    entityList.add(proList);
                }
            }

            entityList.sort(Comparator.comparingInt(Entity::getWorldY));

            for (Entity entity : entityList) {
                entity.draw(g2);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            entityList.clear();

            ui.draw(g2);

            if (onTransition) {
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, Math.min(1f, Math.max(0f, alpha))
                ));
                g2.setColor(Color.black);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(old);
            }

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

    public ArrayList<Projectile> getProjectileList() {
        return projectileList;
    }

    public void setProjectileList(ArrayList<Projectile> projectileList) {
        this.projectileList = projectileList;
    }

    public ArrayList<Entity> getItemList() {
        return itemList;
    }

    public ArrayList<Entity> setItemList(ArrayList<Entity> itemList) {
        return this.itemList = itemList;
    }

    public ArrayList<Entity> getParticleList() {
        return particleList;
    }

    public void setParticleList(ArrayList<Entity> particleList) {
        this.particleList = particleList;
    }

    public InteractiveTile[] getItile() {
        return iTile;
    }

    public void setItile(InteractiveTile[] iTile) {
        this.iTile = iTile;
    }

    public Entity[] getObj() {
        return obj;
    }

    public void setObj(Entity[] obj) {
        this.obj = obj;
    }

    public boolean isDialogueActive() {
        return dialogueActive;
    }

    public int getTitleState() {
        return titleState;
    }

    public void setDialogueActive(boolean active) {
        this.dialogueActive = active;
    }

    public int getCurrentMap() {
        return currentMap;
    }

    public void dropItem(@NotNull Entity droppedItem, @NotNull Entity source) {
        System.out.println("【dropItem】呼ばれた／itemList before=" + itemList.size());
        droppedItem.setWorldX(source.getWorldX());
        droppedItem.setWorldY(source.getWorldY());
        droppedItem.setAlive(true);
        itemList.add(droppedItem);
        System.out.println("【dropItem】 今の itemList サイズ=" + itemList.size());
    }

    public void startNpcRoute(int npcIndex, int mapId, int pathId) {
        if (npcIndex < 0 || npcIndex >= npc.length) return;
        Entity e = npc[npcIndex];
        if (e instanceof NpcOldMan oldMan) {
            oldMan.startRouteFollow(mapId, pathId);
        }
    }

    public void toggleHitBoxDebug() {
        showHitBoxes = !showHitBoxes;
    }

    public int getGameOverState() {
        return gameOverState;
    }

    public boolean isOnTransition() {
        return onTransition;
    }

    public int getTradeState() {
        return tradeState;
    }
}