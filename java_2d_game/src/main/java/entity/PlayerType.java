package entity;

public record PlayerType() implements EntityType {

    @Override
    public int typeId() {
        return 0;
    }

    @Override
    public String name() {
        return "player";
    }
}