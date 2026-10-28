package entity.type;

public record VertGreenType() implements EntityType {

    @Override
    public int typeId() {
        return 29;
    }

    @Override
    public String name() {
        return "vert_green";
    }
}