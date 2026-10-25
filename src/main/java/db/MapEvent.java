package db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapEvent {
    private String id;
    private int x;
    private int y;
    private String name;
    private String trigger; // "interact","touch","auto"
    private int mapId;

    // 追加フィールド
    private List<String> dialogues;
    private boolean questOffered;
    private boolean questAccepted;
    private boolean questCompleted;
    private Map<String, Object> meta;

    // 引数なしコンストラクタ（Jackson 等のために残す）
    public MapEvent() {
        this.dialogues = new ArrayList<>();
        this.meta = new HashMap<>();
    }

    // 既存で使っている引数ありコンストラクタ
    public MapEvent(String id, int x, int y) {
        this();
        this.id = id;
        this.x = x;
        this.y = y;
    }

    // フルコンストラクタ（必要なら）
    public MapEvent(String id, int x, int y, String name, String trigger) {
        this(id, x, y);
        this.name = name;
        this.trigger = trigger;
    }

    // --- getters / setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public List<String> getDialogues() {
        if (dialogues == null) dialogues = new ArrayList<>();
        return dialogues;
    }

    public void setDialogues(List<String> dialogues) {
        this.dialogues = (dialogues == null) ? new ArrayList<>() : dialogues;
    }

    public boolean isQuestOffered() {
        return questOffered;
    }

    public void setQuestOffered(boolean questOffered) {
        this.questOffered = questOffered;
    }

    public boolean isQuestAccepted() {
        return questAccepted;
    }

    public void setQuestAccepted(boolean questAccepted) {
        this.questAccepted = questAccepted;
    }

    public boolean isQuestCompleted() {
        return questCompleted;
    }

    public void setQuestCompleted(boolean questCompleted) {
        this.questCompleted = questCompleted;
    }

    public Map<String, Object> getMeta() {
        if (meta == null) meta = new HashMap<>();
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = (meta == null) ? new HashMap<>() : meta;
    }

    @Override
    public String toString() {
        return "MapEvent{" +
                "id='" + id + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", name='" + name + '\'' +
                ", trigger='" + trigger + '\'' +
                ", dialogues=" + dialogues +
                ", questOffered=" + questOffered +
                ", questAccepted=" + questAccepted +
                ", questCompleted=" + questCompleted +
                ", meta=" + meta +
                '}';
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }
}