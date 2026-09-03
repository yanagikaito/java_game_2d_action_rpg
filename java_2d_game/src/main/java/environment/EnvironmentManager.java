package environment;

import window.GameWindow;

import java.awt.*;

public class EnvironmentManager {

    GameWindow gameWindow;
    Lighting lighting;

    public EnvironmentManager(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void setUp() {

        lighting = new Lighting(gameWindow, 250);
    }

    public void draw(Graphics2D g2) {

        lighting.draw(g2);
    }
}