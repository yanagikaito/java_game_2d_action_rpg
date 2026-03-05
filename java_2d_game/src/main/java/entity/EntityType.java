package entity;

public sealed interface EntityType permits PlayerType, NpcType, MonsterType,
        SwordType, AxeType, ShieldType,
        RedPotionType, GreenPotionType,
        BluePotionType,
        PickupOnlyType, BombType,
        BossMonsterType {

    int typeId();

    String name();
}