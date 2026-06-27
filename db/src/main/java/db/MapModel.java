package db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * MapModel の修正版
 * - import の整理（DeserializationFeature を追加）
 * - loadEventsFromDb で events マップにも格納する（内部整合性）
 * - getAllEvents は allEvents を返すように統一
 */
public class MapModel {

    private final int width, height;
    private final int[][] tiles;
    // イベントは id -> MapEvent のマップと、順序保持用のリストを両方持つ
    private final Map<String, MapEvent> events = new HashMap<>();
    private final List<MapEvent> allEvents = new ArrayList<>();

    public MapModel(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new int[height][width];
    }

    // DB からロードして both structures を更新する
    public void loadEventsFromDb(int mapId) {
        events.clear();
        allEvents.clear();

        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String sql = "SELECT id, map_id, x, y, name, trigger_type, json FROM map_event WHERE map_id = ?";
        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mapId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MapEvent ev = new MapEvent();
                    ev.setId(rs.getString("id"));
                    ev.setMapId(rs.getInt("map_id"));
                    ev.setX(rs.getInt("x"));
                    ev.setY(rs.getInt("y"));
                    ev.setName(rs.getString("name"));
                    ev.setTrigger(rs.getString("trigger_type"));

                    // JSON の復元（CLOB 対応・UTF-8）
                    byte[] raw = rs.getBytes("json");
                    String json = null;
                    if (raw != null) {
                        json = new String(raw, StandardCharsets.UTF_8);
                    } else {
                        json = rs.getString("json"); // フォールバック
                    }

                    if (json != null && !json.isBlank()) {
                        try {
                            MapEvent parsed = om.readValue(json, MapEvent.class);
                            if (parsed.getDialogues() != null) ev.setDialogues(parsed.getDialogues());
                            if (parsed.getMeta() != null) ev.setMeta(parsed.getMeta());
                            // 必要なら他フィールドもコピー
                        } catch (Exception ex) {
                            System.err.println("Failed to parse json for event " + ev.getId() + ": " + ex.getMessage());
                        }
                    }

                    // 両方のコレクションに格納して内部整合性を保つ
                    events.put(ev.getId(), ev);
                    allEvents.add(ev);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTile(int x, int y, int id) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        tiles[y][x] = id;
    }

    public void addEvent(MapEvent e) {
        if (e == null || e.getId() == null) return;
        events.put(e.getId(), e);
        // 重複チェックしてから追加
        if (allEvents.stream().noneMatch(ev -> e.getId().equals(ev.getId()))) {
            allEvents.add(e);
        }
    }

    public MapEvent findEventAt(int tileX, int tileY) {
        for (MapEvent ev : allEvents) {
            if (ev == null) continue;
            if (ev.getX() == tileX && ev.getY() == tileY) {
                return ev;
            }
        }
        return null;
    }

    public MapEvent getEventById(String id) {
        if (id == null) return null;
        return events.get(id);
    }

    public void removeEvent(String id) {
        events.remove(id);
        allEvents.removeIf(ev -> id.equals(ev.getId()));
    }

    public MapEvent getEventAt(int x, int y) {
        return allEvents.stream()
                .filter(ev -> ev.getX() == x && ev.getY() == y)
                .findFirst().orElse(null);
    }

    // ここは events.values() ではなく allEvents を返す（順序が必要な場合に備える）
    public Collection<MapEvent> getAllEvents() {
        return Collections.unmodifiableList(allEvents);
    }

    public void clearEvents() {
        events.clear();
        allEvents.clear();
    }

    public int getTile(int x, int y) {
        return tiles[y][x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}