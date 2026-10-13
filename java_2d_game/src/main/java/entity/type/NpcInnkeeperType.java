package entity.type;

public record NpcInnkeeperType() implements EntityType {

    @Override
    public int typeId() {
        return 4;
    }

    @Override
    public String name() {
        return "npc_innkeeper";
    }
}