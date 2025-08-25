package entity;

public record BombType() implements EntityType {

    @Override
    public int typeId() {
        return 9;
    }

    @Override
    public String name() {
        return "bom";
    }
}
