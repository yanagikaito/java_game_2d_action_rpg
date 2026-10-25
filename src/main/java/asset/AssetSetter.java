package asset;

import entity.Entity;
import entity.type.*;
import factory.EntityFactory;
import frame.FrameApp;
import npc.*;
import tileInteractive.InteractiveTile;
import tileInteractive.ItDryTree;
import window.GameWindow;

import static tile.TileManager.WATER_TILE_ID;

public class AssetSetter {

    private GameWindow gameWindow;
    private EntityFactory entityFactory;

    public AssetSetter(GameWindow gameWindow, EntityFactory entityFactory) {
        this.gameWindow = gameWindow;
        this.entityFactory = entityFactory;
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

        Entity entity = entityFactory.create(new NpcOldManType());

        if (entity instanceof NpcOldMan npc) {

            npc.applyMapEvent(ev);

            if (ev != null) {
                npc.setWorldX(FrameApp.getTileSize() * ev.getX());
                npc.setWorldY(FrameApp.getTileSize() * ev.getY());
            }

            Entity[] npcArray = gameWindow.getNPC();
            if (npcArray == null || npcArray.length <= 1) {
                npcArray = new Entity[Math.max(10, (npcArray == null ? 0 : npcArray.length))];
            }
            npcArray[0] = npc;
            gameWindow.setNPC(npcArray);
        }

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

        entityFactory.setMapEvent(ev);

        Entity entity = entityFactory.create(new NpcMalonyChickenType());

        if (entity instanceof NpcMalonyChicken npc) {

            npc.applyMapEvent(ev);

            if (ev != null) {
                npc.setWorldX(FrameApp.getTileSize() * ev.getX());
                npc.setWorldY(FrameApp.getTileSize() * ev.getY());
            }

            Entity[] npcArray = gameWindow.getNPC();
            if (npcArray == null || npcArray.length <= 1) {
                npcArray = new Entity[Math.max(10, (npcArray == null ? 0 : npcArray.length))];
            }
            npcArray[1] = npc;
            gameWindow.setNPC(npcArray);
        }

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

        Entity entity = entityFactory.create(new NpcMerChantType());

        if (entity instanceof NpcMerChant npc) {

            npc.applyMapEvent(ev);

            if (ev != null) {
                npc.setWorldX(FrameApp.getTileSize() * ev.getX());
                npc.setWorldY(FrameApp.getTileSize() * ev.getY());
            }

            Entity[] npcArray = gameWindow.getNPC();
            if (npcArray == null || npcArray.length <= 1) {
                npcArray = new Entity[Math.max(10, (npcArray == null ? 0 : npcArray.length))];
            }
            npcArray[0] = npc;
            gameWindow.setNPC(npcArray);
        }

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

        Entity entity = entityFactory.create(new NpcSaveType());

        if (entity instanceof NpcSave npc) {

            npc.applyMapEvent(ev);

            if (ev != null) {
                npc.setWorldX(FrameApp.getTileSize() * ev.getX());
                npc.setWorldY(FrameApp.getTileSize() * ev.getY());
            }

            Entity[] npcArray = gameWindow.getNPC();
            if (npcArray == null || npcArray.length <= 1) {
                npcArray = new Entity[Math.max(10, (npcArray == null ? 0 : npcArray.length))];
            }
            npcArray[1] = npc;
            gameWindow.setNPC(npcArray);
        }

        javax.swing.SwingUtilities.invokeLater(() -> gameWindow.repaint());
    }

    public void setNpcInnkeeper(String eventId, int tileX, int tileY) {

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

        Entity entity = entityFactory.create(new NpcInnkeeperType());

        if (entity instanceof NpcInnkeeper npc) {

            npc.applyMapEvent(ev);

            if (ev != null) {
                npc.setWorldX(FrameApp.getTileSize() * ev.getX());
                npc.setWorldY(FrameApp.getTileSize() * ev.getY());
            }

            Entity[] npcArray = gameWindow.getNPC();
            if (npcArray == null || npcArray.length <= 1) {
                npcArray = new Entity[Math.max(10, (npcArray == null ? 0 : npcArray.length))];
            }
            npcArray[2] = npc;
            gameWindow.setNPC(npcArray);
        }

        javax.swing.SwingUtilities.invokeLater(() -> gameWindow.repaint());
    }

    public void setMonster() {

        Entity[] monsters = gameWindow.getMonster();
        int i = 0;

        monsters[i++] = createMonMintSoldier(FrameApp.getTileSize() * 25, FrameApp.getTileSize() * 3);

        monsters[i++] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 26);
        monsters[i++] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 38);
        monsters[i++] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 39);

        monsters[i++] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 31);
        monsters[i++] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 33);
        monsters[i++] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 35);
        monsters[i++] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 37);
        monsters[i++] = createNpcChicken(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 39);
        monsters[i++] = createNpcChicken(FrameApp.getTileSize() * 27, FrameApp.getTileSize() * 32);
        monsters[i++] = createNpcChicken(FrameApp.getTileSize() * 35, FrameApp.getTileSize() * 32);
        monsters[i++] = createNpcChicken(FrameApp.getTileSize() * 35, FrameApp.getTileSize() * 34);
        monsters[i++] = createNpcChicken(FrameApp.getTileSize() * 27, FrameApp.getTileSize() * 35);
        monsters[i++] = createNpcChicken(FrameApp.getTileSize() * 27, FrameApp.getTileSize() * 39);

        gameWindow.setMonster(monsters);
    }

    public void setBossMonster() {

        Entity[] monsters = gameWindow.getMonster();
        int i = 0;

        monsters[i] = createMonGreenGoblin(FrameApp.getTileSize() * 27, FrameApp.getTileSize() * 15);

        gameWindow.setMonster(monsters);
    }

    public void setObjPot(String startEventId, String endEventId, int arrayStartIndex, int currentMapId) {

        db.MapModel model = gameWindow.getModel();
        if (model == null) {
            System.err.println("Model is null in setObjPot");
            return;
        }

        model.loadEventsFromDb(currentMapId);
        int tileSize = FrameApp.getTileSize();
        int currentIndex = arrayStartIndex;

        try {

            String[] startParts = startEventId.split("_");
            String[] endParts = endEventId.split("_");

            // 開始と終了の X と Y をそれぞれ取得する
            int startX = Integer.parseInt(startParts[1]);
            int startY = Integer.parseInt(startParts[2]);
            int endX = Integer.parseInt(endParts[1]);
            int endY = Integer.parseInt(endParts[2]);

            // 【二重ループ】Y座標（行）を進めながら、それぞれの行のX座標（列）を順番に処理する
            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {

                    int tileId = gameWindow.getTileManager().getTileIdAt(x, y);
                    System.out.println("DEBUG: x=" + x + ", y=" + y + " -> tileId = " + tileId);
                    if (tileId == WATER_TILE_ID) {
                        continue; // 水タイル（ID: 2）なら壺を置かない
                    }

                    // 正しく "pot_" のイベントIDを組み立てる
                    String eventId = "pot_" + x + "_" + y;
                    db.MapEvent ev = model.getEventById(eventId);

                    Entity pot = createObjPot(0, 0);

                    if (pot != null) {
                        pot.applyMapEvent(ev);

                        if (ev != null) {
                            pot.setWorldX(tileSize * ev.getX());
                            pot.setWorldY(tileSize * ev.getY());
                        } else {
                            pot.setWorldX(tileSize * x);
                            pot.setWorldY(tileSize * y);
                        }

                        Entity[] objArray = gameWindow.getObj();
                        if (objArray == null || objArray.length <= currentIndex) {
                            int newSize = Math.max(currentIndex + 20, (objArray == null ? 50 : objArray.length * 2));
                            Entity[] newArray = new Entity[newSize];
                            if (objArray != null) {
                                System.arraycopy(objArray, 0, newArray, 0, objArray.length);
                            }
                            objArray = newArray;
                            gameWindow.setObj(objArray);
                        }

                        objArray[currentIndex] = pot;
                        currentIndex++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to parse eventId range: " + startEventId + " to " + endEventId);
        }

        javax.swing.SwingUtilities.invokeLater(() -> gameWindow.repaint());
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

    public void setObjChest(String startEventId, String endEventId, int arrayStartIndex, int currentMapId) {

        db.MapModel model = gameWindow.getModel();
        if (model == null) {
            System.err.println("Model is null in setObjChest");
            return;
        }

        model.loadEventsFromDb(currentMapId);
        int tileSize = FrameApp.getTileSize();
        int currentIndex = arrayStartIndex;

        try {
            String[] startParts = startEventId.split("_");
            String[] endParts = endEventId.split("_");

            // 開始と終了の X と Y をそれぞれ取得する
            int startX = Integer.parseInt(startParts[1]);
            int startY = Integer.parseInt(startParts[2]);
            int endX = Integer.parseInt(endParts[1]);
            int endY = Integer.parseInt(endParts[2]);

            // 【二重ループ】Y座標（行）を進めながら、それぞれの行のX座標（列）を順番に処理する
            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    // 正しく "chest_" のイベントIDを組み立てる
                    String eventId = "chest_" + x + "_" + y;
                    db.MapEvent ev = model.getEventById(eventId);

                    Entity chest = createObjChest(0, 0);

                    if (chest != null) {
                        chest.applyMapEvent(ev);

                        if (ev != null) {
                            chest.setWorldX(tileSize * ev.getX());
                            chest.setWorldY(tileSize * ev.getY());
                        } else {
                            chest.setWorldX(tileSize * x);
                            chest.setWorldY(tileSize * y);
                        }

                        Entity[] objArray = gameWindow.getObj();
                        if (objArray == null || objArray.length <= currentIndex) {
                            int newSize = Math.max(currentIndex + 20, (objArray == null ? 50 : objArray.length * 2));
                            Entity[] newArray = new Entity[newSize];
                            if (objArray != null) {
                                System.arraycopy(objArray, 0, newArray, 0, objArray.length);
                            }
                            objArray = newArray;
                            gameWindow.setObj(objArray);
                        }

                        objArray[currentIndex] = chest;
                        currentIndex++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to parse eventId range: " + startEventId + " to " + endEventId);
        }

        javax.swing.SwingUtilities.invokeLater(() -> gameWindow.repaint());
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

    public void setObjLantern() {

        Entity[] obj = gameWindow.getObj();
        int i = 20;

        obj[i] = createObjLantern(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 21);
    }

    private Entity createObjLantern(int worldX, int worldY) {

        Entity lantern = entityFactory.create(new LanternType());
        if (lantern != null) {
            lantern.setWorldX(worldX);
            lantern.setWorldY(worldY);
        }
        return lantern;
    }

    private Entity createObjRedPotion(int worldX, int worldY) {

        Entity red = entityFactory.create(new RedPotionType());
        if (red != null) {
            red.setWorldX(worldX);
            red.setWorldY(worldY);
        }
        return red;
    }

    private Entity createObjGreenPotion(int worldX, int worldY) {

        Entity green = entityFactory.create(new GreenPotionType());
        if (green != null) {
            green.setWorldX(worldX);
            green.setWorldY(worldY);
        }
        return green;
    }

    private Entity createObjBluePotion(int worldX, int worldY) {

        Entity blue = entityFactory.create(new BluePotionType());
        if (blue != null) {
            blue.setWorldX(worldX);
            blue.setWorldY(worldY);
        }
        return blue;
    }

    private Entity createMonGreenSlime(int worldX, int worldY) {

        Entity monster = entityFactory.create(new GreenSlimeType());
        if (monster != null) {
            monster.setWorldX(worldX);
            monster.setWorldY(worldY);
        }
        return monster;
    }

    private Entity createMonMintSoldier(int worldX, int worldY) {

        Entity monster = entityFactory.create(new MintSoldierType());
        if (monster != null) {
            monster.setWorldX(worldX);
            monster.setWorldY(worldY);
        }
        return monster;
    }

    private Entity createMonGreenGoblin(int worldX, int worldY) {

        Entity monster = entityFactory.create(new GreenGoblinType());
        if (monster != null) {
            monster.setWorldX(worldX);
            monster.setWorldY(worldY);
        }
        return monster;
    }

    public Entity createNpcChicken(int worldX, int worldY) {

        Entity npcChicken = entityFactory.create(new ChickenType());
        if (npcChicken != null) {
            npcChicken.getSolidArea().x = 16;
            npcChicken.getSolidArea().y = 16;

            npcChicken.setSolidAreaDefaultX(npcChicken.getSolidArea().x);
            npcChicken.setSolidAreaDefaultY(npcChicken.getSolidArea().y);

            npcChicken.getSolidArea().width = (FrameApp.getTileSize() + 12);
            npcChicken.getSolidArea().height = (FrameApp.getTileSize() + 12);
            npcChicken.setWorldX(worldX);
            npcChicken.setWorldY(worldY);
            npcChicken.setPickable(true);
            npcChicken.setUser(gameWindow.getPlayer());
            npcChicken.setThrown(false);
            npcChicken.setAlive(true);
            npcChicken.setCollision(true);
        }
        return npcChicken;
    }

    private Entity createObjAxe(int worldX, int worldY) {
        Entity objAxe = entityFactory.create(new AxeType());
        if (objAxe != null) {
            objAxe.setWorldX(worldX);
            objAxe.setWorldY(worldY);
        }
        return objAxe;
    }

    private Entity createObjChest(int worldX, int worldY) {
        Entity lootPrototype = null;
        EntityFactory factory = new EntityFactory(gameWindow);
        Entity objChest = entityFactory.create(new ChestType(), factory);
        if (objChest != null) {
            objChest.setWorldX(worldX);
            objChest.setWorldY(worldY);
        }
        return objChest;
    }

    private Entity createObjPot(int worldX, int worldY) {
        EntityFactory factory = new EntityFactory(gameWindow);
        Entity objPot = entityFactory.create(new PotType(), factory);
        if (objPot != null) {
            objPot.getSolidArea().x = 1;
            objPot.getSolidArea().y = 1;

            objPot.setSolidAreaDefaultX(objPot.getSolidArea().x);
            objPot.setSolidAreaDefaultY(objPot.getSolidArea().y);

            objPot.getSolidArea().width = (FrameApp.getTileSize() - 8);
            objPot.getSolidArea().height = (FrameApp.getTileSize() - 8);
            objPot.setWorldX(worldX);
            objPot.setWorldY(worldY);
            objPot.setPickable(true);
            objPot.setUser(gameWindow.getPlayer());
            objPot.setThrown(false);
            objPot.setAlive(true);
        }
        return objPot;
    }

    private Entity createObjRock(int worldX, int worldY) {
        Entity objRock = entityFactory.create(new RockType());
        if (objRock != null) {
            objRock.getSolidArea().x = 1;
            objRock.getSolidArea().y = 1;

            objRock.setSolidAreaDefaultX(objRock.getSolidArea().x);
            objRock.setSolidAreaDefaultY(objRock.getSolidArea().y);

            objRock.getSolidArea().width = (FrameApp.getTileSize() - 8);
            objRock.getSolidArea().height = (FrameApp.getTileSize() - 8);
            objRock.setWorldX(worldX);
            objRock.setWorldY(worldY);
            objRock.setPickable(true);
            objRock.setUser(gameWindow.getPlayer());
            objRock.setThrown(false);
            objRock.setAlive(true);
        }
        return objRock;
    }

    public void setInteractiveTile() {
        var tiles = new InteractiveTile[100];
        for (int row = 25, i = 0; row <= 33; row++, i++) {
            tiles[i] = new ItDryTree(gameWindow, 11, row);
        }
        gameWindow.setItile(tiles);
    }
}