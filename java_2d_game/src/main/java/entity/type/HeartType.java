package entity.type;

public record HeartType() implements EntityType {

    @Override
    public int typeId() {
        return 19;
    }

    @Override
    public String name() {
        return "heart";
    }
}