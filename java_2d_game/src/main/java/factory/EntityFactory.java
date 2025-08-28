package factory;

import entity.*;
import monster.MonGreenSlime;
import npc.NpcOldMan;
import object.*;
import player.Player;
import window.GameWindow;

public class EntityFactory {

    private GameWindow gameWindow;

    public EntityFactory(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public Entity create(EntityType type) {
        return switch (type.typeId()) {
            case 0 -> new Player(gameWindow, gameWindow.getKeyHandler());
            case 1 -> new NpcOldMan(gameWindow);
            case 2 -> new MonGreenSlime(gameWindow);
            case 3 -> new ObjSwordNormal(gameWindow);
            case 4 -> new ObjAxe(gameWindow);
            case 5 -> new ObjShieldWood(gameWindow);
            case 6 -> new ObjRedPotion(gameWindow);
            case 7 -> new ObjGreenPotion(gameWindow);
            case 8 -> new ObjBomb(gameWindow);
            default -> null;
        };
    }
}