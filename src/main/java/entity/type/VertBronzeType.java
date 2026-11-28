package entity.type;

public record VertBronzeType() implements EntityType {

    @Override
    public int typeId() {
        return 31;
    }

    @Override
    public String name() {
        return "vert_bronze";
    }
}