// model/ParticleSystemModel.java
package model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class ParticleSystemModel {
    // 描画用の Emitter リスト
    private final List<Emitter> emitters = new ArrayList<>();

    // ControlPanel で色やレートを設定するプロトタイプ
    private final Emitter template = new Emitter();
    private final Point2D gravity = new Point2D.Double(0, 200);

    public Emitter getTemplate() {
        return template;
    }

    public void addEmitter(Emitter e) {
        emitters.add(e);
    }

    public List<Emitter> getEmitters() {
        return List.copyOf(emitters);
    }

    public void update(double dt) {
        emitters.forEach(e -> e.update(dt,gravity));
    }
}