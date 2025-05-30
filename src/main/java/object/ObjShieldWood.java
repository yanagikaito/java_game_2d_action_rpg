package object;

import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.io.IOException;

public class ObjShieldWood extends Entity {

    private GameWindow gameWindow;

    public ObjShieldWood(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setName("普通の盾");
        setDefenseValue(1);
        try {

            setImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/shield-wood.gif")),
                    FrameApp.getTileSize(), FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}