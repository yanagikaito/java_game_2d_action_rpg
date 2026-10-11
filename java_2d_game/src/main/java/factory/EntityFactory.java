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

            // キャラクター・モンスター系
            case PlayerType player -> new Player(gameWindow, gameWindow.getKeyHandler());
            case NpcType npc -> new NpcOldMan(gameWindow, ev);
            case MonsterType monster -> new MonGreenSlime(gameWindow);
            case ChickenType chicken -> new NpcChicken(gameWindow);

            // 装備・通常アイテム系（変数名の重複を解消）
            case SwordType sword -> new ObjSwordNormal(gameWindow);
            case AxeType axe -> new ObjAxe(gameWindow);
            case ShieldType shield -> new ObjShieldWood(gameWindow);
            case RedPotionType redPotion -> new ObjRedPotion(gameWindow);
            case GreenPotionType greenPotion -> new ObjGreenPotion(gameWindow);
            case BluePotionType bluePotion -> new ObjBluePotion(gameWindow);
            case BombType bomb -> new ObjBomb(gameWindow);
            case LanternType lantern -> new ObjLantern(gameWindow);

            // 6色の通貨（ヴェール）系
            case VertBronzeType bronzeVert -> new ObjVertBronze(gameWindow);
            case VertGreenType greenVert -> new ObjVertGreen(gameWindow);
            case VertBlueType blueVert -> new ObjVertBlue(gameWindow);
            case VertRedType redVert -> new ObjVertRed(gameWindow);
            case VertSilverType silverVert -> new ObjVertSilver(gameWindow);
            case VertGoldType goldVert -> new ObjVertGold(gameWindow);

            default -> null;
        };
    }

    public Entity createBronzeVertEntity() {
        return new ObjVertBronze(gameWindow);
    }

    public Entity createGreenVertEntity() {
        return new ObjVertGreen(gameWindow);
    }

    public Entity createBlueVertEntity() {
        return new ObjVertBlue(gameWindow);
    }

    public Entity createRedVertEntity() {
        return new ObjVertRed(gameWindow);
    }

    public Entity createSilverVertEntity() {
        return new ObjVertSilver(gameWindow);
    }

    public Entity createGoldVertEntity() {
        return new ObjVertGold(gameWindow);
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