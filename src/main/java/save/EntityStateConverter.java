package save;

import entity.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EntityStateConverter {

    public static SaveData toSaveData(int slot, Entity entity, LocalDateTime savedAt) {
        List<ItemSaveData> inventoryData = new ArrayList<>();

        for (int i = 0; i < entity.getInventory().size(); i++) {
            Entity item = entity.getInventory().get(i);
            if (item == null) continue;  // ← null スキップ

            // item.getType() も null チェック
            if (item.getType() == null) {
                System.err.println("Item type is null at slot " + i);
                continue;
            }

            int typeId = item.getType().typeId();
            if (typeId <= 0) continue;

            inventoryData.add(new ItemSaveData(i, typeId, item.getCount()));
        }

        int weaponId = -1;
        if (entity.getCurrentWeapon() != null && entity.getCurrentWeapon().getType() != null) {
            weaponId = entity.getCurrentWeapon().getType().typeId();
        }

        int shieldId = -1;
        if (entity.getCurrentShield() != null && entity.getCurrentShield().getType() != null) {
            shieldId = entity.getCurrentShield().getType().typeId();
        }

        return new SaveData(
                slot,
                entity.getWorldX(), entity.getWorldY(), entity.getMapId(),
                entity.getLife(), entity.getMaxLife(), entity.getMana(), entity.getMaxMana(),
                entity.getLevel(), entity.getStrength(), entity.getDexterity(), entity.getExp(),
                entity.getNextLevelExp(), entity.getCoin(),
                weaponId, shieldId,
                inventoryData,
                savedAt
        );
    }
}