package object;

import entity.Entity;
import frame.FrameApp;
import window.GameWindow;

import java.io.IOException;

public class ObjHeart extends Entity {

    private GameWindow gameWindow;

    public ObjHeart(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        setName("ハート");
        try {

            loadAnimationFrames(
                    "heart/heart_full.gif",
                    "heart/heart_half.gif",
                    "heart/heart_blank.gif",
                    FrameApp.getTileSize()
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}