package entity.particle;

import java.awt.*;

public interface WaterEffect {
    void update(double dt);
    void draw(Graphics2D g2, int viewX, int viewY);
    boolean isAlive();
    void reset(); // プール用
}