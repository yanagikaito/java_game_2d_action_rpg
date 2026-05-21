package save;

import db.DbManager;
import entity.*;
import factory.EntityFactory;
import player.Player;
import window.GameWindow;

import java.sql.*;

public class LoadManager {

    public static Entity loadPlayer(int slot, GameWindow gameWindow) {

        String sql = "SELECT * FROM saves WHERE id = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, slot);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) return null;

            Player player = new Player(gameWindow, gameWindow.getKeyHandler());
            player.setLoaded(true);

            player.getInventory().clear();
            player.setCurrentWeapon(null);
            player.setCurrentShield(null);

            // 基本ステータス設定
            player.setWorldX(rs.getInt("x"));
            player.setWorldY(rs.getInt("y"));
            player.setMapId(rs.getInt("map_id"));
            player.setLife(rs.getInt("hp"));
            player.setMaxLife(rs.getInt("max_hp"));
            player.setMana(rs.getInt("mp"));
            player.setMaxMana(rs.getInt("max_mp"));
            player.setLevel(rs.getInt("level"));
            player.setStrength(rs.getInt("strength"));
            player.setDexterity(rs.getInt("dexterity"));
            player.setExp(rs.getInt("exp"));
            player.setNextLevelExp(rs.getInt("next_exp"));
            player.setCoin(rs.getInt("coin"));

            // 2. インベントリ復元
            loadInventory(conn, slot, player, gameWindow);

            // 3. 装備復元
            loadEquipment(conn, slot, player, gameWindow);

            // 4. メタからプレイ時間を復元（SaveMeta は JSON ファイルに保存されている想定）
            try {
                int metaIndex = slot - 1; // SaveManager.loadMeta は 0-based を受ける
                SaveMeta meta = SaveManager.loadMeta(metaIndex);
                if (meta != null && meta.exists()) {
                    player.setPlayTimeSeconds(meta.getPlayTimeSeconds());
                    System.out.println("DEBUG: Restored playTimeSeconds=" + meta.getPlayTimeSeconds() + " for slot " + slot);
                } else {
                    // メタがない場合は DB 側に playTime を保存しているならそちらを使う（なければ 0 のまま）
                    System.out.println("DEBUG: No meta found for slot " + slot + ", playTime left as default");
                }
            } catch (Exception e) {
                System.err.println("Warning: failed to restore playTimeSeconds for slot " + slot + ": " + e.getMessage());
            }

            return player;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasSaveData(int slot) {

        String sql = "SELECT 1 FROM saves WHERE id = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, slot);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // データがあれば true
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void loadInventory(Connection conn, int saveId, Entity player, GameWindow gameWindow) {

        String sql = "SELECT * FROM inventory_items WHERE save_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saveId);
            ResultSet rs = pstmt.executeQuery();

            EntityFactory factory = new EntityFactory(gameWindow);

            while (rs.next()) {
                int typeId = rs.getInt("type_id");
                EntityType type = findEntityTypeById(typeId);
                if (type != null) {
                    Entity item = factory.create(type);
                    if (item != null) {
                        player.getInventory().add(item);
                    } else {
                        System.err.println("create failed: typeId=" + typeId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loadEquipment(Connection conn, int saveId, Entity player, GameWindow gameWindow) {

        String sql = "SELECT * FROM equipment WHERE save_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saveId);
            ResultSet rs = pstmt.executeQuery();

            EntityFactory factory = new EntityFactory(gameWindow);

            while (rs.next()) {
                String itemType = rs.getString("item_type");
                EntityType type = findEntityTypeById(rs.getInt("type_id"));
                if (type != null) {
                    Entity equipped = factory.create(type);
                    if (equipped != null) {
                        if ("weapon".equals(itemType)) {
                            player.setCurrentWeapon(equipped);
                        } else if ("shield".equals(itemType)) {
                            player.setCurrentShield(equipped);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // typeId → EntityType 変換
    private static EntityType findEntityTypeById(int typeId) {
        return switch (typeId) {
            case 0 -> new PlayerType();
            case 1 -> new NpcType();
            case 2 -> new MonsterType();
            case 3 -> new SwordType();
            case 4 -> new AxeType();
            case 5 -> new ShieldType();
            case 6 -> new RedPotionType();
            case 7 -> new GreenPotionType();
            case 8 -> new PickupOnlyType();
            case 9 -> new BombType();
            default -> null;
        };
    }
}