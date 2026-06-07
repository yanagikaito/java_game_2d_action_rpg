package entity.type;

public record BluePotionType() implements EntityType {

    @Override
    public int typeId() {
        return 9;
    }

    @Override
    public String name() {
        return "blue_potion";
    }
}