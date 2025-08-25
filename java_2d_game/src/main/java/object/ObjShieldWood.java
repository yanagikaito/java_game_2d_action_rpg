package object;

import entity.Entity;
import entity.ShieldType;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjShieldWood extends Entity {

    private GameWindow gameWindow;

    public ObjShieldWood(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setType(new ShieldType());
        setName("木の盾");
        setDefenseValue(1);
        setDescription("[" + getName() + "]\n 木で,できた盾");
        setPrice(75);
        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("object/shield-wood.gif"));
            setImage(raw, FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}