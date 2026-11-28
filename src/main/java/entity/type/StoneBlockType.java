package entity.type;

public record StoneBlockType() implements EntityType {

    @Override
    public int typeId() {
        return 29;
    }

    @Override
    public String name() {
        return "stone_block";
    }
}