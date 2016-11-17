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

import org.freecolandroid.repackaged.java.awt.event.WindowEvent;
import org.freecolandroid.repackaged.java.awt.event.WindowListener;

import net.sf.freecol.client.FreeColClient;

/**
* The WindowListener for the WindowedFrame class.
*/
public final class WindowedFrameListener implements WindowListener {

    private FreeColClient freeColClient;

    /**
    * Constructs this WindowListener.
    * @param canvas The <code>Canvas</code>.
    */
    public WindowedFrameListener(FreeColClient freeColClient) {
        this.freeColClient = freeColClient;
    }
    
    /**
    * Invoked when the window gets activated.
    * @param event The event that has information on the action.
    */
    public void windowActivated(WindowEvent event) {
    }
    
    /**
    * Invoked when the window gets closed.
    * @param event The event that has information on the action.
    */
    public void windowClosed(WindowEvent event) {
    }
    
    /**
    * Invoked when the window is closing.
    * @param event The event that has information on the action.
    */
    public void windowClosing(WindowEvent event) {
        freeColClient.askToQuit();
    }
    
    /**
    * Invoked when the window gets deactivated.
    * @param event The event that has information on the action.
    */
    public void windowDeactivated(WindowEvent event) {
    }
    
    /**
    * Invoked when the window gets deiconified.
    * @param event The event that has information on the action.
    */
    public void windowDeiconified(WindowEvent event) {
    }
    
    /**
    * Invoked when the window gets iconified.
    * @param event The event that has information on the action.
    */
    public void windowIconified(WindowEvent event) {
    }
    
    /**
    * Invoked when the window gets opened.
    * @param event The event that has information on the action.
    */
    public void windowOpened(WindowEvent event) {
    }
}
