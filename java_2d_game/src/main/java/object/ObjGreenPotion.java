package object;

import entity.Entity;
import entity.type.GreenPotionType;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjGreenPotion extends Entity {

    private GameWindow gameWindow;
    private int healAmount = 20;

    public ObjGreenPotion(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setType(new GreenPotionType());
        setName("グリーンポーション");
        setDescription("[" + getName() + "]\n 魔力を20回復する");
        setPrice(10);
        setStackable(true);
        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("object/green-potion.gif"));
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
        return new ObjGreenPotion(gameWindow);
    }


    public int getHealAmount() {
        return healAmount;
    }

    public int getPrice() {
        return 10;
    }
}