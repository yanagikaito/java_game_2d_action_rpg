package entity.type;

public record BossMonsterType() implements EntityType {

    @Override
    public int typeId() {
        return 8;
    }

    @Override
    public String name() {
        return "boss";
    }
}