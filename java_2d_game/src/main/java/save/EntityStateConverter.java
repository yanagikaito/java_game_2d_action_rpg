package save;

import entity.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EntityStateConverter {

    public static SaveData toSaveData(int slot, Entity entity) {
        List<ItemSaveData> inventoryData = new ArrayList<>();
        for (int i = 0; i < entity.getInventory().size(); i++) {
            Entity item = entity.getInventory().get(i);
            inventoryData.add(new ItemSaveData(i, item.getType().typeId(), 1));
        }

        return new SaveData(
                slot,
                entity.getWorldX(), entity.getWorldY(), entity.getMapId(),
                entity.getLife(), entity.getMaxLife(), entity.getMana(), entity.getMaxMana(),
                entity.getLevel(), entity.getStrength(), entity.getDexterity(), entity.getExp(),
                entity.getNextLevelExp(), entity.getCoin(),
                entity.getCurrentWeapon() != null ? entity.getCurrentWeapon().getType().typeId() : -1,
                entity.getCurrentShield() != null ? entity.getCurrentShield().getType().typeId() : -1,
                inventoryData,
                LocalDateTime.now()
        );
    }
}