package core;

/**
 * フロー・フィールドやパスファインディングで使用される、マップ上の1つのセル（ノード）を表すクラス。
 * <p>
 * 各セルは座標 (x, y)、通行可否、およびゴールまでのコスト（距離）を持つ。
 * フロー・フィールドの計算やNPCの移動判定に使用。
 * </p>
 */

public class Node {


    /**
     * このノードのX座標（横方向）。
     * マップ配列の列番号に対応。
     */

    public final int x;

    /**
     * このノードのY座標（縦方向）。
     * マップ配列の行番号に対応。
     */

    public final int y;

    /**
     * このノードからゴールまでの累積コスト（距離）。
     * <p>
     * 初期値は {@link Double#MAX_VALUE}（到達不能)。
     * 統合フィールドの計算中に、最短距離が設定。
     * </p>
     */

    public double cost;

    /**
     * このノードが通行可能かどうかを示すフラグ。
     * <p>
     * {@code true}：通行可能（道）<br>
     * {@code false}：通行不能（壁）
     * </p>
     */

    public boolean walkable;

    /**
     * Nodeを初期化します。
     *
     * @param x このノードのX座標
     * @param y このノードのY座標
     * @see #cost
     * @see #walkable
     */

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.cost = Double.MAX_VALUE;
        this.walkable = true;
    }
}
