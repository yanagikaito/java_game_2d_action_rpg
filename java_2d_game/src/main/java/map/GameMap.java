// map/GameMap.java
package map;

import entity.Entity;
import npc.NpcChicken;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameMap {

    private final List<Entity> objects = new ArrayList<>();

    // --- swarm 管理用フィールド ---
    public static final int GLOBAL_MAX_CHICKENS = 50;
    public static final int SWARM_SPAWN_COUNT = 20;

    public GameMap() {
    }

    // 既存の addObject を残しつつ、重複チェックを行う addNpc を用意
    public void addObject(Entity e) {
        if (e == null) return;
        objects.add(e);
        sortObjectsByY();
    }

    /**
     * NPC を追加（重複チェックを行う）
     */

    public void addNpc(Entity e) {
        if (e == null) return;
        if (objects.contains(e)) return; // 同一参照の重複追加を防ぐ
        objects.add(e);
        sortObjectsByY();
    }

    // 描画順ソート（Y座標順）
    private void sortObjectsByY() {
        objects.sort(Comparator.comparingInt(Entity::getWorldY));
    }

    /**
     * 指定クラスのエンティティ数を数える汎用メソッド
     */
    public int countEntitiesOfType(Class<?> cls) {
        int count = 0;
        for (Entity e : objects) {
            if (cls.isInstance(e)) count++;
        }
        return count;
    }

    /**
     * NpcChicken の数を数える簡易メソッド（呼び出し側が分かりやすいように）
     */

    public int countChickens() {
        return countEntitiesOfType(NpcChicken.class);
    }

    /**
     * 指定ワールド座標に NPC を置けるかの簡易チェック
     * （衝突判定の実装に合わせて調整してください）
     */

    public boolean canPlaceNpcAt(int worldX, int worldY, int width, int height) {
        Rectangle r = new Rectangle(worldX, worldY, width, height);
        for (Entity e : objects) {
            // 自分自身や非衝突オブジェクトはスキップするロジックが必要なら追加
            Rectangle er = new Rectangle(e.getWorldX(), e.getWorldY(), e.getWidth(), e.getHeight());
            if (r.intersects(er)) return false;
        }
        // ここでタイルの通行判定やマップ境界チェックを行う（必要なら実装）
        return true;
    }

    /**
     * objects の参照を返す（読み取り専用で使う場合）
     */

    public Entity[] getObjects() {
        return objects.toArray(new Entity[0]);
    }
}