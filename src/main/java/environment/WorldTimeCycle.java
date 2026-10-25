package environment;

public class WorldTimeCycle {

    private double timeSeconds = 0.0;
    private final double periodSeconds;

    public WorldTimeCycle(double periodSeconds) {
        this.periodSeconds = Math.max(1.0, periodSeconds);
    }

    /**
     * 毎フレーム呼ぶ。delta は前フレームからの経過秒数。
     */

    public void update(double delta) {
        timeSeconds += delta;
        // オーバーフロー防止
        if (timeSeconds > 1e9) timeSeconds %= periodSeconds;
    }

    /**
     * 0.0（最も暗い）〜1.0（最も明るい）を返す。
     * cos を使って昼→夜→昼の滑らかなループを作る。
     */

    public float getBrightness() {
        // 正規化 t in [0,1)
        double t = (timeSeconds % periodSeconds) / periodSeconds;
        // brightness = 0.5 * (1 + cos(2π * t)) を使うと
        // t=0 -> 1.0（明）、 t=0.5 -> 0.0（暗）、 t=1 -> 1.0（明）
        double b = 0.5 * (1.0 + Math.cos(2.0 * Math.PI * t));
        return (float) b;
    }
}