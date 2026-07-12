package save;

import db.DbManager;
import entity.*;
import entity.type.*;
import factory.EntityFactory;
import player.Player;
import window.GameWindow;

import java.sql.*;
import java.util.List;

public class LoadManager {

    public static Entity loadPlayer(int slotNumber, GameWindow gameWindow) {

        String sql = "SELECT * FROM saves WHERE id = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, slotNumber);
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
            loadInventory(conn, slotNumber, player, gameWindow);

            // 3. 装備復元
            loadEquipment(conn, slotNumber, player, gameWindow);

            // 4. メタからプレイ時間を復元（SaveMeta は JSON ファイルに保存されている想定）
            try {
                // SaveManager.loadMeta は 0-based を受ける
                int metaIndex = slotNumber;
                SaveMeta meta = SaveManager.loadMeta(metaIndex);
                if (meta != null && meta.exists()) {
                    player.setPlayTimeSeconds(meta.getPlayTimeSeconds());
                    System.out.println("DEBUG: Restored playTimeSeconds="
                            + meta.getPlayTimeSeconds() + " for slotNumber " + slotNumber);
                } else {
                    // メタがない場合は DB 側に playTime を保存しているならそちらを使う（なければ 0 のまま）
                    System.out.println("DEBUG: No meta found for slotNumber "
                            + slotNumber + ", playTime left as default");
                }
            } catch (Exception e) {
                System.err.println("Warning: failed to restore playTimeSeconds for slotNumber "
                        + slotNumber + ": " + e.getMessage());
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
                // データがあれば true
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void loadInventory(Connection conn, int saveId, Entity player, GameWindow gameWindow) {
        String sql = "SELECT slot, type_id, count FROM inventory_items WHERE save_id = ? ORDER BY slot";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saveId);
            try (ResultSet rs = pstmt.executeQuery()) {
                EntityFactory factory = new EntityFactory(gameWindow);
                List<Entity> inv = player.getInventory();
                inv.clear();

                while (rs.next()) {
                    int slot = rs.getInt("slot");
                    int typeId = rs.getInt("type_id");

                    int count = rs.getInt("count");
                    if (rs.wasNull() || count <= 0) count = 1; // NULL や 0 の扱い

                    EntityType type = findEntityTypeById(typeId);
                    if (type == null) {
                        System.err.println("DEBUG: Unknown typeId=" + typeId);
                        continue;
                    }

                    Entity item = factory.create(type);
                    if (item == null) {
                        System.err.println("DEBUG: create failed: typeId=" + typeId);
                        continue;
                    }

                    // デバッグ: 生成直後の状態
                    System.out.println("DEBUG before setAmount: name=" + item.getName()
                            + " stackable=" + item.isStackable()
                            + " max=" + item.getMaxStack()
                            + " amount=" + item.getAmount()
                            + " dbCount=" + count);

                    // スタック上限を先に設定してから setAmount
                    if (item.isStackable()) {
                        item.setMaxStack(determineMaxStackForType(typeId));
                        item.setAmount(Math.max(1, Math.min(count, item.getMaxStack())));
                    }

                    // スロット復元（List のサイズを伸ばしてセット）
                    while (inv.size() <= slot) inv.add(null);
                    inv.set(slot, item);

                    System.out.println("DEBUG after setAmount: name=" + item.getName()
                            + " amount=" + item.getAmount() + " max=" + item.getMaxStack());
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

            List<Entity> inv = player.getInventory();
            EntityFactory factory = new EntityFactory(gameWindow);

            while (rs.next()) {
                String itemType = rs.getString("item_type");
                EntityType type = findEntityTypeById(rs.getInt("type_id"));
                Entity found = null;
                for (Entity e : inv) {
                    if (e == null) continue;
                    EntityType et = e.getType();
                    if (et != null && et.typeId() == type.typeId()) {
                        found = e;
                        break;
                    }
                }
                if (found != null) {
                    if ("weapon".equals(itemType)) {
                        player.setCurrentWeapon(found);
                    } else if ("shield".equals(itemType)) {
                        player.setCurrentShield(found);
                    }
                } else {
                    // フォールバック: インベントリに見つからない場合は factory で作って装備にセットする
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
            case 3 -> new ChickenType();
            case 4 -> new SwordType();
            case 5 -> new AxeType();
            case 6 -> new ShieldType();
            case 7 -> new RedPotionType();
            case 8 -> new GreenPotionType();
            case 9 -> new BluePotionType();
            case 10 -> new BombType();
            default -> null;
        };
    }

    private static int determineMaxStackForType(int typeId) {
        return switch (typeId) {
            case 7, 8, 9 -> 99;
            default -> 1;
        };
    }
}