package frame;

import canvas.PathfindingCanvas;
import db.DbManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PathfindingEditorFrame extends JFrame {

    private static final int DEFAULT_ROWS = 50;
    private static final int DEFAULT_COLS = 50;

    private final PathfindingCanvas canvas;

    public PathfindingEditorFrame() throws Exception {
        initDatabase();
        canvas = new PathfindingCanvas(DEFAULT_ROWS, DEFAULT_COLS);
        initUI();
    }

    private void initDatabase() throws Exception {
        DbManager.initSchema();
    }

    private void initUI() {
        setTitle("Pathfinding Editor");
        setLayout(new BorderLayout());

        // コントロールパネルとキャンバスを配置
        add(createControlPanel(), BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private @NotNull JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(createButton("Set Start", e -> canvas.setMode(EditMode.SET_START)));
        panel.add(createButton("Set Goal", e -> canvas.setMode(EditMode.SET_GOAL)));
        panel.add(createButton("Set Block", e -> canvas.setMode(EditMode.SET_BLOCK)));
        panel.add(createButton("Run", e -> canvas.runPath()));
        return panel;
    }

    private @NotNull JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new PathfindingEditorFrame().setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }
}