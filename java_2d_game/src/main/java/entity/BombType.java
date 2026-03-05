package entity;

public record BombType() implements EntityType {

    @Override
    public int typeId() {
        return 10;
    }

    @Override
    public String name() {
        return "bom";
    }
}
