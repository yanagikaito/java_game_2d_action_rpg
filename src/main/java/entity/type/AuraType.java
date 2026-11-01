package entity.type;

public record AuraType() implements EntityType {

    @Override
    public int typeId() {
        return 28;
    }

    @Override
    public String name() {
        return "aura";
    }
}