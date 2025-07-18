package db;

import org.h2.tools.Server;

import javax.swing.SwingUtilities;

import frame.MainFrame;

public class TileDbMain {
    public static void main(String[] args) throws Exception {

        Server web = Server.createWebServer(
                "-web",
                "-webPort", "8082",
                "-ifNotExists"

        ).start();
        Server tcp = Server.createTcpServer(
                "-tcp",
                "-tcpPort", "9092",
                "-ifNotExists"
        ).start();
        System.out.println("H2 Web Console: " + web.getURL());
        System.out.println("H2 TCP Server Port: " + tcp.getPort());

        db.DbManager.initSchema();

        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}