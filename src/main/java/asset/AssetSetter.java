package asset;

import entity.Entity;
import frame.FrameApp;
import npc.NpcOldMan;
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
}