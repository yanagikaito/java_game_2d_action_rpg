package ui;

import java.awt.*;

public interface TradeScreenState {

    void handleKey(int code);

    void draw(Graphics2D g2);
}
