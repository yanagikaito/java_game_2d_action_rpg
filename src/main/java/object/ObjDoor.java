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
        setCollision(true);
        try {
            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("object/door_plant_primeval.gif"));
            setImage(raw, FrameApp.getTileSize());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 鍵などによって強制的に扉を開けるメソッド
     */

    public void openDoor() {
        this.isOpen = true;
        setCollision(false);
        setAlive(false);
    }

    @Override
    public void update() {

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

            java.util.ArrayList<Entity> inventory = gameWindow.getPlayer().getInventory();
            int keyIndex = -1;

            for (int i = 0; i < inventory.size(); i++) {
                Entity item = inventory.get(i);
                if (item != null && item.getType() instanceof entity.type.KeyType) {
                    keyIndex = i; // 鍵が見つかった
                    break;
                }
            }

            // 鍵を持っている場合のみ開ける
            if (keyIndex != -1) {

                isOpen = true;
                setCollision(false);
                setAlive(false);

                Entity keyItem = inventory.get(keyIndex);
                int currentAmount = keyItem.getAmount();
                if (currentAmount > 1) {
                    keyItem.setAmount(currentAmount - 1);
                } else {
                    inventory.remove(keyIndex);
                }

                gameWindow.getSoundmanager().playDoorBangSE();
                System.out.println("Opened the door with a key!");

            } else {
                // 鍵を持っていない場合
                System.out.println("The door is locked and won't open...");
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        // すでに開いて（消えて）いる場合は描画しない
        if (!getAlive()) {
            return;
        }

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }

    public boolean isOpen() {
        return isOpen;
    }
}