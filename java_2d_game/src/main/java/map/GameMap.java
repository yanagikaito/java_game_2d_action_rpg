// map/GameMap.java
package map;

import entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameMap {

    private final List<Entity> objects = new ArrayList<>();

    public void addObject(Entity e) {
        if (e == null) return;
        objects.add(e);
        sortObjectsByY();
    }

    // 描画順ソート（Y座標順）
    private void sortObjectsByY() {
        objects.sort(Comparator.comparingInt(Entity::getWorldY));
    }
}