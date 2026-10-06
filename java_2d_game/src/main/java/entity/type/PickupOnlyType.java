package entity.type;

public record PickupOnlyType() implements EntityType {

    @Override
    public int typeId() {
        return 12;
    }

    @Override
    public String name() {
        return "coin";
    }
}
