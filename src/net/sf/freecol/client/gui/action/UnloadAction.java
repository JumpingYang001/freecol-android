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

package net.sf.freecol.client.gui.action;

import org.freecolandroid.repackaged.java.awt.event.ActionEvent;

import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.panel.MapControls;
import net.sf.freecol.common.model.Unit;


/**
 * An action for unloading a unit.
 */
public class UnloadAction extends MapboardAction {

    public static final String id = "unloadAction";

    private Unit unit = null;

    /**
     * Creates an action for unloading the currently selected unit.
     *
     * @param freeColClient The main controller object for the client.
     * @param gui 
     */
    public UnloadAction(FreeColClient freeColClient, GUI gui) {
        this(freeColClient, gui, null);
    }

    /**
     * Creates an action for unloading the <code>Unit</code>
     * provided. If the <code>Unit</code> is <code>null</code>, then
     * the currently selected unit is used instead.
     *
     * @param freeColClient The main controller object for the client.
     * @param unit an <code>Unit</code> value
     * @see net.sf.freecol.client.gui.MapViewer#getActiveUnit()
     */
    public UnloadAction(FreeColClient freeColClient, GUI gui, Unit unit) {
        super(freeColClient, gui, id);
        this.unit = unit;
    }

    private Unit getUnit() {
        return (unit != null) ? unit
            : gui.getActiveUnit();
    }

    /**
     * Checks if this action should be enabled.
     *
     * @return <code>true</code> if there is a carrier active
     */
    @Override
    protected boolean shouldBeEnabled() {
        Unit carrier = getUnit();
        return super.shouldBeEnabled()
            && carrier != null
            && carrier.isCarrier()
            && carrier.getGoodsCount() > 0;
    }

    /**
     * Applies this action.
     * @param e The <code>ActionEvent</code>.
     */
    public void actionPerformed(ActionEvent e) {
        Unit carrier = getUnit();
        if (carrier != null) {
            getFreeColClient().getInGameController().unload(carrier);
            MapControls controls
                = ((MapControlsAction) getFreeColClient().getActionManager()
                   .getFreeColAction(MapControlsAction.id)).getMapControls();
            if (controls != null) controls.update();
        }
    }
}
