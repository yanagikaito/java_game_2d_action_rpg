package environment;

import window.GameWindow;

import java.awt.*;

public class EnvironmentManager {

    GameWindow gameWindow;
    Lighting lighting;
    WorldTimeCycle timeCycle;
    private final int lightCircleSize = 250;

    public EnvironmentManager(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.timeCycle = new WorldTimeCycle(120.0);
    }

    public void setUp() {
        lighting = new Lighting(gameWindow, lightCircleSize);
    }

    /**
     * 毎フレーム呼ぶ。delta は秒（例: 1/60.0）。
     */

    public void update(double delta) {
        timeCycle.update(delta);
        float brightness = timeCycle.getBrightness();
        lighting.updateAndMaybeRecreate(lightCircleSize, brightness);
    }

    public void draw(Graphics2D g2) {
        lighting.draw(g2);
    }
}