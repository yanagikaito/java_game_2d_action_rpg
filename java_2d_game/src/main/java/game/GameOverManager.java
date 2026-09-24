package game;

import player.SpriteManager;

import java.awt.image.BufferedImage;

public class GameOverManager {
    private final SpriteManager spriteManager;
    private final String basePrefix;
    private final int totalFrames;
    // キャッシュされた animator（遅延生成）
    private volatile GameOverAnimator animator;

    public GameOverManager(SpriteManager spriteManager, String basePrefix, int totalFrames, double a, double b, double c) {
        this.spriteManager = spriteManager;
        this.basePrefix = basePrefix;
        this.totalFrames = totalFrames;
    }

    public GameOverAnimator getAnimator() {
        BufferedImage[] frames = spriteManager.getAnimationFrames(basePrefix, totalFrames); // death_, 9
        // 1フレームあたりの秒数はここで決める（例 0.12）、ループ回数は 2
        return new GameOverAnimator(frames, 0.12, 2);
    }
}