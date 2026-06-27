import java.io.File;
import java.sql.*;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class DbTest {
    public static void main(String[] args) {

        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = "jdbc:h2:./data/mapdb;AUTO_SERVER=TRUE";
        String user = "sa";
        String password = "";

        System.out.println("Working dir: " + new File(".").getAbsolutePath());

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to H2 database.");

            // 1) saves テーブルのダンプ
            System.out.println("\n=== saves table ===");
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, x, y, map_id, hp, coin, saved_at FROM saves")) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("id=%d map_id=%s pos=(%d,%d) hp=%d coin=%d saved_at=%s%n",
                            rs.getInt("id"),
                            rs.getObject("map_id"),
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("hp"),
                            rs.getInt("coin"),
                            rs.getTimestamp("saved_at"));
                }
                if (!any) System.out.println(" (no rows in saves)");
            } catch (SQLException e) {
                System.out.println("Could not read saves table: " + e.getMessage());
            }

            // map_event テーブルのダンプ（json カラムを含める）
            System.out.println("\n=== map_event table ===");
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT id, map_id, x, y, name, trigger_type, created_at, json FROM map_event ORDER BY created_at DESC")) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    String id = rs.getString("id");
                    int mapId = rs.getInt("map_id");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    String name = rs.getString("name");
                    String trigger = rs.getString("trigger_type");
                    Timestamp created = rs.getTimestamp("created_at");
                    String json = rs.getString("json");
                    String preview = json == null ? "null" : (json.length() > 200 ? json.substring(0, 200) + "..." : json);
                    System.out.printf("id=%s map=%d pos=(%d,%d) name=%s trigger=%s created=%s json_preview=%s%n",
                            id, mapId, x, y, name, trigger, created, preview);
                }
                if (!any) System.out.println(" (no rows in map_event)");
            } catch (SQLException e) {
                System.out.println("Could not read map_event table: " + e.getMessage());
            }

            // 指定イベントID をチェックする（json と dialogues を詳しく表示）
            String checkId = (args.length > 0) ? args[0] : null;
            if (checkId != null) {
                System.out.println("\n=== check event id: " + checkId + " ===");
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, map_id, x, y, name, trigger_type, json FROM map_event WHERE id = ?")) {
                    ps.setString(1, checkId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String id = rs.getString("id");
                            int mapId = rs.getInt("map_id");
                            int x = rs.getInt("x");
                            int y = rs.getInt("y");
                            String name = rs.getString("name");
                            String trigger = rs.getString("trigger_type");
                            String json = rs.getString("json");

                            System.out.printf("Found: id=%s map=%d pos=(%d,%d) name=%s trigger=%s%n",
                                    id, mapId, x, y, name, trigger);

                            // ← ここに追加するコード
                            byte[] raw = rs.getBytes("json");
                            System.out.println("raw bytes length=" + (raw == null ? "null" : raw.length));
                            if (raw != null) {
                                StringBuilder sb = new StringBuilder();
                                for (int j = 0; j < Math.min(raw.length, 200); j++) {
                                    sb.append(String.format("%02X ", raw[j]));
                                }
                                System.out.println("raw bytes hex preview: " + sb.toString());
                                String asUtf8 = new String(raw, java.nio.charset.StandardCharsets.UTF_8);
                                System.out.println("decoded as UTF-8 preview: " + (asUtf8.length() > 1000 ? asUtf8.substring(0, 1000) + "..." : asUtf8));
                            }

                            // raw json 出力（長ければプレビュー）
                            if (json == null) {
                                System.out.println("json: null");
                            } else {
                                System.out.println("raw json length=" + json.length());
                                System.out.println("raw json preview=" + (json.length() > 1000 ? json.substring(0, 1000) + "..." : json));
                                // 簡易的に dialogues 配列の中身を抜き出して表示
                                String[] dialogues = extractDialogues(json);
                                if (dialogues == null) {
                                    System.out.println("dialogues: (not found or could not parse)");
                                } else if (dialogues.length == 0) {
                                    System.out.println("dialogues: (empty array)");
                                } else {
                                    for (int i = 0; i < dialogues.length; i++) {
                                        System.out.println("dialogue[" + i + "] = " + dialogues[i]);
                                    }
                                }
                            }
                        } else {
                            System.out.println("Not found: " + checkId);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * json 中の "dialogues": [ ... ] を簡易抽出して文字列配列で返す。
     * 外部ライブラリ不要の簡易実装。厳密な JSON パーサではないため
     * 複雑なネストやエスケープに弱いが、デバッグ用途には十分。
     */
    private static String[] extractDialogues(String json) {
        if (json == null) return null;
        String key = "\"dialogues\"";
        int k = json.indexOf(key);
        if (k < 0) return null;
        int arrStart = json.indexOf('[', k);
        if (arrStart < 0) return null;
        // find matching closing bracket for the array (handle nested brackets roughly)
        int depth = 0;
        int i = arrStart;
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) break;
            }
        }
        if (i >= json.length()) return null;
        String arrText = json.substring(arrStart + 1, i).trim(); // inside [...]
        // split by commas that are not inside quotes
        java.util.List<String> items = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int j = 0; j < arrText.length(); j++) {
            char c = arrText.charAt(j);
            if (c == '"' && (j == 0 || arrText.charAt(j - 1) != '\\')) {
                inQuotes = !inQuotes;
                cur.append(c);
            } else if (c == ',' && !inQuotes) {
                String s = cur.toString().trim();
                if (!s.isEmpty()) items.add(stripQuotes(s));
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        String last = cur.toString().trim();
        if (!last.isEmpty()) items.add(stripQuotes(last));
        return items.toArray(new String[0]);
    }

    private static String stripQuotes(String s) {
        s = s.trim();
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            // unescape simple sequences
            String inner = s.substring(1, s.length() - 1);
            inner = inner.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\");
            return inner;
        }
        return s;
    }
}