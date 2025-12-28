package ui;

import javax.swing.*;
import java.awt.geom.Point2D;

public class TriforcePanel extends JPanel {

    private final SmallTri[] tris = new SmallTri[3];
    private final Timer timer;
    private boolean forming = false;
    private final double formDuration = 1.0;
    private double formTime = 0.0;
    private final int fpsDelay = 16;

    public TriforcePanel() {
        // 背景を透過したい場合は false、黒背景なら true
        setOpaque(false);

        double finalSize = 1.5 * 0.28;

        tris[0] = new SmallTri(-0.9, -1.6, finalSize, 0.35, 80);
        tris[1] = new SmallTri(0.0, -1.7, finalSize, 0.40, -90);
        tris[2] = new SmallTri(0.9, -1.6, finalSize, 0.35, 100);

        // Timer 内で update と repaint を呼ぶ
        timer = new Timer(fpsDelay, e -> {
            double dt = fpsDelay / 1000.0;
            update(dt);
            repaint();
        });
    }

    // TriforcePanel の一部として追加
    public void resetAnimation() {

        // 初期位置・角度・サイズを再設定
        double finalSize = 1.5 * 0.28;
        tris[0] = new SmallTri(-0.9, -1.6, finalSize, 0.35, 80);
        tris[1] = new SmallTri(0.0, -1.7, finalSize, 0.40, -90);
        tris[2] = new SmallTri(0.9, -1.6, finalSize, 0.35, 100);

        // 合体フラグ・時間をリセット
        forming = false;
        formTime = 0.0;

        // 再描画を要求
        repaint();
    }

    /**
     * タイマーを確実に開始するユーティリティ
     */

    public void startAnimation() {
        // Swing の Timer は EDT 上で安全に start/stop する
        if (!timer.isRunning()) {
            timer.start();
        }
    }


    private Point2D.Double[] finalPositions() {
        return new Point2D.Double[]{
                new Point2D.Double(0.0, 0.39),
                new Point2D.Double(-0.42, -0.42),
                new Point2D.Double(0.42, -0.42)
        };
    }

    private void update(double dt) {
        if (!forming) {
            // 通常フェーズ：現在位置（getX/getY）と角度を更新する
            for (SmallTri t : tris) {
                t.y += t.vy * dt;
                t.angle += t.angularVel * dt;
            }

            // 合体開始判定は現在位置 y を参照する（startY ではない）
            boolean allAbove = true;
            for (SmallTri t : tris) {
                if (t.y < -0.05) {
                    allAbove = false;
                    break;
                }
            }

            if (allAbove) {
                forming = true;
                formTime = 0.0;
                Point2D.Double[] targets = finalPositions();
                for (int i = 0; i < tris.length; i++) {
                    // setTarget 内で startX/startY/startSize/startAngle を記録する想定
                    tris[i].setTarget(targets[i].x, targets[i].y, tris[i].size);
                }
            }
        } else {
            // 合体フェーズ：start* と target* を使って x,y,size,angle を補間してセットする
            formTime += dt;
            double t = Math.min(1.0, formTime / formDuration);
            double ease = t * t * (3 - 2 * t); // smoothstep

            for (SmallTri st : tris) {
                st.x = lerp(st.startX, st.targetX, ease);
                st.y = lerp(st.startY, st.targetY, ease);
                st.size = lerp(st.startSize, st.targetSize, ease);
                st.angle = lerp(st.startAngle, 0.0, ease);
            }
        }
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public SmallTri[] getTris() {
        return tris;
    }

    public boolean isForming() {
        return forming;
    }

    public double getFormTime() {
        return formTime;
    }

    public double getFormDuration() {
        return formDuration;
    }
}