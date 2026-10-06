package entity.type;

public record RockType() implements EntityType {

    @Override
    public int typeId() {
        return 16;
    }

    @Override
    public String name() {
        return "rock";
    }
}
