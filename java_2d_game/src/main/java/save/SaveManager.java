package save;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import entity.Entity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class SaveManager {

//    public static void main(String[] args) {
//        int idToDelete = 0; // デフォルトで id=3 を削除
//        if (args.length > 0) {
//            try {
//                idToDelete = Integer.parseInt(args[0]);
//            } catch (NumberFormatException e) {
//                System.err.println("Invalid id argument, using default 3");
//            }
//        }
//
//        System.out.println("Make sure you backed up ./data/mapdb.mv.db before proceeding.");
//        boolean ok = deleteSaveById(idToDelete);
//        System.out.println("deleteSaveById(" + idToDelete + ") returned: " + ok);
//    }

    // DB 関連
    static final String DB_URL = "jdbc:h2:./data/mapdb;AUTO_SERVER=TRUE";

    private static final int SLOT_COUNT = 3;
    private static final SaveMeta[] metas = new SaveMeta[SLOT_COUNT];

    // JSON メタ関連
    private static final String SAVE_DIR = "saves";
    private static final String SLOT_PREFIX = "slot";
    private static final String SUFFIX = ".json";
    private static final Gson gson = new Gson();

    public static boolean saveGame(int slot, Entity entity) {
        System.out.println("DEBUG: SaveManager.saveGame called slot=" + slot + " thread=" + Thread.currentThread().getName());
        boolean result = false;
        LocalDateTime now = LocalDateTime.now();
        SaveData data = EntityStateConverter.toSaveData(slot, entity, now);

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, "sa", "");
            conn.setAutoCommit(false);  // トランザクション開始

            // 1. メインデータ保存
            saveMainData(conn, data);

            // 2. インベントリ削除 → 挿入
            clearInventory(conn, slot);
            saveInventory(conn, slot, data.inventory());

            // 3. 装備保存
            saveEquipment(conn, slot, data);

            // コミット
            conn.commit();
            System.out.println("Game saved : slot " + slot);

            // 既存のメタ生成箇所（抜粋）
            SaveMeta meta = new SaveMeta(true, data.hp(), data.maxHp(),
                    entity.getFacing(), entity.getSpriteKey(), System.currentTimeMillis());

            // ここでプレイ時間をセットする（Player 型チェック）
            try {
                // Player クラスのパッケージに合わせて import してください
                if (entity instanceof player.Player) {
                    long playSeconds = ((player.Player) entity).getPlayTimeSeconds();
                    meta.setPlayTimeSeconds(playSeconds);
                    System.out.println("DEBUG: SaveManager - meta.playTimeSeconds = " + playSeconds + " for slot " + slot);
                } else {
                    // entity が Player でない場合は 0 のままにする（安全策）
                    meta.setPlayTimeSeconds(0L);
                }
            } catch (NoClassDefFoundError | Exception e) {
                // 万が一 Player クラスが見つからない等の問題があってもセーブ自体は続行する
                meta.setPlayTimeSeconds(0L);
                System.err.println("Warning: failed to set playTimeSeconds on SaveMeta: " + e.getMessage());
            }

            // writeMeta を呼ぶ（既存）
            writeMeta(slot, meta);


            // すべて成功したら true を返す
            result = true;

        } catch (SQLException e) {
            // DB エラー時はロールバック
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            // UI は EDT で表示する
            javax.swing.SwingUtilities.invokeLater(() ->
                    javax.swing.JOptionPane.showMessageDialog(null, "セーブに失敗しました: " + e.getMessage())
            );
        } catch (Exception e) {
            // writeMeta 等のその他例外を捕捉
            e.printStackTrace();
            javax.swing.SwingUtilities.invokeLater(() ->
                    javax.swing.JOptionPane.showMessageDialog(null, "セーブに失敗しました: " + e.getMessage())
            );
            // 必要ならここで conn.rollback() も試みる
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("DEBUG: SaveManager.saveGame finished slot=" + slot + " result=" + result);
        return result;
    }

    private static void saveMainData(Connection conn, SaveData data) throws SQLException {
        String sql = """
                MERGE INTO saves (
                    id, x, y, map_id, hp, max_hp, mp, max_mp,
                    level, strength, dexterity, exp, next_exp, coin,
                    weapon_type_id, shield_type_id, saved_at
                )
                KEY (id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, data.id());
            pstmt.setInt(2, data.x());
            pstmt.setInt(3, data.y());
            pstmt.setInt(4, data.mapId());
            pstmt.setInt(5, data.hp());
            pstmt.setInt(6, data.maxHp());
            pstmt.setInt(7, data.mana());
            pstmt.setInt(8, data.maxMana());
            pstmt.setInt(9, data.level());
            pstmt.setInt(10, data.strength());
            pstmt.setInt(11, data.dexterity());
            pstmt.setInt(12, data.exp());
            pstmt.setInt(13, data.next_exp());
            pstmt.setInt(14, data.coin());
            pstmt.setInt(15, data.weaponTypeId());
            pstmt.setInt(16, data.shieldTypeId());
            pstmt.setTimestamp(17, Timestamp.valueOf(data.savedAt()));
            pstmt.executeUpdate();
        }
    }

    private static void clearInventory(Connection conn, int saveId) throws SQLException {
        String sql = "DELETE FROM inventory_items WHERE save_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saveId);
            pstmt.executeUpdate();
        }
    }

    private static void saveInventory(Connection conn, int saveId, List<ItemSaveData> items) throws SQLException {

        String sql = "INSERT INTO inventory_items (save_id, slot, type_id, count) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (ItemSaveData item : items) {
                if (item.typeId() <= 0) continue;
                pstmt.setInt(1, saveId);
                pstmt.setInt(2, item.slot());
                pstmt.setInt(3, item.typeId());
                pstmt.setInt(4, item.count());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private static void saveEquipment(Connection conn, int saveId, SaveData data) throws SQLException {
        // 武器と盾を別々に処理（PreparedStatementを安全に）
        saveEquipmentItem(conn, saveId, "weapon", data.weaponTypeId());
        saveEquipmentItem(conn, saveId, "shield", data.shieldTypeId());
    }

    // 共通化：装備アイテムを安全に保存
    private static void saveEquipmentItem(Connection conn, int saveId, String itemType, int typeId) throws SQLException {
        if (typeId == -1) return;

        String sql = "MERGE INTO equipment (save_id, item_type, type_id) KEY (save_id, item_type) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saveId);
            pstmt.setString(2, itemType);
            pstmt.setInt(3, typeId);
            pstmt.executeUpdate();
        }
    }

    // --- JSON メタ読み書き ---
    public static SaveMeta loadMeta(int slotIndex) {
        File dir = new File(SAVE_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return new SaveMeta(false, 0, 0, "player_basic", "down", 0L);
        }
        File file = new File(dir, SLOT_PREFIX + slotIndex + SUFFIX);
        if (!file.exists() || !file.isFile()) {
            return new SaveMeta(false, 0, 0, "player_basic", "down", 0L);
        }
        try (FileReader fr = new FileReader(file)) {
            SaveMeta meta = gson.fromJson(fr, SaveMeta.class);
            if (meta == null) {
                return new SaveMeta(false, 0, 0, "player_basic", "down", 0L);
            }
            return meta;
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Failed to read meta for slot " + slotIndex + ": " + e.getMessage());
            return new SaveMeta(false, 0, 0, "down", "player_basic", 0L);
        }
    }

    public static boolean writeMeta(int slotIndex, SaveMeta meta) {

        try {
            Path dir = Path.of(SAVE_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path target = dir.resolve(SLOT_PREFIX + slotIndex + SUFFIX);
            Path tmp = dir.resolve(SLOT_PREFIX + slotIndex + SUFFIX + ".tmp");
            try (BufferedWriter bw = Files.newBufferedWriter(tmp)) {
                gson.toJson(meta, bw);
            }
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            System.out.println("DEBUG: atomic writeMeta path=" + target.toAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hasSave(int slotNumber) {
        try {
            int idx = slotNumber;
            if (idx < 0 || idx >= SLOT_COUNT) return false;

            // metas が初期化済みならそれを優先
            SaveMeta meta = metas[idx];
            if (meta != null) {
                return meta.exists();
            }

            // フォールバック: ファイルを直接チェック（loadMeta を使う）
            SaveMeta diskMeta = loadMeta(idx);
            return diskMeta != null && diskMeta.exists();
        } catch (Exception e) {
            System.err.println("hasSave check failed for slot " + slotNumber + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteSaveById(int id) {
        String delInv = "DELETE FROM inventory_items WHERE save_id = ?";
        String delEquip = "DELETE FROM equipment WHERE save_id = ?";
        String delSave = "DELETE FROM saves WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, "sa", "")) {
            conn.setAutoCommit(false);
            try (PreparedStatement psInv = conn.prepareStatement(delInv);
                 PreparedStatement psEquip = conn.prepareStatement(delEquip);
                 PreparedStatement psSave = conn.prepareStatement(delSave)) {

                psInv.setInt(1, id);
                psInv.executeUpdate();

                psEquip.setInt(1, id);
                psEquip.executeUpdate();

                psSave.setInt(1, id);
                int affected = psSave.executeUpdate();

                conn.commit();
                System.out.println("DEBUG: deleteSaveById id=" + id + " deleted saves rows=" + affected);
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}