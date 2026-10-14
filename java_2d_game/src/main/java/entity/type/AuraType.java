package entity.type;

public record AuraType() implements EntityType {

    @Override
    public int typeId() {
        return 26;
    }

    @Override
    public String name() {
        return "aura";
    }
}