package entity;

public record BluePotionType() implements EntityType {

    @Override
    public int typeId() {
        return 8;
    }

    @Override
    public String name() {
        return "blue";
    }
}