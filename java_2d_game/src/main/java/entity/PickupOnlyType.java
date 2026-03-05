package entity;

public record PickupOnlyType() implements EntityType {

    @Override
    public int typeId() {
        return 9;
    }

    @Override
    public String name() {
        return "pickup";
    }
}
