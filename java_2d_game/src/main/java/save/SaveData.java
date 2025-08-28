package save;

import java.time.LocalDateTime;
import java.util.List;

public record SaveData(
        int id,
        int x, int y, int mapId,
        int hp, int maxHp, int mana, int maxMana,
        int level, int strength, int dexterity, int exp, int next_exp, int coin,
        int weaponTypeId, int shieldTypeId,
        List<ItemSaveData> inventory,
        LocalDateTime savedAt
) {
}