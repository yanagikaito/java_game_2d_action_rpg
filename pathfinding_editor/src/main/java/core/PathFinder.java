package core;

import java.util.*;

public class PathFinder {

    /**
     * 北（上）方向を表す定数。
     * Y座標が -1 変化する方向。
     */

    private static final int DIR_NORTH = 0;

    /**
     * 東（右）方向を表す定数。
     * X座標が +1 変化する方向。
     */

    private static final int DIR_EAST = 1;

    /**
     * 南（下）方向を表す定数。
     * Y座標が +1 変化する方向。
     */

    private static final int DIR_SOUTH = 2;


    /**
     * 西（左）方向を表す定数。
     * X座標が -1 変化する方向。
     */

    private static final int DIR_WEST = 3;

    /**
     * 無効な方向を表す定数。
     * セルがマップ外、壁の中、または到達不能な場合に返す。
     */

    private static final int INVALID_DIR = -1;

    /**
     * 移動コスト：直進（上下左右）。
     */

    private static final double MOVE_COST_STRAIGHT = 1.0;

    /**
     * 移動コスト：斜め（北東、南西など）。
     * √2 ≈ 1.41
     */

    private static final double MOVE_COST_DIAGONAL = 1.41;

    /**
     * 方向の数：4方向（N,  E, S,  W）。
     */

    private static final int DIRECTIONS_4 = 4;

    /**
     * 2次元ノード配列（マップ）。
     * 各セルの座標、通行可否、コストを保持。
     */

    private final Node[][] grid;

    /**
     * マップの高さ（行数）。
     */

    private final int height;

    /**
     * マップの幅（列数）。
     */

    private final int width;

    /**
     * X方向の移動差分（4方向）。
     * <p>
     * 各要素は、対応する方向（N,  E,  S,  W）におけるX座標の変化量を表す。
     * </p>
     * <ul>
     *   <li>index 0 (N):  0</li>
     *   <li>index 1 (E):  1</li>
     *   <li>index 2 (S):  0</li>
     *   <li>index 3 (W): -1</li>
     * </ul>
     */

    private final int[] diffX = {0, 1, 0, -1};

    /**
     * Y方向の移動差分（4方向）。
     * <p>
     * 各要素は、対応する方向（N,  E,  S, W）におけるY座標の変化量を表す。
     * </p>
     * <ul>
     *   <li>index 0 (N): -1</li>
     *   <li>index 1 (E):  0</li>
     *   <li>index 2 (S):  1</li>
     *   <li>index 3 (W):  0</li>
     * </ul>
     */

    private final int[] diffY = {-1, 0, 1, 0};


    public PathFinder(Node[][] grid) {
        this.grid = grid;
        this.height = grid.length;
        this.width = (height > 0 ? grid[0].length : 0);
    }

    /**
     * 統合フィールドを計算。
     * <p>
     * ゴールから全セルへの最短距離（コスト）を、BFS風のDijkstraアルゴリズムで計算。
     * 8方向（上下左右 + 斜め）を探索し、斜め移動には追加コスト（約1.41）を適用。
     * 計算後、各セルの {@link Node#cost} にゴールまでの最短距離が格納。
     * </p>
     * <p>
     * この統合フィールドは、NPCが最短経路でゴールに向かう「フロー・フィールド・パスファインディング」の基礎。
     * </p>
     *
     * @param goalX ゴールのX座標（マップ内の有効な範囲）
     * @param goalY ゴールのY座標（マップ内の有効な範囲）
     * @throws ArrayIndexOutOfBoundsException ゴール座標がマップ外の場合（事前にチェック推奨）
     * @see #getMoveDirection(int, int)
     * @see Node
     */

    public void calculateIntegrationField(int goalX, int goalY) {
        // 全セルのコストをリセット
        for (Node[] row : grid) {
            for (Node cell : row) {
                cell.cost = Double.MAX_VALUE;
            }
        }

        Node goal = grid[goalX][goalY];
        goal.cost = 0.0;

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
        openList.add(goal);

        while (!openList.isEmpty()) {
            Node current = openList.poll();
            int x = current.x;
            int y = current.y;

            // 4方向をチェック
            for (int i = 0; i < DIRECTIONS_4; i++) {
                int nx = x + diffX[i];
                int ny = y + diffY[i];

                // マップ外チェック
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;

                Node neighbor = grid[ny][nx];
                if (!neighbor.walkable) continue;

                // 斜め移動はコストを1.41に
                double moveCost = (i % 2 == 0) ? MOVE_COST_STRAIGHT : MOVE_COST_DIAGONAL;
                double newCost = current.cost + moveCost;

                if (newCost < neighbor.cost) {
                    neighbor.cost = newCost;
                    if (openList.contains(neighbor)) {
                        openList.remove(neighbor);
                    }
                    openList.add(neighbor);
                }
            }
        }
    }

    /**
     * 指定されたセルが進むべき方向を返す（8方向対応）。
     * <p>
     * 周囲8方向の中で、コストが最小の方向を選ぶ。
     * 通行不能またはマップ外の場合は {@link #INVALID_DIR} を返す。
     * </p>
     *
     * @param x 指定セルのX座標
     * @param y 指定セルのY座標
     * @return 進むべき方向（0~7）または -1（無効）
     */

    public int getMoveDirection(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return INVALID_DIR;
        }

        Node cell = grid[y][x];
        if (cell.cost >= Double.MAX_VALUE) {
            return INVALID_DIR;
        }

        double minCost = cell.cost;
        int bestDir = INVALID_DIR;

        // 北（上）
        if (y > 0) {
            Node n = grid[y - 1][x];
            if (n.cost < minCost) {
                minCost = n.cost;
                bestDir = DIR_NORTH;
            }
        }
        // 東（右）
        if (x < width - 1) {
            Node n = grid[y][x + 1];
            if (n.cost < minCost) {
                minCost = n.cost;
                bestDir = DIR_EAST;
            }
        }
        // 南（下）
        if (y < height - 1) {
            Node n = grid[y + 1][x];
            if (n.cost < minCost) {
                minCost = n.cost;
                bestDir = DIR_SOUTH;
            }
        }
        // 西（左）
        if (x > 0) {
            Node n = grid[y][x - 1];
            if (n.cost < minCost) {
                bestDir = DIR_WEST;
            }
        }

        return bestDir;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}