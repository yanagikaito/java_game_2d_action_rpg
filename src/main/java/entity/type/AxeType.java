package entity.type;

public record AxeType() implements EntityType {

    @Override
    public int typeId() {
        return 13;
    }

    @Override
    public String name() {
        return "axe";
    }
}