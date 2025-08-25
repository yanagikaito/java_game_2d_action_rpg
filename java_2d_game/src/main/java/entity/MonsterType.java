package entity;

public record MonsterType() implements EntityType {

    @Override
    public int typeId() {
        return 2;
    }

    @Override
    public String name() {
        return "monster";
    }
}