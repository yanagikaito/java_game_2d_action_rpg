package entity.type;

public record GreenSlimeType() implements EntityType {

    @Override
    public int typeId() {
        return 6;
    }

    @Override
    public String name() {
        return "green_slime";
    }
}