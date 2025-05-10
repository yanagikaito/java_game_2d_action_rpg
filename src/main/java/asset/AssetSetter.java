package asset;

import entity.Entity;
import frame.FrameApp;
import monster.MonGreenSlime;
import npc.NpcOldMan;
import org.jetbrains.annotations.NotNull;
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

        monsters[0] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 36);
        monsters[1] = createMonGreenSlime(FrameApp.getTileSize() * 23, FrameApp.getTileSize() * 39);

        gameWindow.setMonster(monsters);
    }

    private @NotNull MonGreenSlime createMonGreenSlime(int worldX, int worldY) {
        MonGreenSlime monster = new MonGreenSlime(gameWindow);
        monster.setWorldX(worldX);
        monster.setWorldY(worldY);
        return monster;
    }
}