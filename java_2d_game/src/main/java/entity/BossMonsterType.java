package entity;

public record BossMonsterType() implements EntityType {

    @Override
    public int typeId() {
        return 10;
    }

    @Override
    public String name() {
        return "boss";
    }
}