package entity.type;

public record RockType() implements EntityType {

    @Override
    public int typeId() {
        return 24;
    }

    @Override
    public String name() {
        return "rock";
    }
}
