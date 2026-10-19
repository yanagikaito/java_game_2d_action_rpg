package entity.type;

public record GreenGoblinType() implements EntityType {

    @Override
    public int typeId() {
        return 8;
    }

    @Override
    public String name() {
        return "green_goblin";
    }
}