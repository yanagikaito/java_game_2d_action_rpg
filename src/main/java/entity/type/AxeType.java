package entity.type;

public record AxeType() implements EntityType {

    @Override
    public int typeId() {
        return 12;
    }

    @Override
    public String name() {
        return "axe";
    }
}