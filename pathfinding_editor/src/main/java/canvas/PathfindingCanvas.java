package canvas;

import core.Node;
import db.DbManager;
import frame.EditMode;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;

public class PathfindingCanvas extends JPanel implements Scrollable {

    private final int numRows;
    private final int numCols;
    private final int tileSize;
    private EditMode mode = EditMode.TILE;
    private Point start;
    private Point goal;
    private List<Node> path = Collections.emptyList();
    private final boolean[][] blocks;

    /**
     * @param numRows  行数
     * @param numCols  列数
     * @param tileSize １タイルの描画サイズ(px)
     */

    public PathfindingCanvas(int numRows, int numCols, int tileSize) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.tileSize = tileSize;
        this.blocks = new boolean[numRows][numCols];

        setPreferredSize(new Dimension(numCols * tileSize, numRows * tileSize));
        addMouseListener(new CanvasMouseListener());
    }

    /**
     * 指定した mapId のタイル情報を DB から読み込み、blocks 配列に反映する。
     * デフォルトの判定は「tile_id == 0 => 通行可、それ以外 => 障害物」。
     */
    public void loadMap(int mapId) {
        // デフォルトの判定を渡す
        loadMap(mapId, tileId -> tileId != 0);
    }

    /**
     * 指定した mapId のタイル情報を DB から読み込み、与えた predicate で
     * tile_id を障害物かどうか判定して blocks を更新する。
     *
     * @param mapId     マップID
     * @param isBlocked tile_id を受け取り障害物なら true を返す判定
     */
    public void loadMap(int mapId, IntPredicate isBlocked) {
        // まず全セルを通行可にリセット
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                blocks[r][c] = false;
            }
        }

        // DB からタイル情報を読み込んで blocks を設定
        String sql = "SELECT x,y,tile_id FROM map_tile WHERE map_id=?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mapId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int tileId = rs.getInt("tile_id");

                    // 範囲チェック（DB に不正な座標が入っている可能性に備える）
                    if (y >= 0 && y < numRows && x >= 0 && x < numCols) {
                        blocks[y][x] = isBlocked.test(tileId);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 読み込み後は start/goal がブロックになっていないか確認（必要ならクリア）
        if (start != null && blocks[start.y][start.x]) {
            // start がブロックなら解除するか null にする。ここでは解除（通行可にする）
            blocks[start.y][start.x] = false;
        }
        if (goal != null && blocks[goal.y][goal.x]) {
            blocks[goal.y][goal.x] = false;
        }

        repaint();
    }

    /**
     * デフォルトのタイルサイズ 32px を使う場合
     */

    public PathfindingCanvas(int numRows, int numCols) {
        this(numRows, numCols, 32);
    }

    /**
     * モード切替（Start/Goal/Block）
     */

    public void setMode(EditMode mode) {
        this.mode = mode;
    }

    /**
     * Run ボタン呼び出しで経路探索
     */

    public void runPath() {
        if (start == null || goal == null) return;

        Node[][] grid = buildGrid(); // grid[y][x] 規約
        Node startNode = grid[start.y][start.x];
        Node goalNode = grid[goal.y][goal.x];

        // start/goal が通行不可なら終了
        if (!startNode.walkable || !goalNode.walkable) {
            this.path = Collections.emptyList();
            repaint();
            return;
        }

        List<Node> result = core.AStar.findPath(grid, startNode, goalNode);
        this.path = (result == null ? Collections.emptyList() : result);
        repaint();
    }

    /**
     * Node 配列生成
     */

    private Node[][] buildGrid() {
        Node[][] grid = new Node[numRows][numCols];
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                Node n = new Node(c, r);
                // blocks[row][col] が true のときは障害物（通行不可）
                n.walkable = !blocks[r][c];
                // A* 用フィールドがあるなら初期化（Node.resetAStar() があれば呼ぶ）
                try {
                    n.resetAStar();
                } catch (Throwable ignored) {
                    // resetAStar が無ければ無視
                }
                grid[r][c] = n;
            }
        }
        return grid;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g2);
        drawGrid(g2);
        drawBlocks(g2);
        drawCoordinates(g2);
        drawStartGoal(g2);
        drawPath(g2);
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        // 垂直線
        for (int c = 0; c <= numCols; c++) {
            int x = c * tileSize;
            g.drawLine(x, 0, x, numRows * tileSize);
        }
        // 水平線
        for (int r = 0; r <= numRows; r++) {
            int y = r * tileSize;
            g.drawLine(0, y, numCols * tileSize, y);
        }
    }

    private void drawBlocks(Graphics g) {
        g.setColor(Color.YELLOW);
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                if (blocks[r][c]) {
                    g.fillRect(
                            c * tileSize + 1,
                            r * tileSize + 1,
                            tileSize - 2,
                            tileSize - 2
                    );
                }
            }
        }
    }

    private void drawStartGoal(Graphics g) {
        int s = tileSize / 4;
        int d = tileSize / 2;
        if (start != null) {
            g.setColor(Color.GREEN);
            g.fillOval(start.x * tileSize + s, start.y * tileSize + s, d, d);
        }
        if (goal != null) {
            g.setColor(Color.RED);
            g.fillOval(goal.x * tileSize + s, goal.y * tileSize + s, d, d);
        }
    }

    private void drawPath(Graphics g) {
        if (path.isEmpty()) return;
        g.setColor(Color.BLUE);
        int s = tileSize / 4;
        int d = tileSize / 2;
        for (Node n : path) {
            g.fillRect(n.x * tileSize + s, n.y * tileSize + s, d, d);
        }
    }

    /**
     * マウスクリックで Start/Goal/障害物 設定
     */

    private class CanvasMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int col = e.getX() / tileSize;
            int row = e.getY() / tileSize;
            if (col < 0 || col >= numCols || row < 0 || row >= numRows) return;

            switch (mode) {
                case SET_START -> start = new Point(col, row);
                case SET_GOAL -> goal = new Point(col, row);
                case SET_BLOCK -> blocks[row][col] = !blocks[row][col];
                default -> {
                }
            }
            repaint();
        }
    }

    /**
     * 経路をDBに保存する
     *
     * @param mapId  マップID
     * @param pathId 経路ID
     * @param path   保存する経路（Nodeのリスト）
     */

    public void savePathToDb(int mapId, int pathId, List<Node> path) {
        String deleteSql = "DELETE FROM map_path WHERE map_id=? AND path_id=?";
        String insertSql = "INSERT INTO map_path(map_id,path_id,step,x,y) VALUES(?,?,?,?,?)";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement del = conn.prepareStatement(deleteSql);
             PreparedStatement ins = conn.prepareStatement(insertSql)) {

            // 既存経路を削除
            del.setInt(1, mapId);
            del.setInt(2, pathId);
            del.executeUpdate();

            // 経路をバッチ登録
            int step = 0;
            for (Node n : path) {
                ins.setInt(1, mapId);
                ins.setInt(2, pathId);
                ins.setInt(3, step++);
                ins.setInt(4, n.x);
                ins.setInt(5, n.y);
                ins.addBatch();
            }
            ins.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void drawCoordinates(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(Color.BLACK);

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                String text = col + "," + row;
                // 文字のベースラインをタイル内左下に
                int x = col * tileSize + 2;
                int y = (row + 1) * tileSize - 4;
                g2.drawString(text, x, y);
            }
        }
    }

    public void loadPathFromDb(int mapId, int pathId) {

        List<Point> points = db.PathManager.loadPath(mapId, pathId);
        this.path = points.stream()
                .map(p -> new Node(p.x, p.y))
                .toList();

        System.out.println(">> Loaded path: " + points);

        repaint();
    }

    public List<Node> getPath() {
        return path;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
        return tileSize;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
        return tileSize * 5;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}