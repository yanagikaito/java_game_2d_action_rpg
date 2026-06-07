package entity.type;

public record RedPotionType() implements EntityType {

    @Override
    public int typeId() {
        return 7;
    }

    @Override
    public String name() {
        return "red_potion";
    }
}