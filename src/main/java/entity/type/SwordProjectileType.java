package entity.type;

public record SwordProjectileType() implements EntityType {

    @Override
    public int typeId() {
        return 22;
    }

    @Override
    public String name() {
        return "sword_projectile";
    }
}