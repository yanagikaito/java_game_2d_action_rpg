package ui.state.trade.load;

import entity.Entity;
import frame.FrameApp;
import game.GameState;
import player.Player;
import save.LoadManager;
import ui.UI;

import javax.swing.*;
import java.awt.*;

public class LoadConfirmState implements LoadScreenState {

    private final LoadScreenContext ctx;
    private final int slot; // 0-based
    private boolean started = false;
    private boolean loading = false;

    public LoadConfirmState(LoadScreenContext ctx, int slot) {
        this.ctx = ctx;
        this.slot = slot;
        this.started = false;
        this.loading = false;
        System.out.println("DEBUG: LoadConfirmState.<init> slot=" + slot + " thread=" + Thread.currentThread().getName());
    }

    @Override
    public void handleKey(int code) {

    }

    @Override
    public void draw(Graphics2D g2) {
        System.out.println("DEBUG: LoadConfirmState.draw called slot=" + slot + " started=" + started);
        System.out.println("DEBUG: LoadConfirmState.draw called slot=" + slot + " loading=" + loading);

        if (started) {
            started = false;
        }

        // 保存処理は一度だけ開始する
        if (!started) {
            started = true;
            System.out.println("DEBUG: LoadConfirmState.starting startLoad()");
            startLoad();
        }

        // 簡易な「ロード中」ダイアログを描画
        UI ui = ctx.ui();
        int w = FrameApp.getScreenWidth();
        int h = FrameApp.getScreenHeight();
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, w, h);

        g2.setFont(ui.getArial40());
        g2.setColor(Color.WHITE);
        String text = "ロード中...";
        int tx = ui.getXForCenteredText(g2, text);
        int ty = h / 2;
        g2.drawString(text, tx, ty);
    }

    private void startLoad() {
        if (loading) {
            System.out.println("DEBUG: already loading, abort");
            return;
        }
        loading = true;
        try {
            ctx.ui().addMessage("ロード中...");
        } catch (Throwable ignored) {
        }
        try {
            ctx.ui().setSaveInProgress(true);
        } catch (Throwable ignored) {
        }
        new Thread(() -> {
            Entity loaded = null;
            boolean ok = false;
            try {
                loaded = LoadManager.loadPlayer(slot, ctx.gw());
                ok = (loaded != null);
            } catch (Throwable ex) {
                ex.printStackTrace();
                ok = false;
            }
            final Entity finalLoaded = loaded;
            final boolean finalOk = ok;
            SwingUtilities.invokeLater(() -> {
                try {
                    try {
                        ctx.ui().setSaveInProgress(false);
                    } catch (Throwable ignored) {
                    }
                    if (finalOk && finalLoaded instanceof Player) {
                        ctx.gw().setPlayer((Player) finalLoaded);
                        ctx.gw().setGameState(GameState.PLAY);
                        ctx.ui().addMessage("ロードしました（Slot " + slot + "）");
                    } else {
                        ctx.ui().addMessage("ロードに失敗しました");
                        ctx.setState(new LoadMenuState(ctx));
                    }
                } finally {
                    loading = false;
                }
            });
        }, "LoadWorker").start();
    }
}