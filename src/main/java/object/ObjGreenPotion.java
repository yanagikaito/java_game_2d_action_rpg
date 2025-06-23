package object;

import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.io.IOException;

public class ObjGreenPotion extends Entity {

    private GameWindow gameWindow;
    private int healAmount = 20;
    private int stackSize = 1;

    public ObjGreenPotion(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setName("グリーンポーション");
        setDescription("[" + getName() + "]\n 魔力を20回復する");
        try {

            setImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/green-potion.gif")),
                    FrameApp.getTileSize(), FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getHealAmount() {
        return healAmount;
    }

    public int getStackSize() {
        return stackSize;
    }
}