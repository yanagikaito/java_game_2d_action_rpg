package game;

import window.GameWindow;

import javax.swing.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        try {
            db.DbManager.initSchema();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        SwingUtilities.invokeLater(() -> {
            GameWindow gameFrame = GameWindow.getInstance();
            gameFrame.frame();
        });
    }
}