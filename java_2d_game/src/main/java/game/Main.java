package game;

import window.GameWindow;
import window.Window;

import java.sql.SQLException;


public class Main {
    public static void main(String[] args) {

        try {
            db.DbManager.initSchema();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize the database");
        }

        Window gameFrame = GameWindow.getInstance();
        gameFrame.frame();
    }
}