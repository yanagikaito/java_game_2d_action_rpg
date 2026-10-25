package entity.loot;

import entity.Entity;

import java.util.function.Supplier;

public record LootEntry(Supplier<Entity> factory, int weight) {

    @Override
    public String toString() {
        return "LootEntry[" +
                "factory=" + factory + ", " +
                "weight=" + weight + ']';
    }
}