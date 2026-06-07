package entity.type;

public record PotType() implements EntityType {

    @Override
    public int typeId() {
        return 14;
    }

    @Override
    public String name() {
        return "pot";
    }
}