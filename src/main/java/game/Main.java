package game;

import window.GameWindow;
import window.Window;


public class Main {
    public static void main(String[] args) {
        Window gameFrame = GameWindow.getInstance();
        gameFrame.frame();
    }
}