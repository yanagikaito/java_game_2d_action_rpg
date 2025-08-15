package sound;

import window.GameWindow;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

public class SoundManager {

    private GameWindow gameWindow;

    public SoundManager(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void damageWAV(String filePath) {
        playWAV(filePath);
    }

    public void defeatedWAV(String filePath) {
        playWAV(filePath);
    }

    public void levelWAV(String filePath) {
        playWAV(filePath);
    }

    public void cursorWAV(String filePath) {
        playWAV(filePath);
    }

    public void redPotionWAV(String filePath) {
        playWAV(filePath);
    }

    public void greenPotionWAV(String filePath) {
        playWAV(filePath);
    }

    public void explosionWAV(String filePath) {
        playWAV(filePath);
    }

    private void playWAV(String filePath) {

        try {

            InputStream in = getClass().getClassLoader()
                    .getResourceAsStream(filePath);
            if (in == null) {
                throw new IllegalStateException("リソースが見つかりません: " + filePath);
            }
            try (BufferedInputStream bin = new BufferedInputStream(in)) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(bin);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}