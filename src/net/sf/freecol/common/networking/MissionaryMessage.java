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

package net.sf.freecol.common.networking;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Map.Direction;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.MoveType;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message sent when a missionary establishes/denounces a mission.
 */
public class MissionaryMessage extends DOMMessage {
    /**
     * The id of the missionary.
     */
    private String unitId;

    /**
     * The direction to the settlement.
     */
    private String directionString;

    /**
     * Is this a denunciation?
     */
    private boolean denounce;

    /**
     * Create a new <code>MissionaryMessage</code> with the
     * supplied name.
     *
     * @param unit The missionary <code>Unit</code>.
     * @param direction The <code>Direction</code> to the settlement.
     * @param denounce True if this is a denunciation.
     */
    public MissionaryMessage(Unit unit, Direction direction, boolean denounce) {
        this.unitId = unit.getId();
        this.directionString = String.valueOf(direction);
        this.denounce = denounce;
    }

    /**
     * Create a new <code>MissionaryMessage</code> from a
     * supplied element.
     *
     * @param game The <code>Game</code> this message belongs to.
     * @param element The <code>Element</code> to use to create the message.
     */
    public MissionaryMessage(Game game, Element element) {
        this.unitId = element.getAttribute("unitId");
        this.directionString = element.getAttribute("direction");
        this.denounce = Boolean.valueOf(element.getAttribute("denounce")).booleanValue();
    }

    /**
     * Handle a "missionary"-message.
     *
     * @param server The <code>FreeColServer</code> handling the message.
     * @param player The <code>Player</code> the message applies to.
     * @param connection The <code>Connection</code> message was received on.
     *
     * @return An element containing the result of the mission operation,
     *         or an error <code>Element</code> on failure.
     */
    public Element handle(FreeColServer server, Player player,
                          Connection connection) {
        ServerPlayer serverPlayer = server.getPlayer(connection);
        Unit unit;
        try {
            unit = server.getUnitSafely(unitId, serverPlayer);
        } catch (Exception e) {
            return DOMMessage.clientError(e.getMessage());
        }
        if (unit.getTile() == null) {
            return DOMMessage.clientError("Unit is not on the map: " + unitId);
        }
        Direction direction = Enum.valueOf(Direction.class, directionString);
        Tile tile = unit.getTile().getNeighbourOrNull(direction);
        if (tile == null) {
            return DOMMessage.clientError("Could not find tile"
                + " in direction: " + direction + " from unit: " + unitId);
        }
        IndianSettlement is = tile.getIndianSettlement();
        if (is == null) {
            return DOMMessage.clientError("There is no native settlement at: "
                + tile.getId());
        }
        Unit missionary = is.getMissionary();
        if (denounce) {
            if (missionary == null) {
                return DOMMessage.clientError("Denouncing an empty mission at: "
                    + is.getId());
            } else if (missionary.getOwner() == player) {
                return DOMMessage.clientError("Denouncing our own missionary at: "
                    + is.getId());
            }
        } else {
            if (missionary != null) {
                return DOMMessage.clientError("Establishing extra mission at: "
                    + is.getId());
            }
        }
        MoveType type = unit.getMoveType(is.getTile());
        if (type != MoveType.ENTER_INDIAN_SETTLEMENT_WITH_MISSIONARY) {
            return DOMMessage.clientError("Unable to enter " + is.getName()
                + ": " + type.whyIllegal());
        }

        // Valid, proceed to denounce/establish.
        return (denounce)
            ? server.getInGameController()
                .denounceMission(serverPlayer, unit, is)
            : server.getInGameController()
                .establishMission(serverPlayer, unit, is);
    }

    /**
     * Convert this MissionaryMessage to XML.
     *
     * @return The XML representation of this message.
     */
    public Element toXMLElement() {
        Element result = createNewRootElement(getXMLElementTagName());
        result.setAttribute("unitId", unitId);
        result.setAttribute("direction", directionString);
        result.setAttribute("denounce", Boolean.toString(denounce));
        return result;
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "missionary".
     */
    public static String getXMLElementTagName() {
        return "missionary";
    }
}
