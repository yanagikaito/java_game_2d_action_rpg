package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbManager {

    // H2 データベースのURL（data/mapdb.db に保存、AUTO_SERVERで複数接続可）
    private static final String JDBC_URL = "jdbc:h2:./data/mapdb;AUTO_SERVER=TRUE";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASS = "";

    /**
     * データベースのスキーマを初期化。
     * 既にテーブルが存在する場合は何もしない（IF NOT EXISTS）
     */
    public static void initSchema() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // --------------------------
            // 🌍 マップ関連テーブル
            // --------------------------

            // 各マップのタイルデータ
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS map_tile (
                      map_id INT,
                      x      INT,
                      y      INT,
                      tile_id INT,
                      PRIMARY KEY(map_id, x, y)
                    )""");

            // NPCやモンスターの移動パス
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS map_path (
                      map_id  INT NOT NULL,
                      path_id INT NOT NULL,
                      step    INT NOT NULL,
                      x       INT NOT NULL,
                      y       INT NOT NULL,
                      PRIMARY KEY(map_id, path_id, step)
                    )""");

            // --------------------------
            // 💾 セーブデータ関連テーブル
            // --------------------------

            // プレイヤーのメインセーブデータ
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS saves (
                        id INT PRIMARY KEY,
                        x INT NOT NULL,
                        y INT NOT NULL,
                        map_id INT,
                        hp INT NOT NULL,
                        max_hp INT NOT NULL,
                        mp INT NOT NULL,
                        max_mp INT NOT NULL,
                        level INT NOT NULL,
                        strength INT NOT NULL DEFAULT 1,
                        dexterity INT NOT NULL DEFAULT 1,
                        exp INT NOT NULL,
                        next_exp INT NOT NULL,
                        coin INT NOT NULL,
                        weapon_type_id INT,
                        shield_type_id INT,
                        saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            // インベントリアイテム（複数可）
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS inventory_items (
                        save_id INT,
                        slot INT,
                        type_id INT,
                        count INT,
                        FOREIGN KEY (save_id) REFERENCES saves(id)
                    )""");

            // 装備情報（武器・盾）
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS equipment (
                        save_id INT,
                        item_type VARCHAR(20), -- "weapon", "shield"
                        type_id INT,
                        FOREIGN KEY (save_id) REFERENCES saves(id)
                    )""");

            System.out.println("Database schema initialization completed successfully");
        } catch (SQLException e) {
            System.err.println("An error occurred during schema initialization");
            throw e;
        }
    }

    /**
     * データベース接続を取得
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
    }
}