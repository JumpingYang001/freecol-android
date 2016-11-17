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

package net.sf.freecol.client.gui.plaf;


import org.freecolandroid.repackaged.javax.swing.JComponent;
import org.freecolandroid.repackaged.javax.swing.plaf.ComponentUI;
import org.freecolandroid.repackaged.javax.swing.plaf.basic.BasicPanelUI;

import net.sf.freecol.client.gui.ImageLibrary;


/**
 * Draw the "background.FreeColPanel" resource as a tiled background image.
 */
public class FreeColPanelUI extends BasicPanelUI {

    private static FreeColPanelUI sharedInstance = new FreeColPanelUI();


    private FreeColPanelUI() {}

    public static ComponentUI createUI(JComponent c) {
        return sharedInstance;
    }

    public void paint(org.freecolandroid.repackaged.java.awt.Graphics g, org.freecolandroid.repackaged.javax.swing.JComponent c) {
        if (c.isOpaque()) {
            ImageLibrary.drawTiledImage("background.FreeColPanel", g, c, null);
        }
    }
}
