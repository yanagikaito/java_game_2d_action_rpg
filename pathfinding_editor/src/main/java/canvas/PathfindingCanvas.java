package canvas;

import core.Node;
import db.DbManager;
import frame.EditMode;


import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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
    private Timer movementTimer;
    private int pathIndex = 0;
    private Point movingPos = null;
    private static final int MOVE_DELAY_MS = 150;

    // --- 追加フィールド: 複数 Start と Start ごとの経路・タイマー管理 ---
    private final java.util.List<Point> starts = new ArrayList<>(); // タイル座標で保持、最大10
    private int selectedStartIndex = -1; // 選択中の Start、未選択は -1
    private final java.util.Map<Integer, java.util.List<Node>> startPaths = new HashMap<>();
    private final java.util.Map<Integer, Timer> movementTimers = new HashMap<>();
    private final java.util.Map<Integer, Integer> pathIndices = new HashMap<>();

    // 表示用の位置を Start ごとに保持する
    private final java.util.Map<Integer, Point> movingPositions = new java.util.HashMap<>();

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
        // 選択中の Start があればそれを優先して実行
        if (selectedStartIndex >= 0 && selectedStartIndex < starts.size()) {
            System.out.println("[PATH] runPath delegating to runPathForStart selectedStart=" + selectedStartIndex);
            runPathForStart(selectedStartIndex);
            return;
        }

        // 従来の単一 start/goal 用（互換性維持）
        if (start == null || goal == null) return;

        Node[][] grid = buildGrid();
        Node startNode = grid[start.y][start.x];
        Node goalNode = grid[goal.y][goal.x];

        if (!startNode.walkable || !goalNode.walkable) {
            stopMovement();
            this.path = Collections.emptyList();
            repaint();
            return;
        }

        List<Node> result = core.AStar.findPath(grid, startNode, goalNode);
        this.path = (result == null ? Collections.emptyList() : result);
        if (!this.path.isEmpty()) {
            startMovement();
        } else {
            stopMovement();
        }
        repaint();
    }

    public void runPathForStart(int startIndex) {
        if (startIndex < 0 || startIndex >= starts.size()) return;
        if (goal == null) return;

        Node[][] grid = buildGrid();
        Point sTile = starts.get(startIndex);
        Node startNode = grid[sTile.y][sTile.x];
        Node goalNode = grid[goal.y][goal.x];

        if (!startNode.walkable || !goalNode.walkable) {
            startPaths.remove(startIndex);
            repaint();
            return;
        }

        List<Node> result = core.AStar.findPath(grid, startNode, goalNode);
        startPaths.put(startIndex, result == null ? Collections.emptyList() : result);

        if (!startPaths.get(startIndex).isEmpty()) {
            startMovementForStart(startIndex);
        }
        repaint();
    }

    private void startMovementForStart(int startIndex) {
        // 既にタイマーがある場合は停止してから再開
        stopMovementForStart(startIndex);

        java.util.List<Node> pathFor = startPaths.getOrDefault(startIndex, Collections.emptyList());
        if (pathFor.isEmpty()) return;

        pathIndices.put(startIndex, 0);

        Timer t = new Timer(MOVE_DELAY_MS, e -> {
            int idx = pathIndices.getOrDefault(startIndex, 0);
            if (idx >= pathFor.size()) {
                stopMovementForStart(startIndex);
                return;
            }
            Node n = pathFor.get(idx);
            pathIndices.put(startIndex, idx + 1);

            // Start ごとの表示位置を更新する（共有しない）
            movingPositions.put(startIndex, new Point(n.x, n.y));
            repaint();
        });

        movementTimers.put(startIndex, t);
        t.start();

        // デバッグログ（任意）
        System.out.println("[PATH] startMovementForStart startIndex=" + startIndex + " pathSize=" + pathFor.size());
    }


    // Start ごとの移動を停止するメソッド
    private void stopMovementForStart(int startIndex) {
        Timer t = movementTimers.remove(startIndex);
        if (t != null) t.stop();
        pathIndices.remove(startIndex);
    }


    private void startMovement() {
        stopMovement();
        pathIndex = 0;
        movingPos = new Point(start.x, start.y);

        movementTimer = new Timer(MOVE_DELAY_MS, e -> {
            if (pathIndex >= path.size()) {
                stopMovement();
                return;
            }
            Node n = path.get(pathIndex++);
            movingPos = new Point(n.x, n.y);
            repaint();
        });
        movementTimer.start();
    }

    private void stopMovement() {
        if (movementTimer != null) {
            movementTimer.stop();
            movementTimer = null;
        }
        pathIndex = 0;
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

    public void runAllStarts() {
        if (goal == null) {
            System.out.println("[PATH] runAllStarts: goal is null");
            return;
        }
        for (int i = 0; i < starts.size(); i++) {
            System.out.println("[PATH] runAllStarts calling runPathForStart for index=" + i);
            runPathForStart(i);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g2);
        drawGrid(g2);
        drawBlocks(g2);
        drawStarts(g2);
        drawCoordinates(g2);
        drawStartGoal(g2);
        drawPath(g2);
        drawMovingRect(g2);
    }

    private void drawMovingRect(Graphics g) {
        if (movingPositions.isEmpty()) return;
        g.setColor(Color.MAGENTA);
        int s = tileSize / 4;
        int d = tileSize / 2;
        for (var entry : movingPositions.entrySet()) {
            int startIndex = entry.getKey();
            Point pos = entry.getValue();
            if (pos == null) continue;
            // 色を Start ごとに変えたい場合は配列やハッシュで色を選ぶ
            g.fillRect(pos.x * tileSize + s, pos.y * tileSize + s, d, d);
            // Start 番号を表示する（任意）
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(startIndex + 1), pos.x * tileSize + tileSize / 2 - 4, pos.y * tileSize + tileSize / 2 + 4);
            g.setColor(Color.MAGENTA);
        }
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

    // --- 追加: 複数 Start を番号付きで描画 ---
    private void drawStarts(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int s = tileSize / 6;
        for (int i = 0; i < starts.size(); i++) {
            Point p = starts.get(i);
            int x = p.x * tileSize;
            int y = p.y * tileSize;
            if (i == selectedStartIndex) {
                g2.setColor(Color.ORANGE);
                g2.fillRect(x + s, y + s, tileSize - 2 * s, tileSize - 2 * s);
            } else {
                g2.setColor(Color.GREEN);
                g2.fillOval(x + s, y + s, tileSize - 2 * s, tileSize - 2 * s);
            }
            g2.setColor(Color.BLACK);
            g2.drawString(String.valueOf(i + 1), x + tileSize / 2 - 4, y + tileSize / 2 + 4);
        }
        g2.dispose();
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
            // 既存の移動を止める
            stopMovement();
            int col = e.getX() / tileSize;
            int row = e.getY() / tileSize;
            if (col < 0 || col >= numCols || row < 0 || row >= numRows) return;

            switch (mode) {
                case SET_START -> {
                    // 左クリックで追加（Ctrl 押下で選択切替）
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (e.isControlDown()) {
                            // クリックした Start を選択
                            for (int i = 0; i < starts.size(); i++) {
                                Point p = starts.get(i);
                                if (p.x == col && p.y == row) {
                                    selectedStartIndex = i;
                                    repaint();
                                    return;
                                }
                            }
                        } else {
                            if (starts.size() < 10) {
                                starts.add(new Point(col, row));
                                selectedStartIndex = starts.size() - 1;
                                repaint();
                            } else {
                                System.out.println("最大10個の Start に達しています");
                            }
                        }
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        // 右クリックでそのタイルにある Start を削除
                        for (int i = 0; i < starts.size(); i++) {
                            Point p = starts.get(i);
                            if (p.x == col && p.y == row) {
                                starts.remove(i);
                                if (selectedStartIndex == i) selectedStartIndex = -1;
                                else if (selectedStartIndex > i) selectedStartIndex--;
                                repaint();
                                return;
                            }
                        }
                    }
                }
                case SET_GOAL -> {
                    goal = new Point(col, row);
                    repaint();
                }
                case SET_BLOCK -> {
                    blocks[row][col] = !blocks[row][col];
                    repaint();
                }
                default -> {
                }
            }
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


    public Map<Integer, List<Node>> getStartPaths() {
        return Collections.unmodifiableMap(startPaths);
    }

    public int getSelectedStartIndex() {
        return selectedStartIndex;
    }

    public int getStartsCount() {
        return starts.size();
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