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

import net.sf.freecol.common.model.Location;
import net.sf.freecol.common.model.PathNode;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Map.Direction;
import net.sf.freecol.common.networking.Connection;
import net.sf.freecol.server.ai.AIMain;
import net.sf.freecol.server.ai.AIUnit;

import org.w3c.dom.Element;


/**
 * Mission for idling in colony.
 * TODO: this has now been generalized to settlements, rename one day.
 */
public class IdleAtColonyMission extends Mission {

    private static final Logger logger = Logger.getLogger(IdleAtColonyMission.class.getName());


    /**
     * Creates a mission for the given <code>AIUnit</code>.
     *
     * @param aiMain The main AI-object.
     * @param aiUnit The <code>AIUnit</code> this mission
     *        is created for.
     */
    public IdleAtColonyMission(AIMain aiMain, AIUnit aiUnit) {
        super(aiMain, aiUnit);
    }


    /**
     * Loads a mission from the given element.
     *
     * @param aiMain The main AI-object.
     * @param element An <code>Element</code> containing an
     *      XML-representation of this object.
     */
    public IdleAtColonyMission(AIMain aiMain, Element element) {
        super(aiMain);
        readFromXMLElement(element);
    }

    /**
     * Creates a new <code>IdleAtColonyMission</code> and reads the
     * given element.
     *
     * @param aiMain The main AI-object.
     * @param in The input stream containing the XML.
     * @throws XMLStreamException if a problem was encountered
     *      during parsing.
     * @see net.sf.freecol.server.ai.AIObject#readFromXML
     */
    public IdleAtColonyMission(AIMain aiMain, XMLStreamReader in)
        throws XMLStreamException {
        super(aiMain);
        readFromXML(in);
    }


    /**
     * Gets the transport destination for units with this mission.
     *
     * @return The destination for this <code>Transportable</code>.
     */
    public Location getTransportDestination() {
        final Unit unit = getUnit();
        if (unit.getTile() == null
            || unit.getTile().getSettlement() != null) return null;
        PathNode path = findNearestOtherSettlement(unit);
        Tile target = (path == null) ? null : path.getLastNode().getTile();
        return (target != null && shouldTakeTransportToTile(target))
            ? target
            : null;
    }

    /**
     * Should this Mission only be carried out once?
     *
     * @return True.
     */
    public boolean isOneTime() {
        return true;
    }

    /**
     * Performs the mission. This is done by moving in a random direction
     * until the move points are zero or the unit gets stuck.
     *
     * @param connection The <code>Connection</code> to the server.
     */
    public void doMission(Connection connection) {
        final Unit unit = getUnit();
        final Tile tile = unit.getTile();
        if (tile == null) return;

        // If our tile contains a settlement, idle
        if (tile.getSettlement() != null) {
            logger.finest("Unit " + unit.getId()
                + " idle at settlement: " + tile.getSettlement().getId());
            return;
        }

        PathNode path = findNearestOtherSettlement(unit);
        if (path != null) {
            Direction r = moveTowards(path);
            if (r == null || !moveButDontAttack(r)) return;
        } else {
            // Just make a random move if no target can be found.
            moveRandomly();
        }
    }

    // Serialization

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
     * @return "idleAtColonyMission".
     */
    public static String getXMLElementTagName() {
        return "idleAtColonyMission";
    }
}
