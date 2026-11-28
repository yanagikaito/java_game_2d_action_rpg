package save;

import entity.Entity;
import player.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntityStateConverter {

    private static final int FLAG_GREEN_GOBLIN = 1;
    private static final int FLAG_CRAB_GUARDIAN = 2;

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

        int obtainedBossKeysFlags = toObtainedBossKeysFlags(entity);

        return new SaveData(
                slot,
                entity.getWorldX(), entity.getWorldY(), entity.getMapId(),
                entity.getLife(), entity.getMaxLife(), entity.getMana(), entity.getMaxMana(),
                entity.getLevel(), entity.getStrength(), entity.getDexterity(), entity.getExp(),
                entity.getNextLevelExp(), entity.getCoin(), entity.getGreenForceFragmentCount(),
                obtainedBossKeysFlags, weaponId, shieldId,
                inventoryData,
                savedAt
        );
    }

    private static int toObtainedBossKeysFlags(Entity entity) {
        if (!(entity instanceof Player player)) {
            return 0;
        }
        Set<String> keys = player.getObtainedFragmentBossKeys();
        if (keys == null || keys.isEmpty()) {
            return 0;
        }

        int flags = 0;
        if (keys.contains("entity.type.GreenGoblinType")) {
            flags |= FLAG_GREEN_GOBLIN;
        }
        if (keys.contains("entity.type.CrabGuardianType")) {
            flags |= FLAG_CRAB_GUARDIAN;
        }
        return flags;
    }
}