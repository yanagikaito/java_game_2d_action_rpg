package object;

import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjSwordNormal extends Entity {

    private GameWindow gameWindow;

    public ObjSwordNormal(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setType(getType_sword());
        setName("普通の剣");
        setAttackValue(1);
        setDescription("[" + getName() + "]\n 古くからある剣");
        setPrice(100);
        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/sword-normal.gif"));
            setImage(raw, FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}