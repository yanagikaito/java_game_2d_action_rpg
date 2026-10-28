package entity.type;

public record DoorType() implements EntityType {

    @Override
    public int typeId() {
        return 25;
    }

    @Override
    public String name() {
        return "door";
    }
}