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

import org.freecolandroid.debug.FCLog;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.javax.swing.Timer;
import org.freecolandroid.repackaged.javax.swing.event.EventListenerList;



import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.common.model.Tile;

public class TerrainCursor implements ActionListener  {
    

    public static final int OFF = 0;
    public static final int ON = 1;
    
    private Tile tile;
    private int canvasX;
    private int canvasY;
    private Timer blinkTimer;
    private boolean active;
    private EventListenerList listenerList;
    
    /**
     * Creates a new <code>Cursor</code> instance.
     *
     */
    public TerrainCursor(FreeColClient client) {
        active = true;
        
        final int blinkDelay = 500; // Milliseconds
        
        blinkTimer = new Timer(blinkDelay,this, client);
        blinkTimer.setRepeats(true);
        
        listenerList = new EventListenerList();
    }
    
    /**
     * Returns whether this TerrainCursor is active.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isActive(){
        return active;
    }
    
    /**
     * Sets the active state of the TerrainCursor.
     *
     * @param newState a <code>boolean</code> value
     */
    public void setActive(boolean newState){
        active = newState;
    }
    
    public void startBlinking(){
        if(!blinkTimer.isRunning())
            blinkTimer.start();
    }
    
    public void stopBlinking(){
        if(blinkTimer.isRunning())
            blinkTimer.stop();
    }
    
    public void setTile(Tile tile){
        this.tile = tile;
    }
    
    public Tile getTile(){
        return tile;
    }
    
    public void setCanvasPos(int x,int y){
        canvasX = x;
        canvasY = y;
    }
    
    public int getCanvasX(){
        return canvasX;
    }
    
    public int getCanvasY(){
        return canvasY;
    }
    
    public void addActionListener(ActionListener listener){
        listenerList.add(ActionListener.class, listener);
    }
    
    public void removeActionListener(ActionListener listener){
        listenerList.remove(ActionListener.class, listener);
    }
    
    public void actionPerformed(ActionEvent timerEvent){
        active = !active;
        int eventId = active? ON : OFF;
        ActionEvent blinkEvent = new ActionEvent(this,eventId,"blink");
        
        fireActionEvent(blinkEvent);
    }
    
    public void fireActionEvent(ActionEvent event){
        ActionListener[] listeners = listenerList.getListeners(ActionListener.class);
        for(int i=0; i < listenerList.getListenerCount(); i++){
            listeners[i].actionPerformed(event);
        }
    }
  
    
}
