package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbManager {

    private static final String JDBC_URL = "jdbc:h2:./data/mapdb;AUTO_SERVER=TRUE";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASS = "";

    public static void initSchema() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 既存の map_tile テーブル
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS map_tile (" +
                            " map_id INT, x INT, y INT, tile_id INT, " +
                            " PRIMARY KEY(map_id, x, y) )"
            );

            // ← ここで map_path テーブルを追加
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS map_path (" +
                            " map_id   INT NOT NULL, " +
                            " path_id  INT NOT NULL, " +
                            " step     INT NOT NULL, " +
                            " x        INT NOT NULL, " +
                            " y        INT NOT NULL, " +
                            " PRIMARY KEY(map_id, path_id, step) )"
            );
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
    }
}