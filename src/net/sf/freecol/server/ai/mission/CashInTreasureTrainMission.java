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

package net.sf.freecol.server.ai.mission;

import java.util.logging.Logger;

import org.freecolandroid.xml.stream.XMLStreamException;
import org.freecolandroid.xml.stream.XMLStreamReader;
import org.freecolandroid.xml.stream.XMLStreamWriter;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Map.Direction;
import net.sf.freecol.common.model.PathNode;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.pathfinding.CostDeciders;
import net.sf.freecol.common.model.pathfinding.GoalDecider;
import net.sf.freecol.common.networking.Connection;
import net.sf.freecol.server.ai.AIMain;
import net.sf.freecol.server.ai.AIMessage;
import net.sf.freecol.server.ai.AIUnit;

import org.w3c.dom.Element;


/**
 * Mission for cashing in a treasure train.
 */
public class CashInTreasureTrainMission extends Mission {
    // TODO: Use a transport
    // TODO: Avoid enemy units
    // TODO: Require protection

    private static final Logger logger = Logger.getLogger(CashInTreasureTrainMission.class.getName());


    /**
     * Creates a mission for the given <code>AIUnit</code>.
     *
     * @param aiMain The main AI-object.
     * @param aiUnit The <code>AIUnit</code> this mission
     *        is created for.
     */
    public CashInTreasureTrainMission(AIMain aiMain, AIUnit aiUnit) {
        super(aiMain, aiUnit);
    }

    /**
     * Loads a mission from the given element.
     *
     * @param aiMain The main AI-object.
     * @param element An <code>Element</code> containing an
     *      XML-representation of this object.
     */
    public CashInTreasureTrainMission(AIMain aiMain, Element element) {
        super(aiMain);
        readFromXMLElement(element);
    }

    /**
     * Creates a new <code>CashInTreasureTrainMission</code> and reads the given element.
     *
     * @param aiMain The main AI-object.
     * @param in The input stream containing the XML.
     * @throws XMLStreamException if a problem was encountered
     *      during parsing.
     * @see net.sf.freecol.server.ai.AIObject#readFromXML
     */
     public CashInTreasureTrainMission(AIMain aiMain, XMLStreamReader in) throws XMLStreamException {
         super(aiMain);
         readFromXML(in);
     }

    /**
     * Disposes this <code>Mission</code>.
     */
    public void dispose() {
        super.dispose();
    }

    /**
     * Performs this mission.
     * @param connection The <code>Connection</code> to the server.
     */
    public void doMission(Connection connection) {
        Unit unit = getUnit();
    	Map map = unit.getGame().getMap();

        if (!isValid()) {
            return;
        }

        if (!unit.canCashInTreasureTrain()) {
            GoalDecider destinationDecider = new GoalDecider() {
                private PathNode best = null;

                public PathNode getGoal() {
                    return best;
                }

                public boolean hasSubGoals() {
                    return false;
                }

                public boolean check(Unit u, PathNode pathNode) {
                    Tile t = pathNode.getTile();
                    if (u.canCashInTreasureTrain(t)) {
                        best = pathNode;
                        return true;
                    }
                    return false;
                }
            };
            PathNode bestPath = unit.search(unit.getTile(), destinationDecider,
                CostDeciders.avoidSettlementsAndBlockingUnits(),
                Integer.MAX_VALUE, null);
            if (bestPath != null) {
                Direction r = moveTowards(bestPath);
                if (r == null || !moveButDontAttack(r)) return;
            }
        }

        if (unit.canCashInTreasureTrain()) {
            AIMessage.askCashInTreasureTrain(getAIUnit());
        }
    }

    /**
     * Returns the destination for this <code>Transportable</code>.
     * This can either be the target {@link Tile} of the transport
     * or the target for the entire <code>Transportable</code>'s
     * mission. The target for the tansport is determined by
     * {@link TransportMission} in the latter case.
     *
     * @return The destination for this <code>Transportable</code>.
     */
    public Tile getTransportDestination() {
        return null;
    }

    /**
     * Returns the priority of getting the unit to the
     * transport destination.
     *
     * @return The priority.
     */
    public int getTransportPriority() {
        if (getTransportDestination() != null) {
            return NORMAL_TRANSPORT_PRIORITY;
        } else {
            return 0;
        }
    }

    /**
     * Writes all of the <code>AIObject</code>s and other AI-related
     * information to an XML-stream.
     *
     * @param out The target stream.
     * @throws XMLStreamException if there are any problems writing
     *      to the stream.
     */
    protected void toXMLImpl(XMLStreamWriter out) throws XMLStreamException {
        toXML(out, getXMLElementTagName());
    }

    /**
     * Returns the tag name of the root element representing this object.
     *
     * @return "cashInTreasureTrainMission".
     */
    public static String getXMLElementTagName() {
        return "cashInTreasureTrainMission";
    }
}
