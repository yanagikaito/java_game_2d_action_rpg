package entity;

public record ChestType() implements EntityType {

    @Override
    public int typeId() {
        return 12;
    }

    @Override
    public String name() {
        return "chest";
    }
}