package ui.state.trade.save;

import frame.FrameApp;
import save.SaveManager;
import save.SaveMeta;
import ui.UI;

import javax.swing.*;
import java.awt.*;

public class SaveConfirmState implements SaveScreenState {
    private final SaveScreenContext ctx;
    private final int slot; // 0-based
    private boolean started = false;

    public SaveConfirmState(SaveScreenContext ctx, int slot) {
        this.ctx = ctx;
        this.slot = slot;
        System.out.println("DEBUG: SaveConfirmState.<init> slot=" + slot + " thread=" + Thread.currentThread().getName());
    }

    @Override
    public void handleKey(int code) {

    }

    @Override
    public void draw(Graphics2D g2) {
        System.out.println("DEBUG: SaveConfirmState.draw called slot=" + slot + " started=" + started);
        // 保存処理は一度だけ開始する
        if (!started) {
            started = true;
            System.out.println("DEBUG: SaveConfirmState.starting startSave()");
            startSave();
        }

        // 簡易な「セーブ中」ダイアログを描画
        UI ui = ctx.ui();
        int w = FrameApp.getScreenWidth();
        int h = FrameApp.getScreenHeight();
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, w, h);

        g2.setFont(ui.getArial40());
        g2.setColor(Color.WHITE);
        String text = "セーブ中...";
        int tx = ui.getXForCenteredText(g2, text);
        int ty = h / 2;
        g2.drawString(text, tx, ty);
    }

    private void startSave() {
        System.out.println("DEBUG: startSave() begin slot=" + slot);

        // 二重保存防止
        if (ctx.ui().getSaveInProgress()) {
            System.out.println("DEBUG: save already in progress, aborting startSave()");
            return;
        }
        ctx.ui().setSaveInProgress(true);

        new Thread(() -> {
            boolean ok = false;
            try {
                System.out.println("DEBUG: calling SaveManager.saveGame slot=" + slot);
                ok = SaveManager.saveGame(slot, ctx.gw().getPlayer()); // セーブ実行

                System.out.println("DEBUG: SaveManager.saveGame returned=" + ok);

                // セーブ成功なら最新のメタを読み込んで UI に反映
                if (ok) {
                    SaveMeta meta = SaveManager.loadMeta(slot);
                    System.out.println("DEBUG: loaded meta for slot=" + slot + " playTime=" + (meta != null ? meta.getPlayTimeSeconds() : "null"));

                    // UI 更新は必ず EDT で行う
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        try {
                            // UI#setSaveMeta を事前に実装しておく
                            ctx.ui().setSaveMeta(slot, meta);
                            // GameWindow の再描画
                            ctx.gw().repaint();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }

            } catch (Throwable ex) {
                ex.printStackTrace();
                ok = false;
            }

            final boolean finalOk = ok;
            // 結果表示や後処理は EDT で行う
            SwingUtilities.invokeLater(() -> {
                try {
                    ctx.ui().setSaveInProgress(false);
                    if (finalOk) {
                    } else {
                    }

                    // 少し待ってからセーブメニューに戻す
                    Timer t = new Timer(800, evt -> {
                        try {
                            // Confirm ダイアログを閉じる UI 側のメソッドがあれば呼ぶ（無ければ無視）
                            try {
                                ctx.ui().closeSaveMenuUI();
                            } catch (Throwable ignored) {
                            }

                            // Save メニューに戻す
                            ctx.setState(new SaveMenuState(ctx));

                        } catch (Throwable e) {
                            e.printStackTrace();
                        } finally {
                            ((Timer) evt.getSource()).stop();
                        }
                    });
                    t.setRepeats(false);
                    t.start();

                } catch (Throwable uiEx) {
                    uiEx.printStackTrace();
                }
            });
        }, "SaveWorker").start();
    }
}