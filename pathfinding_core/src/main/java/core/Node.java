package core;

public class Node implements Comparable<Node> {

    public final int x, y;
    public boolean walkable;

    public double gCost = Double.MAX_VALUE;
    public double hCost = 0;
    public Node parent = null;

    public Node(int x, int y, boolean walkable) {
        this.x = x;
        this.y = y;
        this.walkable = walkable;
    }

    /**
     * fCost = gCost + hCost
     */
    public double fCost() {
        return gCost + hCost;
    }

    /**
     * PriorityQueue で fCost の小さい順ソート
     */
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.fCost(), other.fCost());
    }
}
