package entity.loot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import entity.Entity;
import object.*;
import factory.EntityFactory;
import window.GameWindow;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class LootTableLoader {

    private static final Random random = new Random();

    /**
     * JSONファイルからLootテーブルを読み込み、リストを構築する
     */

    public static List<LootEntry> loadTable(GameWindow gameWindow, String resourcePath) {
        
        List<LootEntry> lootTable = new ArrayList<>();
        EntityFactory entityFactory = gameWindow.getEntityFactory();

        try (InputStream is = LootTableLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                addDefaultTable(lootTable, entityFactory);
                return lootTable;
            }
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Type listType = new TypeToken<List<LootConfigEntry>>() {
            }.getType();
            List<LootConfigEntry> configs = new Gson().fromJson(reader, listType);

            if (configs == null || configs.isEmpty()) {
                addDefaultTable(lootTable, entityFactory);
                return lootTable;
            }

            for (LootConfigEntry c : configs) {
                if (c == null || c.weight <= 0 || c.type == null) continue;

                Supplier<Entity> factory = switch (c.type) {
                    case "vert_bronze" -> () -> {
                        Entity e = entityFactory.createBronzeVertEntity();
                        if (e instanceof ObjVertBronze vert && c.amount > 0) vert.setValue(c.amount);
                        return e;
                    };
                    case "vert_green" -> () -> {
                        Entity e = entityFactory.createGreenVertEntity();
                        if (e instanceof ObjVertGreen vert && c.amount > 0) vert.setValue(c.amount);
                        return e;
                    };
                    case "vert_blue" -> () -> {
                        Entity e = entityFactory.createBlueVertEntity();
                        if (e instanceof ObjVertBlue vert && c.amount > 0) vert.setValue(c.amount);
                        return e;
                    };
                    case "vert_red" -> () -> {
                        Entity e = entityFactory.createRedVertEntity();
                        if (e instanceof ObjVertRed vert && c.amount > 0) vert.setValue(c.amount);
                        return e;
                    };
                    case "vert_silver" -> () -> {
                        Entity e = entityFactory.createSilverVertEntity();
                        if (e instanceof ObjVertSilver vert && c.amount > 0) vert.setValue(c.amount);
                        return e;
                    };
                    case "vert_gold" -> () -> {
                        Entity e = entityFactory.createGoldVertEntity();
                        if (e instanceof ObjVertGold vert && c.amount > 0) vert.setValue(c.amount);
                        return e;
                    };
                    case "red_potion" -> () -> entityFactory.createRedPotionEntity();
                    case "green_potion" -> () -> entityFactory.createGreenPotionEntity();
                    case "blue_potion" -> () -> entityFactory.createBluePotionEntity();
                    default -> () -> null;
                };
                lootTable.add(new LootEntry(factory, c.weight));
            }
        } catch (Exception e) {
            e.printStackTrace();
            addDefaultTable(lootTable, entityFactory);
        }
        return lootTable;
    }

    /**
     * デフォルトのフォールバックテーブルを追加
     */

    private static void addDefaultTable(List<LootEntry> lootTable, EntityFactory entityFactory) {
        lootTable.add(new LootEntry(() -> entityFactory.createBronzeVertEntity(), 30));
        lootTable.add(new LootEntry(() -> entityFactory.createGreenVertEntity(), 20));
        lootTable.add(new LootEntry(() -> entityFactory.createBlueVertEntity(), 15));
        lootTable.add(new LootEntry(() -> entityFactory.createRedVertEntity(), 8));
        lootTable.add(new LootEntry(() -> entityFactory.createSilverVertEntity(), 4));
        lootTable.add(new LootEntry(() -> entityFactory.createGoldVertEntity(), 1));
        lootTable.add(new LootEntry(() -> entityFactory.createRedPotionEntity(), 10));
        lootTable.add(new LootEntry(() -> entityFactory.createGreenPotionEntity(), 8));
        lootTable.add(new LootEntry(() -> entityFactory.createBluePotionEntity(), 5));
    }

    /**
     * 構築されたテーブルから重み付きランダムで1つ選んで返す
     */

    public static Entity rollRandomLoot(List<LootEntry> lootTable) {
        if (lootTable == null || lootTable.isEmpty()) return null;
        int total = lootTable.stream().mapToInt(LootEntry::weight).sum();
        if (total <= 0) return null;
        int pick = random.nextInt(total);
        int acc = 0;
        for (LootEntry e : lootTable) {
            acc += e.weight();
            if (pick < acc) {
                try {
                    return e.factory().get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }
}