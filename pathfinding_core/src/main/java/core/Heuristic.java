package core;

public final class Heuristic {

    private Heuristic() {
    }

    public static double manhattan(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}
