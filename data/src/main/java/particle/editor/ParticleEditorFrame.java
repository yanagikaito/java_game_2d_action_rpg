package particle.editor;

import model.Emitter;
import model.ParticleSystemModel;

import javax.swing.*;
import java.awt.*;

public class ParticleEditorFrame extends JFrame {
    public ParticleEditorFrame() {
        setTitle("Particle Editor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // モデル & Emitter を用意
        ParticleSystemModel model = new ParticleSystemModel();
        Emitter emitter = new Emitter(); // 初期座標例
        model.addEmitter(emitter);

        // 描画用パネル
        ParticlePanel view = new ParticlePanel(model);
        add(view, BorderLayout.CENTER);

        // コントロールパネル
        ControlPanel controls = new ControlPanel(emitter);
        add(controls, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ParticleEditorFrame::new);
    }
}