package entity.type;

public record CrabGuardianType() implements EntityType {

    @Override
    public int typeId() {
        return 9;
    }

    @Override
    public String name() {
        return "crab_guardian";
    }
}