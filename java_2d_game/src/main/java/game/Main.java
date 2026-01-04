package game;

import window.GameWindow;

import javax.swing.*;
import java.sql.SQLException;


public class Main {
    public static void main(String[] args) {

        try {
            db.DbManager.initSchema();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize the database");
        }

        SwingUtilities.invokeLater(() -> {
            GameWindow gameFrame = GameWindow.getInstance();
            gameFrame.frame();
        });
    }
}