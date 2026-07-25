package asset;

import entity.Entity;
import factory.EntityFactory;
import frame.FrameApp;
import monster.MonGreenGoblin;
import monster.MonGreenSlime;
import monster.MonMintSoldier;
import npc.*;
import object.*;
import tileInteractive.InteractiveTile;
import tileInteractive.ItDryTree;
import window.GameWindow;

import java.awt.*;

public class AssetSetter {

    private GameWindow gameWindow;

    public AssetSetter(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void setNpcOldMan(String eventId, int tileX, int tileY) {
        db.MapModel model = gameWindow.getModel();
        if (model == null) {
            System.err.println("Model is null in setNpcMalonyChicken");
            return;
        }

        model.loadEventsFromDb(1);

        db.MapEvent ev = null;

        if (eventId != null) {
            ev = model.getEventById(eventId);
        }

        if (ev == null) {
            ev = model.findEventAt(tileX, tileY);
        }

        NpcOldMan npc = new NpcOldMan(gameWindow, ev);
        npc.applyMapEvent(ev);

        npc.setWorldX(FrameApp.getTileSize() * ev.getX());
        npc.setWorldY(FrameApp.getTileSize() * ev.getY());

        Entity[] npcArray = gameWindow.getNPC();
        if (npcArray == null || npcArray.length <= 1) {
            npcArray = new Entity[Math.max(10, (npcArray == null ? 0 : npcArray.length))];
        }
        npcArray[0] = npc;
        gameWindow.setNPC(npcArray);

        javax.swing.SwingUtilities.invokeLater(() -> gameWindow.repaint());
    }

    public void setNpcMalonyChicken(String eventId, int tileX, int tileY) {
        db.MapModel model = gameWindow.getModel();
        if (model == null) {
            System.err.println("Model is null in setNpcMalonyChicken");
            return;
        }

        model.loadEventsFromDb(1);

        db.MapEvent ev = null;

        if (eventId != null) {
            ev = model.getEventById(eventId);
        }

        if (ev == null) {
            ev = model.findEventAt(tileX, tileY);
        }

        NpcMalonyChicken npc = new NpcMalonyChicken(gameWindow, ev);
        npc.applyMapEvent(ev);

        npc.setWorldX(FrameApp.getTileSize() * ev.getX());
        npc.setWorldY(FrameApp.getTileSize() * ev.getY());

        Entity[] npcArray = gameWindow.getNPC();
        if (npcArray == null || npcArray.length <= 1) {
            npcArray = new Entity[Math.max(10, (npcArray == null ? 0 : npcArray.length))];
        }
        npcArray[1] = npc;
        gameWindow.setNPC(npcArray);

        javax.swing.SwingUtilities.invokeLater(() -> gameWindow.repaint());
    }


    public void setNpcMerChant(String eventId, int tileX, int tileY) {
        db.MapModel model = gameWindow.getModel();
        if (model == null) {
            System.err.println("Model is null in setNpcMalonyChicken");
            return;
        }

        model.loadEventsFromDb(2);

        db.MapEvent ev = null;

        if (eventId != null) {
            ev = model.getEventById(eventId);
        }

        if (ev == null) {
            ev = model.findEventAt(tileX, tileY);
        }

        NpcMerChant npc = new NpcMerChant(gameWindow, ev);
        npc.applyMapEvent(ev);

        npc.setWorldX(FrameApp.getTileSize() * ev.getX());
        npc.setWorldY(FrameApp.getTileSize() * ev.getY());

        Entity[] npcArray = gameWindow.getNPC();
        if (npcArray == null || npcArray.length <= 1) {
            npcArray = new Entity[Math.max(10, (npcArray == null ? 0 : npcArray.length))];
        }
        npcArray[0] = npc;
        gameWindow.setNPC(npcArray);

        javax.swing.SwingUtilities.invokeLater(() -> gameWindow.repaint());
    }

    public void setNpcSave(String eventId, int tileX, int tileY) {
        db.MapModel model = gameWindow.getModel();
        if (model == null) {
            System.err.println("Model is null in setNpcMalonyChicken");
            return;
        }

        model.loadEventsFromDb(2);

        db.MapEvent ev = null;

        if (eventId != null) {
            ev = model.getEventById(eventId);
        }

        if (ev == null) {
            ev = model.findEventAt(tileX, tileY);
        }

        NpcSave npc = new NpcSave(gameWindow, ev);
        npc.applyMapEvent(ev);

        npc.setWorldX(FrameApp.getTileSize() * ev.getX());
        npc.setWorldY(FrameApp.getTileSize() * ev.getY());

        Entity[] npcArray = gameWindow.getNPC();
        if (npcArray == null || npcArray.length <= 1) {
            npcArray = new Entity[Math.max(10, (npcArray == null ? 0 : npcArray.length))];
        }
        npcArray[1] = npc;
        gameWindow.setNPC(npcArray);

        javax.swing.SwingUtilities.invokeLater(() -> gameWindow.repaint());
    }

    public void setMonster() {

        Entity[] monsters = gameWindow.getMonster();
        int i = 0;

        monsters[i] = createMonMintSoldier(FrameApp.getTileSize() * 25, FrameApp.getTileSize() * 3);
        i++;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 26);
        i++;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 38);
        i++;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 39);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 30);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 33);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 35);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 37);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 39);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 35, FrameApp.getTileSize() * 30);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 35, FrameApp.getTileSize() * 32);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 35, FrameApp.getTileSize() * 34);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 36, FrameApp.getTileSize() * 23);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 36, FrameApp.getTileSize() * 21);

        gameWindow.setMonster(monsters);
    }

    public void setBossMonster() {

        Entity[] monsters = gameWindow.getMonster();
        int i = 0;

        monsters[i] = createMonGreenGoblin(FrameApp.getTileSize() * 27, FrameApp.getTileSize() * 15);

        gameWindow.setMonster(monsters);
    }

    public void setObjPot() {

        Entity[] obj = gameWindow.getObj();
        int i = 0;

        obj[i] = createObjPot(FrameApp.getTileSize() * 27, FrameApp.getTileSize() * 15);
        i++;
        obj[i] = createObjPot(FrameApp.getTileSize() * 28, FrameApp.getTileSize() * 15);
        i++;
        obj[i] = createObjPot(FrameApp.getTileSize() * 29, FrameApp.getTileSize() * 15);
        i++;
        obj[i] = createObjPot(FrameApp.getTileSize() * 29, FrameApp.getTileSize() * 16);
        i++;
        obj[i] = createObjPot(FrameApp.getTileSize() * 27, FrameApp.getTileSize() * 16);
        i++;
        obj[i] = createObjPot(FrameApp.getTileSize() * 28, FrameApp.getTileSize() * 16);
    }

    public void setObjRock() {

        Entity[] obj = gameWindow.getObj();
        int i = 6;

        obj[i] = createObjRock(FrameApp.getTileSize() * 10, FrameApp.getTileSize() * 11);
    }

    public void setObjAxe() {

        Entity[] obj = gameWindow.getObj();
        int i = 7;

        obj[i] = createObjAxe(FrameApp.getTileSize() * 33, FrameApp.getTileSize() * 21);
    }

    public void setObjChest() {

        Entity[] obj = gameWindow.getObj();
        int i = 8;

        obj[i] = createObjChest(FrameApp.getTileSize() * 30, FrameApp.getTileSize() * 22);
        i++;
        obj[i] = createObjChest(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 21);
        i++;
        obj[i] = createObjChest(FrameApp.getTileSize() * 32, FrameApp.getTileSize() * 22);
    }

    public void setObjRedPotion() {

        Entity[] obj = gameWindow.getObj();
        int i = 11;

        obj[i] = createObjRedPotion(FrameApp.getTileSize() * 33, FrameApp.getTileSize() * 23);
        i++;
        obj[i] = createObjRedPotion(FrameApp.getTileSize() * 34, FrameApp.getTileSize() * 24);
        i++;
        obj[i] = createObjRedPotion(FrameApp.getTileSize() * 35, FrameApp.getTileSize() * 25);
    }

    public void setObjGreenPotion() {

        Entity[] obj = gameWindow.getObj();
        int i = 14;

        obj[i] = createObjGreenPotion(FrameApp.getTileSize() * 36, FrameApp.getTileSize() * 26);
        i++;
        obj[i] = createObjGreenPotion(FrameApp.getTileSize() * 37, FrameApp.getTileSize() * 27);
        i++;
        obj[i] = createObjGreenPotion(FrameApp.getTileSize() * 38, FrameApp.getTileSize() * 28);
    }

    public void setObjBluePotion() {

        Entity[] obj = gameWindow.getObj();
        int i = 17;

        obj[i] = createObjBluePotion(FrameApp.getTileSize() * 39, FrameApp.getTileSize() * 29);
        i++;
        obj[i] = createObjBluePotion(FrameApp.getTileSize() * 40, FrameApp.getTileSize() * 30);
        i++;
        obj[i] = createObjBluePotion(FrameApp.getTileSize() * 41, FrameApp.getTileSize() * 31);
    }

    private ObjRedPotion createObjRedPotion(int worldX, int worldY) {
        ObjRedPotion red = new ObjRedPotion(gameWindow);
        red.setWorldX(worldX);
        red.setWorldY(worldY);
        return red;
    }

    private ObjGreenPotion createObjGreenPotion(int worldX, int worldY) {
        ObjGreenPotion green = new ObjGreenPotion(gameWindow);
        green.setWorldX(worldX);
        green.setWorldY(worldY);
        return green;
    }

    private ObjBluePotion createObjBluePotion(int worldX, int worldY) {
        ObjBluePotion blue = new ObjBluePotion(gameWindow);
        blue.setWorldX(worldX);
        blue.setWorldY(worldY);
        return blue;
    }

    private MonGreenSlime createMonGreenSlime(int worldX, int worldY) {
        MonGreenSlime monster = new MonGreenSlime(gameWindow);
        monster.setWorldX(worldX);
        monster.setWorldY(worldY);
        return monster;
    }

    private MonMintSoldier createMonMintSoldier(int worldX, int worldY) {
        MonMintSoldier monster = new MonMintSoldier(gameWindow);
        monster.setWorldX(worldX);
        monster.setWorldY(worldY);
        return monster;
    }

    private MonGreenGoblin createMonGreenGoblin(int worldX, int worldY) {
        MonGreenGoblin monster = new MonGreenGoblin(gameWindow);
        monster.setWorldX(worldX);
        monster.setWorldY(worldY);
        return monster;
    }

    private ObjAxe createObjAxe(int worldX, int worldY) {
        ObjAxe objAxe = new ObjAxe(gameWindow);
        objAxe.setWorldX(worldX);
        objAxe.setWorldY(worldY);
        return objAxe;
    }

    private ObjChest createObjChest(int worldX, int worldY) {
        Entity lootPrototype = null;
        EntityFactory factory = new EntityFactory(gameWindow);
        ObjChest objChest = new ObjChest(gameWindow, lootPrototype, factory);
        objChest.setWorldX(worldX);
        objChest.setWorldY(worldY);
        return objChest;
    }

    private ObjPot createObjPot(int worldX, int worldY) {
        EntityFactory factory = new EntityFactory(gameWindow);
        ObjPot objPot = new ObjPot(gameWindow, factory);
        objPot.setWorldX(worldX);
        objPot.setWorldY(worldY);
        objPot.setPickable(true);
        objPot.setUser(gameWindow.getPlayer());
        objPot.setThrown(false);
        objPot.setAlive(true);
        return objPot;
    }

    private ObjRock createObjRock(int worldX, int worldY) {
        ObjRock objRock = new ObjRock(gameWindow);
        objRock.getSolidArea().x = 1;
        objRock.getSolidArea().y = 1;

        objRock.setSolidAreaDefaultX(objRock.getSolidArea().x);
        objRock.setSolidAreaDefaultY(objRock.getSolidArea().y);

        objRock.getSolidArea().width = (FrameApp.getTileSize() - 8);
        objRock.getSolidArea().height = (FrameApp.getTileSize() - 8);
        objRock.setWorldX(worldX);
        objRock.setWorldY(worldY);
        objRock.setPickable(true);
//        objRock.setUser(gameWindow.getPlayer());
        objRock.setThrown(false);
        objRock.setAlive(true);
        return objRock;
    }

    public NpcChicken createNpcChicken(int worldX, int worldY) {
        NpcChicken npcChicken = new NpcChicken(gameWindow);
        npcChicken.setWorldX(worldX);
        npcChicken.setWorldY(worldY);
        npcChicken.setPickable(true);
        npcChicken.setUser(gameWindow.getPlayer());
        npcChicken.setThrown(false);
        npcChicken.setAlive(true);
        return npcChicken;
    }

    public void setInteractiveTile() {
        var tiles = new InteractiveTile[100];
        for (int row = 25, i = 0; row <= 33; row++, i++) {
            tiles[i] = new ItDryTree(gameWindow, 11, row);
        }
        gameWindow.setItile(tiles);
    }
}