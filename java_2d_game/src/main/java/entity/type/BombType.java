package entity.type;

public record BombType() implements EntityType {

    @Override
    public int typeId() {
        return 11;
    }

    @Override
    public String name() {
        return "bom";
    }
}
