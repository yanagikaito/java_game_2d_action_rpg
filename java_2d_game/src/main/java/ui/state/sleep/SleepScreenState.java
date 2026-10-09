package ui.state.sleep;

import java.awt.*;

public interface SleepScreenState {

    void handleKey(int code);

    void draw(Graphics2D g2);

    default void update(double deltaSeconds) {
    }
}