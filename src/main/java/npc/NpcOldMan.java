package npc;

import db.PathManager;
import entity.Entity;
import entity.type.NpcOldManType;
import frame.FrameApp;
import game.GameState;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class NpcOldMan extends Entity {

    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private BufferedImage[][] sprites = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private static final int SPRITE_COUNT = 3;
    private static final int ACTION_LOCK_THRESHOLD = 120;
    private static final int MAX_RANDOM_VALUE = 100;
    private static final int THRESHOLD_UP = 25;
    private static final int THRESHOLD_DOWN = 50;
    private static final int THRESHOLD_LEFT = 75;
    private Random random = new Random();
    private int actionLockCounter = 0;
    private List<Point> route = Collections.emptyList();
    private int routeIndex = 0;
    private boolean following = false;
    private String eventId;

    public NpcOldMan(GameWindow gameWindow, db.MapEvent ev) {
        super(gameWindow);
        if (ev != null) {
            this.eventId = String.valueOf(ev.getId());
            applyMapEvent(ev);
        }
        setType(new NpcOldManType());
        setDirection("down");
        setSpeed(1);
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
                                    .getResourceAsStream("npc/oldman-" + DIRECTIONS[dir] + "-" + (i + 1) + ".png"));
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

        if (ev.getId() != null) this.eventId = ev.getId();
        this.setName(ev.getName());
        this.setTrigger(ev.getTrigger());

        java.util.List<String> dlg = ev.getDialogues();
        if (dlg == null || dlg.isEmpty()) {
            setDialogueArray(new String[0]);
        } else {
            // 空文字や null、空白だけの要素を除外した新しいリストを作る
            java.util.List<String> cleanList = new java.util.ArrayList<>();
            for (String text : dlg) {
                if (text != null && !text.trim().isEmpty()) {
                    cleanList.add(text);
                }
            }
            setDialogueArray(cleanList.toArray(new String[0]));
        }

        // 新しいダイアログが適用されたら、インデックスを確実に 0 に初期化する
        setDialogueIndex(0);
    }

    @Override
    public void setAction() {

        randomWalkStep();

    }

    private void onHitPlayer() {
        this.following = false;
        getGameWindow().setGameState(GameState.GAME_OVER);
    }

    private void randomWalkStep() {
        actionLockCounter++;
        if (actionLockCounter < ACTION_LOCK_THRESHOLD) return;

        int i = random.nextInt(MAX_RANDOM_VALUE) + 1;
        if (i <= THRESHOLD_UP) setDirection("up");
        else if (i <= THRESHOLD_DOWN) setDirection("down");
        else if (i <= THRESHOLD_LEFT) setDirection("left");
        else setDirection("right");

        actionLockCounter = 0;
    }

    public void startRouteFollow(int mapId, int pathId) {

        List<Point> loadedRoutePoints = PathManager.loadPath(mapId, pathId);
        if (loadedRoutePoints.isEmpty()) {
            following = false;
            return;
        }

        int tileSize = FrameApp.getTileSize();
        int npcPosX = getWorldX();
        int npcPosY = getWorldY();

        // 最も近いポイントのインデックスを探す
        int minIndex = 0;
        int minDist = Integer.MAX_VALUE;
        for (int i = 0; i < loadedRoutePoints.size(); i++) {
            Point p = loadedRoutePoints.get(i);
            int wx = p.x * tileSize;
            int wy = p.y * tileSize;
            int dist = Math.abs(npcPosX - wx) + Math.abs(npcPosY - wy);
            if (dist < minDist) {
                minDist = dist;
                minIndex = i;
            }
        }

        // タイル座標 -> ワールド座標に変換して新しいリストにコピー
        List<Point> worldRoute = new ArrayList<>();
        for (int i = minIndex; i < loadedRoutePoints.size(); i++) {
            Point p = loadedRoutePoints.get(i);
            worldRoute.add(new Point(p.x * tileSize, p.y * tileSize));
        }

        this.route = worldRoute;
        this.routeIndex = 0;
        this.following = true;
    }

    private void followRouteStep() {
        if (!following || route == null || route.isEmpty()) return;
        if (routeIndex >= route.size()) {
            following = false;
            return;
        }

        Point target = route.get(routeIndex);
        int npcX = getWorldX();
        int npcY = getWorldY();

        int diffX = target.x - npcX;
        int diffY = target.y - npcY;

        // 距離
        double dist = Math.hypot(diffX, diffY);
        int speed = Math.max(1, getSpeed());

        // 到達判定：距離が speed 以下ならスナップして次へ
        if (dist <= speed) {
            setWorldX(target.x);
            setWorldY(target.y);
            routeIndex++;
            if (routeIndex >= route.size()) following = false;
            return;
        }

        // 正規化ベクトルで滑らかに移動
        double nx = diffX / dist;
        double ny = diffY / dist;
        int moveX = (int) Math.round(nx * speed);
        int moveY = (int) Math.round(ny * speed);

        // 最低でも1ピクセルは動くようにする
        if (moveX == 0 && Math.abs(diffX) >= 1) moveX = diffX > 0 ? 1 : -1;
        if (moveY == 0 && Math.abs(diffY) >= 1) moveY = diffY > 0 ? 1 : -1;

        setWorldX(npcX + moveX);
        setWorldY(npcY + moveY);

        // 方向更新
        if (Math.abs(diffX) >= Math.abs(diffY)) {
            setDirection(diffX > 0 ? "right" : "left");
        } else {
            setDirection(diffY > 0 ? "down" : "up");
        }
    }

    @Override
    public void speak() {

        String[] dialogues = getDialogue();
        int dialogueIndex = getDialogueIndex();

        // 1. セリフデータがない場合は安全に終了
        if (dialogues == null || dialogues.length == 0) return;

        // 2. すでに最後のセリフを超えている、または null に達した場合の処理（会話終了）
        if (dialogueIndex >= dialogues.length || dialogues[dialogueIndex] == null) {
            setDialogueIndex(0); // 次回話しかけたときのためにインデックスをリセット
            getGameWindow().getUi().setCurrentDialogueMessage(""); // メッセージを空にする
            getGameWindow().setGameState(GameState.PLAY); // ゲーム状態を通常に戻してウィンドウを閉じる
            return;
        }

        // 3. 現在のセリフをUIにセットしてインデックスを進める
        getGameWindow().getUi().setCurrentDialogueMessage(dialogues[dialogueIndex]);
        dialogueIndex++;
        setDialogueIndex(dialogueIndex);

        // 4. プレイヤーの方を向く
        if (getGameWindow().getPlayer() != null) {
            String playerDir = getGameWindow().getPlayer().getDirection();
            setDirection(getOppositeDirection(playerDir));
        }
    }

    private BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }
}