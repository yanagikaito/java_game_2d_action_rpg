package npc;

import collision.CollisionChecker;
import db.PathManager;
import entity.Entity;
import entity.type.ChickenType;
import frame.FrameApp;
import map.GameMap;
import player.Player;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NpcChicken extends Entity {

    public static final java.util.Map<Integer, java.util.List<java.awt.Point>> routeCache =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final ExecutorService ROUTE_LOADER = Executors.newSingleThreadExecutor();
    private int routeMapId = -1;
    private int routePathId = -1;
    private static final String[] DIRECTIONS = {"up", "down", "left", "right"};
    private BufferedImage[][] spritesChicken = new BufferedImage[DIRECTIONS.length][SPRITE_COUNT];
    private static final int SPRITE_COUNT = 3;
    private static final int ACTION_LOCK_THRESHOLD = 120;
    private static final int MAX_RANDOM_VALUE = 100;
    private static final int THRESHOLD_UP = 25;
    private static final int THRESHOLD_DOWN = 50;
    private static final int THRESHOLD_LEFT = 75;
    private int invincibleCounter = 0;
    private Random random = new Random();
    private int actionLockCounter = 0;
    private final CollisionChecker collisionChecker;

    private static final long PICKABLE_DELAY_MS = 500L;
    private long landedAt = 0L;

    private int hitCount = 0;
    private static final int HELP_TRIGGER_THRESHOLD = 3;
    private int life = 30;
    private boolean hasTriggeredHelp = false;
    private static final long TRIGGER_COOLDOWN_MS = 30_000L;
    private long lastTriggerTime = 0L;
    private boolean following = false;
    private boolean playerFollowing = false;
    private boolean beingHeld = false;
    private Player holder = null;

    private boolean inCoop = false;
    private int prevTileX = -1;
    private int prevTileY = -1;
    private int lastGroundTileY = -1; // 最後に地上にいた tileY を保持
    private GameMap gameMap;

    private java.util.List<Point> route = null; // ワールド座標（ピクセル）で保持するリスト
    private int routeIndex = 0;
    private boolean followingRoute = false;
    private boolean useAStarOnHit = false; // 攻撃時に A* を実行して経路を作るなら true

    // 1体あたりのダメージ
    private int attackDamage = 2;
    // 1体が連続で与える最短間隔（ms）
    private long attackCooldownMs = 800L;
    // 最後に攻撃した時刻
    private long lastAttackAt = 0L;

    // 投げられたときの物理挙動用
    private boolean hasShadow = false;
    private double vx = 0;
    private double vy = 0;
    // 高さ方向速度（上向き正）
    private double vz = 0.0;
    // 地面からの高さ（ピクセル）
    private double z = 0.0;
    // 毎フレーム vy に加算する重力
    private double gravity = 0.3;
    // 垂直重力（正、調整可）
    private double verticalGravity = 0.45;
    // 投げられて飛んでいる状態
    private boolean thrown = false;
    private Entity user = null;
    // 置かれていて拾える状態
    private boolean pickable = false;

    // 地面に着地しているか
    private boolean landed = false;

    // 割れ管理
    private boolean shattering = false;
    private int shatterFrame = 0;
    private boolean damageGiven = false;


    public NpcChicken(GameWindow gameWindow) {
        super(gameWindow);
        this.collisionChecker = new CollisionChecker(gameWindow);
        setDirection("down");
        setType(new ChickenType());
        setMaxLife(life);
        setLife(getMaxLife());
        setSpeed(1);
        setCollision(true);
        setBlocking(true);

        this.getSolidArea().x = 16;
        this.getSolidArea().y = 16;

        this.setSolidAreaDefaultX(this.getSolidArea().x);
        this.setSolidAreaDefaultY(this.getSolidArea().y);

        this.getSolidArea().width = (FrameApp.getTileSize() + 12);
        this.getSolidArea().height = (FrameApp.getTileSize() + 12);

        try {
            this.gameMap = gameWindow.getCurrentMap();
        } catch (Exception e) {
            this.gameMap = null;
        }

        loadNPCImages();
    }

    public void loadNPCImages() {

        int s = 2;
        setSprites(spritesChicken);
        try {
            int tileSize = FrameApp.getTileSize();
            for (int dir = 0; dir < DIRECTIONS.length; dir++) {
                for (int i = 0; i < SPRITE_COUNT; i++) {
                    BufferedImage original = ImageIO.read(
                            getClass().getClassLoader()
                                    .getResourceAsStream("npc/chicken-" + DIRECTIONS[dir] + "-" + (i + 1) + ".gif"));
                    BufferedImage processed = createImage(original, tileSize * s, tileSize * s);
                    spritesChicken[dir][i] = processed;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAction() {

        // 優先: ルート追従
        if (following && route != null && !route.isEmpty()) {
            followRouteStep();
            checkPlayerCollision();
            return;
        }

        // 既存の挙動
        if (playerFollowing || following) {
            followPlayerStep();
            checkPlayerCollision();
            return;
        }

        // 現在のタイル（フレーム開始時の位置）
        prevTileX = worldToTile(getWorldX());
        prevTileY = worldToTile(getWorldY());

        if (!thrown && !landed) {
            lastGroundTileY = prevTileY;
        }

        // --- 着地後の復元処理（pickable 復帰） ---
        if (landed && !pickable) {
            long now = System.currentTimeMillis();
            if (now - landedAt >= PICKABLE_DELAY_MS) {
                pickable = true;
                landed = false;
                this.setCollision(true);
                if (this.getSolidArea() != null) {
                    this.getSolidArea().x = 16;
                    this.getSolidArea().y = 16;
                    this.getSolidArea().width = FrameApp.getTileSize() + 12;
                    this.getSolidArea().height = FrameApp.getTileSize() + 12;
                }
                System.out.println("[CHICKEN] pickable now true at world=(" + getWorldX() + "," + getWorldY() + ")");
            }
        }


        // 無敵カウンタ処理
        if (getInvincible()) {
            invincibleCounter--;
            if (invincibleCounter <= 0) setInvincible(false);
        }

        // 追尾フラグが立っていれば常にプレイヤー追尾
        if (playerFollowing || following) {
            followPlayerStep();
            checkPlayerCollision();
            return;
        }

        // --- 投げられている状態（空中） ---
        if (thrown) {
            // 空中では move() を使わず world 座標を直接更新する
            setWorldX(getWorldX() + (int) Math.round(vx));
            setWorldY(getWorldY() + (int) Math.round(vy));

            vy += gravity;

            z += vz;
            vz -= verticalGravity;

            // デバッグログ（投げ中）
            // System.out.println("[DBG] thrown pos=(" + getWorldX() + "," + getWorldY() + ") z=" + z + " vx=" + vx + " vy=" + vy);

            // --- 着地処理内の修正（z <= 0 の分岐） ---
            if (z <= 0) {
                // 着地確定直後
                z = 0;
                vz = 0;
                thrown = false;

                vx = 0.0;
                vy = 0.0;

                int tileSize = FrameApp.getTileSize();

                // 当たり判定を復元
                this.setCollision(true);
                if (this.getSolidArea() != null) {
                    this.getSolidArea().x = 16;
                    this.getSolidArea().y = 16;
                    this.getSolidArea().width = tileSize + 12;
                    this.getSolidArea().height = tileSize + 12;
                }

                // タイル判定など既存の処理
                boolean tileCollision = getGameWindow().getCollisionChecker().checkTile(this);
                setAlive(true);
                if (tileCollision) {
                    landed = true;
                    pickable = false;
                    hasShadow = false;
                    landedAt = System.currentTimeMillis();
                } else {
                    pickable = true;
                    hasShadow = false;
                }
            }

            // 投げ中はここで終了（空中処理のみ）
            return;
        }

        if (!pickable && !landed) {
            randomWalkStep();
            getGameWindow().getCollisionChecker().checkPlayer(this);
            return;
        }

        // それ以外は従来のランダム歩行
        randomWalkStep();
        getGameWindow().getCollisionChecker().checkPlayer(this);
    }

    public void startRouteFollow(int mapId, int pathId) {

        List<Point> loadedRoutePoints = PathManager.loadPath(mapId, pathId);
        if (loadedRoutePoints == null || loadedRoutePoints.isEmpty()) {
            following = false;
            return;
        }

        int tileSize = FrameApp.getTileSize();
        int npcPosX = getWorldX();
        int npcPosY = getWorldY();

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

        List<Point> worldRoute = new ArrayList<>();
        for (int i = minIndex; i < loadedRoutePoints.size(); i++) {
            Point p = loadedRoutePoints.get(i);
            worldRoute.add(new Point(p.x * tileSize, p.y * tileSize));
        }

        this.route = worldRoute;
        this.routeIndex = 0;
        this.following = true;
        setSpeed(Math.max(1, getSpeed()));
    }

    public void startRouteFollow() {
        if (this.route != null && !this.route.isEmpty()) {
            this.routeIndex = 0;
            this.following = true;
            setSpeed(Math.max(1, getSpeed()));
            System.out.println("[CHICKEN] startRouteFollow(): using pre-set route, size=" + route.size());
            return;
        }

        if (routeMapId >= 0 && routePathId >= 0) {
            System.out.println("[CHICKEN] startRouteFollow(): loading route map=" + routeMapId + " path=" + routePathId);
            startRouteFollow(routeMapId, routePathId);
            return;
        }

        System.out.println("[CHICKEN] startRouteFollow(): no route set and no routeId available");
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

        double nx = diffX / dist;
        double ny = diffY / dist;
        int moveX = (int) Math.round(nx * speed);
        int moveY = (int) Math.round(ny * speed);

        if (moveX == 0 && Math.abs(diffX) >= 1) moveX = diffX > 0 ? 1 : -1;
        if (moveY == 0 && Math.abs(diffY) >= 1) moveY = diffY > 0 ? 1 : -1;

        // 衝突判定を行うならここでチェック（CollisionChecker を利用）
        setWorldX(npcX + moveX);
        setWorldY(npcY + moveY);

        if (Math.abs(diffX) >= Math.abs(diffY)) {
            setDirection(diffX > 0 ? "right" : "left");
        } else {
            setDirection(diffY > 0 ? "down" : "up");
        }
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

    /**
     * 移動後に必ず呼ぶ衝突判定
     */

    private void checkPlayerCollision() {
        if (getGameWindow().getCollisionChecker().checkPlayer(this)) {
            long now = System.currentTimeMillis();
            if (now - lastAttackAt >= attackCooldownMs) {
                lastAttackAt = now;
                try {
                    getGameWindow().getPlayer().takeDamage(attackDamage);
                    onHitPlayer();
                } catch (Exception e) {
                    System.out.println("[CHICKEN] failed to damage player: " + e);
                }
            }
        }
    }

    private void onHitPlayer() {
        try {
            getGameWindow().getSoundmanager().damageWAV("sound/damage-sound.wav");
        } catch (Exception ignored) {
        }

        // 投げられた/所持状態を解除して追従可能にする
        this.thrown = false;
        this.beingHeld = false;
        this.pickable = false;
        this.setCollision(true);
        this.setAlive(true);

        // 既に route がセットされていればそれを使って追従開始
        if (this.route != null && !this.route.isEmpty()) {
            this.routeIndex = 0;
            this.following = true;
            setSpeed(2);
            System.out.println("[CHICKEN] onHitPlayer: starting existing route, size=" + route.size());
            return;
        }
    }

    public void setFollowing(boolean f) {
        this.following = f;
        if (f) {
            setSpeed(2);
        } else {
            setSpeed(1);
        }
    }

    public void setPlayerFollowing(boolean f) {
        this.playerFollowing = f;
        if (f) {
            setSpeed(2);
        } else {
            setSpeed(1);
        }
    }

    private void callForHelp() {

        GameMap map = getGameWindow().getCurrentMap();
        int current = map.countChickens();
        int allowed = Math.max(0, GameMap.GLOBAL_MAX_CHICKENS - current);
        int toSpawn = Math.min(GameMap.SWARM_SPAWN_COUNT, allowed);
        if (toSpawn <= 0) return;

        int tileSize = FrameApp.getTileSize();

        // プレイヤー基準で逆算
        int cameraWorldX = getGameWindow().getPlayer().getWorldX() - getGameWindow().getPlayer().getScreenX();

        int spawnX = cameraWorldX + FrameApp.getScreenWidth() + tileSize * 2;
        int playerY = getGameWindow().getPlayer().getWorldY();

        for (int i = 0; i < toSpawn; i++) {
            NpcChicken baby = new NpcChicken(getGameWindow());
            int offsetY = (random.nextInt(7) - 3) * tileSize;
            int spawnY = playerY + offsetY;
            int spawnPosX = spawnX + i * (tileSize / 2);

            if (!map.canPlaceNpcAt(spawnPosX, spawnY, baby.getWidth(), baby.getHeight())) {
                continue;
            }

            baby.setWorldX(spawnPosX);
            baby.setWorldY(spawnY);
            baby.setFollowing(true);
            baby.setDirection("left");
            map.addMonster(baby);
        }
    }

    private void followPlayerStep() {
        int px = getGameWindow().getPlayer().getWorldX();
        int py = getGameWindow().getPlayer().getWorldY();
        int npcX = getWorldX();
        int npcY = getWorldY();

        int dx = px - npcX;
        int dy = py - npcY;
        int speed = getSpeed();

        // X/Y のステップを speed に制限して移動
        int stepX = 0;
        int stepY = 0;
        if (Math.abs(dx) > 0) stepX = Math.min(Math.abs(dx), speed) * (dx > 0 ? 1 : -1);
        if (Math.abs(dy) > 0) stepY = Math.min(Math.abs(dy), speed) * (dy > 0 ? 1 : -1);

        // 衝突判定を使うならここでチェックしてから setWorldX/Y する
        setWorldX(npcX + stepX);
        setWorldY(npcY + stepY);

        // 向き更新
        if (Math.abs(dx) >= Math.abs(dy)) {
            setDirection(dx > 0 ? "right" : "left");
        } else {
            setDirection(dy > 0 ? "down" : "up");
        }
    }

    /**
     * プレイヤーなどからのダメージ処理。
     * dmg: 与ダメージ、kbX/kbY: ノックバック量（ワールド座標単位）
     */

    public void takeDamage(int dmg, int knockBackPower) {

        if (this.getInvincible()) return;

        try {
            getGameWindow().getSoundmanager().damageWAV("sound/hit.wav");
        } catch (Exception ignored) {
        }

        // ノックバック
        int kbX = 0, kbY = 0;
        switch (getDirection()) {
            case "up" -> kbY = -knockBackPower;
            case "down" -> kbY = knockBackPower;
            case "left" -> kbX = -knockBackPower;
            case "right" -> kbX = knockBackPower;
        }
        // CollisionChecker を使って移動可否を確認する
        setWorldX(getWorldX() + kbX);
        setWorldY(getWorldY() + kbY);

        // HP を減らすが 1 未満にはしない（倒せない）
        int newLife = Math.max(1, getLife() - dmg);
        setLife(newLife);

        // 無敵フレームを付与するなら
        this.setInvincible(true);
        this.invincibleCounter = 30;

        // 被攻撃回数カウント
        hitCount++;
        long now = System.currentTimeMillis();
        if (!hasTriggeredHelp && hitCount >= HELP_TRIGGER_THRESHOLD && (now - lastTriggerTime) >= TRIGGER_COOLDOWN_MS) {
            hitCount = 0;
            hasTriggeredHelp = true;
            lastTriggerTime = now;
            callForHelp();
        }

        // 既存のダメージ処理の後、route を使う分岐
        if (this.route != null && !this.route.isEmpty()) {
            this.routeIndex = 0;
            this.following = true;
            setSpeed(2);
            this.thrown = false;
            this.beingHeld = false;
            this.pickable = false;
        } else {
            // route が無ければ非同期で DB からロードして route をセットする例
            int mapId = 1;
            int basePathId = 0;
            int chosenEncodedPathId = /* choose logic or pass in */ 0; // 実際は damage 発生元で決定

            // 非同期ロード
            NpcChicken chicken = this; // this が NpcChicken の場合
            ROUTE_LOADER.submit(() -> {
                java.util.List<java.awt.Point> tiles = PathManager.loadPath(mapId, chosenEncodedPathId);
                if (tiles == null || tiles.isEmpty()) return;
                int tileSize = FrameApp.getTileSize();
                java.util.List<java.awt.Point> worldRoute = new java.util.ArrayList<>();
                for (java.awt.Point p : tiles) worldRoute.add(new java.awt.Point(p.x * tileSize, p.y * tileSize));

                // スレッドセーフに route をセット（setter は synchronized 推奨）
                chicken.setRoute(worldRoute);
                chicken.setRouteIndex(0);
                chicken.setFollowing(true);
            });
        }
    }

    public void resetState() {
        this.hitCount = 0;
        this.hasTriggeredHelp = false;
        this.lastTriggerTime = 0L;
        this.setInvincible(false);
        this.playerFollowing = false;
        this.following = false;
    }

    private BufferedImage createImage(BufferedImage original, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    /**
     * 所持中に Player が頭上に描画するためのスプライトを返す。
     * デフォルトは Down 方向の最初のフレームを使い、タイルサイズに合わせてリサイズする。
     */

    public BufferedImage getHeldSprite() {
        int ts = FrameApp.getTileSize();
        int downIndex = 1;
        int frameIndex = 0; // 所持時は最初のフレームを使う
        if (spritesChicken != null && spritesChicken.length > downIndex && spritesChicken[downIndex].length > frameIndex) {
            BufferedImage src = spritesChicken[downIndex][frameIndex];
            // 小さめにリサイズして返す
            int w = ts * 3 / 2;
            int h = ts * 3 / 2;
            BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = buf.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, w, h, null);
            g.dispose();
            return buf;
        }
        return null;
    }

    // マップ読み込み時の初期化例
    public void preloadRoutesForMap(int mapId, int basePathId, int maxStarts) {
        for (int i = 0; i < maxStarts; i++) {
            int encodedPathId = basePathId * 100 + i;
            List<Point> tiles = PathManager.loadPath(mapId, encodedPathId);
            if (tiles != null && !tiles.isEmpty()) {
                NpcChicken.routeCache.put(encodedPathId, tiles);
                System.out.println("[PRELOAD] cached pathId=" + encodedPathId + " size=" + tiles.size());
            }
        }
    }

    // 投げるときの向き・フレーム別スプライトを返す（存在しなければ null を返す）
    public BufferedImage getThrowSprite(String dir, int frame) {
        // ここでは sprites の向きインデックスとフレームをそのまま使う例
        int dirIndex = 1; // default Down
        switch (dir) {
            case "up" -> dirIndex = 0;
            case "down" -> dirIndex = 1;
            case "left" -> dirIndex = 2;
            case "right" -> dirIndex = 3;
        }
        int frameIndex = Math.max(0, Math.min(frame, spritesChicken[dirIndex].length - 1));
        return spritesChicken[dirIndex][frameIndex];
    }

    public void onPickedUp(Player player) {
        System.out.println("[DBG] onPickedUp id=" + System.identityHashCode(this));
        if (beingHeld) return;
        beingHeld = true;
        holder = player;
        pickable = false;
        thrown = false;
        setVelocity(0, 0);
        setWorldX(player.getWorldX());
        setWorldY(player.getWorldY());
        setZ(0);
        setAlive(false);
        setCollision(false);
    }

    public synchronized void setRoute(List<Point> worldRoute) {
        this.route = worldRoute;
    }

    public synchronized void setRouteIndex(int idx) {
        this.routeIndex = idx;
    }

    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
        this.thrown = true;
        setAlive(true);
    }

    private int worldToTile(int worldCoord) {
        int tileSize = FrameApp.getTileSize();
        return Math.floorDiv(worldCoord, tileSize);
    }

    public boolean isHasShadow() {
        return hasShadow;
    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public double getVz() {
        return vz;
    }

    public void setVz(double vz) {
        this.vz = vz;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public double getVerticalGravity() {
        return verticalGravity;
    }

    public void setVerticalGravity(double verticalGravity) {
        this.verticalGravity = verticalGravity;
    }

    public void setVerticalVelocity(double vz) {
        this.vz = Math.max(-40.0, Math.min(40.0, vz));
    }

    @Override
    public boolean isThrown() {
        return thrown;
    }

    @Override
    public void setThrown(boolean thrown) {
        this.thrown = thrown;
    }

    public Entity getUser() {
        return user;
    }

    public void setUser(Entity user) {
        this.user = user;
    }

    public boolean isPickable() {
        return pickable;
    }

    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }

    public boolean isBeingHeld() {
        return beingHeld;
    }

    public Player getHolder() {
        return holder;
    }

    public void setRouteIds(int mapId, int pathId) {
        this.routeMapId = mapId;
        this.routePathId = pathId;
    }
}