package entity;

public record RedPotionType() implements EntityType {

    @Override
    public int typeId() {
        return 6;
    }

    @Override
    public String name() {
        return "red";
    }
}