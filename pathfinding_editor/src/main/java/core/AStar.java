package core;

import java.util.*;

public class AStar {

    // 8方向対応（斜め移動を許可）
    private static final int[] DX = {0, 1, 0, -1, 1, 1, -1, -1};
    private static final int[] DY = {-1, 0, 1, 0, -1, 1, 1, -1};
    private static final double[] MOVE_COST = {
            1.0, 1.0, 1.0, 1.0, 1.41, 1.41, 1.41, 1.41
    };

    /**
     * grid[y][x] の規約を前提とする。
     * start, goal は grid 内の Node オブジェクト（同一インスタンス）であること。
     */

    public static List<Node> findPath(Node[][] grid, Node start, Node goal) {
        int rows = grid.length;
        int cols = (rows > 0 ? grid[0].length : 0);

        // 初期化
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x].resetAStar();
            }
        }

        // 開始ノード初期化
        start.g = 0.0;
        start.h = Heuristic.manhattan(start, goal);
        start.f = start.g + start.h;

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        HashSet<Node> openSet = new HashSet<>();
        HashSet<Node> closedSet = new HashSet<>();

        open.add(start);
        openSet.add(start);

        while (!open.isEmpty()) {
            Node current = open.poll();
            openSet.remove(current);

            if (current.equals(goal)) {
                // 経路復元（描画用に新しい Node を作る）
                LinkedList<Node> path = new LinkedList<>();
                Node p = current;
                while (p != null) {
                    path.addFirst(new Node(p.x, p.y));
                    p = p.parent;
                }
                return path;
            }

            closedSet.add(current);

            for (int i = 0; i < DX.length; i++) {
                int nx = current.x + DX[i];
                int ny = current.y + DY[i];

                if (nx < 0 || nx >= cols || ny < 0 || ny >= rows) continue;
                Node neighbor = grid[ny][nx];

                // 通行不可はスキップ
                if (!neighbor.walkable) continue;
                if (closedSet.contains(neighbor)) continue;

                // 斜め移動の角抜け防止（斜めインデックスは 4..7）
                if (i >= 4) {
                    int adjX1 = current.x + DX[i]; // nx
                    int adjY1 = current.y;         // current.y
                    int adjX2 = current.x;         // current.x
                    int adjY2 = current.y + DY[i]; // ny

                    boolean blockedAdj1;
                    boolean blockedAdj2;

                    if (adjY1 >= 0 && adjY1 < rows && adjX1 >= 0 && adjX1 < cols) {
                        blockedAdj1 = !grid[adjY1][adjX1].walkable;
                    } else {
                        blockedAdj1 = true;
                    }
                    if (adjY2 >= 0 && adjY2 < rows && adjX2 >= 0 && adjX2 < cols) {
                        blockedAdj2 = !grid[adjY2][adjX2].walkable;
                    } else {
                        blockedAdj2 = true;
                    }

                    if (blockedAdj1 || blockedAdj2) {
                        continue; // 角抜け禁止
                    }
                }

                double tentativeG = current.g + MOVE_COST[i];

                if (!openSet.contains(neighbor)) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.h = Heuristic.manhattan(neighbor, goal);
                    neighbor.f = neighbor.g + neighbor.h;
                    open.add(neighbor);
                    openSet.add(neighbor);
                } else if (tentativeG < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + neighbor.h;
                    // PQ の順序を更新するため再挿入
                    open.remove(neighbor);
                    open.add(neighbor);
                }
            }
        }

        // 到達不能
        return Collections.emptyList();
    }
}