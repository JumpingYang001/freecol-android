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

import org.freecolandroid.repackaged.java.awt.datatransfer.DataFlavor;
import org.freecolandroid.repackaged.java.awt.datatransfer.Transferable;
import org.freecolandroid.repackaged.javax.swing.JLabel;


/**
* Represents an image selection that can be selected and dragged/dropped to/from
* Swing components.
*/
public final class ImageSelection implements Transferable {
    
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(ImageSelection.class.getName());

    //private static final DataFlavor[] flavors = {DataFlavor.imageFlavor};

    private JLabel label;

    /**
    * The constructor to use.
    * @param label The data that this ImageSelection should hold.
    */
    public ImageSelection(JLabel label) {
        this.label = label;
    }

    /**
    * Returns the data that this Transferable represents or 'null' if
    * the data is not of the given flavor.
    * @param flavor The flavor that the data should have.
    * @return The data that this Transferable represents or 'null' if
    * the data is not of the given flavor.
    */
    public Object getTransferData(DataFlavor flavor) {
        if (isDataFlavorSupported(flavor)) {
            return label;
        }
        return null;
    }

    /**
    * Returns the flavors that are supported by this Transferable.
    * @return The flavors that are supported by this Transferable.
    */
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavors = {DefaultTransferHandler.flavor};
        return flavors;
    }

    /**
    * Checks if the given data flavor is supported by this Transferable.
    * @param flavor The data flavor to check.
    * @return 'true' if the given data flavor is supported by this Transferable,
    * 'false' otherwise.
    */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DefaultTransferHandler.flavor);
    }
}
