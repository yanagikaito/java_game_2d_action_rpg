package entity.type;

public record NpcOldManType() implements EntityType {

    @Override
    public int typeId() {
        return 1;
    }

    @Override
    public String name() {
        return "npc_old_man";
    }
}