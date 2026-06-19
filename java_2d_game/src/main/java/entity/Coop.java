package entity;

import npc.NpcChicken;
import npc.NpcMalonyChicken;
import object.GameObject;
import window.GameWindow;

public class Coop extends GameObject {

    private final java.util.List<NpcChicken> chickens = new java.util.ArrayList<>();

    public Coop() {
        this.collision = true;
    }

    public void addChicken(NpcChicken chicken) {
        if (chicken == null) return;
        if (chickens.add(chicken)) {
            try {
                chicken.setInCoop(true);
            } catch (Throwable ignored) {
            }

            int count = chickens.size();
            chicken.getGameWindow().getUi().addMessage("[Coop] addChicken -> count=" + count);

            // Malony に即時通知
            try {
                GameWindow gw = chicken.getGameWindow();
                if (gw != null) {
                    Entity[] monsters = gw.getObj();
                    if (monsters != null) {
                        for (Entity e : monsters) {
                            if (e instanceof NpcMalonyChicken) {
                                ((NpcMalonyChicken) e).checkCoopObjective();
                                break;
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            System.out.println("[Coop] addChicken: already present");
        }
    }

    public int getChickenCount() {
        return chickens.size();
    }
}