package entity;

public record ShieldType() implements EntityType {

    @Override
    public int typeId() {
        return 5;
    }

    @Override
    public String name() {
        return "shield";
    }
}