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

import org.freecolandroid.repackaged.java.awt.AlphaComposite;
import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Composite;
import org.freecolandroid.repackaged.java.awt.Graphics2D;
import org.freecolandroid.repackaged.javax.swing.JComponent;
import org.freecolandroid.repackaged.javax.swing.plaf.ComponentUI;
import org.freecolandroid.repackaged.javax.swing.plaf.basic.BasicPanelUI;




/**
 * Draws with partial transparency.  Used in Europe.
 */
public class FreeColTransparentPanelUI extends BasicPanelUI {

    private static FreeColTransparentPanelUI sharedInstance = new FreeColTransparentPanelUI();


    public static ComponentUI createUI(JComponent c) {
        return sharedInstance;
    }

    public void paint(org.freecolandroid.repackaged.java.awt.Graphics g, org.freecolandroid.repackaged.javax.swing.JComponent c) {
        if (c.isOpaque()) {
            throw new IllegalStateException("FreeColTransparentPanelUI can only be used on components which are !isOpaque()");
        }

        int width = c.getWidth();
        int height = c.getHeight();

        Composite oldComposite = ((Graphics2D) g).getComposite();
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        ((Graphics2D) g).setComposite(oldComposite);
    }

}
