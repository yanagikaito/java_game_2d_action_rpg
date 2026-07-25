package object;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import entity.Entity;
import entity.loot.LootConfigEntry;
import entity.loot.LootEntry;
import factory.EntityFactory;
import frame.FrameApp;
import player.Player;
import popup.PopupVariant;
import window.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class ObjChest extends Entity {

    private final Random random = new Random();
    private GameWindow gameWindow;
    private Entity loot;
    private final EntityFactory entityFactory;
    private boolean opened = false;
    private BufferedImage[] chestFrames;
    private int chestFrameIndex = 0;
    private int chestFrameDelay = 10;
    private int chestFrameCounter = 0;
    private boolean animatingOpen = false;
    private boolean playerNearby = false;

    private final List<LootEntry> lootTable = new ArrayList<>();

    public ObjChest(GameWindow gameWindow, Entity loot, EntityFactory entityFactory) {
        super(gameWindow);
        this.gameWindow = gameWindow;
        this.entityFactory = entityFactory;
        this.loot = loot;
        setName("宝箱");

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
        int dx = Math.abs(player.getWorldX() - getWorldX());
        int dy = Math.abs(player.getWorldY() - getWorldY());
        int threshold = FrameApp.getTileSize();
        playerNearby = dx < threshold && dy < threshold;

        // 近くにいて Enter 押下なら開く
        if (playerNearby && !opened && gameWindow.getKeyHandler().isPlayerEnter()) {
            open(player);
        }
    }


    private void loadLootTableFromJson(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                // リソースが見つからない場合はデフォルトテーブルを使う
                buildDefaultLootTable();
                return;
            }
            InputStreamReader reader = new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8);
            Type listType = new TypeToken<List<LootConfigEntry>>() {
            }.getType();
            List<LootConfigEntry> configs = new Gson().fromJson(reader, listType);
            buildLootTableFromConfig(configs);
        } catch (Exception e) {
            e.printStackTrace();
            buildDefaultLootTable();
        }
    }

    private void buildLootTableFromConfig(List<LootConfigEntry> configs) {
        lootTable.clear();
        if (configs == null || configs.isEmpty()) {
            buildDefaultLootTable();
            return;
        }
        for (LootConfigEntry c : configs) {
            if (c == null || c.weight <= 0 || c.type == null) continue;
            Supplier<Entity> factory = switch (c.type) {
                case "coin" -> () -> entityFactory.createCoinEntity();
                case "red_potion" -> () -> entityFactory.createRedPotionEntity();
                case "green_potion" -> () -> entityFactory.createGreenPotionEntity();
                case "blue_potion" -> () -> entityFactory.createBluePotionEntity();
                default -> () -> null;
            };
            lootTable.add(new LootEntry(factory, c.weight));
        }
    }

    private void buildDefaultLootTable() {
        lootTable.clear();
        lootTable.add(new LootEntry(() -> entityFactory.createCoinEntity(), 40));
        lootTable.add(new LootEntry(() -> entityFactory.createRedPotionEntity(), 25));
        lootTable.add(new LootEntry(() -> entityFactory.createGreenPotionEntity(), 20));
        lootTable.add(new LootEntry(() -> entityFactory.createBluePotionEntity(), 15));
    }

    private Entity createRandomLoot() {
        if (lootTable.isEmpty()) return null;
        int total = lootTable.stream().mapToInt(LootEntry::weight).sum();
        if (total <= 0) return null;
        int pick = random.nextInt(total);
        int acc = 0;
        for (LootEntry e : lootTable) {
            acc += e.weight();
            if (pick < acc) {
                try {
                    Entity result = e.factory().get();
                    // デバッグ出力
                    if (result == null) {
                        System.out.println("DEBUG loot factory returned null for entry weight=" + e.weight());
                    } else {
                        System.out.println("DEBUG loot factory returned: " + result.getClass().getName());
                    }
                    return result;
                } catch (Throwable t) {
                    t.printStackTrace();
                    return null;
                }
            }
        }
        return null;
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
                if (dropped instanceof ObjCoinBronze) {
                    // コインはインベントリに入れず所持金に加算
                    ObjCoinBronze coin = (ObjCoinBronze) dropped;
                    player.addCoin(coin);
                    return;
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