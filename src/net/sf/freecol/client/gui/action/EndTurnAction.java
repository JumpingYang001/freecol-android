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
import org.freecolandroid.repackaged.java.awt.event.KeyEvent;
import org.freecolandroid.repackaged.javax.swing.KeyStroke;



import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;

/**
 * An action for ending the turn.
 *
 * @see net.sf.freecol.client.gui.panel.MapControls
 */
public class EndTurnAction extends MapboardAction {

    public static final String id = "endTurnAction";


    /**
     * Creates a new <code>EndTurnAction</code>.
     *
     * @param freeColClient The main controller object for the client.
     * @param gui 
     */
    EndTurnAction(FreeColClient freeColClient, GUI gui) {
        super(freeColClient, gui, id);
        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    }

    /**
     * Applies this action.
     *
     * @param e The <code>ActionEvent</code>.
     */
    public void actionPerformed(ActionEvent e) {
        getFreeColClient().getInGameController().endTurn();
    }
}
