package object;

import entity.Entity;
import entity.type.KeyType;
import frame.FrameApp;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ObjKey extends Entity {

    private GameWindow gameWindow;
    private static final int SPRITE_COUNT = 32;
    private BufferedImage[] animationFrames = new BufferedImage[SPRITE_COUNT];
    private int spriteCounter = 0;
    private int spriteNum = 0;

    public ObjKey(GameWindow gameWindow) {
        super(gameWindow);
        this.gameWindow = gameWindow;

        setType(new KeyType());
        setName("始原の森の鍵");
        setDescription("[" + getName() + "]\n ドアを開ける鍵");

        loadAnimationImages();
    }

    private void loadAnimationImages() {

        try {
            for (int i = 0; i < SPRITE_COUNT; i++) {

                String path = String.format("object/object_key/primeval_forest_key-%d.gif", i + 1);

                // リソースから読み込み
                var stream = getClass().getClassLoader().getResourceAsStream(path);
                if (stream != null) {
                    BufferedImage raw = ImageIO.read(stream);
                    animationFrames[i] = raw;
                } else {
                    // デバッグ用：どの画像が見つからないかコンソールに表示する
                    System.out.println("Image not found : " + path);
                }
            }

            // 初期画像として0番目をセット
            if (animationFrames[0] != null) {
                setImage(animationFrames[0], FrameApp.getTileSize());
            } else {
                System.out.println("Error: Failed to load the first frame of the animation.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {

        // アニメーションの速度調整
        int animationSpeed = 5;

        spriteCounter++;
        if (spriteCounter > animationSpeed) {
            spriteNum++;
            if (spriteNum >= SPRITE_COUNT) {
                // 32枚到達したら最初に戻る
                spriteNum = 0;
            }

            // 現在のフレーム画像を更新
            if (animationFrames[spriteNum] != null) {
                setImage(animationFrames[spriteNum], FrameApp.getTileSize());
            }

            spriteCounter = 0;
        }
    }

    @Override
    public void draw(Graphics2D g2) {

        if (!getAlive()) {
            return;
        }

        int screenX = getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }
}