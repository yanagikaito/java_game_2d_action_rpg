package core;

public class Node {
    public final int x;
    public final int y;
    public boolean walkable = true;

    // A* 用フィールド
    public double g = Double.POSITIVE_INFINITY; // start からのコスト
    public double h = 0.0;                      // ヒューリスティック
    public double f = Double.POSITIVE_INFINITY; // g + h
    public Node parent = null;

    public double cost = Double.MAX_VALUE;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void resetAStar() {
        g = Double.POSITIVE_INFINITY;
        h = 0.0;
        f = Double.POSITIVE_INFINITY;
        parent = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node n = (Node) o;
        return x == n.x && y == n.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "Node(" + x + "," + y + ")";
    }
}