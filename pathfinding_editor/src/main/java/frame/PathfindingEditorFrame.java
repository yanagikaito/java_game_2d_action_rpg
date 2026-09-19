package frame;

import canvas.PathfindingCanvas;
import core.Node;
import db.DbManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.awt.event.ActionListener;

/**
 * PathfindingEditorFrame を Map ID 1〜4 に対応させた実装例。
 * - 起動時に mapId を渡すコンストラクタを追加
 * - 上部に Map ID 選択用の JComboBox を追加
 * - public void loadMap(int mapId) を実装（canvas.loadMap(mapId) を呼ぶ想定）
 * <p>
 * 注意:
 * - PathfindingCanvas に loadMap(int) が未実装なら、canvas.repaint() にフォールバックします。
 * - MainFrame と連携してライブ同期する場合は MainFrame 側でリスナー登録してください。
 */

public class PathfindingEditorFrame extends JFrame {

    private static final int DEFAULT_ROWS = 50;
    private static final int DEFAULT_COLS = 50;

    private final PathfindingCanvas canvas;
    private int currentMapId = 1;
    private final JComboBox<Integer> selector = new JComboBox<>(new Integer[]{1, 2, 3, 4});

    public PathfindingEditorFrame() throws Exception {
        this(1);
    }

    /**
     * mapId を渡して起動するコンストラクタ
     */

    public PathfindingEditorFrame(int mapId) throws Exception {
        this.currentMapId = mapId;
        initDatabase();
        canvas = new PathfindingCanvas(DEFAULT_ROWS, DEFAULT_COLS);
        initUI();
        // 起動時に指定された mapId を読み込む
        loadMap(currentMapId);
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

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(createButton("Set Start", e -> canvas.setMode(EditMode.SET_START)));
        panel.add(createButton("Set Goal", e -> canvas.setMode(EditMode.SET_GOAL)));
        panel.add(createButton("Set Block", e -> canvas.setMode(EditMode.SET_BLOCK)));
        panel.add(createButton("Save", e -> onSavePath()));
        panel.add(createButton("Load", e -> canvas.loadPathFromDb(1, 0)));
        panel.add(createButton("Run", e -> canvas.runPath()));
        panel.add(createButton("RunAll", e -> canvas.runAllStarts()));

        selector.setSelectedItem(currentMapId);
        selector.addActionListener(e -> {
            Integer sel = (Integer) selector.getSelectedItem();
            if (sel != null) {
                currentMapId = sel;
                loadMap(currentMapId);
            }
        });
        panel.add(new JLabel("Map ID:"));
        panel.add(selector);

        return panel;
    }

    private void onSavePath() {

        int sel = canvas.getSelectedStartIndex();
        if (sel >= 0) {
            List<core.Node> p = canvas.getStartPaths().getOrDefault(sel, Collections.emptyList());
            if (p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "選択中の Start に対する経路がありません。先に Run を実行してください。");
                return;
            }
            int basePathId = 0;
            int encodedPathId = basePathId * 100 + sel;
            canvas.savePathToDb(1, encodedPathId, p);
            JOptionPane.showMessageDialog(this, "Selected path saved to DB. pathId=" + encodedPathId);
            return;
        }

        Map<Integer, List<core.Node>> all = canvas.getStartPaths();
        boolean anyNonEmpty = all.values().stream().anyMatch(list -> list != null && !list.isEmpty());
        if (!anyNonEmpty) {
            JOptionPane.showMessageDialog(this, "先にRunで経路を生成してください（個別または RunAll）。");
            return;
        }

        int basePathId = 0;
        for (int i = 0; i < canvas.getStartsCount(); i++) {
            List<Node> p = canvas.getStartPaths().getOrDefault(i, Collections.emptyList());
            int encodedPathId = basePathId * 100 + i;
            canvas.savePathToDb(1, encodedPathId, p);
            System.out.println("[DB] saved startIndex=" + i + " encodedPathId=" + encodedPathId + " size=" + p.size());
        }
        JOptionPane.showMessageDialog(this, "All start paths saved to DB with basePathId=" + basePathId);
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);
        return btn;
    }

    /**
     * public にして外部（MainFrame など）からも呼べるようにする。
     * 実装は canvas.loadMap(mapId) を呼び出す想定。未実装なら repaint() でフォールバック。
     */

    public void loadMap(int mapId) {
        this.currentMapId = mapId;
        try {
            canvas.loadMap(mapId);
        } catch (NoSuchMethodError | UnsupportedOperationException ex) {
            System.out.println("PathfindingEditorFrame.loadMap: canvas.loadMap not implemented. mapId=" + mapId);
            canvas.repaint();
        }
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