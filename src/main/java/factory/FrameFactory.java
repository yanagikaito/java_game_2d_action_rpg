package factory;

import frame.FrameSize;
import frame.GameFrame;
import window.GameWindow;

import javax.swing.*;

public class FrameFactory {

    public static GameFrame createFrame(FrameSize size, GameWindow gameWindow) {
        return () -> {
            JFrame frame = new JFrame();
            frame.setResizable(false);
            frame.setSize(size.width(), size.height());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("2D Action RPG");
            frame.add(gameWindow);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        };
    }
}