package entity.type;

public record StoneBlockType() implements EntityType {

    @Override
    public int typeId() {
        return 28;
    }

    @Override
    public String name() {
        return "stone_block";
    }
}