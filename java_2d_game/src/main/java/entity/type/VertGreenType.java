package entity.type;

public record VertGreenType() implements EntityType {

    @Override
    public int typeId() {
        return 28;
    }

    @Override
    public String name() {
        return "vert_green";
    }
}