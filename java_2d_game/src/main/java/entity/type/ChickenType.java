package entity.type;

public record ChickenType() implements EntityType {

    @Override
    public int typeId() {
        return 3;
    }

    @Override
    public String name() {
        return "chicken";
    }

    // プレイヤーにダメージを与えない
    @Override
    public boolean isHostileToPlayer() {
        return false;
    }
}