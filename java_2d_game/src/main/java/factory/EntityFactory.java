package factory;

import entity.*;
import entity.type.EntityType;
import monster.MonGreenSlime;
import npc.NpcChicken;
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
            case 3 -> new NpcChicken(gameWindow);
            case 4 -> new ObjSwordNormal(gameWindow);
            case 5 -> new ObjAxe(gameWindow);
            case 6 -> new ObjShieldWood(gameWindow);
            case 7 -> new ObjRedPotion(gameWindow);
            case 8 -> new ObjGreenPotion(gameWindow);
            case 9 -> new ObjBluePotion(gameWindow);
            case 10 -> new ObjBomb(gameWindow);
            default -> null;
        };
    }

    public Entity createCoinEntity() {
        return new ObjCoinBronze(gameWindow);
    }

    public Entity createRedPotionEntity() {
        return new ObjRedPotion(gameWindow);
    }

    public Entity createGreenPotionEntity() {
        return new ObjGreenPotion(gameWindow);
    }

    public Entity createBluePotionEntity() {
        return new ObjBluePotion(gameWindow);
    }
}