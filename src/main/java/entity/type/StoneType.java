package entity.type;

public record StoneType() implements EntityType {

    @Override
    public int typeId() {
        return 25;
    }

    @Override
    public String name() {
        return "stone";
    }
}