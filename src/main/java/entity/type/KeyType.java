package entity.type;

public record KeyType() implements EntityType {

    @Override
    public int typeId() {
        return 26;
    }

    @Override
    public String name() {
        return "key";
    }
}
