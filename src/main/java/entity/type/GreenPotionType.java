package entity.type;

public record GreenPotionType() implements EntityType {

    @Override
    public int typeId() {
        return 16;
    }

    @Override
    public String name() {
        return "green_potion";
    }
}