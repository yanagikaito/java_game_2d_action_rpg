package entity.type;

public record NpcMalonyChickenType() implements EntityType {

    @Override
    public int typeId() {
        return 5;
    }

    @Override
    public String name() {
        return "npc_malony_chicken";
    }
}