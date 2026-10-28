package object;

import entity.Entity;
import entity.type.DoorType;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjDoor extends Entity {

    private GameWindow gameWindow;
    private boolean isOpen = false; // 開閉状態を管理するフラグ

    public ObjDoor(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;

        setType(new DoorType());
        setName("扉");

        // 1. 最初は通行不可（当たり判定あり）にする
        setCollision(true);
        try {
            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("object/door_plant_primeval.gif"));
            setImage(raw, FrameApp.getTileSize());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 2. プレイヤーが衝突（接触）したときに呼び出されるメソッド
    // プレイヤーが衝突したときに呼び出されるメソッド
    @Override
    public void update() {

        int distanceX = Math.abs(getWorldX() - gameWindow.getPlayer().getWorldX());
        int distanceY = Math.abs(getWorldY() - gameWindow.getPlayer().getWorldY());
        int threshold = FrameApp.getTileSize(); // 例：3タイル分以上離れたら

        if (isOpen) {
            return;
        }

        // プレイヤーとドアの当たり判定用矩形（ワールド座標）を取得して重なりをチェック
        Rectangle doorRect = new Rectangle(getWorldX(), getWorldY(), FrameApp.getTileSize(), FrameApp.getTileSize());
        Rectangle playerRect = new Rectangle(
                gameWindow.getPlayer().getWorldX(),
                gameWindow.getPlayer().getWorldY(),
                FrameApp.getTileSize(),
                FrameApp.getTileSize()
        );

        if (doorRect.intersects(playerRect)) {
            isOpen = true;
            setCollision(false);
            setAlive(false);

            gameWindow.getSoundmanager().playDoorBangSE();
            System.out.println("バン！と音が鳴ってドアが開いた！");
        }

        // すでに開いていて、プレイヤーが十分に離れた場合
        if (isOpen && (distanceX > threshold || distanceY > threshold)) {
            isOpen = false;
            setAlive(true);
            setCollision(true);
            System.out.println("ドアが自動で閉まった！");
        }
    }

    @Override
    public void draw(Graphics2D g2) {

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        // すでに開いて（消えて）
        if (!getAlive()) {
            return;
        }

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }
}