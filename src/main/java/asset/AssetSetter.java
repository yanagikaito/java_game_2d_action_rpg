package asset;

import entity.Entity;
import frame.FrameApp;
import monster.MonGreenSlime;
import npc.NpcOldMan;
import object.ObjAxe;
import org.jetbrains.annotations.NotNull;
import tileInteractive.InteractiveTile;
import tileInteractive.ItDryTree;
import window.GameWindow;

public class AssetSetter {

    private GameWindow gameWindow;

    public AssetSetter(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void setNPC() {
        NpcOldMan npcOldMan = new NpcOldMan(gameWindow);
        npcOldMan.setWorldX(FrameApp.getTileSize() * 21);
        npcOldMan.setWorldY(FrameApp.getTileSize() * 21);
        Entity[] npcArray = gameWindow.getNPC();
        npcArray[0] = npcOldMan;
        gameWindow.setNPC(npcArray);
    }

    public void setMonster() {

        Entity[] monsters = gameWindow.getMonster();
        int i = 0;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 36);
        i++;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 39);

        gameWindow.setMonster(monsters);
    }

    public void setObjAxe() {

        Entity[] obj = gameWindow.getObj();
        int i = 0;

        obj[i] = createObjAxe(FrameApp.getTileSize() * 33, FrameApp.getTileSize() * 21);
    }

    public void setInteractiveTile() {

        InteractiveTile[] iTile = gameWindow.getItile();
        int i = 0;

        iTile[i] = createInteractiveTile(FrameApp.getTileSize() * 25, FrameApp.getTileSize() * 11);
        i++;
        iTile[i] = createInteractiveTile(FrameApp.getTileSize() * 26, FrameApp.getTileSize() * 11);
        i++;
        iTile[i] = createInteractiveTile(FrameApp.getTileSize() * 27, FrameApp.getTileSize() * 11);
        i++;
        iTile[i] = createInteractiveTile(FrameApp.getTileSize() * 28, FrameApp.getTileSize() * 11);
        i++;
        iTile[i] = createInteractiveTile(FrameApp.getTileSize() * 29, FrameApp.getTileSize() * 11);
        i++;
        iTile[i] = createInteractiveTile(FrameApp.getTileSize() * 30, FrameApp.getTileSize() * 11);
        i++;
        iTile[i] = createInteractiveTile(FrameApp.getTileSize() * 31, FrameApp.getTileSize() * 11);
        i++;
        iTile[i] = createInteractiveTile(FrameApp.getTileSize() * 32, FrameApp.getTileSize() * 11);
        i++;
        iTile[i] = createInteractiveTile(FrameApp.getTileSize() * 33, FrameApp.getTileSize() * 11);

        gameWindow.setItile(iTile);
    }

    private @NotNull MonGreenSlime createMonGreenSlime(int worldX, int worldY) {
        MonGreenSlime monster = new MonGreenSlime(gameWindow);
        monster.setWorldX(worldX);
        monster.setWorldY(worldY);
        return monster;
    }

    private @NotNull ObjAxe createObjAxe(int worldX, int worldY) {
        ObjAxe objAxe = new ObjAxe(gameWindow);
        objAxe.setWorldX(worldX);
        objAxe.setWorldY(worldY);
        return objAxe;
    }

    private @NotNull InteractiveTile createInteractiveTile(int worldX, int worldY) {
        ItDryTree tree = new ItDryTree(gameWindow);
        tree.setWorldX(worldX);
        tree.setWorldY(worldY);
        return tree;
    }
}