package map;

import db.MapEvent;
import db.MapModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class MapCanvas extends JPanel implements Scrollable {

    private final MapModel model;
    private final Map<Integer, Image> tileImages;
    private int selectedTileId = 0;
    private final int tileSize = 32;
    public static final int CHEST_TILE_ID = 99;

    private Image eventIcon;
    private Image chestImage;
    private String selectedEventId = null;
    private java.util.function.Consumer<MapEvent> selectionListener;
    private String currentTool = "tile"; // "tile" or "event"
    private final java.util.Random idRand = new java.util.Random();

    public MapCanvas(MapModel model, Map<Integer, Image> tileImages) {
        this.model = model;
        this.tileImages = tileImages;
        setPreferredSize(new Dimension(
                model.getWidth() * tileSize,
                model.getHeight() * tileSize
        ));

        // マウスの操作ハンドラー
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseEvent(e); // 押した瞬間
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseEvent(e); // 押したまま動かしたとき（なぞる動作）
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            private void handleMouseEvent(MouseEvent e) {

                // タイル座標
                int tx = e.getX() / tileSize;
                int ty = e.getY() / tileSize;
                if (tx < 0 || ty < 0 || tx >= model.getWidth() || ty >= model.getHeight()) return;

                // 右クリック
                boolean isPopup = SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger();
                MapEvent hit = model.getEventAt(tx, ty);

                if (isPopup) {
                    if (hit != null) {
                        // 選択状態にする
                        selectedEventId = hit.getId();
                        // 選択リスナーに通知（MainFrame 側で showProperties を呼ぶ）
                        fireSelectionChanged(hit);

                        // 簡易コンテキストメニューを表示
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem props = new JMenuItem("Properties");
                        props.addActionListener(a -> {
                            // 再通知してプロパティを表示
                            fireSelectionChanged(hit);
                        });
                        JMenuItem delete = new JMenuItem("Delete Event");
                        delete.addActionListener(a -> {
                            model.removeEvent(hit.getId());
                            selectedEventId = null;
                            repaint();
                        });
                        menu.add(props);
                        menu.addSeparator();
                        menu.add(delete);
                        menu.show(MapCanvas.this, e.getX(), e.getY());
                    }
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (hit != null) {
                        if (!hit.getId().equals(selectedEventId)) {
                            selectedEventId = hit.getId();
                            fireSelectionChanged(hit);
                            repaint();
                        }
                        return;
                    }

                    if ("event".equalsIgnoreCase(currentTool)) {
                        // イベントツールの場合は、ドラッグ中に意図せず大量生成されないよう
                        // 「押した瞬間（mousePressed）」のみ新規作成されるようにガードを入れる
                        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                            String id = generateEventId();
                            MapEvent ne = new MapEvent(id, tx, ty);
                            ne.setName("event_" + id);
                            ne.setTrigger("interact");
                            model.addEvent(ne);
                            selectedEventId = id;
                            fireSelectionChanged(ne);
                            repaint();
                        }
                    } else if ("object".equalsIgnoreCase(currentTool)) {
                        // オブジェクト（宝箱等）をタイル座標ベースの固定IDで配置
                        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                            // 座標を元にした固定ID
                            String id = "chest_" + tx + "_" + ty;

                            // すでにこの座標にイベント/オブジェクトがなければ新規作成
                            if (model.getEventAt(tx, ty) == null) {
                                MapEvent obj = new MapEvent(id, tx, ty);
                                obj.setName("chest_" + tx + "_" + ty);
                                obj.setTrigger("interact");
                                obj.getMeta().put("type", "chest");

                                model.addEvent(obj);
                                selectedEventId = id;
                                fireSelectionChanged(obj);
                                repaint();
                            }
                        }
                    } else {
                        // 通常タイルツール
                        if (model.getTile(tx, ty) != selectedTileId) {
                            model.setTile(tx, ty, selectedTileId);
                            repaint();
                        }
                    }
                }
            }
        };

        // クリックとドラッグ（モーション）の両方のリスナーを登録する
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void setSelectedTile(int tileId) {
        this.selectedTileId = tileId;
        this.currentTool = "tile";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

        // 1. タイルを描画
        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                Image img = tileImages.get(model.getTile(x, y));
                if (img != null) {
                    g2.drawImage(img, x * tileSize, y * tileSize, tileSize, tileSize, null);
                }
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);

                // 座標テキストの描画
                g2.setColor(Color.DARK_GRAY);
                String coord = x + "," + y;
                int strX = x * tileSize + 2;
                int strY = y * tileSize + 12;
                g2.drawString(coord, strX, strY);
            }
        }

        // 2. イベント・オブジェクトを描画
        for (MapEvent e : model.getAllEvents()) {
            int sx = e.getX() * tileSize;
            int sy = e.getY() * tileSize;

            // 宝箱の判定（名前が "chest" で始まる、またはメタデータに type=chest がある場合）
            boolean isChest = (e.getName() != null && e.getName().startsWith("chest"))
                    || ("chest".equals(e.getMeta().get("type")));

            if (isChest && chestImage != null) {
                g2.drawImage(chestImage, sx, sy, tileSize, tileSize, null);
            } else if (eventIcon != null) {
                g2.drawImage(eventIcon, sx, sy, tileSize, tileSize, null);
            }

            // 選択中の黄色い枠線
            if (e.getId().equals(selectedEventId)) {
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(sx, sy, tileSize - 1, tileSize - 1);
            }
        }
    }

    public void setSelectionListener(java.util.function.Consumer<MapEvent> listener) {
        this.selectionListener = listener;
    }

    private void fireSelectionChanged(MapEvent ev) {
        if (selectionListener != null) selectionListener.accept(ev);
    }

    public void setTool(String tool) {
        this.currentTool = tool;
    }

    private String generateEventId() {
        // 簡易ID生成。必要なら UUID に変更
        return "ev_" + Long.toHexString(System.currentTimeMillis()) + "_" + Integer.toHexString(idRand.nextInt(0xFFFF));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return tileSize;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return tileSize * 5;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public void setEventIcon(Image eventIcon) {
        this.eventIcon = eventIcon;
    }

    public void setChestImage(Image chestImage) {
        this.chestImage = chestImage;
    }
}