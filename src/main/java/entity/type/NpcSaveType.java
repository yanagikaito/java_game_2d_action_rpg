package entity.type;

public record NpcSaveType() implements EntityType {

    @Override
    public int typeId() {
        return 3;
    }

    @Override
    public String name() {
        return "npc_save";
    }
}