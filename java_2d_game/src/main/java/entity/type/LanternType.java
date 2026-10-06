package entity.type;

public record LanternType() implements EntityType {

    @Override
    public int typeId() {
        return 11;
    }

    @Override
    public String name() {
        return "lantern";
    }
}