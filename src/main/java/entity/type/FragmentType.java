package entity.type;

public record FragmentType() implements EntityType {

    @Override
    public int typeId() {
        return 35;
    }

    @Override
    public String name() {
        return "green_force_fragment";
    }
}