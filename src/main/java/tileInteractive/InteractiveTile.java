package tileInteractive;

import entity.Entity;
import window.GameWindow;

public class InteractiveTile extends Entity {

    private GameWindow gameWindow;
    private boolean destructible = false;

    public InteractiveTile(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
    }

    public boolean isCorrectItem(Entity entity) {
        boolean isCorrectItem = false;
        return isCorrectItem;
    }

    public void update() {

    }

    public boolean isDestructible() {
        return destructible;
    }

    public void setDestructible(boolean destructible) {
        this.destructible = destructible;
    }
}
