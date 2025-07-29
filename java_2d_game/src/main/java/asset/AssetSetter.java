package asset;

import entity.Entity;
import frame.FrameApp;
import monster.MonGreenSlime;
import npc.MerChant;
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

    public void setMerchant() {
        MerChant npcMerChant = new MerChant(gameWindow);
        npcMerChant.setWorldX(FrameApp.getTileSize() * 29);
        npcMerChant.setWorldY(FrameApp.getTileSize() * 11);
        Entity[] npcArray = gameWindow.getNPC();
        npcArray[0] = npcMerChant;
        gameWindow.setNPC(npcArray);
    }

    public void setMonster() {

        Entity[] monsters = gameWindow.getMonster();
        int i = 0;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 36);
        i++;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 37);
        i++;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 38);
        i++;

        monsters[i] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 39);

        gameWindow.setMonster(monsters);
    }

    public void setObjAxe() {

        Entity[] obj = gameWindow.getObj();
        int i = 0;

        obj[i] = createObjAxe(FrameApp.getTileSize() * 33, FrameApp.getTileSize() * 21);
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

    public void setInteractiveTile() {
        var tiles = new InteractiveTile[100];
        for (int col = 25, i = 0; col <= 33; col++, i++) {
            tiles[i] = new ItDryTree(gameWindow, 11, col);
        }
        gameWindow.setItile(tiles);
    }
}