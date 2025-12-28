package ui;

/**
 * トライフォースを構成する小さな三角形の状態を保持するデータクラス。
 * <p>
 * このクラスは描画・補間（合体）用の位置・角度・サイズ・速度などを公開フィールドとして持つ。
 * フィールドは軽量にアクセスできるよう public としていますが、必要なら getter/setter に変更してください。
 * </p>
 */

public class SmallTri {

    /**
     * 初期回転角度（度）
     */

    private static final double DEFAULT_START_ANGLE = 0.0;

    /**
     * colorSeed の上限（乱数生成に使用）
     */

    private static final int COLOR_SEED_BOUND = 10_000;

    // 表示位置（正規化座標系）
    public double x;
    public double y;

    // 目標位置（合体時のターゲット）
    public double targetX;
    public double targetY;

    // 補間開始時の記録（合体補間で使用）
    public double startX;
    public double startY;

    // サイズ（半高さに相当）
    public double size;
    public double targetSize;
    public double startSize;

    // 回転角度（度）
    public double angle;
    public double startAngle;

    // 回転速度（度/秒）
    public double angularVel;

    // 上昇速度（正の値で上に移動する想定）
    public double vy;

    // アニメーション補助（色やバリエーションの種）
    public int colorSeed;

    /**
     * SmallTri を生成します。
     *
     * @param x          初期 x 座標（正規化座標）
     * @param y          初期 y 座標（正規化座標）
     * @param size       初期サイズ（半高さ、正規化単位）
     * @param vy         上昇速度（正の値で上方向に移動する想定）
     * @param angularVel 回転速度（度/秒）
     */

    public SmallTri(double x, double y, double size, double vy, double angularVel) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.vy = vy;
        this.angularVel = angularVel;

        this.angle = DEFAULT_START_ANGLE;
        this.startAngle = this.angle;

        this.targetX = x;
        this.targetY = y;
        this.targetSize = size;

        this.startX = x;
        this.startY = y;
        this.startSize = size;

        this.colorSeed = (int) (Math.random() * COLOR_SEED_BOUND);
    }

    /**
     * 合体（補間）時のターゲット位置・サイズを設定し、補間開始時の状態を記録。
     * <p>
     * 呼び出し後は外部の update/lerp ロジックが startX/startY から targetX/targetY へ補間を行う。
     * </p>
     *
     * @param tx    目標 x 座標（正規化座標）
     * @param ty    目標 y 座標（正規化座標）
     * @param tSize 目標サイズ（半高さ、正規化単位）
     */

    public void setTarget(double tx, double ty, double tSize) {
        this.startX = this.x;
        this.startY = this.y;
        this.startSize = this.size;
        this.startAngle = this.angle;

        this.targetX = tx;
        this.targetY = ty;
        this.targetSize = tSize;
    }
}