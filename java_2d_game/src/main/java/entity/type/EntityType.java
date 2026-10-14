package entity.type;

public sealed interface EntityType permits PlayerType, NpcOldManType,
        NpcMerChantType, NpcSaveType, NpcInnkeeperType, NpcMalonyChickenType,
        GreenSlimeType, MintSoldierType, BossMonsterType, ChickenType,
        ChestType, SwordType, AxeType, ShieldType,
        RedPotionType, GreenPotionType, BluePotionType, BombType,
        LanternType, HeartType, FireballType, SwordProjectileType, PickupOnlyType, PotType, RockType, StoneType,
        AuraType, VertBronzeType, VertGreenType, VertBlueType, VertRedType,
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