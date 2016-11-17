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

import org.freecolandroid.repackaged.java.awt.Toolkit;
import org.freecolandroid.repackaged.java.awt.datatransfer.Clipboard;
import org.freecolandroid.repackaged.java.awt.datatransfer.Transferable;
import org.freecolandroid.repackaged.java.awt.event.MouseAdapter;
import org.freecolandroid.repackaged.java.awt.event.MouseEvent;
import org.freecolandroid.repackaged.javax.swing.JComponent;
import org.freecolandroid.repackaged.javax.swing.TransferHandler;


/**
* A DropListener should be attached to Swing components that have a
* TransferHandler attached. The DropListener will make sure that the
* Swing component to which it is attached can accept dragable data.
*/
public final class DropListener extends MouseAdapter {
    
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(DropListener.class.getName());

    /**
    * Gets called when the mouse was released on a Swing component that has this
    * object as a MouseListener.
    * @param e The event that holds the information about the mouse click.
    */
    public void mouseReleased(MouseEvent e) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable clipData = clipboard.getContents(clipboard);
        if (clipData != null) {
            if (clipData.isDataFlavorSupported(DefaultTransferHandler.flavor)) {
                JComponent comp = (JComponent)e.getSource();
                TransferHandler handler = comp.getTransferHandler();
                handler.importData(comp, clipData);
            }
        }
    }
}
