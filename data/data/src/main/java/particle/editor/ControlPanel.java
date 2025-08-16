// particle/editor/ControlPanel.java
package particle.editor;

import model.Emitter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

public class ControlPanel extends JPanel {
    public ControlPanel(Emitter template) {
        setLayout(new GridLayout(0, 2, 5, 5));

        // Emit Rate
        JSlider rateSlider = new JSlider(0, 500, (int) template.getEmitRate());
        rateSlider.addChangeListener(e ->
                template.setEmitRate(rateSlider.getValue()));
        add(new JLabel("Emit Rate"));
        add(rateSlider);

        // Start Color
        JButton startColorBtn = new JButton("Start Color");
        startColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(
                    this, "Start Color", template.getStartColor());
            if (c != null) template.setStartColor(c);
        });
        add(new JLabel("")); // placeholder
        add(startColorBtn);

        // End Color
        JButton endColorBtn = new JButton("End Color");
        endColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(
                    this, "End Color", template.getEndColor());
            if (c != null) template.setEndColor(c);
        });
        add(new JLabel("")); // placeholder
        add(endColorBtn);

        // Velocity Min
        JSpinner velMinSpinner = new JSpinner(
                new SpinnerNumberModel(template.getVelMin(), 0.0, 1000.0, 1.0));
        velMinSpinner.addChangeListener(e ->
                template.setVelMin(((Number) velMinSpinner.getValue()).doubleValue()));
        add(new JLabel("Velocity Min"));
        add(velMinSpinner);

        // Velocity Max
        JSpinner velMaxSpinner = new JSpinner(
                new SpinnerNumberModel(template.getVelMax(), 0.0, 1000.0, 1.0));
        velMaxSpinner.addChangeListener(e ->
                template.setVelMax(((Number) velMaxSpinner.getValue()).doubleValue()));
        add(new JLabel("Velocity Max"));
        add(velMaxSpinner);

        // Life Min
        JSpinner lifeMinSpinner = new JSpinner(
                new SpinnerNumberModel(template.getLifeMin(), 0.0, 100.0, 0.1));
        lifeMinSpinner.addChangeListener(e ->
                template.setLifeMin(((Number) lifeMinSpinner.getValue()).doubleValue()));
        add(new JLabel("Life Min (s)"));
        add(lifeMinSpinner);

        // Life Max
        JSpinner lifeMaxSpinner = new JSpinner(
                new SpinnerNumberModel(template.getLifeMax(), 0.0, 100.0, 0.1));
        lifeMaxSpinner.addChangeListener(e ->
                template.setLifeMax(((Number) lifeMaxSpinner.getValue()).doubleValue()));
        add(new JLabel("Life Max (s)"));
        add(lifeMaxSpinner);

        // Start Size
        JSpinner startSizeSpinner = new JSpinner(
                new SpinnerNumberModel(template.getStartSize(), 0.0, 200.0, 0.5));
        startSizeSpinner.addChangeListener(e ->
                template.setStartSize(((Number) startSizeSpinner.getValue()).floatValue()));
        add(new JLabel("Start Size"));
        add(startSizeSpinner);

        // End Size
        JSpinner endSizeSpinner = new JSpinner(
                new SpinnerNumberModel(template.getEndSize(), 0.0, 200.0, 0.5));
        endSizeSpinner.addChangeListener(e ->
                template.setEndSize(((Number) endSizeSpinner.getValue()).floatValue()));
        add(new JLabel("End Size"));
        add(endSizeSpinner);

        // Position X
        JSpinner posXSpinner = new JSpinner(
                new SpinnerNumberModel(template.getPosition().getX(), -1000.0, 2000.0, 1.0));
        posXSpinner.addChangeListener(e -> {
            double x = ((Number) posXSpinner.getValue()).doubleValue();
            Point2D p = template.getPosition();
            template.setPosition(new Point2D.Double(x, p.getY()));
        });
        add(new JLabel("Position X"));
        add(posXSpinner);

        // Position Y
        JSpinner posYSpinner = new JSpinner(
                new SpinnerNumberModel(template.getPosition().getY(), -1000.0, 2000.0, 1.0));
        posYSpinner.addChangeListener(e -> {
            double y = ((Number) posYSpinner.getValue()).doubleValue();
            Point2D p = template.getPosition();
            template.setPosition(new Point2D.Double(p.getX(), y));
        });
        add(new JLabel("Position Y"));
        add(posYSpinner);
    }
}