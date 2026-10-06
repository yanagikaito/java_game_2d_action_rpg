package factory;

import db.MapEvent;
import entity.*;
import entity.type.*;
import monster.MonGreenSlime;
import npc.NpcChicken;
import npc.NpcOldMan;
import object.*;
import player.Player;
import window.GameWindow;

public class EntityFactory {

    private GameWindow gameWindow;
    private MapEvent ev = null;

    public EntityFactory(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public Entity create(EntityType type) {
        return switch (type) {
            case PlayerType p -> new Player(gameWindow, gameWindow.getKeyHandler());
            case NpcType n -> new NpcOldMan(gameWindow, ev);
            case MonsterType m -> new MonGreenSlime(gameWindow);
            case ChickenType c -> new NpcChicken(gameWindow);
            case SwordType s -> new ObjSwordNormal(gameWindow);
            case AxeType a -> new ObjAxe(gameWindow);
            case ShieldType s -> new ObjShieldWood(gameWindow);
            case RedPotionType r -> new ObjRedPotion(gameWindow);
            case GreenPotionType g -> new ObjGreenPotion(gameWindow);
            case BluePotionType b -> new ObjBluePotion(gameWindow);
            case BombType b -> new ObjBomb(gameWindow);
            case LanternType l -> new ObjLantern(gameWindow);
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