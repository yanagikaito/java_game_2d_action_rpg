package npc;

import entity.Entity;
import frame.FrameApp;
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
    private int questRewardCoins = 100;
    // 会話が終わって選択肢を出したか
    private boolean questOffered = false;
    // プレイヤーの選択待ち中か（UI と連携）
    private boolean awaitingChoice = false;
    // プレイヤーがクエストを受けたか
    private boolean questAccepted = false;
    // クエストが完了して報酬を渡したか
    private boolean questCompleted = false;

    private String eventId;

    public NpcMalonyChicken(GameWindow gameWindow, db.MapEvent ev) {
        super(gameWindow);
        if (ev != null) {
            this.eventId = String.valueOf(ev.getId());
            applyMapEvent(ev);
        }
        setDirection("right");
        setSpeed(0);
        loadNPCImages();
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

    public void applyMapEvent(db.MapEvent ev) {
        if (ev == null) return;

        // eventId 等の基本情報
        if (ev.getId() != null) this.eventId = ev.getId();
        this.setName(ev.getName());
        this.setTrigger(ev.getTrigger());

        // dialogues を配列にコピー
        java.util.List<String> dlg = ev.getDialogues();
        if (dlg == null || dlg.isEmpty()) {
            // 空なら空配列をセットしておく（null 回避）
            setDialogueArray(new String[0]);
        } else {
            String[] arr = new String[dlg.size()];
            for (int i = 0; i < dlg.size(); i++) {
                arr[i] = dlg.get(i);
            }
            setDialogueArray(arr);
        }

        // クエストや meta の反映
        this.questOffered = ev.isQuestOffered();
        this.questAccepted = ev.isQuestAccepted();
        this.questCompleted = ev.isQuestCompleted();
        if (ev.getMeta() != null && ev.getMeta().get("reward") instanceof Number) {
            this.questRewardCoins = ((Number) ev.getMeta().get("reward")).intValue();
        }
    }

    @Override
    public void setAction() {

        if (questAccepted && !questCompleted) {
            checkCoopObjective();
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

    private String generateEventId() {
        return "ev_" + Long.toHexString(System.currentTimeMillis());
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

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}