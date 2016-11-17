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
import org.freecolandroid.repackaged.javax.swing.plaf.basic.BasicMenuBarUI;

import net.sf.freecol.client.gui.ImageLibrary;


/**
 * Use the "background.FreeColMenuBar" resource as a tiled background
 * image on FreeColMenuBar, such as that at the top of the window.
 */
public class FreeColMenuBarUI extends BasicMenuBarUI {

    private static FreeColMenuBarUI sharedInstance = new FreeColMenuBarUI();


    public static ComponentUI createUI(JComponent c) {
        return sharedInstance;
    }

    public void paint(org.freecolandroid.repackaged.java.awt.Graphics g, org.freecolandroid.repackaged.javax.swing.JComponent c) {
        if (c.isOpaque()) {
            ImageLibrary.drawTiledImage("background.FreeColMenuBar", g, c, null);
        }
    }

}
