package entity.type;

public record RockType() implements EntityType {

    @Override
    public int typeId() {
        return 21;
    }

    @Override
    public String name() {
        return "rock";
    }
}
