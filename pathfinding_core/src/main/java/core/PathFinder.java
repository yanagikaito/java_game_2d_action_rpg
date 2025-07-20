package core;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PathFinder {

    private final Node[][] grid;
    private final int width, height;

    public PathFinder(Node[][] grid) {
        this.grid = grid;
        this.height = grid.length;
        this.width = (height > 0 ? grid[0].length : 0);
    }

    /**
     * A*探索を実行し、start から goal までのノードリストを返す。
     * 経路が存在しない場合は空リスト。
     */

    public List<Node> findPath(@NotNull Node start, Node goal) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();

        start.gCost = 0;
        start.hCost = Heuristic.manhattan(start, goal);
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current == goal) {
                return buildPath(goal);
            }
            closedSet.add(current);

            for (Node neighbor : getNeighbors(current)) {
                if (!neighbor.walkable || closedSet.contains(neighbor)) continue;

                double tentativeG = current.gCost + distance(current, neighbor);
                if (tentativeG < neighbor.gCost) {
                    neighbor.parent = current;
                    neighbor.gCost = tentativeG;
                    neighbor.hCost = Heuristic.manhattan(neighbor, goal);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    } else {
                        // PriorityQueue内の位置を更新したい場合は再挿入
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * 経路が見つかった場合に parent をたどって逆順から整形
     */
    private @NotNull List<Node> buildPath(Node goal) {
        List<Node> path = new ArrayList<>();
        Node curr = goal;
        while (curr != null) {
            path.add(curr);
            curr = curr.parent;
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * 隣接ノードを取得（4方向移動）
     */
    private @NotNull List<Node> getNeighbors(@NotNull Node n) {
        List<Node> neighbors = new ArrayList<>(4);
        int x = n.x, y = n.y;
        if (x > 0) neighbors.add(grid[y][x - 1]);
        if (x < width - 1) neighbors.add(grid[y][x + 1]);
        if (y > 0) neighbors.add(grid[y - 1][x]);
        if (y < height - 1) neighbors.add(grid[y + 1][x]);
        return neighbors;
    }

    /**
     * 現状は上下左右コスト＝1
     */
    private double distance(Node a, Node b) {
        return 1.0;
    }
}