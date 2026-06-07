package entity.type;

public sealed interface EntityType permits PlayerType, NpcType, MonsterType, ChickenType,
        SwordType, AxeType, ShieldType,
        RedPotionType, GreenPotionType,
        BluePotionType,
        PickupOnlyType, BombType,
        BossMonsterType, ChestType,
        PotType {

    int typeId();

    String name();

    default boolean isHostileToPlayer() {
        return true;
    }
}