package entity;

public record PickupOnlyType() implements EntityType {

    @Override
    public int typeId() {
        return 8;
    }

    @Override
    public String name() {
        return "pickup";
    }
}
