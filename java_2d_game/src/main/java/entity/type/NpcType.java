package entity.type;

public record NpcType() implements EntityType {

    @Override
    public int typeId() {
        return 1;
    }

    @Override
    public String name() {
        return "npc";
    }
}
