package entity.type;

public record AuraType() implements EntityType {

    @Override
    public int typeId() {
        return 29;
    }

    @Override
    public String name() {
        return "aura";
    }
}