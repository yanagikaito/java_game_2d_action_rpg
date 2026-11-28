package entity.type;

public record StoneType() implements EntityType {

    @Override
    public int typeId() {
        return 28;
    }

    @Override
    public String name() {
        return "stone";
    }
}