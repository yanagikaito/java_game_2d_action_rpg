package object;

import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.io.IOException;

public class ObjSwordNormal extends Entity {

    private GameWindow gameWindow;

    public ObjSwordNormal(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setName("普通の剣");
        setAttackValue(1);
        setDescription("[" + getName() + "]\n 古くからある剣");
        try {

            setImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/sword-normal.gif")),
                    FrameApp.getTileSize(), FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}