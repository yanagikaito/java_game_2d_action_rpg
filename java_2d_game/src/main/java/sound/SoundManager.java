package sound;

import window.GameWindow;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {

    private final GameWindow gameWindow;

    // キャッシュ・現在のBGM・設定
    private final Map<String, Clip> clipCache = new ConcurrentHashMap<>();
    private volatile Clip currentBgm = null;
    private volatile String currentBgmName = null;
    private volatile boolean muted = false;
    private volatile float bgmVolume = 1.0f; // 0.0 - 1.0

    // IO/再生用シングルスレッド
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SoundIO");
        t.setDaemon(true);
        return t;
    });

    public SoundManager(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }


    // 効果音
    public void playSE(String filePath) {
        ioExecutor.submit(() -> {
            try {
                Clip clip = createClipFromResource(filePath);
                if (clip != null) {
                    setClipVolume(clip, bgmVolume);
                    clip.start();
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void damageWAV(String filePath) {
        playSE(filePath);
    }

    public void defeatedWAV(String filePath) {
        playSE(filePath);
    }

    public void levelWAV(String filePath) {
        playSE(filePath);
    }

    public void cursorWAV(String filePath) {
        playSE(filePath);
    }

    public void redPotionWAV(String filePath) {
        playSE(filePath);
    }

    public void greenPotionWAV(String filePath) {
        playSE(filePath);
    }

    public void explosionWAV(String filePath) {
        playSE(filePath);
    }

    public void questChickenCompleteWAV(String filePath) {
        playSE(filePath);
    }

    // 事前ロード
    public void preload(String filePath) {
        ioExecutor.submit(() -> {
            try {
                if (!clipCache.containsKey(filePath)) {
                    Clip clip = createClipFromResource(filePath);
                    if (clip != null) clipCache.put(filePath, clip);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ループ再生（即時切替）
    public void playBGM(String filePath) {
        ioExecutor.submit(() -> {
            try {
                Clip newClip = clipCache.get(filePath);
                if (newClip == null) {
                    newClip = createClipFromResource(filePath);
                    if (newClip == null) return;
                    clipCache.put(filePath, newClip);
                } else {
                    newClip.setFramePosition(0);
                }

                synchronized (this) {
                    if (currentBgm != null && currentBgm != newClip) {
                        try {
                            currentBgm.stop();
                        } catch (Exception ignored) {
                        }
                    }
                    currentBgm = newClip;
                    currentBgmName = filePath;
                    applyBgmVolumeAndMute(currentBgm);
                    currentBgm.loop(Clip.LOOP_CONTINUOUSLY);
                    currentBgm.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 即時停止（キャッシュは残す）
    public synchronized void stopBGM() {
        if (currentBgm != null) {
            try {
                currentBgm.stop();
                currentBgm.setFramePosition(0);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                currentBgm = null;
                currentBgmName = null;
            }
        }
    }

    // フェードアウト（ミリ秒）。完了後に停止する。
    public void fadeOutBGM(int durationMs) {
        Clip clipToFade;
        synchronized (this) {
            clipToFade = currentBgm;
            currentBgm = null;
            currentBgmName = null;
        }
        if (clipToFade == null) return;

        new Thread(() -> {
            try {
                FloatControl vol = getVolumeControl(clipToFade);
                if (vol == null) {
                    clipToFade.stop();
                    clipToFade.setFramePosition(0);
                    return;
                }
                float start = vol.getValue();
                float min = vol.getMinimum();
                int steps = Math.max(1, durationMs / 20);
                for (int i = 0; i < steps; i++) {
                    float t = 1f - (float) i / (float) steps;
                    float v = min + (start - min) * t;
                    try {
                        vol.setValue(v);
                    } catch (Exception ignored) {
                    }
                    Thread.sleep(durationMs / steps);
                }
                clipToFade.stop();
                clipToFade.setFramePosition(0);
                try {
                    vol.setValue(start);
                } catch (Exception ignored) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "BgmFadeOut").start();
    }

    private void applyBgmVolumeAndMute(Clip clip) {
        if (clip == null) return;
        setClipVolume(clip, muted ? 0f : bgmVolume);
    }

    private void setClipVolume(Clip clip, float linearVolume) {
        FloatControl vol = getVolumeControl(clip);
        if (vol == null) return;
        float min = vol.getMinimum();
        float max = vol.getMaximum();
        float dB;
        if (linearVolume <= 0f) {
            dB = min;
        } else {
            dB = (float) (20.0 * Math.log10(linearVolume));
            dB = Math.max(min, Math.min(max, dB));
        }
        try {
            vol.setValue(dB);
        } catch (Exception ignored) {
        }
    }

    private FloatControl getVolumeControl(Clip clip) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                return (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                return (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 指定されたリソースパスから音声データを読み込み、再生可能な {@link Clip} を生成して返す。
     *
     * <p>内部でリソースを {@link ClassLoader#getResourceAsStream(String)} から取得し、
     * {@link AudioSystem#getAudioInputStream(InputStream)} を用いて {@link AudioInputStream} を作成。
     * 必要に応じて PCM_SIGNED フォーマットにデコードし、{@link Clip#open(AudioInputStream)} を呼んでクリップを開く。</p>
     *
     * <p>注意点:</p>
     * <ul>
     *   <li>リソースが見つからない場合や読み込み/デコード/オープンに失敗した場合は {@code null} を返す（例外は内部でログ出力される）。</li>
     *   <li>戻り値の {@link Clip} は呼び出し側で再生・停止・解放（{@link Clip#close()}）を行う。</li>
     *   <li>このメソッドは内部でストリームをクローズしますが、{@link Clip} 自体のクローズは行わない。</li>
     *   <li>サポートされる音声フォーマットは実行環境の Java Sound 実装に依存。必要なら事前に短いテスト音源で動作確認。</li>
     * </ul>
     *
     * @param filePath クラスパス上の音声リソースの相対パス（例: "sound/meadow_G110.wav"）
     * @return 読み込みに成功した {@link Clip} オブジェクト、失敗した場合は {@code null}
     */

    private Clip createClipFromResource(String filePath) {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
            if (in == null) {
                System.err.println("Sound resource not found: " + filePath);
                return null;
            }
            try (BufferedInputStream bin = new BufferedInputStream(in)) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(bin);
                AudioFormat baseFormat = ais.getFormat();
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );
                AudioInputStream dais = AudioSystem.getAudioInputStream(decodedFormat, ais);
                Clip clip = AudioSystem.getClip();
                clip.open(dais);
                return clip;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized String getCurrentBgmName() {
        return currentBgmName;
    }
}