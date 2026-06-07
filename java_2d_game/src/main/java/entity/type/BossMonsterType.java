package entity.type;

public record BossMonsterType() implements EntityType {

    @Override
    public int typeId() {
        return 12;
    }

    @Override
    public String name() {
        return "boss";
    }
}