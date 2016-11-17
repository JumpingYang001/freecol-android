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

import java.util.ArrayList;
import java.util.List;

import org.freecolandroid.repackaged.javax.swing.JLayeredPane;


import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.Canvas;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.ViewMode;
import net.sf.freecol.client.gui.action.ActionManager;
import net.sf.freecol.client.gui.action.BuildColonyAction;
import net.sf.freecol.client.gui.action.DisbandUnitAction;
import net.sf.freecol.client.gui.action.FortifyAction;
import net.sf.freecol.client.gui.action.FreeColAction;
import net.sf.freecol.client.gui.action.SentryAction;
import net.sf.freecol.client.gui.action.SkipUnitAction;
import net.sf.freecol.client.gui.action.WaitAction;
import net.sf.freecol.client.gui.panel.MapEditorTransformPanel.MapTransform;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;


/**
 * A collection of panels and buttons that are used to provide
 * the user with a more detailed view of certain elements on the
 * map and also to provide a means of input in case the user
 * can't use the keyboard.
 *
 * The MapControls are useless by themselves, this object needs to
 * be placed on a JComponent in order to be usable.
 */
public abstract class MapControls {

    private final FreeColClient freeColClient;

    private final InfoPanel infoPanel;
    private final MiniMap miniMap;
    private final List<UnitButton> unitButtons;

    private GUI gui;

    public static final int CONTROLS_LAYER = JLayeredPane.MODAL_LAYER;

    /**
     * The basic constructor.
     * @param freeColClient The main controller object for the client
     * @param gui
     */
    public MapControls(final FreeColClient freeColClient, GUI gui, boolean useSkin) {
        this.freeColClient = freeColClient;
        this.gui = gui;

        infoPanel = new InfoPanel(freeColClient, gui, useSkin);
        miniMap = new MiniMap(freeColClient, gui, useSkin);
        final ActionManager am = freeColClient.getActionManager();

        unitButtons = new ArrayList<UnitButton>();
        unitButtons.add(new UnitButton(am, WaitAction.id));
        unitButtons.add(new UnitButton(am, SkipUnitAction.id));
        unitButtons.add(new UnitButton(am, SentryAction.id));
        unitButtons.add(new UnitButton(am, FortifyAction.id));
        for (TileImprovementType type : freeColClient.getGame().getSpecification()
                 .getTileImprovementTypeList()) {
            FreeColAction action = am.getFreeColAction(type.getShortId()
                                                       + "Action");
            if (!type.isNatural() && action != null
                && action.hasOrderButtons()) {
                unitButtons.add(new UnitButton(am, type.getShortId() + "Action"));
            }
        }
        unitButtons.add(new UnitButton(am, BuildColonyAction.id));
        unitButtons.add(new UnitButton(am, DisbandUnitAction.id));

        //
        // Don't allow them to gain focus
        //
        infoPanel.setFocusable(false);
        miniMap.setFocusable(false);

        for (UnitButton button : unitButtons) {
            button.setFocusable(false);
        }

    }

    /**
     * Returns the mini map.
     *
     * @return a <code>MiniMap</code> value
     */
    public MiniMap getMiniMap() {
        return miniMap;
    }

    /**
     * Returns the info panel.
     *
     * @return an <code>InfoPanel</code> value
     */
    public InfoPanel getInfoPanel() {
        return infoPanel;
    }

    /**
     * Returns a list of unit buttons.
     *
     * @return an <code>List</code> value
     */
    public List<UnitButton> getUnitButtons() {
        return unitButtons;
    }

    /**
     * Returns the FreeColClient.
     *
     * @return a <code>FreeColClient</code> value
     */
    public FreeColClient getFreeColClient() {
        return freeColClient;
    }

    /**
     * Updates this <code>InfoPanel</code>.
     *
     * @param mapTransform The current MapTransform.
     */
    public void update(MapTransform mapTransform) {
        if (infoPanel != null) {
            infoPanel.update(mapTransform);
        }
    }

    /**
     * Adds the map controls to the given component.
     * @param component The component to add the map controls to.
     */
    public abstract void addToComponent(Canvas component);

    /**
     * Removes the map controls from the parent canvas component.
     *
     * @param canvas <code>Canvas</code> parent
     */
    public abstract void removeFromComponent(Canvas canvas);

    public abstract boolean isShowing();

    /**
     * Updates this <code>MapControls</code>.
     */
    public void update() {
        int viewMode = gui.getCurrentViewMode();
        switch (viewMode) {
        case ViewMode.MOVE_UNITS_MODE:
            infoPanel.update(gui.getActiveUnit());
            break;
        case ViewMode.VIEW_TERRAIN_MODE:
            if (gui.getSelectedTile() != null) {
                Tile selectedTile = gui.getSelectedTile();
                if (infoPanel.getTile() != selectedTile) {
                    infoPanel.update(selectedTile);
                }
            }
            break;
        }
    }
}
