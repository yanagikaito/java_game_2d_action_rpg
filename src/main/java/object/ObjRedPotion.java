package object;

import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.io.IOException;

public class ObjRedPotion extends Entity {

    private GameWindow gameWindow;

    public ObjRedPotion(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setName("レッドポーション");
        setDescription("[" + getName() + "]\n 体力を1回復する");
        try {

            setImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/red-potion.gif")),
                    FrameApp.getTileSize(), FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}