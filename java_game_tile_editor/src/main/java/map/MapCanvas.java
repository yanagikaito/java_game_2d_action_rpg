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

    private Image eventIcon;
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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseEvent(e);
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
                    } else {

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
                        String id = generateEventId();
                        MapEvent ne = new MapEvent(id, tx, ty);
                        ne.setName("event_" + id);
                        ne.setTrigger("interact");
                        model.addEvent(ne);
                        selectedEventId = id;
                        fireSelectionChanged(ne);
                        repaint();
                    } else {
                    }
                }
            }
        });
    }

    public void setSelectedTile(int id) {
        this.selectedTileId = id;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("アリアル", Font.PLAIN, 12));
        g2.setColor(Color.BLACK);

        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                Image img = tileImages.get(model.getTile(x, y));
                if (img != null) {
                    g2.drawImage(img, x * tileSize, y * tileSize, tileSize, tileSize, null);
                }
                g2.setColor(Color.BLACK);
                g2.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);

                g2.setColor(Color.BLACK);
                String coord = x + "," + y;
                int strX = x * tileSize + 2;
                int strY = y * tileSize + tileSize - 4;
                g2.drawString(coord, strX, strY);

                g2.setColor(Color.BLACK);
            }
        }

        for (MapEvent e : model.getAllEvents()) {
            int sx = e.getX() * tileSize;
            int sy = e.getY() * tileSize;
            g.drawImage(eventIcon, sx, sy, tileSize, tileSize, null);
            if (e.getId().equals(selectedEventId)) {
                g.setColor(Color.YELLOW);
                g.drawRect(sx, sy, tileSize - 1, tileSize - 1);
            }
        }

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
                int tx = ev.getX() / tileSize;
                int ty = ev.getY() / tileSize;
                MapEvent hit = model.getEventAt(tx, ty);
                if (hit != null) {
                    selectedEventId = hit.getId();
                    fireSelectionChanged(hit);
                } else if (currentToolIsEventPlace()) {
                    MapEvent ne = new MapEvent();
                    ne.setId(generateEventId());
                    ne.setX(tx);
                    ne.setY(ty);
                    ne.setName("event_" + ne.getId());
                    model.addEvent(ne);
                    selectedEventId = ne.getId();
                    fireSelectionChanged(ne);
                    repaint();
                }
            }
        });

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

    private boolean currentToolIsEventPlace() {
        return "event".equalsIgnoreCase(currentTool);
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
}