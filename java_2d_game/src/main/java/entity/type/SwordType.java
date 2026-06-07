package entity.type;

public record SwordType() implements EntityType {

    @Override
    public int typeId() {
        return 4;
    }

    @Override
    public String name() {
        return "sword";
    }
}