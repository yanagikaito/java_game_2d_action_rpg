package environment;

import entity.Entity;
import window.GameWindow;

import java.awt.*;

public class EnvironmentManager {

    GameWindow gameWindow;
    Lighting lighting;
    WorldTimeCycle timeCycle;
    private final int lightCircleSize = 250;

    public EnvironmentManager(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.timeCycle = new WorldTimeCycle(60.0);
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

        Entity currentLight = gameWindow.getPlayer().getCurrentLight();
        boolean hasLantern = currentLight != null && currentLight.isLightSource();
        int lanternRadius = hasLantern ? currentLight.getLightRadius() : 0;
        float lanternIntensity = hasLantern ? currentLight.getLightIntensity() : 0f;

        lighting.updateAndMaybeRecreate(lightCircleSize, brightness, hasLantern, lanternRadius, lanternIntensity);
    }


    public void draw(Graphics2D g2) {
        lighting.draw(g2);
    }
}