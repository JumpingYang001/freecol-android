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

package net.sf.freecol.client.gui;


import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Graphics2D;
import org.freecolandroid.repackaged.java.awt.Point;
import org.freecolandroid.repackaged.java.awt.event.MouseEvent;
import org.freecolandroid.repackaged.java.awt.event.MouseListener;
import org.freecolandroid.repackaged.java.awt.event.MouseMotionListener;
import org.freecolandroid.repackaged.javax.swing.JComponent;
import org.freecolandroid.repackaged.javax.swing.SwingUtilities;


import net.sf.freecol.FreeCol;
import net.sf.freecol.client.ClientOptions;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.control.MapEditorController;
import net.sf.freecol.client.gui.panel.MapEditorTransformPanel.TileTypeTransform;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Map.Direction;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.resources.ResourceManager;
import net.sf.freecol.server.generator.TerrainGenerator;

/**
 * Listens to the mouse being moved at the level of the Canvas.
 */
public final class CanvasMapEditorMouseListener implements MouseListener, MouseMotionListener {

    private static final Logger logger = Logger.getLogger(CanvasMapEditorMouseListener.class.getName());

    private final Canvas canvas;

    private final MapViewer mapViewer;

    private ScrollThread scrollThread;

    private static final int DRAG_SCROLLSPACE = 100;

    private static final int AUTO_SCROLLSPACE = 1;

    private Point oldPoint;
    private Point startPoint;

    private FreeColClient freeColClient;

    private GUI gui;

    /**
     * The constructor to use.
     *
     * @param canvas The component this object gets created for.
     * @param gui The GUI that holds information such as screen resolution.
     */
    public CanvasMapEditorMouseListener(FreeColClient freeColClient, GUI gui, Canvas canvas) {
        this.freeColClient = freeColClient;
        this.gui = gui;
        this.canvas = canvas;
        this.mapViewer = gui.getMapViewer();
        this.scrollThread = null;
    }


    /**
     * This method can be called to make sure the map is loaded
     * There is no point executing mouse events if the map is not loaded
     */
    private Map getMap() {
        Map map = null;
        if (freeColClient.getGame() != null)
            map = freeColClient.getGame().getMap();
        return map;
    }

    /**
     * Invoked when a mouse button was clicked.
     *
     * @param e The MouseEvent that holds all the information.
     */
    public void mouseClicked(MouseEvent e) {
        if (getMap() == null) {
            return;
        }
        try {
            if (e.getClickCount() > 1) {
                Tile tile = mapViewer.convertToMapTile(e.getX(), e.getY());
                canvas.showColonyPanel(tile);
            } else {
                canvas.requestFocus();
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error in mouseClicked!", ex);
        }
    }

    /**
     * Invoked when the mouse enters the component.
     *
     * @param e The MouseEvent that holds all the information.
     */
    public void mouseEntered(MouseEvent e) {
        // Ignore for now.
    }

    /**
     * Invoked when the mouse exits the component.
     *
     * @param e The MouseEvent that holds all the information.
     */
    public void mouseExited(MouseEvent e) {
        // Ignore for now.
    }


    /**
     * Invoked when a mouse button was pressed.
     *
     * @param e The MouseEvent that holds all the information.
     */
    public void mousePressed(MouseEvent e) {
        if (!e.getComponent().isEnabled()) {
            return;
        }
        if (getMap() == null) {
            return;
        }
        try {
            if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {
                Tile tile = mapViewer.convertToMapTile(e.getX(), e.getY());
                if (tile != null) {
                    if (tile.hasRiver()) {
                        TileImprovement river = tile.getRiver();
                        int style = canvas.showRiverStyleDialog();
                        if (style == -1) {
                            // user canceled
                        } else if (style == 0) {
                            tile.getTileItemContainer().removeTileItem(river);
                        } else if (0 < style && style < ResourceManager.RIVER_STYLES) {
                            river.setStyle(style);
                        } else {
                            logger.warning("Unknown river style: " + style);
                        }
                    }
                    if (tile.getIndianSettlement() != null) {
                        canvas.showEditSettlementDialog(tile.getIndianSettlement());
                    }
                } else {
                    gui.setSelectedTile(null, true);
                }
            } else if (e.getButton() == MouseEvent.BUTTON1) {
                startPoint = e.getPoint();
                oldPoint = e.getPoint();
                JComponent component = (JComponent)e.getSource();
                drawBox(component, startPoint, oldPoint);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error in mousePressed!", ex);
        }
    }

    /**
     * Invoked when a mouse button was released.
     *
     * @param e The MouseEvent that holds all the information.
     */
    public void mouseReleased(MouseEvent e) {
        if (getMap() == null) {
            return;
        }
        JComponent component = (JComponent)e.getSource();

        MapEditorController controller = freeColClient
            .getMapEditorController();
        boolean isTransformActive = controller.getMapTransform() != null;

        if(startPoint == null){
        	startPoint = e.getPoint();
        }
        if(oldPoint == null){
        	oldPoint = e.getPoint();
        }
        drawBox(component, startPoint, oldPoint);
        if (gui.getFocus() != null) {
            Tile start = mapViewer.convertToMapTile(startPoint.x, startPoint.y);
            Tile end = start;
            //Optimization, only check if the points are different
            if(startPoint.x != oldPoint.x || startPoint.y != oldPoint.y){
            	end = mapViewer.convertToMapTile(oldPoint.x, oldPoint.y);
            }

            // no option selected, just center map
            if(!isTransformActive){
            	gui.setFocus(end);
            	return;
            }

            // find the area to transform
            int min_x, max_x, min_y, max_y;
            if (start.getX() < end.getX()) {
                min_x = start.getX();
                max_x = end.getX();
            } else {
                min_x = end.getX();
                max_x = start.getX();
            }
            if (start.getY() < end.getY()) {
                min_y = start.getY();
                max_y = end.getY();
            } else {
                min_y = end.getY();
                max_y = start.getY();
            }

            // apply transformation to all tiles in the area
            Tile t = null;
            for (int x = min_x; x <= max_x; x++) {
                for (int y = min_y; y <= max_y; y++) {
                    t = getMap().getTile(x, y);
                    if (t != null) {
                        controller.transform(t);
                    }
                }
            }
            if (controller.getMapTransform() instanceof TileTypeTransform) {
                for (int x = min_x - 2; x <= max_x + 2; x++) {
                    for (int y = min_y - 2; y <= max_y + 2; y++) {
                        t = getMap().getTile(x, y);
                        if (t != null && t.getType().isWater()) {
                            TerrainGenerator.encodeStyle(t);
                        }
                    }
                }
            }
            gui.refresh();
            canvas.requestFocus();
        }
    }

    /**
     * Invoked when the mouse has been moved.
     *
     * @param e The MouseEvent that holds all the information.
     */
    public void mouseMoved(MouseEvent e) {
        if (getMap() == null) {
            return;
        }

        if (e.getComponent().isEnabled() &&
            freeColClient.getClientOptions()
            .getBoolean(ClientOptions.AUTO_SCROLL)) {
            auto_scroll(e.getX(), e.getY());
        } else if (scrollThread != null) {
            scrollThread.stopScrolling();
            scrollThread = null;
        }
    }

    /**
     * Invoked when the mouse has been dragged.
     *
     * @param e The MouseEvent that holds all the information.
     */
    public void mouseDragged(MouseEvent e) {
        if (getMap() == null) {
            return;
        }

        JComponent component = (JComponent)e.getSource();
        drawBox(component, startPoint, oldPoint);
        oldPoint = e.getPoint();
        drawBox(component, startPoint, oldPoint);
        if (e.getComponent().isEnabled() &&
            freeColClient.getClientOptions()
            .getBoolean(ClientOptions.MAP_SCROLL_ON_DRAG)) {
            drag_scroll(e.getX(), e.getY());
        } else if (scrollThread != null) {
            scrollThread.stopScrolling();
            scrollThread = null;
        }
        gui.refresh();
    }

    private void drawBox(JComponent component, Point startPoint, Point endPoint) {
        if(startPoint == null || endPoint == null){
        	return;
        }
        if(startPoint.distance(endPoint) == 0){
        	return;
        }

        // only bother to draw if a transformation is active
        MapEditorController controller = freeColClient.getMapEditorController();
        if(controller.getMapTransform() == null){
        	return;
        }

    	Graphics2D graphics = (Graphics2D) component.getGraphics ();
        graphics.setColor(Color.WHITE);
        int x = Math.min(startPoint.x, endPoint.x);
        int y = Math.min(startPoint.y, endPoint.y);
        int width = Math.abs(startPoint.x - endPoint.x);
        int height = Math.abs(startPoint.y - endPoint.y);
        graphics.drawRect(x, y, width, height);
    }

    private void auto_scroll(int x, int y){
        scroll(x, y, AUTO_SCROLLSPACE);
    }

    private void drag_scroll(int x, int y){
        scroll(x, y, DRAG_SCROLLSPACE);
    }

    private void scroll(int x, int y, int scrollspace) {
        if (getMap() == null) {
            return;
        }

        /*
         * if (y < canvas.getMenuBarHeight()) { if (scrollThread != null) {
         * scrollThread.stopScrolling(); scrollThread = null; } return; } else
         * if (y < canvas.getMenuBarHeight() + SCROLLSPACE) { y -=
         * canvas.getMenuBarHeight(); }
         */

        Direction direction;
        if ((x < scrollspace) && (y < scrollspace)) {
            // Upper-Left
            direction = Direction.NW;
        } else if ((x >= mapViewer.getWidth() - scrollspace) && (y < scrollspace)) {
            // Upper-Right
            direction = Direction.NE;
        } else if ((x >= mapViewer.getWidth() - scrollspace) && (y >= mapViewer.getHeight() - scrollspace)) {
            // Bottom-Right
            direction = Direction.SE;
        } else if ((x < scrollspace) && (y >= mapViewer.getHeight() - scrollspace)) {
            // Bottom-Left
            direction = Direction.SW;
        } else if (y < scrollspace) {
            // Top
            direction = Direction.N;
        } else if (x >= mapViewer.getWidth() - scrollspace) {
            // Right
            direction = Direction.E;
        } else if (y >= mapViewer.getHeight() - scrollspace) {
            // Bottom
            direction = Direction.S;
        } else if (x < scrollspace) {
            // Left
            direction = Direction.W;
        } else {
            // Center
            if (scrollThread != null) {
                scrollThread.stopScrolling();
                scrollThread = null;
            }
            return;
        }

        if (scrollThread != null) {
            // continue scrolling in a (perhaps new) direction
            scrollThread.setDirection(direction);
        } else {
            // start scrolling in a direction
            scrollThread = new ScrollThread(mapViewer);
            scrollThread.setDirection(direction);
            scrollThread.start();
        }
    }


    /**
     * Scrolls the view of the Map by moving its focus.
     */
    private class ScrollThread extends Thread {

        private final MapViewer mapViewer;

        private Direction direction;

        private boolean cont;


        /**
         * The constructor to use.
         *
         * @param mapViewer The GUI that holds information such as screen resolution.
         */
        public ScrollThread(MapViewer mapViewer) {
            super(FreeCol.CLIENT_THREAD+"Mouse scroller");
            this.mapViewer = mapViewer;
            this.cont = true;
        }

        /**
         * Sets the direction in which this ScrollThread will scroll.
         *
         * @param d The direction in which this ScrollThread will scroll.
         */
        public void setDirection(Direction d) {
            direction = d;
        }

        /**
         * Makes this ScrollThread stop doing what it is supposed to do.
         */
        public void stopScrolling() {
            cont = false;
        }

        /**
         * Performs the actual scrolling.
         */
        @Override
        public void run() {
            do {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                }

                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                try {
                                    int x, y;
                                    Tile t = mapViewer.getFocus();
                                    if (t == null) {
                                        return;
                                    }

                                    t = t.getNeighbourOrNull(direction);
                                    if (t == null) {
                                        return;
                                    }

                                    if (mapViewer.isMapNearTop(t.getY()) && mapViewer.isMapNearTop(mapViewer.getFocus().getY())) {
                                        if (t.getY() > mapViewer.getFocus().getY()) {
                                            y = t.getY();
                                            do {
                                                y += 2;
                                            } while (mapViewer.isMapNearTop(y));
                                        } else {
                                            y = mapViewer.getFocus().getY();
                                        }
                                    } else if (mapViewer.isMapNearBottom(t.getY()) && mapViewer.isMapNearBottom(mapViewer.getFocus().getY())) {
                                        if (t.getY() < mapViewer.getFocus().getY()) {
                                            y = t.getY();
                                            do {
                                                y -= 2;
                                            } while (mapViewer.isMapNearBottom(y));
                                        } else {
                                            y = mapViewer.getFocus().getY();
                                        }
                                    } else {
                                        y = t.getY();
                                    }

                                    if (mapViewer.isMapNearLeft(t.getX(), t.getY())
                                        && mapViewer.isMapNearLeft(mapViewer.getFocus().getX(), mapViewer.getFocus().getY())) {
                                        if (t.getX() > mapViewer.getFocus().getX()) {
                                            x = t.getX();
                                            do {
                                                x++;
                                            } while (mapViewer.isMapNearLeft(x, y));
                                        } else {
                                            x = mapViewer.getFocus().getX();
                                        }
                                    } else if (mapViewer.isMapNearRight(t.getX(), t.getY())
                                               && mapViewer.isMapNearRight(mapViewer.getFocus().getX(), mapViewer.getFocus().getY())) {
                                        if (t.getX() < mapViewer.getFocus().getX()) {
                                            x = t.getX();
                                            do {
                                                x--;
                                            } while (mapViewer.isMapNearRight(x, y));
                                        } else {
                                            x = mapViewer.getFocus().getX();
                                        }
                                    } else {
                                        x = t.getX();
                                    }

                                    mapViewer.setFocus(getMap().getTile(x,y));
                                } catch (Exception e) {
                                    logger.log(Level.WARNING, "Exception while scrolling!", e);
                                }
                            }
                        });
                } catch (InvocationTargetException e) {
                    logger.log(Level.WARNING, "Scroll thread caught error", e);
                    cont = false;
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Scroll thread interrupted", e);
                    cont = false;
                }
            } while (cont);
        }
    }
}
