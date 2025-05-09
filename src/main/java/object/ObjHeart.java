package object;

import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.io.IOException;

public class ObjHeart extends SuperObject {

    private GameWindow gameWindow;

    public ObjHeart(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setName("ハート");
        try {

            setImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream("heart/heart_full.gif")),
                    FrameApp.getTileSize(), FrameApp.getTileSize());
            setImage2(ImageIO.read(getClass().getClassLoader().getResourceAsStream("heart/heart_half.gif")),
                    FrameApp.getTileSize(), FrameApp.getTileSize());
            setImage3(ImageIO.read(getClass().getClassLoader().getResourceAsStream("heart/heart_blank.gif")),
                    FrameApp.getTileSize(), FrameApp.getTileSize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
