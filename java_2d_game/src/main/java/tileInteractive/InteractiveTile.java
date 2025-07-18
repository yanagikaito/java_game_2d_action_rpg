package tileInteractive;

import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

public class InteractiveTile extends Entity {

    private GameWindow gameWindow;
    private boolean destructible = false;
    private static final int SPRITE_ANIMATION_THRESHOLD = 20;

    public InteractiveTile(GameWindow gw, int row, int col) {
        super(gw);
        setWorldX(col * FrameApp.getTileSize());
        setWorldY(row * FrameApp.getTileSize());
    }

    public boolean isCorrectItem(Entity entity) {
        boolean isCorrectItem = false;
        return isCorrectItem;
    }

    public InteractiveTile createDestroyedForm() {
        InteractiveTile trunk = null;
        return trunk;
    }

    @Override
    public void update() {

        if (getInvincible()) {
            setInvincibleCounter(getInvincibleCounter() + 1);
            if (getInvincibleCounter() > SPRITE_ANIMATION_THRESHOLD) {
                setInvincible(false);
                setInvincibleCounter(0);
            }
        }
    }

    public boolean isDestructible() {
        return destructible;
    }

    public void setDestructible(boolean destructible) {
        this.destructible = destructible;
    }
}