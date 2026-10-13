package entity.type;

public record VertBlueType() implements EntityType {

    @Override
    public int typeId() {
        return 24;
    }

    @Override
    public String name() {
        return "vert_blue";
    }
}