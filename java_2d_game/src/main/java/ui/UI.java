package ui;

import entity.Entity;
import entity.EntityType;
import frame.FrameApp;
import game.GameState;
import npc.NpcMerChant;
import object.*;
import player.Player;
import player.SpriteManager;
import save.SaveManager;
import save.SaveMeta;
import triforce.TriforcePanel;
import triforce.TriforceRenderer;
import ui.state.trade.TradeScreenContext;
import ui.state.trade.save.SaveScreenContext;
import window.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class UI {

    private final GameWindow gameWindow;

    // 使用するフォント
    private final Font arial40;
    private final Font arial80Bold;

    private final int MANA_BAR_X = 5;
    private final int MANA_BAR_Y = 60;
    private final int MANA_BAR_WIDTH = 20;
    private final int MANA_BAR_HEIGHT = 200;

    // メッセージ表示用
    private boolean messageOn;
    private String currentDialogueMessage;
    private ArrayList<String> message = new ArrayList<>();
    private ArrayList<Integer> messageCounter = new ArrayList<>();
    private TriforcePanel triforcePanel = new TriforcePanel();
    private SpriteManager spriteManager = new SpriteManager();

    private BufferedImage heartFull;
    private BufferedImage heartHalf;
    private BufferedImage heartBlank;
    private BufferedImage coin;

    private int playerSlotCol = 0;
    private int playerSlotRow = 0;

    private int npcSlotCol = 0;
    private int npcSlotRow = 0;

    private boolean dialogueOn = false;
    private Entity npc;
    private int subState;

    private SaveMeta[] saveMetas;
    private final int SLOT_COUNT = 3;
    private int loadSlotIndex = 0;

    private final TradeScreenContext tradeCtx;
    private final SaveScreenContext saveCtx;

    // セーブメニュー関連
    private int saveMenuSelected = 0;           // 現在選択中のスロット（0-based）
    private boolean saveInProgress = false;     // 保存処理中フラグ
    private boolean saveMenuOpen = false;       // セーブメニューが開いているか
    private long menuInputCooldownUntil = 0L;   // 開いた直後の短い無効化タイムスタンプ（ms）


    public UI(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.tradeCtx = new TradeScreenContext(gameWindow, this);
        this.saveCtx = new SaveScreenContext(gameWindow, this);
        this.arial40 = new Font("エリア", Font.PLAIN, 40);
        this.arial80Bold = new Font("エリア", Font.BOLD, 80);
        this.messageOn = false;
        this.currentDialogueMessage = "";
        this.npc = new NpcMerChant(gameWindow);

        Entity heart = new ObjHeart(gameWindow);
        heartFull = heart.getImage();
        heartHalf = heart.getImage2();
        heartBlank = heart.getImage3();

        Entity bronzeCoin = new ObjCoinBronze(gameWindow);
        coin = bronzeCoin.getImage();
    }

    public void addMessage(String text) {
        message.add(text);
        messageCounter.add(0);
    }

    public void addDialogue(String text) {
        if (text == null || text.isEmpty()) {
            dialogueOn = false;
            currentDialogueMessage = "";
        } else {
            dialogueOn = true;
            currentDialogueMessage = text;
        }
    }

    public void draw(Graphics2D g2) {

        g2.setFont(arial40);
        g2.setColor(Color.white);

        GameState gameState = gameWindow.getGameState();

        if (gameState == GameState.TITLE) {

            drawTitleScreen(g2);

        } else if (gameState == GameState.LOAD) {

            gameWindow.setDialogueActive(false);
            drawLoadScreen(g2);

        } else if (gameState == GameState.PLAY) {

            gameWindow.setDialogueActive(false);
            drawPlayerLife(g2);
            drawBattleLogMessage(g2);
            drawManaBar(g2);

        } else if (gameState == GameState.PAUSE) {

            gameWindow.setDialogueActive(false);
            drawPlayerLife(g2);
            drawPauseScreen(g2);

        } else if (gameState == GameState.DIALOGUE) {

            gameWindow.setDialogueActive(true);
            drawPlayerLife(g2);
            drawDialogueScreen(g2);

        } else if (gameState == GameState.CHARACTER) {

            gameWindow.setDialogueActive(false);
            drawCharacterScreen(g2);
            drawInventory(g2, gameWindow.getPlayer(), true);

        } else if (gameState == GameState.GAME_OVER) {
            drawGameOverScreen(g2);

        } else if (gameState == GameState.TRADE) {
            drawTradeScreen(g2);

        } else if (gameState == GameState.SAVE) {
            drawSaveScreen(g2);
        }


        if (messageOn) {
            drawMessage(g2);
        }
    }

    public void drawDialogueSaveScreen(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

        int frameX = tileSize / 2;
        int frameY = tileSize / 2;
        int frameWidth = tileSize * 15;
        int frameHeight = tileSize * 3;
        drawLoadPlayerLife(g2);
        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        String text;
        int x;
        int y;
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));

        text = "slot0";
        x = getXForCenteredText(g2, text);
        y = tileSize * 2;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 0) {
            g2.drawString(">", x - 40, y);
        }

        // slot0 がセーブされているならハートとプレビューを描画
        if (saveMetas != null && saveMetas.length > 0 && saveMetas[0] != null && saveMetas[0].exists()) {
            SaveMeta meta = saveMetas[0];
            drawPlayerLife(g2);
            // プレイヤープレビュー（右側に表示する例）
            int previewX = frameX + frameWidth - tileSize * 3;
            int previewY = frameY + 8;
            drawSpritePreview(g2, meta, previewX, previewY, tileSize * 2, tileSize * 2);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));

        frameX = tileSize / 2;
        frameY = FrameApp.getScreenHeight() / 3;
        frameWidth = tileSize * 15;
        frameHeight = tileSize * 3;
        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        text = "slot1";
        x = getXForCenteredText(g2, text);
        y += (int) (tileSize * 3.5);
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 1) {
            g2.drawString(">", x - 40, y);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));

        frameX = tileSize / 2;
        frameY = (int) ((FrameApp.getScreenHeight() + tileSize) / 1.7);
        frameWidth = tileSize * 15;
        frameHeight = tileSize * 3;
        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        text = "slot2";
        x = getXForCenteredText(g2, text);
        y += (int) (tileSize * 3.5);
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 2) {
            g2.drawString(">", x - 40, y);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));
    }

    private String ellipsize(Graphics2D g2, String text, int maxWidthPx) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(text) <= maxWidthPx) return text;
        String ell = "...";
        int avail = maxWidthPx - fm.stringWidth(ell);
        if (avail <= 0) return ell;
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(c);
            if (fm.stringWidth(sb.toString()) > avail) {
                sb.setLength(Math.max(0, sb.length() - 1));
                break;
            }
        }
        return sb.toString() + ell;
    }

    public void drawTitleScreen(Graphics2D g2) {

        String text;
        int x;
        int y;
        int tileSize = FrameApp.getTileSize();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.setFont(arial80Bold.deriveFont(Font.PLAIN, 80F));

        text = "GreenFantasy";
        g2.setColor(Color.GREEN);
        x = getXForCenteredText(g2, text);
        y = tileSize * 3;
        g2.drawString(text, x, y);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x - 4, y - 4);

        g2.setFont(g2.getFont().deriveFont(50f));

        text = "ニューゲーム";
        x = getXForCenteredText(g2, text);
        y += tileSize * 4;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 0) {
            g2.drawString(">", x - 40, y);
        }

        text = "ゲームロード";
        x = getXForCenteredText(g2, text);
        y += tileSize + 7;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 1) {
            g2.drawString(">", x - 40, y);
        }

        text = "ゲーム終了";
        x = getXForCenteredText(g2, text);
        y += tileSize + 7;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 2) {
            g2.drawString(">", x - 40, y);
        }

        // トライフォース描画
        if (triforcePanel != null) {
            triforcePanel.startAnimation();
            int triforcePx = FrameApp.getTileSize() * 6; // 表示サイズ（ピクセル）
            int centerX = FrameApp.getScreenWidth() / 2;
            int topY = FrameApp.getTileSize() / 2;

            Graphics2D tg = (Graphics2D) g2.create();

            try {
                // 移動：描画領域の中心に合わせる（正規化座標系を想定）
                tg.translate(centerX, topY + triforcePx / 2);

                // scale は TriforceRenderer が期待する scale に合わせる
                // TriforcePanel と同じ係数
                double scale = Math.min(triforcePx, triforcePx) / 2.6;

                // Y反転して正規化座標系にする
                tg.scale(scale, -scale);

                // Renderer を呼ぶ（TriforcePanel の状態を渡す）
                TriforceRenderer.drawTriforce(
                        tg,
                        triforcePanel.getTris(),
                        triforcePanel.isForming(),
                        triforcePanel.getFormTime(),
                        triforcePanel.getFormDuration()
                );
            } finally {
                tg.dispose();
            }
        }
    }

    public void initLoadScreen() {
        saveMetas = new SaveMeta[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) { // SaveManager.loadMeta はセーブの存在チェックと簡易情報を返す想定
            saveMetas[i] = SaveManager.loadMeta(i); // null ではなく SaveMeta を返す設計が望ましい
        }
        loadSlotIndex = 0;
    }

    public void drawLoadScreen(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

        int frameX = tileSize / 2;
        int frameY = tileSize / 2;
        int frameWidth = tileSize * 15;
        int frameHeight = tileSize * 3;
        drawLoadPlayerLife(g2);
        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        String text;
        int x;
        int y;
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));

        text = "slot0";
        x = getXForCenteredText(g2, text);
        y = tileSize * 2;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 0) {
            g2.drawString(">", x - 40, y);
        }

        // slot0 のサブウィンドウを描いた直後
        if (saveMetas != null && saveMetas.length > 0 && saveMetas[0] != null && saveMetas[0].exists()) {
            SaveMeta meta = saveMetas[0];
            // ハートをサブウィンドウ左上に描く（微調整は y 座標で）
            int lifeX0 = frameX; // frameX はそのスロットのサブウィンドウ左上
            int lifeY0 = frameY;
            drawPlayerLifeAt(g2, lifeX0, lifeY0, meta.getHp(), meta.getMaxHp());

            // プレビューは既存の位置で描画
            int previewX = frameX + frameWidth - tileSize * 3;
            int previewY = frameY + 8;
            drawSpritePreview(g2, meta, previewX, previewY, tileSize * 2, tileSize * 2);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));

        frameX = tileSize / 2;
        frameY = FrameApp.getScreenHeight() / 3;
        frameWidth = tileSize * 15;
        frameHeight = tileSize * 3;
        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        text = "slot1";
        x = getXForCenteredText(g2, text);
        y += (int) (tileSize * 3.5);
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 1) {
            g2.drawString(">", x - 40, y);
        }

        // slot1 のサブウィンドウを描いた直後
        if (saveMetas != null && saveMetas.length > 0 && saveMetas[1] != null && saveMetas[1].exists()) {
            SaveMeta meta = saveMetas[1];
            // ハートをサブウィンドウ左上に描く（微調整は y 座標で）
            int lifeX1 = frameX; // frameX はそのスロットのサブウィンドウ左上
            int lifeY1 = frameY;
            drawPlayerLifeAt(g2, lifeX1, lifeY1, meta.getHp(), meta.getMaxHp());

            // プレビューは既存の位置で描画
            int previewX = frameX + frameWidth - tileSize * 3;
            int previewY = frameY + 8;
            drawSpritePreview(g2, meta, previewX, previewY, tileSize * 2, tileSize * 2);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));

        frameX = tileSize / 2;
        frameY = (int) ((FrameApp.getScreenHeight() + tileSize) / 1.7);
        frameWidth = tileSize * 15;
        frameHeight = tileSize * 3;
        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        text = "slot2";
        x = getXForCenteredText(g2, text);
        y += (int) (tileSize * 3.5);
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 2) {
            g2.drawString(">", x - 40, y);
        }

        // slot0 のサブウィンドウを描いた直後
        if (saveMetas != null && saveMetas.length > 0 && saveMetas[2] != null && saveMetas[2].exists()) {
            SaveMeta meta = saveMetas[2];
            // ハートをサブウィンドウ左上に描く（微調整は y 座標で）
            int lifeX2 = frameX; // frameX はそのスロットのサブウィンドウ左上
            int lifeY2 = frameY;
            drawPlayerLifeAt(g2, lifeX2, lifeY2, meta.getHp(), meta.getMaxHp());

            // プレビューは既存の位置で描画
            int previewX = frameX + frameWidth - tileSize * 3;
            int previewY = frameY + 8;
            drawSpritePreview(g2, meta, previewX, previewY, tileSize * 2, tileSize * 2);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));
    }

    /**
     * 指定座標にハートを描画する（既存の heartFull/heartHalf/heartBlank を再利用）
     * hp と maxHp は SaveMeta から渡す（実数値: HP 単位）
     * x,y は左上の描画開始座標（ピクセル）
     */

    private void drawPlayerLifeAt(Graphics2D g2, int x, int y, int hp, int maxHp) {
        int tileSize = FrameApp.getTileSize();
        // 既存の drawPlayerLife と同じロジックを座標指定で使う
        int maxHearts = maxHp / 2;
        int fullHearts = hp / 2;
        int halfHearts = hp % 2;

        for (int i = 0; i < maxHearts; i++) {
            int hx = x + i * tileSize;
            int hy = y;
            if (i < fullHearts) {
                g2.drawImage(heartFull, hx, hy, tileSize, tileSize, null);
            } else if (i == fullHearts && halfHearts == 1) {
                g2.drawImage(heartHalf, hx, hy, tileSize, tileSize, null);
            } else {
                g2.drawImage(heartBlank, hx, hy, tileSize, tileSize, null);
            }
        }
    }

    // スプライトプレビュー描画ヘルパー（spriteManager を使う想定）
    private void drawSpritePreview(Graphics2D g2, SaveMeta meta, int x, int y, int w, int h) {
        System.out.println("drawSpritePreview: meta=" + meta);
        if (meta == null) {
            System.out.println("meta is null");
        } else {
            System.out.println("key=" + meta.getSpriteKey() + ", facing=" + meta.getFacing() + ", savedAt=" + meta.getSavedAt());
        }
        System.out.println("spriteManager=" + spriteManager);
        BufferedImage sprite = null;
        try {
            // spriteManager.getSprite(key, facing, frame) のような API を想定
            sprite = spriteManager.getSprite(meta.getSpriteKey(), meta.getFacing(), 0);
        } catch (Exception e) {
            // 無ければ null のままフォールバック
        }

        if (sprite != null) {
            g2.drawImage(sprite, x, y, w, h, null);
        } else {
            // フォールバック表示（グレーの四角）
            g2.setColor(Color.GRAY);
            g2.fillRect(x, y, w, h);
            g2.setColor(Color.WHITE);
            g2.drawString("No sprite", x + 4, y + h / 2);
        }
    }


    public void returnToTitleFromGameOver() {

        // ゲーム側の状態をタイトルに切り替え
        gameWindow.setGameState(GameState.TITLE);

        SwingUtilities.invokeLater(() -> {
            triforcePanel.resetAnimation();
            triforcePanel.startAnimation();
        });
    }


    public void drawGameOverScreen(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, FrameApp.getScreenWidth(), FrameApp.getScreenHeight());

        int x;
        int y;
        String text;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 80f));

        text = "ゲームオーバー";
        g2.setColor(Color.BLACK);
        x = getXForCenteredText(g2, text);
        y = tileSize * 4;
        g2.drawString(text, x, y);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x - 4, y - 4);

        g2.setFont(g2.getFont().deriveFont(50f));
        text = "リトライ";
        x = getXForCenteredText(g2, text);
        y += tileSize * 4;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 0) {
            g2.drawString(">", x - 40, y);
        }

        text = "タイトルに戻る";
        x = getXForCenteredText(g2, text);
        y += 55;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 1) {
            g2.drawString(">", x - 40, y);
        }
    }

    public void drawPlayerLife(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        int startX = tileSize / 2;
        int y = tileSize / 2;

        var player = gameWindow.getPlayer();

        int maxHearts = player.getMaxLife() / 2;
        int life = player.getLife();
        int fullHearts = life / 2;
        int halfHearts = life % 2;

        for (int i = 0; i < maxHearts; i++) {
            int x = startX + i * tileSize;
            if (i < fullHearts) {
                g2.drawImage(heartFull, x, y, tileSize, tileSize, null);
            } else if (i == fullHearts && halfHearts == 1) {
                g2.drawImage(heartHalf, x, y, tileSize, tileSize, null);
            } else {
                g2.drawImage(heartBlank, x, y, tileSize, tileSize, null);
            }
        }
    }

    public void drawLoadPlayerLife(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        int startX = tileSize / 2;
        int y = tileSize / 2;

        var player = gameWindow.getPlayer();

        int maxHearts = player.getMaxLife() / 2;
        int life = player.getLife();
        int fullHearts = life / 2;
        int halfHearts = life % 2;

        for (int i = 0; i < maxHearts; i++) {
            int x = startX + i * tileSize;
            if (i < fullHearts) {
                g2.drawImage(heartFull, x, y, tileSize, tileSize, null);
            } else if (i == fullHearts && halfHearts == 1) {
                g2.drawImage(heartHalf, x, y, tileSize, tileSize, null);
            } else {
                g2.drawImage(heartBlank, x, y, tileSize, tileSize, null);
            }
        }
    }

    private void drawManaBar(Graphics2D g2) {

        Player p = gameWindow.getPlayer();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int x = MANA_BAR_X;
        int y = MANA_BAR_Y;
        int w = MANA_BAR_WIDTH;
        int h = MANA_BAR_HEIGHT;
        int arc = 20;

        g2.setColor(Color.BLACK);
        g2.fillRoundRect(x, y, w, h, arc, arc);

        double ratio = (double) p.getMana() / p.getMaxMana();
        int filledH = (int) (h * ratio);
        int filledY = y + (h - filledH);
        g2.setColor(Color.GREEN);
        g2.fillRoundRect(x, filledY, w, filledH, arc, arc);

        Color offWhite = new Color(255, 255, 255);
        g2.setColor(offWhite);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, w, h, arc, arc);
    }

    private void drawPauseScreen(Graphics2D g2) {
        g2.setFont(arial80Bold.deriveFont(Font.PLAIN, 80F));
        String text = "PAUSED";
        int x = getXForCenteredText(g2, text);
        int y = FrameApp.getScreenHeight() / 2;
        g2.drawString(text, x, y);
    }

    public void drawDialogueScreen(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

        int x = tileSize * 2;
        int y = tileSize / 2;
        int width = FrameApp.getScreenWidth() - (FrameApp.getTileSize() * 4);
        int height = tileSize * 4;

        drawSubWindow(g2, x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 32F));
        x += tileSize;
        y += tileSize;
        g2.drawString(currentDialogueMessage, x, y);
    }

    private void drawCharacterScreen(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

        final int frameX = tileSize / 2;
        final int frameY = tileSize / 2;
        final int frameWidth = tileSize * 5;
        final int frameHeight = tileSize * 10;
        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));

        int textX = frameX + 20;
        int textY = frameY + tileSize;
        final int lineHeight = 35;

        g2.drawString("レベル", textX, textY);
        textY += lineHeight;
        g2.drawString("体力", textX, textY);
        textY += lineHeight;
        g2.drawString("力", textX, textY);
        textY += lineHeight;
        g2.drawString("器用さ", textX, textY);
        textY += lineHeight;
        g2.drawString("攻撃力", textX, textY);
        textY += lineHeight;
        g2.drawString("防御力", textX, textY);
        textY += lineHeight;
        g2.drawString("経験値", textX, textY);
        textY += lineHeight;
        g2.drawString("次のレベル", textX, textY);
        textY += lineHeight;
        g2.drawString("所持金", textX, textY);
        textY += lineHeight;
        g2.drawString("右手", textX, textY + 20);
        textY += lineHeight;
        g2.drawString("左手", textX, textY + 38);

        g2.drawString(gameWindow.getPlayer().getCurrentWeapon().getName(), textX + 80, textY - 15);
        textY += lineHeight;
        g2.drawString(gameWindow.getPlayer().getCurrentShield().getName(), textX + 90, textY + 5);

        int tailX = (frameX + frameWidth) - 30;
        textY = frameY + tileSize;
        String value;

        value = String.valueOf(gameWindow.getPlayer().getLevel());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = gameWindow.getPlayer().getLife() +
                "/" + gameWindow.getPlayer().getMaxLife();
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getStrength());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getDexterity());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().calculateTotalAttack());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().calculateTotalDefense());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getExp());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getNextLevelExp());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gameWindow.getPlayer().getCoin());
        textX = getXForAlignToRightText(g2, value, tailX);
        g2.drawString(value, textX, textY);
    }

    private void drawBattleLogMessage(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();
        int messageX = tileSize;
        int messageY = tileSize * 4;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));

        for (int i = 0; i < message.size(); i++) {

            if (message.get(i) != null) {

                g2.setColor(Color.BLACK);
                g2.drawString(message.get(i), messageX + 2, messageY + 2);

                g2.setColor(Color.white);
                g2.drawString(message.get(i), messageX, messageY);

                int counter = messageCounter.get(i) + 1;
                messageCounter.set(i, counter);
                messageY += 50;

                if (messageCounter.get(i) > 180) {
                    message.remove(i);
                    messageCounter.remove(i);
                }
            }
        }
    }

    public void drawInventory(Graphics2D g2, Entity entity, boolean cursor) {

        if (entity == null || entity.getInventory() == null) return;
        System.out.println("NPC Inventory size = " + entity.getInventory().size());

        int frameX = 0;
        int frameY = 0;
        int frameWidth = 0;
        int frameHeight = 0;
        int slotCol = 0;
        int slotRow = 0;
        int tileSize = FrameApp.getTileSize();

        if (entity == gameWindow.getPlayer()) {
            frameX = tileSize * 9;
            frameY = tileSize;
            frameWidth = (tileSize * 6) + tileSize / 2;
            frameHeight = tileSize * 5;
            slotRow = playerSlotRow;
            slotCol = playerSlotCol;
        } else {
            frameX = tileSize * 2;
            frameY = tileSize;
            frameWidth = (tileSize * 6) + tileSize / 2;
            frameHeight = tileSize * 5;
            slotRow = npcSlotRow;
            slotCol = npcSlotCol;
        }

        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        final int slotXstart = frameX + (tileSize / 2) - 4;
        final int slotYstart = frameY + (tileSize / 2) - 4;
        int slotX = slotXstart;
        int slotY = slotYstart;

        int slotSize = tileSize + 3;
        int cursorX = slotXstart + (slotSize * slotRow);
        int cursorY = slotYstart + (slotSize * slotCol);
        int cursorWidth = tileSize;
        int cursorHeight = tileSize;

        // ハイライト用ID取得
        EntityType wId = entity.getCurrentWeapon() != null ? entity.getCurrentWeapon().getType() : null;
        EntityType sId = entity.getCurrentShield() != null ? entity.getCurrentShield().getType() : null;

        // “先頭だけ”ハイライト済みフラグ
        boolean weaponHighlighted = false;
        boolean shieldHighlighted = false;

        if (cursor) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(cursorX, cursorY - 2, cursorWidth, cursorHeight, 10, 10);
        }

        List<Entity> inv = entity.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            Entity item = inv.get(i);
            EntityType type = item.getType();

            // まだハイライトしておらず、かつ装備中のTypeと一致するなら
            if (!weaponHighlighted && wId != null && type.equals(wId)) {
                g2.setColor(new Color(240, 190, 90));
                g2.fillRoundRect(slotX, slotY, tileSize, tileSize, 10, 10);
                weaponHighlighted = true;
            } else if (!shieldHighlighted && sId != null && type.equals(sId)) {
                g2.setColor(new Color(240, 190, 90));
                g2.fillRoundRect(slotX, slotY, tileSize, tileSize, 10, 10);
                shieldHighlighted = true;
            }

            g2.drawImage(item.getImage(), slotX, slotY, tileSize, tileSize, null);

            slotX += slotSize;

            if (i == 4 || i == 9 || i == 14) {
                slotX = slotXstart;
                slotY += slotSize;
            }
        }

        int dFrameX = frameX;
        int dFrameY = frameY + frameHeight;
        int dFrameWidth = frameWidth;
        int dFrameHeight = tileSize * 3;

        int textX = dFrameX + 20;
        int textY = dFrameY + tileSize;
        g2.setFont(g2.getFont().deriveFont(28F));

        if (cursor) {

            int itemIndex = getItemIndexOnSlot(slotRow, slotCol);

            if (0 <= itemIndex && itemIndex < inv.size()) {
                Entity item = inv.get(itemIndex);
                if (item != null) {
                    String desc = item.getDescription();
                    if (desc != null && !desc.isEmpty()) {
                        drawSubWindow(g2, dFrameX, dFrameY, dFrameWidth, dFrameHeight);
                        int ty = textY;
                        for (String line : desc.split("\n")) {
                            g2.drawString(line, textX, ty);
                            ty += 32;
                        }
                    }
                }
            }
        }
    }

    public void drawTradeScreen(Graphics2D g2) {
        tradeCtx.draw(g2);
    }

    public void updateTrade(int keyCode) {
        tradeCtx.handleKey(keyCode);
    }

    public void drawSaveScreen(Graphics2D g2) {
        saveCtx.draw(g2);
    }

    public void updateSave(int keyCode) {
        tradeCtx.handleKey(keyCode);
    }

    public int getItemIndexOnSlot(int slotRow, int slotCol) {
        int itemIndex = slotRow + (slotCol * 5);
        return itemIndex;
    }

    public void drawSubWindow(Graphics2D g2, int x, int y, int width, int height) {

        Color color = new Color(0, 0, 0, 210);
        g2.setColor(color);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        color = new Color(255, 255, 255);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }


    private void drawMessage(Graphics2D g2) {
        g2.setFont(arial40.deriveFont(Font.PLAIN, 40F));
        int x = getXForCenteredText(g2, currentDialogueMessage);
        int y = (int) (FrameApp.getScreenHeight() * 0.8);
        g2.drawString(currentDialogueMessage, x, y);
    }

    public int getXForCenteredText(Graphics2D g2, String text) {
        int textWidth = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return FrameApp.getScreenWidth() / 2 - textWidth / 2;
    }

    public int getXForAlignToRightText(Graphics2D g2, String text, int tailX) {
        int textWidth = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = tailX - textWidth;
        return x;
    }

    public void setCurrentDialogueMessage(String currentDialogueMessage) {
        this.currentDialogueMessage = currentDialogueMessage;
    }

    public int getPlayerSlotRow() {
        return playerSlotRow;
    }

    public void setPlayerSlotRow(int playerSlotRow) {
        this.playerSlotRow = playerSlotRow;
    }

    public int getPlayerSlotCol() {
        return playerSlotCol;
    }

    public void setPlayerSlotCol(int playerSlotCol) {
        this.playerSlotCol = playerSlotCol;
    }

    public int getNpcSlotRow() {
        return npcSlotRow;
    }

    public void setNpcSlotRow(int npcSlotRow) {
        this.npcSlotRow = npcSlotRow;
    }

    public int getNpcSlotCol() {
        return npcSlotCol;
    }

    public void setNpcSlotCol(int npcSlotCol) {
        this.npcSlotCol = npcSlotCol;
    }

    public boolean isDialogueOn() {
        return dialogueOn;
    }

    public void setDialogueOn(boolean dialogueOn) {
        this.dialogueOn = dialogueOn;
    }

    public Entity getNpc() {
        return npc;
    }

    public void setNpc(Entity npc) {
        this.npc = npc;
    }

    public int getSubState() {
        return subState;
    }

    public void setSubState(int subState) {
        this.subState = subState;
    }

    public BufferedImage getCoin() {
        return coin;
    }

    public void setCoin(BufferedImage coin) {
        this.coin = coin;
    }

    public TriforcePanel getTriforcePanel() {
        return triforcePanel;
    }

    /**
     * セーブ中フラグを外部からセット/解除する（KeyHandler から呼ばれる）
     */
    public void setSaveInProgress(boolean inProgress) {
        this.saveInProgress = inProgress;
    }

    /**
     * 指定スロットのメタを再読み込みして反映する（slot は 0-based 想定）
     */
    public void reloadSaveMeta(int slotZeroBased) {
        if (slotZeroBased < 0 || slotZeroBased >= SLOT_COUNT) return;
        // SaveManager の仕様に合わせて引数を調整（下は 0-based 想定）
        SaveMeta meta = SaveManager.loadMeta(slotZeroBased); // もし SaveManager が 1-based なら slotZeroBased+1
        if (saveMetas == null || saveMetas.length != SLOT_COUNT) {
            saveMetas = new SaveMeta[SLOT_COUNT];
        }
        saveMetas[slotZeroBased] = (meta != null) ? meta : new SaveMeta();
    }

    /**
     * セーブメニューを UI 側で閉じる（State 変更は呼び出し元で行う想定）
     */
    public void closeSaveMenuUI() {
        saveMenuOpen = false;
        saveInProgress = false;
        saveMenuSelected = 0;
        // メッセージは残す/消すは設計次第。ここではメッセージは残す。
        // キー状態を安全にクリア
        if (gameWindow != null && gameWindow.getKeyHandler() != null) {
            gameWindow.getKeyHandler().clearAllKeys();
        }
    }

    /**
     * 選択インデックスを返す（0-based）
     */
    public int getSelectedSaveSlot() {
        return Math.max(0, Math.min(SLOT_COUNT - 1, saveMenuSelected));
    }

    public Font getArial40() {
        return arial40;
    }
}