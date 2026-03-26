package entity;

public sealed interface EntityType permits PlayerType, NpcType, MonsterType,
        SwordType, AxeType, ShieldType,
        RedPotionType, GreenPotionType,
        BluePotionType,
        PickupOnlyType, BombType,
        BossMonsterType, ChestType {

    int typeId();

    String name();
}