package entity.type;

public sealed interface EntityType permits PlayerType, NpcType, MonsterType, ChickenType,
        SwordType, AxeType, ShieldType,
        RedPotionType, GreenPotionType,
        BluePotionType, BombType,
        LanternType, PickupOnlyType,
        BossMonsterType, ChestType,
        PotType, RockType {

    int typeId();

    String name();

    default boolean isHostileToPlayer() {
        return true;
    }

    default boolean canRespawn() {
        return true;
    }
}