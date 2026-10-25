package save;

public class SaveMeta {

    // セーブスロットに有効なセーブデータが存在するかどうか
    private boolean exists;
    private int hp;
    private int maxHp;
    private String facing;
    private String spriteKey;
    private long savedAt;
    private long playTimeSeconds = 0L;

    // デフォルトコンストラクタ（Gson 用）
    public SaveMeta() {
    }

    public SaveMeta(boolean exists, int hp, int maxHp, String facing, String spriteKey, long savedAt) {
        this.exists = exists;
        this.hp = hp;
        this.maxHp = maxHp;
        this.facing = facing;
        this.spriteKey = spriteKey;
        this.savedAt = savedAt;
    }

    public boolean exists() {
        return exists;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public String getFacing() {
        return facing;
    }

    public long getSavedAt() {
        return savedAt;
    }

    public String getSpriteKey() {
        return spriteKey;
    }

    public long getPlayTimeSeconds() {
        return playTimeSeconds;
    }

    public void setPlayTimeSeconds(long playTimeSeconds) {
        this.playTimeSeconds = playTimeSeconds;
    }
}