package entity;

public record SwordType() implements EntityType {

    @Override
    public int typeId() {
        return 3;
    }

    @Override
    public String name() {
        return "sword";
    }
}