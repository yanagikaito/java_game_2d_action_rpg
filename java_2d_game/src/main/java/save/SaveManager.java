package save;

import entity.Entity;

import javax.swing.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class SaveManager {

    static final String DB_URL = "jdbc:h2:./data/mapdb;AUTO_SERVER=TRUE";

    public static void saveGame(int slot, Entity entity) {
        // セーブ時刻をここで生成（ロード時にnullになるのを防ぐ）
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
            conn.commit();
            System.out.println("Game saved : slot " + slot);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "セーブに失敗しました: " + e.getMessage());

        } finally {
            // コネクションを確実に閉じる
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
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
}