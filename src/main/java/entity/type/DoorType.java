package entity.type;

public record DoorType() implements EntityType {

    @Override
    public int typeId() {
        return 26;
    }

    @Override
    public String name() {
        return "door";
    }
}