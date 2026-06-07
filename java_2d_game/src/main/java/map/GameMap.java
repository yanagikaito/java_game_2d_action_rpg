package map;

import entity.Entity;
import npc.NpcChicken;
import window.GameWindow;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ゲーム内マップ上のエンティティ（NPC / モンスター / オブジェクト）を管理するクラス。
 *
 * <p>このクラスはマップ上に存在するエンティティのリストを保持し、スポーン判定や
 * ニワトリ群れ（swarm）に関するユーティリティを提供。描画順ソートや
 * GameWindow との簡易同期（monster 配列への登録／クリア呼び出し）を行う。</p>
 *
 * <p>注意: {@code GameWindow} 側の {@code monster[]} と二重管理になっているため、
 * 追加・削除の際は両方を同期する設計になっています。将来的には {@code GameWindow}
 * が {@code GameMap} のリストを参照する一元管理にリファクタすることを推奨。</p>
 */

public class GameMap {

    /**
     * マップ上のエンティティを保持する内部リスト（描画・更新のソース）。
     */

    private final List<Entity> objects = new ArrayList<>();

    /**
     * GameWindow の参照。GameWindow の monster 配列へ登録・クリアするために使用する。
     * null の可能性があるため、呼び出し前に null チェックを行うこと。
     */

    private final GameWindow gameWindow;


    /**
     * マップ上に存在できるニワトリの最大数（グローバル上限）
     */

    public static final int GLOBAL_MAX_CHICKENS = 50;

    /**
     * 群れを呼ぶ際に一度にスポーンさせる最大数
     */

    public static final int SWARM_SPAWN_COUNT = 20;

    /**
     * 指定した GameWindow に紐づく GameMap を生成。
     *
     * @param gameWindow このマップと同期する GameWindow のインスタンス（null 可）
     */

    public GameMap(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    /**
     * 汎用のエンティティ追加メソッド。
     * <p>エンティティを内部リストに追加し、Y 座標順にソートする。</p>
     *
     * @param e 追加するエンティティ（null の場合は無視）
     */

    public void addObject(Entity e) {
        if (e == null) return;
        objects.add(e);
        sortObjectsByY();
    }

    /**
     * マップ上にモンスター（NPC）を追加する。
     *
     * <p>このメソッドは内部リストへの追加は行わず、{@code GameWindow.registerMonster}
     * を呼んで {@code GameWindow} 側の配列に登録するためのラッパです。必要に応じて
     * {@code objects.add(e)} を併用して内部リストにも登録。</p>
     *
     * @param e 追加するモンスター（null の場合は無視）
     */

    public void addMonster(Entity e) {
        if (e == null) return;
        if (gameWindow != null) {
            gameWindow.registerMonster(e);
        }
    }

    /**
     * 内部リストを Y 座標順にソートする（描画順制御用）。
     *
     * <p>private メソッドのため外部から呼び出す必要は通常ない。</p>
     */

    private void sortObjectsByY() {
        objects.sort(Comparator.comparingInt(Entity::getWorldY));
    }

    /**
     * 指定したクラスに属するエンティティの数を数えます。
     *
     * <p>現在の実装は {@code GameWindow.getMonster()} を参照してカウントする。
     * {@code GameMap} の内部リストを基準にしたい場合は実装を変更する。</p>
     *
     * @param cls カウント対象のクラスオブジェクト（例: {@code NpcChicken.class}）
     * @return 指定クラスに一致するエンティティの個数
     */

    public int countEntitiesOfType(Class<?> cls) {
        int count = 0;
        Entity[] monsters = gameWindow.getMonster();
        if (monsters == null) return 0;
        for (Entity e : monsters) {
            if (e == null) continue;
            if (cls.isInstance(e)) count++;
        }
        return count;
    }

    /**
     * マップ上のニワトリ（NpcChicken）の数を返す。
     *
     * @return ニワトリの個数
     */

    public int countChickens() {
        return countEntitiesOfType(NpcChicken.class);
    }

    /**
     * 指定ワールド座標に NPC を配置できるかを簡易判定。
     *
     * <p>現在は {@code GameWindow.getMonster()} の配列に対して矩形衝突判定を行う。
     * タイル通行判定やマップ境界チェックが必要な場合はこのメソッドを拡張。</p>
     *
     * @param worldX 配置候補のワールド X 座標
     * @param worldY 配置候補のワールド Y 座標
     * @param width  エンティティの幅（ワールド単位）
     * @param height エンティティの高さ（ワールド単位）
     * @return 衝突がなければ {@code true}、既存エンティティと重なる場合は {@code false}
     */

    public boolean canPlaceNpcAt(int worldX, int worldY, int width, int height) {
        Rectangle r = new Rectangle(worldX, worldY, width, height);
        Entity[] monsters = gameWindow.getMonster();
        if (monsters == null) return true;
        for (Entity e : monsters) {
            if (e == null) continue;
            Rectangle er = new Rectangle(e.getWorldX(), e.getWorldY(), e.getWidth(), e.getHeight());
            if (r.intersects(er)) return false;
        }
        return true;
    }

    /**
     * マップ上の全てのニワトリを削除。
     *
     * <p>この実装は {@code GameWindow.clearMonsters()} を呼んで {@code GameWindow}
     * 側の配列をクリアする。内部リスト（{@code objects}）にもニワトリが登録されている場合は
     * そちらも併せて削除するように拡張する。</p>
     */

    public void removeAllChickens() {
        if (gameWindow != null) {
            gameWindow.clearMonsters();
        }
    }

    /**
     * マップ上のニワトリの状態（追尾フラグや内部カウント等）をリセット。
     *
     * <p>各ニワトリに対して {@code setFollowing(false)} / {@code setPlayerFollowing(false)} /
     * {@code resetState()} を呼び出します。{@code NpcChicken.resetState()} を実装しておく必要がある。</p>
     */

    public void resetChickensState() {
        Entity[] chickens = gameWindow.getObj();
        if (chickens == null) return;
        for (Entity e : chickens) {
            if (e instanceof NpcChicken) {
                NpcChicken c = (NpcChicken) e;
                c.setFollowing(false);
                c.setPlayerFollowing(false);
                c.resetState();
            }
        }
    }
}