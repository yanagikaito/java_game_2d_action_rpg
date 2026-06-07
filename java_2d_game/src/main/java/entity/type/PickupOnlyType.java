package entity.type;

public record PickupOnlyType() implements EntityType {

    @Override
    public int typeId() {
        return 10;
    }

    @Override
    public String name() {
        return "coin";
    }
}
