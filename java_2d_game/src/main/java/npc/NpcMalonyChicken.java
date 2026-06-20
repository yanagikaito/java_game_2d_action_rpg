package npc;

import collision.CollisionChecker;
import entity.Entity;
import frame.FrameApp;
import game.GameState;
import map.GameMap;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class NpcMalonyChicken extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private static final int SPRITE_COUNT = 3;
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private boolean following = false;
    private final CollisionChecker collisionChecker;
    private final int questRewardCoins = 100;
    // 会話が終わって選択肢を出したか
    private boolean questOffered = false;
    // プレイヤーの選択待ち中か（UI と連携）
    private boolean awaitingChoice = false;
    // プレイヤーがクエストを受けたか
    private boolean questAccepted = false;
    // クエストが完了して報酬を渡したか
    private boolean questCompleted = false;

    public NpcMalonyChicken(GameWindow gameWindow) {
        super(gameWindow);
        this.collisionChecker = new CollisionChecker(gameWindow);
        setDirection("left");
        setSpeed(0);
        loadNPCImages();
        setDialogue();
    }

    public void loadNPCImages() {
        setSprites(sprites);
        try {
            int tileSize = FrameApp.getTileSize();
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream("npc/malonyChicken-" + DIRECTIONS[dir] + "-" + (i + 1) + ".png"));
                    BufferedImage processed = createImage(original, tileSize, tileSize);
                    sprites[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDialogue() {
        // 必要な長さの配列が Entity 側で確保されている前提
        getDialogue()[0] = "こんにちは、私はマロニー。";
        getDialogue()[1] = "お願いがあるの。";
        getDialogue()[2] = "ニワトリを10羽捕まえて";
        getDialogue()[3] = "お礼に100コインあげるわ";
    }

    @Override
    public void setAction() {
        if (following) {
            checkPlayerCollision();
        } else {
        }

        if (questAccepted && !questCompleted) {
            checkCoopObjective();
        }
    }

    /**
     * 移動後に必ず呼ぶ衝突判定
     */

    private void checkPlayerCollision() {
        boolean hit = collisionChecker.checkPlayer(this);
        if (hit) {
            onHitPlayer();
        }
    }

    /**
     * 定期チェック用：クエスト受注中かつ未完了ならかごのニワトリ数を確認する
     */

    public void checkCoopObjective() {

        GameMap map = getGameWindow().getCurrentMap();
        int current = map.countChickens();
        if (!questAccepted || questCompleted) return;
        getGameWindow().getUi().setCurrentDialogueMessage("残り: " + current + "羽");
        if (current <= 0) {
            onQuestObjectiveCompleted();
        }
    }

    private void onHitPlayer() {
        this.following = false;
        getGameWindow().setGameState(GameState.GAME_OVER);
    }

    @Override
    public void speak() {
        // プレイヤー参照を安全に取得
        if (getGameWindow() == null || getGameWindow().getPlayer() == null) {
            showDialogueAndAdvance();
            return;
        }

        // プレイヤーの向きに合わせて NPC を振り向かせる（プレイヤーの向きの逆を向く）
        String playerDir = getGameWindow().getPlayer().getDirection();
        setDirection(getOppositeDirection(playerDir));

        // もしクエスト受注済みかつ未完了なら、かごの達成をチェックして完了処理を呼ぶ
        if (questAccepted && !questCompleted) {

            checkCoopObjective();
        }

        // 既に完了済みなら報酬は渡している想定。会話を変える。
        if (questCompleted) {
            getGameWindow().getUi().setCurrentDialogueMessage("本当に助かったわ。ありがとう！");
            return;
        }

        // 通常の会話フロー
        showDialogueAndAdvance();

        // 会話が最後まで行っていて、まだ選択肢を出していないなら選択肢を出す
        if (!questOffered && isDialogueFinished()) {
            offerQuest();
        }
    }

    // プレイヤー向きの逆を返すユーティリティ
    public String getOppositeDirection(String dir) {
        if (dir == null) return "down";
        switch (dir) {
            case "up":
                return "down";
            case "down":
                return "up";
            case "left":
                return "right";
            case "right":
                return "left";
            default:
                return "down";
        }
    }

    private void showDialogueAndAdvance() {
        // 選択待ち中はダイアログを進めない
        if (awaitingChoice) return;

        String[] dialogues = getDialogue();
        int dialogueIndex = getDialogueIndex();

        // 安全な境界チェック
        if (dialogues == null || dialogues.length == 0) return;
        if (dialogueIndex < 0 || dialogueIndex >= dialogues.length || dialogues[dialogueIndex] == null) {
            dialogueIndex = 0;
        }

        getGameWindow().getUi().setCurrentDialogueMessage(dialogues[dialogueIndex]);
        dialogueIndex++;
        setDialogueIndex(dialogueIndex);
    }

    // 会話が最後まで行ったかを判定するユーティリティ
    private boolean isDialogueFinished() {
        String[] dialogues = getDialogue();
        int dialogueIndex = getDialogueIndex();
        if (dialogues == null) return true;
        // 最後の要素を表示済みかどうか
        for (int i = dialogues.length - 1; i >= 0; i--) {
            if (dialogues[i] != null) {
                return dialogueIndex > i;
            }
        }
        return true;
    }

    private void offerQuest() {
        questOffered = true;
        awaitingChoice = true;
        String title = "クエストを受けますか？";
        String[] options = new String[]{"受ける", "断る"};
        getGameWindow().getUi().showChoice(title, options, this);
    }

    /**
     * UI がプレイヤーの選択を受け取ったらこのメソッドを呼ぶこと。
     * choiceIndex: 0 = 受ける, 1 = 断る
     */

    public void onPlayerChoice(int choiceIndex) {
        if (!awaitingChoice) return;
        awaitingChoice = false;

        if (choiceIndex == 0) {
            acceptQuest();
        } else {
            declineQuest();
        }
    }

    private void acceptQuest() {
        questAccepted = true;
        getGameWindow().getUi().setCurrentDialogueMessage("クエストを受けた！");
    }

    private void declineQuest() {
        questAccepted = false;
        getGameWindow().getUi().setCurrentDialogueMessage("そう、またいつでも来てね。");
    }

    /**
     * クエスト完了判定（外部から呼ぶ想定）または NPC 自身がチェックして呼ぶ
     * 例: マップのかご判定処理が 10 羽を検出したらこのメソッドを呼ぶ
     */

    public void onQuestObjectiveCompleted() {

        if (!questAccepted || questCompleted) return;

        try {
            int current = getGameWindow().getPlayer().getCoin();
            getGameWindow().getPlayer().setCoin(current + questRewardCoins);
        } catch (NoSuchMethodError | NullPointerException ex) {
        }

        questCompleted = true;
        questAccepted = false;
        getGameWindow().getUi().setCurrentDialogueMessage("クエスト完了！ " + questRewardCoins + "コインを受け取ったよ。");
        getGameWindow().getSoundmanager().questChickenCompleteWAV("sound/chicken-quest-completed.wav");

    }

    private BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }
}