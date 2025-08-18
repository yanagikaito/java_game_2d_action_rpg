// particle/editor/ParticlePanel.java
package particle.editor;

import model.Emitter;
import model.ParticleSystemModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ParticlePanel extends JPanel {
    private final ParticleSystemModel model;
    private final Emitter template;

    public ParticlePanel(ParticleSystemModel model) {
        this.model = model;
        this.template = model.getTemplate();

        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                addEmitterAt(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                addEmitterAt(e.getX(), e.getY());
            }

            private void addEmitterAt(int x, int y) {
                // テンプレートをコピー
                Emitter copy = new Emitter(
                        x, y,
                        template.getStartColor(),
                        template.getEndColor(),
                        template.getEmitRate(),
                        template.getVelMin(),
                        template.getVelMax(),
                        template.getLifeMin(),
                        template.getLifeMax(),
                        template.getStartSize(),
                        template.getEndSize()
                );
                model.addEmitter(copy);
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);

        new Timer(16, ev -> {
            model.update(0.016);
            repaint();
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        for (Emitter e : model.getEmitters()) {
            e.getParticles().forEach(p -> p.draw(g2));
        }
        g2.dispose();
    }
}