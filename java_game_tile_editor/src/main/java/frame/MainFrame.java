package frame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import db.DbManager;
import db.MapEvent;
import db.MapModel;
import map.MapCanvas;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private final ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final MapModel model = new MapModel(50, 50);
    private final JComboBox<Integer> selector;
    private int currentMapId = 1;
    private final Map<Integer, Image> tileImages = new HashMap<>();
    private final MapCanvas canvas;

    private final Image eventIcon;
    private final JPanel propertyPanel = new JPanel();
    private String lastShownEventId = null;

    public MainFrame() throws Exception {
        DbManager.initSchema();
        loadTiles();
        Image tmp = null;
        try (InputStream is = getClass().getResourceAsStream("/icons/event.png")) {
            if (is != null) {
                tmp = ImageIO.read(is);
            }
        }
        eventIcon = tmp;
        canvas = new MapCanvas(model, tileImages);
        if (eventIcon != null) canvas.setEventIcon(eventIcon);

        JButton importTxt = new JButton("Import TXT");
        importTxt.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int ret = fc.showOpenDialog(MainFrame.this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    importWorldTxtToModel(f.toPath());
                    canvas.repaint();
                    JOptionPane.showMessageDialog(MainFrame.this, "Import completed: " + f.getName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this, "Import failed: " + ex.getMessage());
                }
            }
        });


        JPanel control = new JPanel();

        JButton eventTool = new JButton("Event");
        eventTool.addActionListener(e -> canvas.setTool("event"));
        control.add(eventTool);

        propertyPanel.setPreferredSize(new Dimension(260, 400));
        propertyPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        propertyPanel.setBackground(new Color(0xF7F7F7));
        propertyPanel.setOpaque(true);

        canvas.setSelectionListener(ev -> {
            System.out.println("selectionListener called: " + (ev == null ? "null" : ev.getId()));
            SwingUtilities.invokeLater(() -> {
                try {
                    showProperties(ev);
                } catch (Throwable t) {
                    t.printStackTrace();
                    propertyPanel.add(new JLabel("Error showing properties: see console"), BorderLayout.CENTER);
                    propertyPanel.revalidate();
                    propertyPanel.repaint();
                }
            });
        });


        for (int id : tileImages.keySet()) {
            ImageIcon icon = new ImageIcon(tileImages.get(id).getScaledInstance(24, 24, 0));
            JButton b = new JButton(icon);
            b.addActionListener(e -> canvas.setSelectedTile(id));
            control.add(b);
        }

        selector = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        selector.setSelectedItem(currentMapId);
        selector.addActionListener(e -> {
            currentMapId = (Integer) selector.getSelectedItem();
            loadMap(currentMapId);
        });
        control.add(new JLabel("Map ID:"));
        control.add(selector);

        JButton save = new JButton("Save");
        save.addActionListener(e -> saveMap(currentMapId));
        JButton load = new JButton("Load");
        load.addActionListener(e -> loadMap(currentMapId));
        control.add(save);
        control.add(load);
        control.add(importTxt);

        setLayout(new BorderLayout());
        add(control, BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        add(propertyPanel, BorderLayout.EAST);

        this.setFocusable(true);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void importWorldTxtToModel(java.nio.file.Path txtPath) throws Exception {
        java.util.List<String> lines = java.nio.file.Files.readAllLines(txtPath, java.nio.charset.StandardCharsets.UTF_8);
        // 空行を除去
        java.util.List<String> trimmed = new java.util.ArrayList<>();
        for (String l : lines) {
            String t = l.trim();
            if (!t.isEmpty()) trimmed.add(t);
        }
        int rows = trimmed.size();
        if (rows == 0) throw new IllegalStateException("入力ファイルが空です");
        String[] firstParts = trimmed.get(0).split("\\s+");
        int cols = firstParts.length;

        // rowMajor[y][x]
        int[][] rowMajor = new int[rows][cols];
        for (int y = 0; y < rows; y++) {
            String[] parts = trimmed.get(y).split("\\s+");
            if (parts.length != cols) throw new IllegalStateException("行長が不揃いです y=" + y);
            for (int x = 0; x < cols; x++) {
                rowMajor[y][x] = Integer.parseInt(parts[x]);
            }
        }

        // colMajor[x][y] に転置して model にセット（model は MapModel(50,50) を想定）
        if (cols != model.getWidth() || rows != model.getHeight()) {
            // サイズが違う場合は警告するが、可能なら縮小/拡張ロジックを入れても良い
            int confirm = JOptionPane.showConfirmDialog(this,
                    "入力サイズがモデルと一致しません (file: " + cols + "x" + rows + ", model: " + model.getWidth() + "x" + model.getHeight() + "). 続行しますか？",
                    "Size mismatch", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        for (int x = 0; x < Math.min(cols, model.getWidth()); x++) {
            for (int y = 0; y < Math.min(rows, model.getHeight()); y++) {
                int tileId = rowMajor[y][x]; // rowMajor[y][x] -> (x,y)
                model.setTile(x, y, tileId); // model expects (x,y)
            }
        }

        // 検証ログ（先頭3列の先頭5行を表示）
        StringBuilder sb = new StringBuilder();
        sb.append("Imported: cols=").append(cols).append(" rows=").append(rows).append("\n");
        for (int sx = 0; sx < Math.min(3, cols); sx++) {
            sb.append("col[").append(sx).append("][0..4] = ");
            for (int sy = 0; sy < Math.min(5, rows); sy++) {
                sb.append(model.getTile(sx, sy));
                if (sy + 1 < Math.min(5, rows)) sb.append(",");
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }

    private void loadTiles() {

        for (int id = 0; id < 15; id++) {

            try (InputStream is = getClass().getResourceAsStream("/tiles/" + id + ".png")) {
                if (is == null) {
                    System.err.println("tiles" + id + ".png");
                } else {
                    BufferedImage img = ImageIO.read(is);
                    tileImages.put(id, img);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // プロパティ表示メソッド
    // 置き換え用: showProperties
    private void showProperties(MapEvent ev) {
        if (ev != null && ev.getId() != null && ev.getId().equals(lastShownEventId)) return;
        lastShownEventId = (ev == null) ? null : ev.getId();

        propertyPanel.removeAll();

        if (ev == null) {
            propertyPanel.add(new JLabel("No event selected"), BorderLayout.CENTER);
            propertyPanel.revalidate();
            propertyPanel.repaint();
            return;
        }

        System.out.println("showProperties for: " + ev.getId() + " x=" + ev.getX() + " y=" + ev.getY());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;

        form.add(new JLabel("ID: " + ev.getId()), c);

        c.gridy++;
        c.gridwidth = 1;
        form.add(new JLabel("Name:"), c);
        c.gridx = 1;
        JTextField nameField = new JTextField(ev.getName() == null ? "" : ev.getName());
        form.add(nameField, c);

        c.gridx = 0;
        c.gridy++;
        form.add(new JLabel("Trigger:"), c);
        c.gridx = 1;
        JComboBox<String> triggerBox = new JComboBox<>(new String[]{"interact", "touch", "auto"});
        triggerBox.setSelectedItem(ev.getTrigger() == null ? "interact" : ev.getTrigger());
        form.add(triggerBox, c);

        // --- Dialogues list ---
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        form.add(new JLabel("Dialogues (lines):"), c);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        if (ev.getDialogues() != null) {
            ev.getDialogues().forEach(listModel::addElement);
        }
        JList<String> dlgList = new JList<>(listModel);
        dlgList.setVisibleRowCount(6);
        JScrollPane dlgScroll = new JScrollPane(dlgList);
        c.gridy++;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        form.add(dlgScroll, c);
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        // add/remove buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton addLine = new JButton("Add line");
        addLine.addActionListener(a -> {
            String line = JOptionPane.showInputDialog(MainFrame.this, "New dialogue line:");
            if (line != null && !line.isEmpty()) listModel.addElement(line);
        });
        JButton editLine = new JButton("Edit selected");
        editLine.addActionListener(a -> {
            int idx = dlgList.getSelectedIndex();
            if (idx >= 0) {
                String cur = listModel.get(idx);
                String line = JOptionPane.showInputDialog(MainFrame.this, "Edit dialogue line:", cur);
                if (line != null) listModel.set(idx, line);
            }
        });
        JButton removeLine = new JButton("Remove selected");
        removeLine.addActionListener(a -> {
            int idx = dlgList.getSelectedIndex();
            if (idx >= 0) listModel.remove(idx);
        });
        btnRow.add(addLine);
        btnRow.add(editLine);
        btnRow.add(removeLine);
        c.gridy++;
        form.add(btnRow, c);

        // --- Quest settings ---
        c.gridy++;
        c.gridwidth = 1;
        c.gridx = 0;
        JCheckBox offerBox = new JCheckBox("Quest offered", ev.isQuestOffered());
        form.add(offerBox, c);
        c.gridx = 1;
        JCheckBox acceptedBox = new JCheckBox("Quest accepted", ev.isQuestAccepted());
        form.add(acceptedBox, c);

        c.gridx = 0;
        c.gridy++;
        JCheckBox completedBox = new JCheckBox("Quest completed", ev.isQuestCompleted());
        form.add(completedBox, c);

        // reward field in meta
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        form.add(new JLabel("Reward :"), c);
        c.gridx = 1;
        String rewardStr = "";
        if (ev.getMeta() != null && ev.getMeta().get("reward") != null) {
            rewardStr = String.valueOf(ev.getMeta().get("reward"));
        }
        JTextField rewardField = new JTextField(rewardStr);
        form.add(rewardField, c);

        // Apply button
        JButton apply = new JButton("Apply");
        apply.addActionListener(a -> {
            ev.setName(nameField.getText());
            ev.setTrigger((String) triggerBox.getSelectedItem());

            // dialogues を List にしてセット
            java.util.List<String> newDialogues = new java.util.ArrayList<>();
            for (int i = 0; i < listModel.size(); i++) newDialogues.add(listModel.get(i));
            ev.setDialogues(newDialogues);

            ev.setQuestOffered(offerBox.isSelected());
            ev.setQuestAccepted(acceptedBox.isSelected());
            ev.setQuestCompleted(completedBox.isSelected());

            // meta の reward をセット（数値変換）
            java.util.Map<String, Object> meta = ev.getMeta() == null ? new java.util.HashMap<>() : ev.getMeta();
            try {
                if (!rewardField.getText().isBlank()) {
                    meta.put("reward", Integer.parseInt(rewardField.getText().trim()));
                } else {
                    meta.remove("reward");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(MainFrame.this, "Reward must be a number");
            }
            ev.setMeta(meta);

            canvas.repaint();
            // 永続化（Save ボタンを押す運用ならここは不要。即保存したければ有効化）
            saveMap(currentMapId);

            System.out.println("Applied properties to event: " + ev.getId() + " name=" + ev.getName() + " trigger=" + ev.getTrigger());
        });

        propertyPanel.setLayout(new BorderLayout());
        propertyPanel.add(form, BorderLayout.CENTER);
        propertyPanel.add(apply, BorderLayout.SOUTH);
        propertyPanel.revalidate();
        propertyPanel.repaint();
    }

    private void saveMap(int mapId) {

        try (Connection conn = DbManager.getConnection()) {
            conn.setAutoCommit(false);

            // --- タイル保存（既存処理） ---
            try (Statement st = conn.createStatement()) {
                st.execute("DELETE FROM map_tile WHERE map_id=" + mapId);
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO map_tile(map_id,x,y,tile_id) VALUES(?,?,?,?)")) {
                for (int y = 0; y < model.getHeight(); y++) {
                    for (int x = 0; x < model.getWidth(); x++) {
                        ps.setInt(1, mapId);
                        ps.setInt(2, x);
                        ps.setInt(3, y);
                        ps.setInt(4, model.getTile(x, y));
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }

            // --- イベント保存 ---
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM map_event WHERE map_id=?")) {
                del.setInt(1, mapId);
                del.executeUpdate();
            }

            try (PreparedStatement psEv = conn.prepareStatement(
                    "INSERT INTO map_event(id,map_id,x,y,name,trigger_type,json) VALUES(?,?,?,?,?,?,?)")) {
                for (db.MapEvent ev : model.getAllEvents()) {
                    psEv.setString(1, ev.getId());
                    psEv.setInt(2, mapId);
                    psEv.setInt(3, ev.getX());
                    psEv.setInt(4, ev.getY());
                    psEv.setString(5, ev.getName());
                    psEv.setString(6, ev.getTrigger());

                    String json = null;
                    try {
                        // MapEvent を JSON 文字列に変換
                        json = om.writeValueAsString(ev);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        // 失敗したら null を入れておく（ログは出る）
                        json = null;
                    }
                    if (json == null) {
                        psEv.setNull(7, java.sql.Types.VARCHAR);
                    } else {
                        // JSON をバイト列で DB に保存
                        psEv.setBytes(7, json.getBytes(StandardCharsets.UTF_8));
                    }
                    psEv.addBatch();
                }
                psEv.executeBatch();
            }

            conn.commit();

            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Save completed (map + events)."));
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage()));
        }
    }

    private void loadMap(int mapId) {

        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT x,y,tile_id FROM map_tile WHERE map_id=?")) {
            ps.setInt(1, mapId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.setTile(rs.getInt("x"), rs.getInt("y"), rs.getInt("tile_id"));
                }
            }

            // イベント読み込み
            try (PreparedStatement psEv = conn.prepareStatement("SELECT id,x,y,name,trigger_type,json FROM map_event WHERE map_id=?")) {
                psEv.setInt(1, mapId);
                try (ResultSet rs = psEv.executeQuery()) {
                    model.clearEvents();
                    while (rs.next()) {
                        String id = rs.getString("id");
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        byte[] raw = rs.getBytes("json");
                        String json;
                        if (raw != null) {
                            json = new String(raw, java.nio.charset.StandardCharsets.UTF_8);
                        } else {
                            json = rs.getString("json"); // フォールバック
                        }
                        System.out.println("map_event row id=" + id + " raw_json_len=" + (json == null ? "null" : json.length()));
                        System.out.println("raw_json_preview=" + (json == null ? "null" : (json.length() > 500 ? json.substring(0, 500) + "..." : json)));
                        db.MapEvent ev = null;

                        if (json != null && !json.isBlank()) {
                            try {
                                // JSON から MapEvent に復元
                                ev = om.readValue(json, db.MapEvent.class);
                            } catch (Exception ex) {
                                // パース失敗時はログを出してフォールバックで最低限の情報をセット
                                ex.printStackTrace();
                                ev = new db.MapEvent(id, x, y);
                                ev.setName(rs.getString("name"));
                                ev.setTrigger(rs.getString("trigger_type"));
                            }
                        } else {
                            // json が無ければ従来どおり最低限の情報で作る
                            ev = new db.MapEvent(id, x, y);
                            ev.setName(rs.getString("name"));
                            ev.setTrigger(rs.getString("trigger_type"));
                        }

                        model.addEvent(ev);
                    }
                }
            }

            canvas.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}