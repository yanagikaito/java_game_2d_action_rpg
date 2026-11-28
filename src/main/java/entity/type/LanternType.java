package entity.type;

public record LanternType() implements EntityType {

    @Override
    public int typeId() {
        return 19;
    }

    @Override
    public String name() {
        return "lantern";
    }
}