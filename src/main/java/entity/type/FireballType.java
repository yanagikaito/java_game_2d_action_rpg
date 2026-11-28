package entity.type;

public record FireballType() implements EntityType {

    @Override
    public int typeId() {
        return 21;
    }

    @Override
    public String name() {
        return "fire_ball";
    }
}