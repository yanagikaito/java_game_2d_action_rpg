package entity;

import npc.NpcChicken;
import object.GameObject;

import java.util.ArrayList;
import java.util.List;

public class Coop extends GameObject {

    private int chickenCount = 0;
    private final int TARGET = 10;
    private boolean notified = false;
    private final List<QuestListener> listeners = new ArrayList<>();

    public Coop() {
        this.collision = true;
    }

    public synchronized void addChicken(NpcChicken chicken) {
        chickenCount++;
        System.out.println("[Coop] addChicken -> count=" + chickenCount);
        if (!notified && chickenCount >= TARGET) {
            notified = true;
            notifyTargetReached();
        }
    }

    public synchronized void removeChicken(NpcChicken chicken) {
        if (chickenCount > 0) chickenCount--;
        System.out.println("[Coop] removeChicken -> count=" + chickenCount);
    }

    private void notifyTargetReached() {
        for (QuestListener l : new ArrayList<>(listeners)) {
            try {
                l.onObjectiveReached("quest_fill_chicken_coop");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}