package ui;

import entity.type.HeartType;
import entity.type.NpcMerChantType;
import entity.type.VertBronzeType;
import factory.EntityFactory;
import monster.MonGreenGoblin;
import object.ObjGreenForceFragment;
import popup.PopupManager;
import entity.Entity;
import entity.type.EntityType;
import frame.FrameApp;
import game.GameState;
import player.Player;
import player.SpriteManager;
import save.SaveManager;
import save.SaveMeta;
import ui.state.load.LoadScreenContext;
import ui.state.save.SaveScreenContext;
import ui.state.sleep.SleepScreenContext;
import ui.state.trade.TradeScreenContext;
import window.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;
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
    private ObjGreenForceFragment objGreenForceFragment;
    private SpriteManager spriteManager = new SpriteManager();
    private PopupManager popupManager = new PopupManager(64);

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

    private SaveMeta[] saveMetas;
    private final int SLOT_COUNT = 3;
    private int loadSlotIndex = 0;

    private final TradeScreenContext tradeCtx;
    private final SaveScreenContext saveCtx;
    private final LoadScreenContext loadCtx;
    private final SleepScreenContext sleepCtx;

    // セーブメニュー関連
    private int saveMenuSelected = 0;           // 現在選択中のスロット（0-based）
    private boolean saveInProgress = false;     // 保存処理中フラグ
    private boolean saveMenuOpen = false;       // セーブメニューが開いているか

    private boolean awaitingChoice = false;
    private Entity currentChoiceNpc = null; // NpcMalonyChicken など
    private String choiceTitle = null;
    private String[] currentOptions = null;
    private int selectedOption = 0;

    private static final long MAX_PLAY_SECONDS = 3_599_999L;

    private boolean showGameOver = false;
    private double gameOverTimer = 0.0;
    private final double gameOverTextDelay = 0.4; // 既存値に合わせる
    private float redAlpha = 0f;
    private final double redFadeDuration = 1.2; // 赤が最大になるまでの秒数

    private int charIndex = 0;
    private int charCounter = 0;
    private final int CHAR_SPEED = 2;

    // --- ボス演出用のフィールド ---
    private boolean isBossCutsceneActive = false;
    private int bossCutscenePhase = 0;      // 演出の進行段階（フェーズ）
    private double bossCutsceneTimer = 0.0; // 経過時間を測るタイマー（秒）
    private MonGreenGoblin targetBoss;      // 演出対象のボス

    // 画面シェイク用のパラメータ
    private int screenShakeTimer = 0;
    private int shakeIntensity = 5;

    public UI(GameWindow gameWindow, EntityFactory entityFactory) {
        this.gameWindow = gameWindow;
        this.tradeCtx = new TradeScreenContext(gameWindow, this);
        this.saveCtx = new SaveScreenContext(gameWindow, this);
        this.loadCtx = new LoadScreenContext(gameWindow, this);
        this.sleepCtx = new SleepScreenContext(gameWindow, this);
        this.objGreenForceFragment = new ObjGreenForceFragment(gameWindow);
        this.arial40 = new Font("エリア", Font.PLAIN, 40);
        this.arial80Bold = new Font("エリア", Font.BOLD, 80);
        this.messageOn = false;
        this.currentDialogueMessage = "";
        this.npc = entityFactory.create(new NpcMerChantType());
        Entity heart = entityFactory.create(new HeartType());
        heartFull = heart.getImage();
        heartHalf = heart.getImage2();
        heartBlank = heart.getImage3();

        Entity bronzeCoin = entityFactory.create(new VertBronzeType());
        coin = bronzeCoin.getImage();
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
            drawLoadScreen(g2);

        } else if (gameState == GameState.PLAY) {

            gameWindow.setDialogueActive(false);
            drawPlayerLife(g2);
            popupManager.drawAll(g2);
            drawManaBar(g2);

        } else if (gameState == GameState.PAUSE) {

            gameWindow.setDialogueActive(false);
            drawPlayerLife(g2);
            drawPauseScreen(g2);

        } else if (gameState == GameState.DIALOGUE) {

            gameWindow.setDialogueActive(true);
            drawDialogueScreen(g2);

        } else if (gameState == GameState.CHARACTER) {

            gameWindow.setDialogueActive(false);
            drawCharacterScreen(g2);
            drawFragmentScreen(g2);
            drawInventory(g2, gameWindow.getPlayer(), true);

        } else if (gameState == GameState.GAME_OVER) {
            drawGameOverScreen(g2);

        } else if (gameState == GameState.TRADE) {
            drawTradeScreen(g2);

        } else if (gameState == GameState.SAVE) {
            drawSaveScreen(g2);

        } else if (gameState == GameState.SLEEP) {
            drawSleepScreen(g2);
        }

        if (messageOn) {
            drawMessage(g2);
        }
    }

    // 演出をスタートする
    public void startBossCutscene(MonGreenGoblin boss) {
        this.targetBoss = boss;
        this.isBossCutsceneActive = true;
        this.bossCutscenePhase = 1; // フェーズ1から開始
        this.bossCutsceneTimer = 0.0;

        System.out.println("UI: ボス登場演出スタート！");
    }

    // 毎フレーム呼ばれて演出の進行状況を更新
    public void updateBossCutscene(double deltaSeconds) {

        if (!isBossCutsceneActive) return;

        bossCutsceneTimer += deltaSeconds;

        switch (bossCutscenePhase) {
            case 1: // フェーズ1：カメラがボスに寄りつつ、少し静止（約1.5秒）
                if (bossCutsceneTimer >= 1.5) {
                    bossCutsceneTimer = 0.0;
                    bossCutscenePhase = 2; // 画面シェイク

                    // 画面を0.5秒ほど激しく揺らす
                    startScreenShake(30, 6);
                }
                break;

            case 2: // フェーズ2：ボス名カットイン表示（約2.5秒）
                if (bossCutsceneTimer >= 2.5) {
                    bossCutsceneTimer = 0.0;
                    bossCutscenePhase = 3; //（終了処理）
                }
                break;

            case 3: // フェーズ3：演出終了、通常のプレイ状態に戻す
                isBossCutsceneActive = false;
                bossCutscenePhase = 0;

                if (gameWindow.getKeyHandler() != null) {
                    gameWindow.getKeyHandler().clearAllKeys();
                }

                // ゲームステートを PLAY に戻してプレイヤーの操作を再開
                gameWindow.setGameState(GameState.PLAY);
                System.out.println("UI: ボス登場演出終了。戦闘開始！");
                break;
        }

        // シェイクタイマーの減算
        if (screenShakeTimer > 0) {
            screenShakeTimer--;
        }
    }

    // ボス演出中の描画
    public void drawBossCutscene(Graphics2D g2) {

        if (!isBossCutsceneActive) return;

        int screenWidth = FrameApp.getScreenWidth();
        int screenHeight = FrameApp.getScreenHeight();
        int tileSize = FrameApp.getTileSize();

        // 上下の黒帯（レターボックス）を描画
        g2.setColor(Color.black);
        int barHeight = tileSize * 2; // 帯の太さ

        // シンプルに常時表示
        g2.fillRect(0, 0, screenWidth, barHeight);                         // 上帯
        g2.fillRect(0, screenHeight - barHeight, screenWidth, barHeight);  // 下帯

        // B. フェーズ2以降で「ボス名カットイン」を表示
        if (bossCutscenePhase >= 2) {
            g2.setColor(Color.white);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 28f));

            String titleText = "【 始原の森の守護者 】";
            String nameText = "グリーンゴブリン";

            int x1 = getXForCenteredText(titleText, g2);
            int y1 = screenHeight / 2 - 20;
            g2.drawString(titleText, x1, y1);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40f));
            g2.setColor(new Color(100, 255, 100));
            int x2 = getXForCenteredText(nameText, g2);
            int y2 = screenHeight / 2 + 30;
            g2.drawString(nameText, x2, y2);
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
            int lifeX0 = frameX; // frameX はそのスロットのサブウィンドウ左上
            int lifeY0 = frameY;
            drawPlayerLifeAt(g2, lifeX0, lifeY0, meta.getHp(), meta.getMaxHp());
            // プレイヤープレビュー（右側に表示する例）
            int previewX = frameX + frameWidth - tileSize * 3;
            int previewY = frameY + 8;
            drawSpritePreview(g2, meta, previewX, previewY, tileSize * 2, tileSize * 2);


            String pt = formatPlayTime(meta.getPlayTimeSeconds());
            g2.setFont(g2.getFont().deriveFont(20f));
            g2.setColor(Color.WHITE);
            g2.drawString("プレイ時間: " + pt, frameX + 8, frameY + tileSize * 2);
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

        // slot1 がセーブされているならハートとプレビューを描画
        if (saveMetas != null && saveMetas.length > 0 && saveMetas[1] != null && saveMetas[1].exists()) {
            SaveMeta meta = saveMetas[1];
            int lifeX1 = frameX;
            int lifeY1 = frameY;
            drawPlayerLifeAt(g2, lifeX1, lifeY1, meta.getHp(), meta.getMaxHp());
            // プレイヤープレビュー（右側に表示する例）
            int previewX = frameX + frameWidth - tileSize * 3;
            int previewY = frameY + 8;
            drawSpritePreview(g2, meta, previewX, previewY, tileSize * 2, tileSize * 2);

            String pt = formatPlayTime(meta.getPlayTimeSeconds());
            g2.setFont(g2.getFont().deriveFont(20f));
            g2.setColor(Color.WHITE);
            g2.drawString("プレイ時間: " + pt, frameX + 8, frameY + tileSize * 2);
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

        // slot2 がセーブされているならハートとプレビューを描画
        if (saveMetas != null && saveMetas.length > 0 && saveMetas[2] != null && saveMetas[2].exists()) {
            SaveMeta meta = saveMetas[2];
            int lifeX2 = frameX;
            int lifeY2 = frameY;
            drawPlayerLifeAt(g2, lifeX2, lifeY2, meta.getHp(), meta.getMaxHp());
            // プレイヤープレビュー（右側に表示する例）
            int previewX = frameX + frameWidth - tileSize * 3;
            int previewY = frameY + 8;
            drawSpritePreview(g2, meta, previewX, previewY, tileSize * 2, tileSize * 2);

            String pt = formatPlayTime(meta.getPlayTimeSeconds());
            g2.setFont(g2.getFont().deriveFont(20f));
            g2.setColor(Color.WHITE);
            g2.drawString("プレイ時間: " + pt, frameX + 8, frameY + tileSize * 2);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(28F));
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
        y = tileSize * 2;
        g2.drawString(text, x, y);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x - 4, y - 4);

        g2.setFont(g2.getFont().deriveFont(50f));

        text = "ロードゲーム";
        x = getXForCenteredText(g2, text);
        y += tileSize * 6;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 0) {
            g2.drawString(">", x - 40, y);
        }

        text = "ニューゲーム";
        x = getXForCenteredText(g2, text);
        y += tileSize + 10;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 1) {
            g2.drawString(">", x - 40, y);
        }

        text = "終了";
        x = getXForCenteredText(g2, text);
        y += tileSize + 10;
        g2.drawString(text, x, y);
        if (gameWindow.getKeyHandler().getCommandNum() == 2) {
            g2.drawString(">", x - 40, y);
        }

        drawGreenForceFragments(g2);
    }

    public void drawGreenForceFragments(Graphics2D g2) {

        if (objGreenForceFragment == null) {
            return;
        }

        // タイトル画面でもアニメーションを進行させる
        objGreenForceFragment.update();

        BufferedImage fragmentImage = objGreenForceFragment.getImage();
        if (fragmentImage == null) {
            return;
        }

        int tileSize = FrameApp.getTileSize();
        int screenWidth = FrameApp.getScreenWidth();
        int centerX = screenWidth / 2;

        // 描画サイズを定数化
        final int DRAW_SIZE = tileSize * 2;

        // 6つの欠片の相対的なオフセット配置
        int[] drawXList = {
                // 一番上
                centerX - tileSize,
                // 2段目
                // 左から2番目
                (int) (centerX - (tileSize * 1.5)),
                // 2段目
                // 左から3番目
                (tileSize * 8) - 20,
                // 3段目
                // 左から4番目
                centerX - (tileSize * 2),
                // 左から5番目
                (tileSize * 9) - 40,
                // 一番下
                centerX - tileSize + 5
        };

        int[] drawYList = {
                tileSize * 2,
                tileSize * 3,
                tileSize * 3,
                tileSize * 4,
                tileSize * 4,
                tileSize * 5
        };

        for (int i = 0; i < drawXList.length; i++) {
            g2.drawImage(fragmentImage, drawXList[i], drawYList[i], DRAW_SIZE, DRAW_SIZE, null);
        }
    }

    public void initLoadScreen() {
        saveMetas = new SaveMeta[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            saveMetas[i] = SaveManager.loadMeta(i); // 0-based index
        }

        // 初期選択を「最初に存在するセーブ」にする（なければ 0 のまま）
        loadSlotIndex = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (saveMetas[i] != null && saveMetas[i].exists()) {
                loadSlotIndex = i;
                break;
            }
        }
    }

    public void drawDialogueLoadScreen(Graphics2D g2) {

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
            // frameX はそのスロットのサブウィンドウ左上
            int lifeX0 = frameX;
            int lifeY0 = frameY;
            drawPlayerLifeAt(g2, lifeX0, lifeY0, meta.getHp(), meta.getMaxHp());

            // プレビューは既存の位置で描画
            int previewX = frameX + frameWidth - tileSize * 3;
            int previewY = frameY + 8;
            drawSpritePreview(g2, meta, previewX, previewY, tileSize * 2, tileSize * 2);

            String pt = formatPlayTime(meta.getPlayTimeSeconds());
            g2.setFont(g2.getFont().deriveFont(20f));
            g2.setColor(Color.WHITE);
            g2.drawString("プレイ時間: " + pt, frameX + 8, frameY + tileSize * 2);
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
            // frameX はそのスロットのサブウィンドウ左上
            int lifeX1 = frameX;
            int lifeY1 = frameY;
            drawPlayerLifeAt(g2, lifeX1, lifeY1, meta.getHp(), meta.getMaxHp());

            // プレビューは既存の位置で描画
            int previewX = frameX + frameWidth - tileSize * 3;
            int previewY = frameY + 8;
            drawSpritePreview(g2, meta, previewX, previewY, tileSize * 2, tileSize * 2);


            String pt = formatPlayTime(meta.getPlayTimeSeconds());
            g2.setFont(g2.getFont().deriveFont(20f));
            g2.setColor(Color.WHITE);
            g2.drawString("プレイ時間: " + pt, frameX + 8, frameY + tileSize * 2);
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

        // slot2 のサブウィンドウを描いた直後
        if (saveMetas != null && saveMetas.length > 0 && saveMetas[2] != null && saveMetas[2].exists()) {
            SaveMeta meta = saveMetas[2];
            // ハートをサブウィンドウ左上に描く（微調整は y 座標で）
            // frameX はそのスロットのサブウィンドウ左上
            int lifeX2 = frameX;
            int lifeY2 = frameY;
            drawPlayerLifeAt(g2, lifeX2, lifeY2, meta.getHp(), meta.getMaxHp());

            // プレビューは既存の位置で描画
            int previewX = frameX + frameWidth - tileSize * 3;
            int previewY = frameY + 8;
            drawSpritePreview(g2, meta, previewX, previewY, tileSize * 2, tileSize * 2);

            String pt = formatPlayTime(meta.getPlayTimeSeconds());
            g2.setFont(g2.getFont().deriveFont(20f));
            g2.setColor(Color.WHITE);
            g2.drawString("プレイ時間: " + pt, frameX + 8, frameY + tileSize * 2);
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
//        System.out.println("drawSpritePreview: meta=" + meta);
        if (meta == null) {
//            System.out.println("meta is null");
        } else {
//            System.out.println("key=" + meta.getSpriteKey() + ", facing=" + meta.getFacing() + ", savedAt=" + meta.getSavedAt());
        }
//        System.out.println("spriteManager=" + spriteManager);
        BufferedImage sprite = null;
        try {
            sprite = spriteManager.getSprite(meta.getSpriteKey(), meta.getFacing(), 0);
        } catch (Exception e) {
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

        // キー入力状態をリセット（エンターキーの持ち越しを防ぐ）
        if (gameWindow.getKeyHandler() != null) {
            gameWindow.getKeyHandler().setPlayerEnter(false);
            gameWindow.getKeyHandler().clearAllKeys();
        }

        // UIのカーソル位置（commandNum）を初期化（一番上にする）
        if (gameWindow.getUI() != null) {
            gameWindow.getKeyHandler().setCommandNum(0);
            gameWindow.setGameState(GameState.TITLE);
        }

        // ゲーム側の状態をタイトルに切り替え
        gameWindow.setGameState(GameState.TITLE);

        if (gameWindow.getSoundmanager() != null) {
            gameWindow.getSoundmanager().stopBGM();
        }
    }

    public void showGameOver() {
        if (showGameOver) return;
        showGameOver = true;
        gameOverTimer = 0.0;
        redAlpha = 0f;
    }

    /**
     * 毎フレーム呼ぶ（GameWindow.update から deltaSeconds を渡す）
     */

    public void updateGameOver(double deltaSeconds) {
        if (!showGameOver) return;

        // タイマー増加
        gameOverTimer += deltaSeconds;

        // redAlpha を 0 -> 1 に線形に増やす（redFadeDuration 秒で最大）
        if (redAlpha < 1.0f) {
            redAlpha += (float) (deltaSeconds / redFadeDuration);
            if (redAlpha > 1.0f) redAlpha = 1.0f;
        }
    }

    public void drawGameOverScreen(Graphics2D g2) {

        if (!showGameOver) return;

        // 赤オーバーレイ
        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, redAlpha * 0.6f));
        g2.setColor(Color.RED);
        g2.fillRect(0, 0, gameWindow.getWidth(), gameWindow.getHeight());
        g2.setComposite(oldComp);

        if (gameOverTimer >= gameOverTextDelay) {

            int tileSize = FrameApp.getTileSize();

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

        // ダイアログ枠の描画
        int x = FrameApp.getTileSize() * 2;
        int y = FrameApp.getTileSize() / 2;
        int width = FrameApp.getScreenWidth() - (FrameApp.getTileSize() * 4);
        int height = FrameApp.getTileSize() * 4;

        drawSubWindow(g2, x, y, width, height);

        // 文字列の更新（1文字ずつ増やすタイピング演出）
        charCounter++;
        if (charCounter >= CHAR_SPEED) {
            charCounter = 0;
            if (charIndex < currentDialogueMessage.length()) {
                charIndex++;
                char currentVisibleChar = currentDialogueMessage.charAt(charIndex - 1);

                // スペースや改行、句読点以外でピコピコ音を鳴らす
                if (currentVisibleChar != ' ' && currentVisibleChar != ' ' && currentVisibleChar != '\n' && currentVisibleChar != '、' && currentVisibleChar != '。') {
                    gameWindow.getSoundmanager().playTextBlip();
                }
            }
        }

        // 表示対象の文字列を取得
        int safeIndex = Math.min(charIndex, currentDialogueMessage.length());
        String visibleText = currentDialogueMessage.substring(0, safeIndex);

        // 自動改行しながら描画
        g2.setFont(g2.getFont().deriveFont(28F));
        g2.setColor(Color.WHITE);

        int textX = x + FrameApp.getTileSize() / 2;
        int textY = y + FrameApp.getTileSize();

        // テキスト枠の内幅
        int maxTextWidth = width - FrameApp.getTileSize();

        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = fm.getHeight();

        // 1文字ずつ幅をチェックして改行しながら描画
        StringBuilder line = new StringBuilder();
        for (char c : visibleText.toCharArray()) {
            if (c == '\n' || fm.stringWidth(line.toString() + c) > maxTextWidth) {
                g2.drawString(line.toString(), textX, textY);
                textY += lineHeight;
                line = new StringBuilder();
                if (c == '\n') continue;
            }
            line.append(c);
        }
        // 残りの文字を描画
        if (line.length() > 0) {
            g2.drawString(line.toString(), textX, textY);
        }

        if (awaitingChoice) {
            drawChoice(g2);
        }
    }

    private void drawCharacterScreen(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

        final int frameX = tileSize / 2;
        final int frameY = tileSize / 2;
        final int frameWidth = tileSize * 6;
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
        g2.drawString("右手", textX, textY);
        textY += lineHeight;
        g2.drawString("左手", textX, textY);
        textY += lineHeight;


        // プレイ時間を取得して整形（日本語表示）
        long playSeconds = gameWindow.getPlayer().getPlayTimeSeconds();
        String playTimeText = "プレイ時間: " + formatPlayTime(playSeconds);
        g2.drawString(playTimeText, textX - 15, textY + 25);

        // 保存時プレイ時間（ロードされた値）を表示
        long savedSeconds = gameWindow.getLoadedPlayTimeSeconds();
        String savedText;
        if (savedSeconds >= 0 && savedSeconds == playSeconds) {
            savedText = "プレイ時間: " + formatPlayTime(savedSeconds);
            g2.drawString(savedText, textX - 15, textY + 25);
        } else {
            savedText = "保存データ: なし";
        }

        g2.drawString(gameWindow.getPlayer().getCurrentWeapon().getName(), textX + 100, textY - tileSize - 25);
        textY += lineHeight;
        g2.drawString(gameWindow.getPlayer().getCurrentShield().getName(), textX + 100, textY - tileSize - 25);

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

    private void drawFragmentScreen(Graphics2D g2) {

        int tileSize = FrameApp.getTileSize();

        final int frameX = (tileSize * 6) + 20;
        final int frameY = (tileSize * 6) - 20;
        final int frameWidth = (tileSize * 3) - 15;
        final int frameHeight = tileSize * 5;
        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        // プレイヤーが「グリーンフォースのかけら」を1枚も持っていなければ描画しない
        int fragmentCount = gameWindow.getPlayer().getGreenForceFragmentCount();
        if (fragmentCount <= 0) {
            return;
        }

        if (objGreenForceFragment == null) {
            return;
        }

        // タイトル画面やメニュー画面でもアニメーションを進行させる
        objGreenForceFragment.update();

        BufferedImage fragmentImage = objGreenForceFragment.getImage();
        if (fragmentImage == null) {
            return;
        }

        int screenWidth = FrameApp.getScreenWidth();
        int centerX = screenWidth / 2;

        // 描画サイズを定数化
        final int DRAW_SIZE = tileSize;

        // 6つの欠片の相対的なオフセット配置
        int[] drawXList = {
                // 一番上
                (centerX - tileSize) + 15,
                // 2段目
                // 左から2番目
                (int) (centerX - (tileSize * 1.5)),
                // 2段目
                // 左から3番目
                (tileSize * 8) - 20,
                // 3段目
                // 左から4番目
                centerX - (tileSize * 2),
                // 左から5番目
                (tileSize * 9) - 40,
                // 一番下
                centerX - tileSize + 5
        };

        int[] drawYList = {
                tileSize * 6,
                tileSize * 3,
                tileSize * 3,
                tileSize * 4,
                tileSize * 4,
                tileSize * 5
        };

        // 所持数に応じて描画する最大6枚
        int drawCount = Math.min(fragmentCount, drawXList.length);

        for (int i = 0; i < drawCount; i++) {
            g2.drawImage(fragmentImage, drawXList[i], drawYList[i], DRAW_SIZE, DRAW_SIZE, null);
        }
    }

    private String formatPlayTime(long seconds) {
        // 二重チェックでクランプ
        if (seconds < 0) seconds = 0;
        if (seconds >= MAX_PLAY_SECONDS) {
            return "999:59:59";
        }
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = seconds / 3600;
        // 時を3桁ゼロ埋めで表示（000, 001, ..., 999）
        return String.format("%03d:%02d:%02d", h, m, s);
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
            frameY = tileSize / 2;
            frameWidth = (tileSize * 6) + tileSize / 2;
            frameHeight = tileSize * 5;
            slotCol = playerSlotCol;
            slotRow = playerSlotRow;
        } else {
            frameX = tileSize * 2;
            frameY = tileSize;
            frameWidth = (tileSize * 6) + tileSize / 2;
            frameHeight = tileSize * 5;
            slotCol = npcSlotCol;
            slotRow = npcSlotRow;
        }

        drawSubWindow(g2, frameX, frameY, frameWidth, frameHeight);

        final int slotXstart = frameX + (tileSize / 2) - 4;
        final int slotYstart = frameY + (tileSize / 2) - 4;
        int slotX = slotXstart;
        int slotY = slotYstart;

        int slotSize = tileSize + 3;
        int cursorX = slotXstart + (slotSize * slotCol);
        int cursorY = slotYstart + (slotSize * slotRow);
        int cursorWidth = tileSize;
        int cursorHeight = tileSize;

        // ハイライト用ID取得
        EntityType wId = entity.getCurrentWeapon() != null ? entity.getCurrentWeapon().getType() : null;
        EntityType sId = entity.getCurrentShield() != null ? entity.getCurrentShield().getType() : null;
        EntityType lId = entity.getCurrentLight() != null ? entity.getCurrentLight().getType() : null;

        // “先頭だけ”ハイライト済みフラグ
        boolean weaponHighlighted = false;
        boolean shieldHighlighted = false;
        boolean lanternHighlighted = false;

        if (cursor) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(cursorX, cursorY - 2, cursorWidth, cursorHeight, 10, 10);
        }

        List<Entity> inv = entity.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            Entity item = inv.get(i);

            // item が null ならスキップ
            if (item == null) {
                // デバッグ
                // System.out.println("DBG: inventory slot " + i + " is null");
                slotX += slotSize;
                if (i == 4 || i == 9 || i == 14) {
                    slotX = slotXstart;
                    slotY += slotSize;
                }
                continue;
            }

            EntityType type = item.getType();

            // ハイライト（装備中の type と一致するか） - type が null の場合は false
            if (!weaponHighlighted && wId != null && type != null && type == wId) {
                g2.setColor(new Color(240, 190, 90));
                g2.fillRoundRect(slotX, slotY, tileSize, tileSize, 10, 10);
                weaponHighlighted = true;
            } else if (!shieldHighlighted && sId != null && type != null && type == sId) {
                g2.setColor(new Color(240, 190, 90));
                g2.fillRoundRect(slotX, slotY, tileSize, tileSize, 10, 10);
                shieldHighlighted = true;
            } else if (!lanternHighlighted && lId != null && type != null && type == lId) {
                g2.setColor(new Color(240, 190, 90));
                g2.fillRoundRect(slotX, slotY, tileSize, tileSize, 10, 10);
                lanternHighlighted = true;
            }

            // 画像描画は例外保護。例外時はプレースホルダを描画する
            try {
                BufferedImage img = item.getImage();
                if (img == null) {
                    // プレースホルダ
                    BufferedImage ph = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D pg = ph.createGraphics();
                    pg.setColor(Color.MAGENTA);
                    pg.fillRect(0, 0, tileSize, tileSize);
                    pg.setColor(Color.BLACK);
                    pg.drawString("?", tileSize / 3, tileSize * 2 / 3);
                    pg.dispose();
                    g2.drawImage(ph, slotX, slotY, tileSize, tileSize, null);
                } else {
                    g2.drawImage(img, slotX, slotY, tileSize, tileSize, null);
                }
            } catch (Throwable t) {
                // 描画で落ちないように握る。ログを残す。
                System.err.println("ERROR drawing image for inventory slot " + i + " item=" + item + " : " + t);
                g2.setColor(Color.RED);
                g2.fillRect(slotX, slotY, tileSize, tileSize);
            }

            // 個数表示
            int amount = 0;
            try {
                try {
                    amount = item.getAmount();
                } catch (NoSuchMethodError | AbstractMethodError e) {
                    amount = item.getCount();
                }
            } catch (Throwable t) {
                amount = 0;
            }

            if (amount > 1) {
                g2.setFont(g2.getFont().deriveFont(32f));
                String s = "" + amount;
                int amountX = getXForAlignToRightText(g2, s, slotX + 44);
                int amountY = slotY + tileSize;
                g2.setColor(new Color(60, 60, 60));
                g2.drawString(s, amountX, amountY);
                g2.setColor(Color.WHITE);
                g2.drawString(s, amountX - 3, amountY - 3);
            }

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

            int itemIndex = getItemIndexOnSlot(slotCol, slotRow);

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

    public void drawLoadScreen(Graphics2D g2) {
        loadCtx.draw(g2);
    }

    public void drawSleepScreen(Graphics2D g2) {
        sleepCtx.draw(g2);
    }

    public void updateSave(int keyCode) {
        saveCtx.handleKey(keyCode);
    }

    public void updateSleep(int keyCode) {
        sleepCtx.handleKey(keyCode);
    }

    public void updateLoad(int keyCode) {
        loadCtx.handleKey(keyCode);
    }

    public int getItemIndexOnSlot(int slotCol, int slotRow) {
        return slotCol + (slotRow * 5);
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

    public Entity getNpc() {
        return npc;
    }

    public void setNpc(Entity npc) {
        this.npc = npc;
    }

    public BufferedImage getCoin() {
        return coin;
    }

    public void setCoin(BufferedImage coin) {
        this.coin = coin;
    }

    public boolean getSaveInProgress() {
        return saveInProgress;
    }

    /**
     * セーブ中フラグを外部からセット/解除する（KeyHandler から呼ばれる）
     */

    public void setSaveInProgress(boolean inProgress) {
        this.saveInProgress = inProgress;
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

    public Font getArial40() {
        return arial40;
    }

    // saveMetas が null の場合は initLoadScreen() を呼んで初期化する想定
    public void setSaveMeta(int slot, SaveMeta meta) {
        if (saveMetas == null) initLoadScreen();
        if (slot >= 0 && slot < saveMetas.length) {
            saveMetas[slot] = meta;
        }
    }

    public boolean isAwaitingChoice() {
        return awaitingChoice;
    }

    public void showChoice(String title, String[] options, Entity npc) {
        this.awaitingChoice = true;
        this.choiceTitle = title;
        this.currentOptions = options;
        this.currentChoiceNpc = npc;
        this.selectedOption = 0;
        // ダイアログ領域にタイトルだけ表示したければここで setCurrentDialogueMessage(title) を呼ぶ
    }

    private void drawChoice(Graphics2D g2) {

        // レイアウト定義（必要に応じて調整）
        int boxX = 50;
        int boxY = 400;
        int boxW = 700;
        int boxH = 120;
        int padding = 20;

        // 背景ボックス
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 12, 12);

        // タイトル
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("MS Gothic", Font.PLAIN, 20));
        int titleX = boxX + padding;
        int titleY = boxY + padding + 20;
        g2.drawString(choiceTitle, titleX, titleY);

        // 選択肢を横並びで描画
        if (currentOptions == null) return;
        g2.setFont(new Font("MS Gothic", Font.PLAIN, 18));
        int optionY = titleY + 40;
        int optionStartX = boxX + padding;
        int gap = 220; // 選択肢間の間隔

        for (int i = 0; i < currentOptions.length; i++) {
            int optX = optionStartX + i * gap;
            String text = currentOptions[i];

            // 選択中の装飾（矢印と色）
            if (i == selectedOption) {
                g2.setColor(Color.YELLOW);
                g2.drawString(">", optX - 20, optionY);
                g2.setColor(Color.WHITE);
                // 選択中の背景
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(optX - 10, optionY - 20, 180, 30, 8, 8);
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(Color.LIGHT_GRAY);
            }

            g2.drawString(text, optX, optionY);
        }

        // ヒント（A/B キー）
        g2.setFont(new Font("MS Gothic", Font.PLAIN, 14));
        g2.setColor(Color.GRAY);
        g2.drawString("[A] 決定  [←/→] 選択  [B] キャンセル", boxX + padding, boxY + boxH - 10);
    }

    public void clearChoice() {
        this.awaitingChoice = false;
        this.currentOptions = null;
        this.currentChoiceNpc = null;
    }

    public Entity getCurrentChoiceNpc() {
        return currentChoiceNpc;
    }

    public PopupManager getDamagePopupManager() {
        return popupManager;
    }

    public void moveChoiceLeft() {
        if (!awaitingChoice || currentOptions == null) return;
        selectedOption = Math.max(0, selectedOption - 1);
    }

    public void moveChoiceRight() {
        if (!awaitingChoice || currentOptions == null) return;
        selectedOption = Math.min(currentOptions.length - 1, selectedOption + 1);
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public SleepScreenContext getSleepCtx() {
        return sleepCtx;
    }

    // --- シェイク機能の管理 ---
    public void startScreenShake(int durationFrames, int intensity) {
        this.screenShakeTimer = durationFrames;
        this.shakeIntensity = intensity;
    }

    public int getShakeOffsetX() {
        if (screenShakeTimer <= 0) return 0;
        return (int) (Math.random() * (shakeIntensity * 2 + 1)) - shakeIntensity;
    }

    public int getShakeOffsetY() {
        if (screenShakeTimer <= 0) return 0;
        return (int) (Math.random() * (shakeIntensity * 2 + 1)) - shakeIntensity;
    }

    // 中央揃え用のヘルパー
    private int getXForCenteredText(String text, Graphics2D g2) {
        int textLength = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return (FrameApp.getScreenWidth() - textLength) / 2;
    }

    public MonGreenGoblin getTargetBoss() {
        return targetBoss;
    }
}