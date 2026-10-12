package entity.type;

public record BossMonsterType() implements EntityType {

    @Override
    public int typeId() {
        return 4;
    }

    @Override
    public String name() {
        return "boss";
    }
}