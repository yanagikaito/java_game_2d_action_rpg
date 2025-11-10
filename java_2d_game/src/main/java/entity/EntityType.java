package entity;

public sealed interface EntityType permits PlayerType, NpcType, MonsterType,
        BossMonsterType,
        SwordType, AxeType, ShieldType,
        RedPotionType, GreenPotionType,
        PickupOnlyType, BombType {

    int typeId();

    String name();
}