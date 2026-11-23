package entity.type;

public record VertRedType() implements EntityType {

    @Override
    public int typeId() {
        return 33;
    }

    @Override
    public String name() {
        return "vert_red";
    }
}