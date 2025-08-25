package object;

import entity.Entity;
import entity.RedPotionType;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjRedPotion extends Entity {

    private GameWindow gameWindow;
    private int healAmount = 2;
    private int stackSize = 1;

    public ObjRedPotion(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setType(new RedPotionType());
        setName("レッドポーション");
        setDescription("[" + getName() + "]\n 体力を2回復する");
        setPrice(10);
        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("object/red-potion.gif"));
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

    public int getHealAmount() {
        return healAmount;
    }

    public int getStackSize() {
        return stackSize;
    }
}