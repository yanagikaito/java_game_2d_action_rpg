package entity.type;

public sealed interface EntityType permits PlayerType, NpcOldManType,
        NpcMerChantType, NpcSaveType, NpcInnkeeperType, NpcMalonyChickenType,
        GreenSlimeType, MintSoldierType, BossMonsterType, ChickenType,
        ChestType, SwordType, AxeType, ShieldType,
        RedPotionType, GreenPotionType, BluePotionType, BombType,
        LanternType, PickupOnlyType, PotType, RockType,
        VertBronzeType, VertGreenType, VertBlueType, VertRedType,
        VertSilverType, VertGoldType {

    int typeId();

    String name();

    default boolean isHostileToPlayer() {
        return true;
    }

    default boolean canRespawn() {
        return true;
    }
}