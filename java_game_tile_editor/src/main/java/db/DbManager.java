package db;

import java.sql.*;

public class DbManager {

    private static final String URL = "jdbc:h2:./data/tilemap";
    private static final String USER = "sa", PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void initSchema() {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS map (id INT AUTO_INCREMENT, name VARCHAR, PRIMARY KEY(id))");
            st.execute("CREATE TABLE IF NOT EXISTS tile (id INT PRIMARY KEY, image_path VARCHAR)");
            st.execute("CREATE TABLE IF NOT EXISTS map_tile (map_id INT, x INT, y INT, tile_id INT, PRIMARY KEY(map_id,x,y))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}