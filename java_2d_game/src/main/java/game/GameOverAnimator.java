package game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class GameOverAnimator {
    private final BufferedImage[] allFrames;
    private final BufferedImage[][] dirFrames;
    private final BufferedImage[] fallFrames;
    private final double frameDurationSeconds;
    private final int loopsTarget;

    private int dirIndex = 0;
    private int frameIndex = 0;
    private double elapsed = 0.0;
    private int completedLoops = 0;
    private boolean playing = false;
    private boolean falling = false;
    private boolean finished = false;

    /**
     * frames: spriteManager.getAnimationFrames("death_", 9) の戻り配列
     * frameDurationSeconds: 1フレームの表示秒数（例 0.12）
     * loopsTarget: 何周するか（ここでは 2）
     */

    public GameOverAnimator(BufferedImage[] frames, double frameDurationSeconds, int loopsTarget) {
        this.allFrames = frames != null ? frames : new BufferedImage[0];
        this.frameDurationSeconds = frameDurationSeconds;
        this.loopsTarget = loopsTarget;

        // 分割ルール: 各方向 2 フレームずつ、最後の 1 枚を倒れ用に使う
        dirFrames = new BufferedImage[4][];
        for (int i = 0; i < 4; i++) {
            int start = i * 2;
            int len = Math.min(2, Math.max(0, allFrames.length - start));
            dirFrames[i] = new BufferedImage[len];
            for (int j = 0; j < len; j++) {
                dirFrames[i][j] = allFrames[start + j];
            }
        }
        // 倒れ用は最後のフレーム（存在しなければ空配列）
        if (allFrames.length >= 9) {
            fallFrames = new BufferedImage[]{allFrames[8]};
        } else {
            fallFrames = new BufferedImage[0];
        }
    }

    public void start() {
        if (playing) return;
        playing = true;
        finished = false;
        falling = false;
        dirIndex = 0;
        frameIndex = 0;
        elapsed = 0.0;
        completedLoops = 0;
    }

    public boolean isFinished() {
        return finished;
    }

    public void update(double deltaSeconds) {
        if (!playing || finished) return;
        elapsed += deltaSeconds;
        while (elapsed >= frameDurationSeconds) {
            elapsed -= frameDurationSeconds;
            advanceFrame();
            if (finished) break;
        }
    }

    private void advanceFrame() {
        if (!falling) {
            BufferedImage[] frames = dirFrames[dirIndex];
            if (frames != null && frames.length > 0) {
                frameIndex++;
                if (frameIndex >= frames.length) {
                    frameIndex = 0;
                    dirIndex++;
                    if (dirIndex >= dirFrames.length) {
                        dirIndex = 0;
                        completedLoops++;
                        if (completedLoops >= loopsTarget) {
                            falling = true;
                            frameIndex = 0;
                        }
                    }
                }
            } else {
                // フレームが無ければ即次へ
                dirIndex++;
                if (dirIndex >= dirFrames.length) {
                    dirIndex = 0;
                    completedLoops++;
                    if (completedLoops >= loopsTarget) {
                        falling = true;
                        frameIndex = 0;
                    }
                }
            }
        } else {
            if (fallFrames != null && fallFrames.length > 0) {
                frameIndex++;
                if (frameIndex >= fallFrames.length) {
                    finished = true;
                    playing = false;
                }
            } else {
                finished = true;
                playing = false;
            }
        }
    }

    public void draw(Graphics2D g2, int x, int y, int w, int h) {
        if (!playing && !finished) return;
        BufferedImage img = null;
        if (!falling) {
            BufferedImage[] frames = dirFrames[dirIndex];
            if (frames != null && frames.length > 0) {
                int idx = Math.min(frameIndex, frames.length - 1);
                img = frames[idx];
            }
        } else {
            if (fallFrames != null && fallFrames.length > 0) {
                int idx = Math.min(frameIndex, fallFrames.length - 1);
                img = fallFrames[idx];
            }
        }
        if (img != null) {
            g2.drawImage(img, x, y, w, h, null);
        }
    }
}