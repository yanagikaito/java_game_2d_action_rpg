import java.sql.*;

public class DbTest {
    public static void main(String[] args) {

        // TCP経由のJDBC URL
        String url = "jdbc:h2:./data/mapdb;AUTO_SERVER=TRUE";
        String user = "sa";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Successfully connected to H2 database via TCP!");

            // savesテーブルのデータを取得
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM saves");

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("X: " + rs.getInt("x"));
                System.out.println("Y: " + rs.getInt("y"));
                System.out.println("HP: " + rs.getInt("hp"));
                System.out.println("Coin: " + rs.getInt("coin"));
                System.out.println("Saved At: " + rs.getTimestamp("saved_at"));
                System.out.println("---");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}