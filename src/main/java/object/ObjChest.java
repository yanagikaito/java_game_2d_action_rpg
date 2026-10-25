package object;

import entity.Entity;
import entity.loot.LootEntry;
import entity.loot.LootTableLoader;
import factory.EntityFactory;
import frame.FrameApp;
import player.Player;
import popup.PopupVariant;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjChest extends Entity {

    private GameWindow gameWindow;
    private final EntityFactory entityFactory;
    private boolean opened = false;
    private BufferedImage[] chestFrames;
    private int chestFrameIndex = 0;
    private int chestFrameDelay = 10;
    private int chestFrameCounter = 0;
    private boolean animatingOpen = false;
    private boolean playerNearby = false;

    private final List<LootEntry> lootTable = new ArrayList<>();

    public ObjChest(GameWindow gameWindow, EntityFactory entityFactory) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        this.entityFactory = entityFactory;
        setName("宝箱");
        setBlocking(true);

        try {
            // 画像ファイルを個別に読み込む
            chestFrames = new BufferedImage[]{
                    ImageIO.read(getClass().getResourceAsStream("/object/chest.gif")),
                    ImageIO.read(getClass().getResourceAsStream("/object/chest-open-1.gif")),
                    ImageIO.read(getClass().getResourceAsStream("/object/chest-open-2.gif"))
            };
            // 初期表示は閉じたフレーム
            setImage(chestFrames[0], FrameApp.getTileSize());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // クラスパスから読み込む
        loadLootTableFromJson("items/loot_table.json");
    }

    @Override
    public void update() {
        // 既存のアニメ処理
        if (animatingOpen && chestFrames != null) {
            chestFrameCounter++;
            if (chestFrameCounter >= chestFrameDelay) {
                chestFrameCounter = 0;
                chestFrameIndex++;
                if (chestFrameIndex >= chestFrames.length) {
                    chestFrameIndex = chestFrames.length - 1;
                    animatingOpen = false;
                }
                setImage(chestFrames[chestFrameIndex], FrameApp.getTileSize());
            }
        }

        // プレイヤー近接判定
        Player player = gameWindow.getPlayer();
        int tileSize = FrameApp.getTileSize();

        int targetX = player.getWorldX();
        int targetY = player.getWorldY();

        String dir = player.getDirection();
        if (dir.equals("up")) {
            targetY = player.getWorldY() - tileSize;
        }

        int allow = tileSize / 2;

        // 宝箱がプレイヤー正面のタイルにあるか判定
        boolean isDirectlyInFront = Math.abs(getWorldX() - targetX) <= allow
                && Math.abs(getWorldY() - targetY) <= allow;

        // 近接フラグ
        int dx = Math.abs(player.getWorldX() - getWorldX());
        int dy = Math.abs(player.getWorldY() - getWorldY());
        playerNearby = dx < tileSize && dy < tileSize;

        // 開ける条件：正面にあり、未開放、Enter 押下
        if (isDirectlyInFront && !opened && gameWindow.getKeyHandler().isPlayerEnter()) {
            open(player);
        }
    }

    private void loadLootTableFromJson(String resourcePath) {
        lootTable.clear();
        lootTable.addAll(LootTableLoader.loadTable(gameWindow, resourcePath));
    }

    private Entity createRandomLoot() {
        return LootTableLoader.rollRandomLoot(lootTable);
    }

    public void interact(Player player) {
        if (opened) {
            return;
        }
    }

    private void open(Player player) {
        opened = true;
        // アニメ開始
        if (chestFrames != null && chestFrames.length > 1) {
            animatingOpen = true;
            chestFrameIndex = 1;
            chestFrameCounter = 0;
            setImage(chestFrames[chestFrameIndex], FrameApp.getTileSize());
        }

        if (opened) {
            Entity dropped = createRandomLoot();
            if (dropped != null) {

                int tileSize = FrameApp.getTileSize();
                int screenX = this.getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
                int screenY = this.getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();
                int sx = screenX;
                int sy = screenY;

                sx += tileSize / 2;
                sy -= tileSize / 2;
                PopupVariant variant = PopupVariant.DROP;
                gameWindow.getUi().getDamagePopupManager().popItem(dropped, sx, sy, variant, 60);
                if (dropped != null) {
                    // 6色のヴェール（通貨）を個別に指定して所持金に加算
                    if (dropped instanceof ObjVertBronze coin) {
                        player.addCoin(coin);
                        return;
                    } else if (dropped instanceof ObjVertGreen coin) {
                        player.addCoin(coin);
                        return;
                    } else if (dropped instanceof ObjVertBlue coin) {
                        player.addCoin(coin);
                        return;
                    } else if (dropped instanceof ObjVertRed coin) {
                        player.addCoin(coin);
                        return;
                    } else if (dropped instanceof ObjVertSilver coin) {
                        player.addCoin(coin);
                        return;
                    } else if (dropped instanceof ObjVertGold coin) {
                        player.addCoin(coin);
                        return;
                    }
                }
                // --- 爆弾はインベントリに入れず地面に落とす（pickable にする） ---
                if (dropped instanceof object.ObjBomb) {
                    object.ObjBomb bomb = (object.ObjBomb) dropped;

                    // 初期化：ワールドに置ける状態にする
                    bomb.setPickable(true);
                    bomb.setThrown(false);
                    bomb.setAlive(true);
                    bomb.setLife(bomb.getMaxLife());
                    bomb.setVelocity(0, 0);

                    // チェスト位置に落とす（必要なら少しオフセット）
                    bomb.setWorldX(this.getWorldX());
                    bomb.setWorldY(this.getWorldY());

                    // マップに追加
                    gameWindow.getCurrentMap().addObject(bomb);
                    return;
                }
                // 通常アイテムはインベントリに追加（成功判定を取る）
                boolean added = player.canObtainItem(dropped);
                if (added) {
                    gameWindow.getSoundmanager().redPotionWAV("sound/potion-sound.wav");
                } else {
                    dropped.setWorldX(this.getWorldX());
                    dropped.setWorldY(this.getWorldY());
                    gameWindow.getCurrentMap().addObject(dropped);
                }
            }
        } else {
        }
    }

    public boolean isOpened() {
        return opened;
    }

    public void draw(Graphics2D g2) {

        int screenX = this.getWorldX() - gameWindow.getPlayer().getWorldX() + gameWindow.getPlayer().getScreenX();
        int screenY = this.getWorldY() - gameWindow.getPlayer().getWorldY() + gameWindow.getPlayer().getScreenY();

        g2.drawImage(this.getImage(), screenX, screenY, null);
    }
}