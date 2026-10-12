package entity.type;

public record ShieldType() implements EntityType {

    @Override
    public int typeId() {
        return 9;
    }

    @Override
    public String name() {
        return "shield";
    }
}