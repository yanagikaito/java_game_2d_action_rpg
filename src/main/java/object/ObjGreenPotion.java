package object;

import entity.Entity;
import frame.FrameApp;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjGreenPotion extends Entity {

    private GameWindow gameWindow;
    private int healAmount = 20;
    private int stackSize = 1;

    public ObjGreenPotion(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setType(getType_pickupOnly());
        setName("グリーンポーション");
        setDescription("[" + getName() + "]\n 魔力を20回復する");
        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/green-potion.gif"));
            setImage(raw, FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(@NotNull Graphics2D g2) {

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