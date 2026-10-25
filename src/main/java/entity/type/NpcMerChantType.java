package entity.type;

public record NpcMerChantType() implements EntityType {

    @Override
    public int typeId() {
        return 2;
    }

    @Override
    public String name() {
        return "npc_mer_chant";
    }
}