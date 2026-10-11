package entity.type;

public record VertSilverType() implements EntityType {

    @Override
    public int typeId() {
        return 21;
    }

    @Override
    public String name() {
        return "vert_silver";
    }
}