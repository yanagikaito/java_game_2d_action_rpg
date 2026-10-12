package entity.type;

public record ChestType() implements EntityType {

    @Override
    public int typeId() {
        return 6;
    }

    @Override
    public String name() {
        return "chest";
    }
}