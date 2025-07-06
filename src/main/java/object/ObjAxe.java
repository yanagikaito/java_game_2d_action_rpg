package object;

import entity.Entity;
import frame.FrameApp;
import org.jetbrains.annotations.NotNull;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjAxe extends Entity {

    private GameWindow gameWindow;

    public ObjAxe(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;

        setType(getType_axe());
        setName("木の斧");
        setAttackValue(2);
        getAttackArea().width = 30;
        getAttackArea().height = 30;
        setDescription("[" + getName() + "]\n木を切ることができる");

        try {

            BufferedImage raw = ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/axe.gif"));
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
}
