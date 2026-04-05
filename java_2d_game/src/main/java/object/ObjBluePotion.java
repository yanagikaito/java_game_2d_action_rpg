package object;

import entity.BluePotionType;
import entity.Entity;
import entity.RedPotionType;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjBluePotion extends Entity {

    private GameWindow gameWindow;
    private int healAmount = 100;

    public ObjBluePotion(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setType(new BluePotionType());
        setName("ブルーポーション");
        setDescription("[" + getName() + "]\n 魔力を全回復する。");
        setPrice(100);
        setStackable(true);
        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("object/blue-potion.gif"));
            setImage(raw, FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }

    @Override
    public Entity copy() {
        return new ObjBluePotion(gameWindow);
    }

    public int getHealAmount() {
        return healAmount;
    }

    public int getPrice() {
        return 100;
    }
}