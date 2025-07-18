package map;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Map;

public class MapCanvas extends JPanel implements Scrollable {

    private final MapModel model;
    private final Map<Integer, Image> tileImages;
    private int selectedTileId = 0;
    private final int tileSize = 32;

    public MapCanvas(@NotNull MapModel model, Map<Integer, Image> tileImages) {
        this.model = model;
        this.tileImages = tileImages;
        setPreferredSize(new Dimension(
                model.getWidth() * tileSize,
                model.getHeight() * tileSize
        ));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX() / tileSize;
                int y = e.getY() / tileSize;
                model.setTile(x, y, selectedTileId);
                repaint();
            }
        });
    }

    public void setSelectedTile(int id) {
        this.selectedTileId = id;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                int id = model.getTile(x, y);
                BufferedImage img = (BufferedImage) tileImages.get(id);
                if (img != null) {
                    g.drawImage(img, x * tileSize, y * tileSize, tileSize, tileSize, null);
                }
                g.setColor(Color.GRAY);
                g.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }
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
}