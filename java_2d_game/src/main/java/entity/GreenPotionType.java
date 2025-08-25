package entity;

public record GreenPotionType() implements EntityType {

    @Override
    public int typeId() {
        return 7;
    }

    @Override
    public String name() {
        return "green";
    }
}
