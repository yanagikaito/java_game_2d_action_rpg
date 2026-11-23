package entity.type;

public record VertBlueType() implements EntityType {

    @Override
    public int typeId() {
        return 32;
    }

    @Override
    public String name() {
        return "vert_blue";
    }
}