package core;

import org.jetbrains.annotations.NotNull;

public final class Heuristic {

    private Heuristic() {
    }

    public static double manhattan(@NotNull Node a, @NotNull Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}