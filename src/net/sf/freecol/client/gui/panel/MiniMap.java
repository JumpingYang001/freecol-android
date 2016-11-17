/**
 *  Copyright (C) 2002-2012   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.client.gui.panel;


import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.BasicStroke;
import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Graphics;
import org.freecolandroid.repackaged.java.awt.Graphics2D;
import org.freecolandroid.repackaged.java.awt.Image;
import org.freecolandroid.repackaged.java.awt.Rectangle;
import org.freecolandroid.repackaged.java.awt.RenderingHints;
import org.freecolandroid.repackaged.java.awt.event.MouseEvent;
import org.freecolandroid.repackaged.java.awt.geom.AffineTransform;
import org.freecolandroid.repackaged.java.awt.geom.GeneralPath;
import org.freecolandroid.repackaged.javax.swing.JButton;
import org.freecolandroid.repackaged.javax.swing.JPanel;
import org.freecolandroid.repackaged.javax.swing.border.BevelBorder;
import org.freecolandroid.repackaged.javax.swing.event.MouseInputListener;


import net.sf.freecol.client.ClientOptions;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.ImageLibrary;
import net.sf.freecol.client.gui.action.MiniMapZoomInAction;
import net.sf.freecol.client.gui.action.MiniMapZoomOutAction;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.option.IntegerOption;
import net.sf.freecol.common.resources.ResourceManager;



/**
 * This component draws a small version of the map. It allows us
 * to see a larger part of the map and to relocate the viewport by
 * clicking on it.
 */
public final class MiniMap extends JPanel implements MouseInputListener {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(MiniMap.class.getName());

    public static final int MAX_TILE_SIZE = 24;
    public static final int MIN_TILE_SIZE = 4;
    public static final int SCALE_STEP = 4;

    private static final int MAP_WIDTH = 220;
    private static final int MAP_HEIGHT = 128;
    private static final int GAP = 4;

    /**
     * The part of the panel that is used for the map. If a skin is
     * used, this is smaller than the whole panel.
     */
    private final Rectangle mapWindow;

    private FreeColClient freeColClient;
    private final JButton miniMapZoomOutButton;
    private final JButton miniMapZoomInButton;
    private Color backgroundColor;
    private Image back = ResourceManager.getImage("MiniMap.back");
    private Image skin = ResourceManager.getImage("MiniMap.skin");
    private Color newBackground = ResourceManager.getColor("miniMapBackground.color");
    private boolean useSkin = true;

    private int tileSize; //tileSize is the size (in pixels) that each tile will take up on the mini map

    /**
     * The top left tile on the mini map represents the tile.
     * (firstColumn, firstRow) in the world map
     */
    private int firstColumn, firstRow, lastColumn, lastRow;

    /**
     * Used for adjusting the position of the mapboard image.
     * @see #paintMap
     */
    private int adjustX = 0, adjustY = 0;

    private GUI gui;

    /**
     * The constructor that will initialize this component.
     *
     * @param freeColClient The main controller object for the client
     * @param gui a <code>GUI</code> value
     */
    public MiniMap(FreeColClient freeColClient, GUI gui) {
        this(freeColClient, gui, true);
    }

    /**
     * The constructor that will initialize this component.
     *
     * @param freeColClient The main controller object for the client
     * @param gui a <code>GUI</code> value
     */
    public MiniMap(FreeColClient freeColClient, GUI gui, boolean useSkin) {
        this.freeColClient = freeColClient;
        this.gui = gui;
        this.useSkin = useSkin;
        backgroundColor = Color.BLACK;
        setLayout(null);

        tileSize = 4 * (freeColClient.getClientOptions().getInteger(ClientOptions.DEFAULT_MINIMAP_ZOOM) + 1);

        addMouseListener(this);
        addMouseMotionListener(this);

        // Add buttons:
        miniMapZoomOutButton = new UnitButton(freeColClient.getActionManager(), MiniMapZoomOutAction.id);
        miniMapZoomInButton = new UnitButton(freeColClient.getActionManager(), MiniMapZoomInAction.id);

        miniMapZoomOutButton.setFocusable(false);
        miniMapZoomInButton.setFocusable(false);

        add(miniMapZoomInButton);
        add(miniMapZoomOutButton);

        if (useSkin && skin != null) {
            setBorder(null);
            setSize(skin.getWidth(null), skin.getHeight(null));
            setOpaque(false);
            // TODO-LATER: The values below should be specified by a skin-configuration-file:
            mapWindow = new Rectangle(38, 75, MAP_WIDTH, MAP_HEIGHT);
            miniMapZoomInButton.setLocation(4, 174);
            miniMapZoomOutButton.setLocation(264, 174);
        } else {
            int width = miniMapZoomOutButton.getWidth()
                + miniMapZoomInButton.getWidth() + 4 * GAP;
            setBorder(new BevelBorder(BevelBorder.RAISED));
            setSize(MAP_WIDTH + width, MAP_HEIGHT + 2 * GAP);
            setOpaque(true);
            mapWindow = new Rectangle(width/2, GAP, MAP_WIDTH, MAP_HEIGHT);
            miniMapZoomInButton.setLocation(GAP, MAP_HEIGHT + GAP - miniMapZoomInButton.getHeight());
            miniMapZoomOutButton.setLocation(getWidth() - GAP - miniMapZoomOutButton.getWidth(),
                                             MAP_HEIGHT + GAP - miniMapZoomOutButton.getHeight());
        }

    }

    /**
     * Zooms in the mini map.
     */
    public void zoomIn() {
        tileSize = Math.min(tileSize + SCALE_STEP, MAX_TILE_SIZE);
        setZoomOption(tileSize);
        ((MiniMapZoomInAction) miniMapZoomInButton.getAction()).update();
        repaint();
    }


    /**
     * Zooms out the mini map.
     */
    public void zoomOut() {
        tileSize = Math.max(tileSize - SCALE_STEP, MIN_TILE_SIZE);
        setZoomOption(tileSize);
        ((MiniMapZoomOutAction) miniMapZoomOutButton.getAction()).update();
        repaint();
    }

    /**
     * Set tile size to the given value, or the minimum or maximum
     * bound of the tile size.
     *
     * @param size an <code>int</code> value
     */
    public void setTileSize(int size) {
        tileSize = Math.max(Math.min(size, MAX_TILE_SIZE), MIN_TILE_SIZE);
        setZoomOption(tileSize);
        ((MiniMapZoomOutAction) miniMapZoomOutButton.getAction()).update();
        repaint();
    }

    /**
     * Return true if tile size can be decreased.
     *
     * @return a <code>boolean</code> value
     */
    public boolean canZoomIn() {
        return (freeColClient.getGame() != null
                && freeColClient.getGame().getMap() != null
                && tileSize < MAX_TILE_SIZE);
    }

    /**
     * Return true if tile size can be increased.
     *
     * @return a <code>boolean</code> value
     */
    public boolean canZoomOut() {
        return (freeColClient.getGame() != null
                && freeColClient.getGame().getMap() != null
                && tileSize > MIN_TILE_SIZE);
    }


    private void setZoomOption(int tileSize) {
        int zoom = tileSize / 4 - 1;
        ((IntegerOption) freeColClient.getClientOptions().getOption(ClientOptions.DEFAULT_MINIMAP_ZOOM))
        .setValue(zoom);
    }


    /**
     * Paints this component.
     * @param graphics The <code>Graphics</code> context in which
     *                 to draw this component.
     */
    @Override
    public void paintComponent(Graphics graphics) {
        if (freeColClient.getGame() == null
            || freeColClient.getGame().getMap() == null) {
            return;
        }
    	this.setBackgroundColor(newBackground);

        if (useSkin && skin != null) {
            graphics.drawImage(back, 0, 0, null);
            graphics.drawImage(skin, 0, 0, null);
        }
        graphics.translate(mapWindow.x, mapWindow.y);
        paintMap(graphics, MAP_WIDTH, MAP_HEIGHT);
        graphics.translate(-mapWindow.x, -mapWindow.y);
    }

    private Color getMinimapColor(TileType type) {
        return ResourceManager.getColor(type.getId() + ".color");
    }


    /**
     * Paints a representation of the mapboard onto this component.
     * @param graphics The <code>Graphics</code> context in which
     *                 to draw this component.
     * @param width The width of the map.
     * @param height The height of the map.
     */
    public void paintMap(Graphics graphics, int width, int height) {
        final Graphics2D g = (Graphics2D) graphics;
        final AffineTransform originTransform = g.getTransform();
        final Map map = freeColClient.getGame().getMap();
        final ImageLibrary library = gui.getImageLibrary();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);

        /* Fill the rectangle with background color */
        g.setColor(ResourceManager.getColor("miniMapBackground.color"));
        g.fillRect(0, 0, width, height);

        if (gui.getFocus() == null) {
            return;
        }

        /* xSize and ySize represent how many tiles can be represented on the
           mini map at the current zoom level */
        int xSize = width / tileSize;
        int ySize = (height / tileSize) * 4;

        /* Center the mini map correctly based on the map's focus */
        firstColumn = gui.getFocus().getX() - (xSize / 2);
        firstRow = gui.getFocus().getY() - (ySize / 2);

        /* Make sure the mini map won't try to display tiles off the
         * bounds of the world map */

        if (firstColumn < 0) {
            firstColumn = 0;
        } else if (firstColumn + xSize + 1 > map.getWidth()) {
            firstColumn = map.getWidth() - xSize - 1;
        }
        if (firstRow < 0) {
            firstRow = 0;
        } else if (firstRow + ySize + 1> map.getHeight()) {
            firstRow = map.getHeight() - ySize - 1;
        }


        if (map.getWidth() <= xSize) {
            firstColumn = 0;
            adjustX = ((xSize - map.getWidth()) * tileSize)/2;
            width = map.getWidth() * tileSize;
        } else {
            adjustX = 0;
        }

        if (map.getHeight() <= ySize) {
            firstRow = 0;
            adjustY = ((ySize - map.getHeight()) * tileSize)/8;
            height = map.getHeight() * (tileSize/4);
        } else {
            adjustY = 0;
        }

        lastRow = Math.min(firstRow + ySize, map.getHeight() - 1);
        lastColumn = Math.min(firstColumn + xSize, map.getWidth() - 1);

        int tileWidth = tileSize;
        int tileHeight = tileSize/2;
        int halfWidth = tileSize/2;
        int halfHeight = tileSize/4;

        /* Iterate through all the squares on the mini map and paint the
         * tiles based on terrain */
        GeneralPath tilePath = new GeneralPath();
        tilePath.moveTo(halfWidth, 0);
        tilePath.lineTo(tileWidth, halfHeight);
        tilePath.lineTo(halfWidth, tileHeight);
        tilePath.lineTo(0, halfHeight);
        tilePath.closePath();
        GeneralPath settlementPath = new GeneralPath(tilePath);
        settlementPath.transform(AffineTransform.getScaleInstance(0.7, 0.7));
        settlementPath.transform(AffineTransform.getTranslateInstance(0.15 * tileWidth, 0.15 * tileHeight));
        GeneralPath unitPath = new GeneralPath(tilePath);
        unitPath.transform(AffineTransform.getScaleInstance(0.5, 0.5));
        unitPath.transform(AffineTransform.getTranslateInstance(0.25 * tileWidth, 0.25 * tileHeight));
        g.setStroke(new BasicStroke(1f));

        AffineTransform baseTransform = g.getTransform();
        AffineTransform rowTransform = null;

        // Row per row; start with the top modified row
        for (int row = firstRow; row <= lastRow; row++) {
            rowTransform = g.getTransform();
            if (row % 2 == 1) {
                g.translate(halfWidth, 0);
            }

            // Column per column; start at the left side to display the tiles.
            for (int column = firstColumn; column <= lastColumn; column++) {
                Tile tile = map.getTile(column, row);
                if (tile.isExplored()) {
                    g.setColor(getMinimapColor(tile.getType()));
                    g.fill(tilePath);
                    if (tile.getSettlement() == null) {
                        Unit unit = tile.getFirstUnit();
                        if (unit != null) {
                            g.setColor(Color.BLACK);
                            g.draw(unitPath);
                            g.setColor(library.getColor(unit.getOwner()));
                            g.fill(unitPath);
                        }
                    } else {
                        g.setColor(Color.BLACK);
                        g.draw(settlementPath);
                        g.setColor(library.getColor(tile.getSettlement().getOwner()));
                        g.fill(settlementPath);
                    }
                }
                g.translate(tileWidth, 0);
            }
            g.setTransform(rowTransform);
            g.translate(0, halfHeight);
        }
        g.setTransform(baseTransform);

        /* Defines where to draw the white rectangle on the mini map.
         * miniRectX/Y are the center of the rectangle.
         * Use miniRectWidth/Height / 2 to get the upper left corner.
         * x/yTiles are the number of tiles that fit on the large map */
        if (getParent() != null) {
            TileType tileType = freeColClient.getGame().getSpecification().getTileTypeList().get(0);
            int miniRectX = (gui.getFocus().getX() - firstColumn) * tileSize;
            int miniRectY = (gui.getFocus().getY() - firstRow) * tileSize / 4;
            int miniRectWidth = (getParent().getWidth() / library.getTerrainImageWidth(tileType) + 1) * tileSize;
            int miniRectHeight = (getParent().getHeight() / library.getTerrainImageHeight(tileType) + 1) * tileSize / 2;
            if (miniRectX + miniRectWidth / 2 > width) {
                miniRectX = width - miniRectWidth / 2 - 1;
            } else if (miniRectX - miniRectWidth / 2 < 0) {
                miniRectX = miniRectWidth / 2;
            }
            if (miniRectY + miniRectHeight / 2 > height) {
                miniRectY = height - miniRectHeight / 2 - 1;
            } else if (miniRectY - miniRectHeight / 2 < 0) {
                miniRectY = miniRectHeight / 2;
            }

            g.setColor(ResourceManager.getColor("miniMapBorder.color"));
            /* Use Math max and min to prevent the rect from being larger than the minimap. */
            int miniRectMaxX = Math.max(miniRectX - miniRectWidth / 2, 0);
            int miniRectMaxY = Math.max(miniRectY - miniRectHeight / 2, 0);
            int miniRectMinWidth = Math.min(miniRectWidth, width - 1);
            int miniRectMinHeight = Math.min(miniRectHeight, height - 1);
            /* Prevent the rect from overlapping the bigger adjust rect */
            if(miniRectMaxX + miniRectMinWidth > width - 1) {
                miniRectMaxX = width - miniRectMinWidth - 1;
            }
            if(miniRectMaxY + miniRectMinHeight > height - 1) {
                miniRectMaxY = height - miniRectMinHeight - 1;
            }
            /* Draw the rect. */
            g.drawRect(miniRectMaxX, miniRectMaxY, miniRectMinWidth, miniRectMinHeight);
            /* Draw an additional rect, if the whole map is shown on the minimap */
            if (adjustX > 0 && adjustY > 0) {
                g.setColor(ResourceManager.getColor("miniMapBorder.color"));
                g.drawRect(0, 0, width - 1, height - 1);
            }
        }
        g.setTransform(originTransform);
    }


    private void focus(int x, int y) {
        int tileX = ((x - adjustX) / tileSize) + firstColumn;
        int tileY = ((y - adjustY) / tileSize * 4) + firstRow;

        gui.setFocus(freeColClient.getGame().getMap().getTile(tileX,tileY));
    }

    private void focus(MouseEvent e) {
        if (e.getComponent().isEnabled() && mapWindow.contains(e.getPoint())) {
            focus(e.getX() - mapWindow.x, e.getY() - mapWindow.y);
        }
    }

    public void mouseClicked(MouseEvent e) {

    }

    /**
     * If the user clicks on the mini map, refocus the map
     * to center on the tile that he clicked on
     * @param e a <code>MouseEvent</code> value
     */
    public void mousePressed(MouseEvent e) {
        focus(e);
    }


    public void mouseReleased(MouseEvent e) {

    }


    public void mouseEntered(MouseEvent e) {

    }


    public void mouseExited(MouseEvent e) {

    }


    public void mouseDragged(MouseEvent e) {
        focus(e);
    }


    public void mouseMoved(MouseEvent e) {

    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
