package entity.type;

public record MintSoldierType() implements EntityType {

    @Override
    public int typeId() {
        return 3;
    }

    @Override
    public String name() {
        return "mint_soldier";
    }
}