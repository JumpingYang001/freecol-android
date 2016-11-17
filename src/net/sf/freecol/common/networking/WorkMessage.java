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

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.WorkLocation;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * Class to handle changes in work location.
 */
public class WorkMessage extends DOMMessage {

    /**
     * The id of the unit.
     */
    private String unitId;

    /**
     * The id of the work location.
     */
    private String workLocationId;


    /**
     * Create a new <code>WorkMessage</code> for the supplied unit and
     * work location.
     *
     * @param unit The <code>Unit</code> to change the work location of.
     * @param workLocation The <code>WorkLocation</code> to change to.
     */
    public WorkMessage(Unit unit, WorkLocation workLocation) {
        this.unitId = unit.getId();
        this.workLocationId = workLocation.getId();
    }

    /**
     * Create a new <code>WorkMessage</code> from a supplied element.
     *
     * @param game The <code>Game</code> this message belongs to.
     * @param element The <code>Element</code> to use to create the message.
     */
    public WorkMessage(Game game, Element element) {
        this.unitId = element.getAttribute("unit");
        this.workLocationId = element.getAttribute("workLocation");
    }

    /**
     * Handle a "work"-message.
     *
     * @param server The <code>FreeColServer</code> handling the message.
     * @param player The <code>Player</code> the message applies to.
     * @param connection The <code>Connection</code> message received on.
     *
     * @return An update encapsulating the work location change
     *         or an error <code>Element</code> on failure.
     */
    public Element handle(FreeColServer server, Player player,
                          Connection connection) {
        ServerPlayer serverPlayer = server.getPlayer(connection);
        Game game = server.getGame();

        Unit unit;
        try {
            unit = server.getUnitSafely(unitId, serverPlayer);
        } catch (Exception e) {
            return DOMMessage.clientError(e.getMessage());
        }
        Tile tile = unit.getTile();
        if (tile == null) {
            return DOMMessage.clientError("Unit is not on the map: "
                + unitId);
        }
        Colony colony = tile.getColony();
        if (colony == null) {
            return DOMMessage.clientError("Unit is not at a colony: "
                + unitId);
        }
        WorkLocation workLocation;
        if (game.getFreeColGameObjectSafely(workLocationId)
            instanceof WorkLocation) {
            workLocation = (WorkLocation) game
                .getFreeColGameObjectSafely(workLocationId);
        } else {
            return DOMMessage.clientError("Not a work location: "
                + workLocationId);
        }
        if (workLocation.getColony() != colony) {
            return DOMMessage.clientError("Work location is not in the colony where the unit is: "
                + workLocationId);
        }
        if (!workLocation.canAdd(unit)) {
            return DOMMessage.clientError("Can not add " + unit.toString()
                + " to " + workLocation.toString()
                + ": " + workLocation.getNoAddReason(unit));
        }

        // Work.
        return server.getInGameController()
            .work(serverPlayer, unit, workLocation);
    }

    /**
     * Convert this WorkMessage to XML.
     *
     * @return The XML representation of this message.
     */
    public Element toXMLElement() {
        Element result = createNewRootElement(getXMLElementTagName());
        result.setAttribute("unit", unitId);
        result.setAttribute("workLocation", workLocationId);
        return result;
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "work".
     */
    public static String getXMLElementTagName() {
        return "work";
    }
}
