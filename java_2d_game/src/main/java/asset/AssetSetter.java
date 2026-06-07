package asset;

import entity.Entity;
import factory.EntityFactory;
import frame.FrameApp;
import monster.MonGreenGoblin;
import monster.MonGreenSlime;
import monster.MonMintSoldier;
import npc.NpcChicken;
import npc.NpcMerChant;
import npc.NpcOldMan;
import npc.NpcSave;
import object.*;
import tileInteractive.InteractiveTile;
import tileInteractive.ItDryTree;
import window.GameWindow;

public class AssetSetter {

    private GameWindow gameWindow;

    public AssetSetter(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void setNpcOldMan() {
        NpcOldMan npcOldMan = new NpcOldMan(gameWindow);
        npcOldMan.setWorldX(FrameApp.getTileSize() * 21);
        npcOldMan.setWorldY(FrameApp.getTileSize() * 21);
        Entity[] npcArray = gameWindow.getNPC();
        npcArray[0] = npcOldMan;
        gameWindow.setNPC(npcArray);
    }

    public void setNpcMerChant() {
        NpcMerChant npcMerChant = new NpcMerChant(gameWindow);
        npcMerChant.setWorldX(FrameApp.getTileSize() * 29);
        npcMerChant.setWorldY(FrameApp.getTileSize() * 11);
        Entity[] npcArray = gameWindow.getNPC();
        npcArray[0] = npcMerChant;
        gameWindow.setNPC(npcArray);
    }

    public void setNpcSave() {
        NpcSave npcSave = new NpcSave(gameWindow);
        npcSave.setWorldX(FrameApp.getTileSize() * 27);
        npcSave.setWorldY(FrameApp.getTileSize() * 11);
        Entity[] npcArray = gameWindow.getNPC();
        npcArray[1] = npcSave;
        gameWindow.setNPC(npcArray);
    }

    public void setMonster() {

        Entity[] monsters = gameWindow.getMonster();
        int i = 0;

        monsters[i] = createMonMintSoldier(FrameApp.getTileSize() * 25, FrameApp.getTileSize() * 3);
        i++;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 25, FrameApp.getTileSize() * 38);
        i++;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 38);
        i++;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 39);
        i++;

        monsters[i] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 17);

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
    }

    public void setObjAxe() {

        Entity[] obj = gameWindow.getObj();
        int i = 3;

        obj[i] = createObjAxe(FrameApp.getTileSize() * 33, FrameApp.getTileSize() * 21);
    }

    public void setObjChest() {

        Entity[] obj = gameWindow.getObj();
        int i = 4;

        obj[i] = createObjChest(FrameApp.getTileSize() * 30, FrameApp.getTileSize() * 22);
        i++;
        obj[i] = createObjChest(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 21);
        i++;
        obj[i] = createObjChest(FrameApp.getTileSize() * 32, FrameApp.getTileSize() * 22);
    }

    public void setObjRedPotion() {

        Entity[] obj = gameWindow.getObj();
        int i = 7;

        obj[i] = createObjRedPotion(FrameApp.getTileSize() * 33, FrameApp.getTileSize() * 23);
        i++;
        obj[i] = createObjRedPotion(FrameApp.getTileSize() * 34, FrameApp.getTileSize() * 24);
        i++;
        obj[i] = createObjRedPotion(FrameApp.getTileSize() * 35, FrameApp.getTileSize() * 25);
    }

    public void setObjGreenPotion() {

        Entity[] obj = gameWindow.getObj();
        int i = 10;

        obj[i] = createObjGreenPotion(FrameApp.getTileSize() * 36, FrameApp.getTileSize() * 26);
        i++;
        obj[i] = createObjGreenPotion(FrameApp.getTileSize() * 37, FrameApp.getTileSize() * 27);
        i++;
        obj[i] = createObjGreenPotion(FrameApp.getTileSize() * 38, FrameApp.getTileSize() * 28);
    }

    public void setObjBluePotion() {

        Entity[] obj = gameWindow.getObj();
        int i = 13;

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
        gameWindow.getUi().addMessage(" pickable=" + objPot.isPickable());
        return objPot;
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
        for (int col = 25, i = 0; col <= 33; col++, i++) {
            tiles[i] = new ItDryTree(gameWindow, 11, col);
        }
        gameWindow.setItile(tiles);
    }
}