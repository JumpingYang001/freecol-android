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

package net.sf.freecol.server.ai.goal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.freecolandroid.xml.stream.XMLStreamException;
import org.freecolandroid.xml.stream.XMLStreamReader;
import org.freecolandroid.xml.stream.XMLStreamWriter;

import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.PathNode;
import net.sf.freecol.common.model.Unit.Role;
import net.sf.freecol.server.ai.AIMessage;
import net.sf.freecol.server.ai.AIPlayer;
import net.sf.freecol.server.ai.AIUnit;


/**
 * This {@link Goal} deals with one missionary unit.
 * </p><p>
 * On construction, an {@link AIUnit} and an {@link IndianSettlement}
 * are given to this.
 * The Goal will try to create a mission at that settlement,
 * eventually by bringing the missionary unit there first using a
 * {@link GotoAdjacentGoal}.
 * Should the target become invalid, the missionary will be given back
 * to the parent of this goal ({@link ManageMissionariesGoal}, in most cases).
 * Excess units will be given back to the parent, or the {@link AIPlayer} directly. 
 */
public class CreateMissionAtSettlementGoal extends Goal {

    private static final Logger logger = Logger.getLogger(CreateMissionAtSettlementGoal.class.getName());

    //the settlement to build a mission at
    private IndianSettlement target;
    
    //our only possible subgoal, a GoToAdjacentGoal
    private GotoAdjacentGoal gotoSubGoal;

    public CreateMissionAtSettlementGoal(AIPlayer p, Goal g, float w, AIUnit u, IndianSettlement i) {
        super(p,g,w,u);
        target = i;
        gotoSubGoal = null;
    }
    
    protected Iterator<AIUnit> getOwnedAIUnitsIterator() {
        //we're using units by putting them to individual subgoals,
        //so all our own units at any moment are the unused ones
        return availableUnitsList.iterator();
    }
    
    protected Iterator<Goal> getSubGoalIterator() {
        //For the moment, we only have one goal.
        //Let's create an iterator of it. :)
        List<Goal> subGoalList = new ArrayList<Goal>();
        if (gotoSubGoal != null) {
            subGoalList.add(gotoSubGoal);
        }
        return subGoalList.iterator();
    }
    
    protected void removeUnit(AIUnit u) {
        Iterator<AIUnit> uit = availableUnitsList.iterator();
        while (uit.hasNext()) {
            AIUnit unit = uit.next();
            if (unit.equals(u)) {
                uit.remove();
            }
        }
    }

    /**
     * Plans this goal.
     * NOTE: This goal currently does not send unit requests, but only deals
     * with the units it gets passively.          
     */ 
    protected void plan() {
        isFinished = false;
        
        //TODO: Check whether our target settlement is still valid.
        //If not, skip the rest and set isFinished = true.
        
        if (gotoSubGoal != null) {
            //We do have a gotoSubGoal, so probably a missionary there.
            //Run through available units. They must be excess,
            //so return units to our parent.
            validateOwnedUnits();
            
            Iterator<AIUnit> uit = availableUnitsList.iterator();
            while (uit.hasNext()) {
                AIUnit u = uit.next();
                uit.remove();
                addUnitToParent(u);
            }
            
            if (gotoSubGoal.isFinished()) {
                //the goto is Finished, so we should get back our missionary
                List<AIUnit> units = gotoSubGoal.cancelGoal();
                availableUnitsList.addAll(units);
                gotoSubGoal = null;
            }
        } 
        if (gotoSubGoal == null) {
            //We don't have a gotoSubGoal. Check for a missionary
            //adjacent to our target, or create a subgoal for an available missionary.
            //Return all other units.
            validateOwnedUnits();
            
            boolean hasFoundMissionary = false;
            Iterator<AIUnit> uit = availableUnitsList.iterator();
            while (uit.hasNext()) {
                AIUnit u = uit.next();
                uit.remove();
                if (u.getUnit().getRole() != Role.MISSIONARY) {
                    //TODO: Uncomment after this method has been added to AIPlayer
                    //player.addUnit(u);
                } else {
                    if (!hasFoundMissionary) {
                        hasFoundMissionary = true;
                        if (u.getUnit().getTile().isAdjacent(target.getTile())) {
                            //Missionary is adjacent, use it to finish the goal.
                            if (((IndianSettlement)target).getMissionary()==null ||
                                ((IndianSettlement)target).getMissionary().getOwner()!=player.getPlayer()) {
                                PathNode pathNode = u.getUnit().findPath(target.getTile());
                                u.getUnit().setMovesLeft(0);
                                                        
                                AIMessage.askEstablishMission(u,
                                    pathNode.getDirection(),
                                    ((IndianSettlement)target).getMissionary() != null);
                            } else {
                                //we can't establish a mission here
                                addUnitToParent(u);
                            }
                            isFinished = true;
                        } else {
                            //Missionary is not adjacent to target,
                            //use it to create a GoToAdjacentGoal
                            logger.info("Creating subgoal GotoAdjacentGoal.");
                            gotoSubGoal = new GotoAdjacentGoal(player,this,1,u,target.getTile());
                        }
                    } else {
                        //We already have one missionary at work, so we send all
                        //others back to our parent. In rare cases, this might
                        //include the one already adjacent to our target.
                        //However, that one will finish the work after being added
                        //to another goal, so this is no big deal.
                        addUnitToParent(u);
                    }
                }
            }
        }
    }

    public String getGoalDescription() {
        String descr = super.getGoalDescription();
        if (target!=null) {
            descr += ":"+target.getName();
        } else {
            descr += ":null";
        }
        return descr;
    }

    
    protected void toXMLImpl(XMLStreamWriter out) throws XMLStreamException {
        //TODO
    }
    
    protected void readFromXMLImpl(XMLStreamReader in) throws XMLStreamException {
        //TODO
    }
}
