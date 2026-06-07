package entity.type;

public record AxeType() implements EntityType {

    @Override
    public int typeId() {
        return 5;
    }

    @Override
    public String name() {
        return "axe";
    }
}
