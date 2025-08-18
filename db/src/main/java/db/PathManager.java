package db;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PathManager {

    /**
     * map_id, path_id を指定してステップ順に Point 一覧を取得
     */

    public static List<Point> loadPath(int mapId, int pathId) {
        List<Point> list = new ArrayList<>();
        String sql = "SELECT x, y FROM map_path " +
                "WHERE map_id=? AND path_id=? ORDER BY step";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mapId);
            ps.setInt(2, pathId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Point(rs.getInt("x"), rs.getInt("y")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}