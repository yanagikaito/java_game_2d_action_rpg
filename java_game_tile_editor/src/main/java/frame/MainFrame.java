package frame;

import db.DbManager;
import map.MapModel;
import map.MapCanvas;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private final MapModel model = new MapModel(50, 50);
    private final Map<Integer, Image> tileImages = new HashMap<>();
    private final MapCanvas canvas;

    public MainFrame() throws Exception {
        DbManager.initSchema();
        loadTiles();
        canvas = new MapCanvas(model, tileImages);

        JPanel control = new JPanel();
        for (int id : tileImages.keySet()) {
            ImageIcon icon = new ImageIcon(tileImages.get(id).getScaledInstance(24, 24, 0));
            JButton b = new JButton(icon);
            b.addActionListener(e -> canvas.setSelectedTile(id));
            control.add(b);
        }

        JButton save = new JButton("Save");
        save.addActionListener(e -> saveMap(1));
        JButton load = new JButton("Load");
        load.addActionListener(e -> loadMap(1));
        control.add(save);
        control.add(load);

        setLayout(new BorderLayout());
        add(control, BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);

        this.setFocusable(true);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void loadTiles() {

        for (int id = 0; id < 5; id++) {

            try (InputStream is = getClass().getResourceAsStream("/tiles/" + id + ".png")) {
                if (is == null) {
                    System.err.println("タイル画像が見つかりません: " + "/tiles/" + id + ".png");
                } else {
                    BufferedImage img = ImageIO.read(is);
                    tileImages.put(id, img);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveMap(int mapId) {
        try (Connection conn = DbManager.getConnection()) {
            conn.createStatement().execute("DELETE FROM map_tile WHERE map_id=" + mapId);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO map_tile(map_id,x,y,tile_id) VALUES(?,?,?,?)");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMap(int mapId) {
        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT x,y,tile_id FROM map_tile WHERE map_id=?")) {
            ps.setInt(1, mapId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.setTile(rs.getInt("x"), rs.getInt("y"), rs.getInt("tile_id"));
                }
                canvas.repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}