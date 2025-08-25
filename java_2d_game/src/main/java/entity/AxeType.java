package entity;

public record AxeType() implements EntityType {

    @Override
    public int typeId() {
        return 4;
    }

    @Override
    public String name() {
        return "axe";
    }
}
