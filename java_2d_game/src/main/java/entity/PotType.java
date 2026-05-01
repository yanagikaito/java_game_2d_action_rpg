package entity;

public record PotType() implements EntityType {

    @Override
    public int typeId() {
        return 13;
    }

    @Override
    public String name() {
        return "pot";
    }
}