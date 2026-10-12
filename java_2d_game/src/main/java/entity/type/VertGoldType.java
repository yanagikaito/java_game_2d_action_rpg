package entity.type;

public record VertGoldType() implements EntityType {

    @Override
    public int typeId() {
        return 23;
    }

    @Override
    public String name() {
        return "vert_gold";
    }
}