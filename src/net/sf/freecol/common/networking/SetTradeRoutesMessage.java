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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.sf.freecol.common.model.FreeColObject;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.TradeRoute;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * The message sent when setting the trade routes.
 */
public class SetTradeRoutesMessage extends DOMMessage {

    private static final String idPrefix = "shadow-";

    /**
     * The trade routes to set.
     */
    private List<TradeRoute> tradeRoutes;


    /**
     * Create a new <code>SetTradeRoutesMessage</code> with the
     * supplied routes.
     *
     * @param tradeRoutes A list of <code>TradeRoute</code>s to set.
     */
    public SetTradeRoutesMessage(List<TradeRoute> tradeRoutes) {
        this.tradeRoutes = tradeRoutes;
    }

    /**
     * Create a new <code>SetTradeRoutesMessage</code> from a
     * supplied element.
     *
     * @param game The <code>Game</code> this message belongs to.
     * @param element The <code>Element</code> to use to create the message.
     */
    public SetTradeRoutesMessage(Game game, Element element) {
        List<TradeRoute> newRoutes = new ArrayList<TradeRoute>();
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            TradeRoute route
                = tradeRouteFromElement(game, (Element) nodes.item(i));
            if (route != null) newRoutes.add(route);
        }
        this.tradeRoutes = newRoutes;
    }

    /**
     * Creates a trade route from an element.  Be careful not to allow
     * real IDs to surface or the trade route constructor will replace
     * the existing game trade route with the one in the element
     * before we have finished checking it.
     *
     * Public routine as updateTradeRoute has the same problem.
     *
     * @param game The <code>Game</code> to create in.
     * @param element An <code>Element</code> to read from.
     * @return A <code>TradeRoute</code> on success, null on error.
     */
    public static TradeRoute tradeRouteFromElement(Game game, Element element) {
        String id = element.getAttribute(FreeColObject.ID_ATTRIBUTE);
        element.setAttribute(FreeColObject.ID_ATTRIBUTE, idPrefix + id);
        try {
            return new TradeRoute(game, element);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not build trade route " + id, e);
            return null;
        }
    }

    public static String getPrefix(TradeRoute route) {
        return route.getId().substring(0, idPrefix.length());
    }

    public static String removePrefix(TradeRoute route) {
        return route.getId().substring(idPrefix.length());
    }

    public static boolean hasPrefix(TradeRoute route) {
        return idPrefix.equals(getPrefix(route));
    }

    /**
     * Handle a "setTradeRoutes"-message.
     *
     * @param server The <code>FreeColServer</code> handling the message.
     * @param connection The <code>Connection</code> message was received on.
     *
     * @return Null, or an error <code>Element</code> on failure.
     */
    public Element handle(FreeColServer server, Connection connection) {
        Game game = server.getGame();
        ServerPlayer serverPlayer = server.getPlayer(connection);
        List<TradeRoute> newRoutes = new ArrayList<TradeRoute>();
        String errors = "";

        for (TradeRoute tradeRoute : tradeRoutes) {
            if (tradeRoute.getId() == null || !hasPrefix(tradeRoute)) {
                errors += "Bogus route: " + tradeRoute.getId() + ". ";
                continue;
            }
            String id = removePrefix(tradeRoute);
            if (!(game.getFreeColGameObject(id) instanceof TradeRoute)) {
                errors += "Not a trade route: " + id + ". ";
                continue;
            }
            TradeRoute realRoute = (TradeRoute) game.getFreeColGameObject(id);
            if (tradeRoute.getOwner() != (Player) serverPlayer) {
                errors += "Not your trade route: " + id + ". ";
                continue;
            }
        }
        if (!"".equals(errors)) return DOMMessage.clientError(errors);
        
        for (TradeRoute tradeRoute : tradeRoutes) {
            TradeRoute realRoute = (TradeRoute) game
                .getFreeColGameObject(removePrefix(tradeRoute));
            realRoute.updateFrom(tradeRoute);
            newRoutes.add(realRoute);
            tradeRoute.dispose();
        }

        // Proceed to set trade routes
        return server.getInGameController()
            .setTradeRoutes(serverPlayer, newRoutes);
    }

    /**
     * Convert this SetTradeRoutesMessage to XML.
     *
     * @return The XML representation of this message.
     */
    public Element toXMLElement() {
        Element result = createNewRootElement(getXMLElementTagName());
        Document doc = result.getOwnerDocument();
        for (TradeRoute tradeRoute : tradeRoutes) {
            result.appendChild(tradeRoute.toXMLElement(null, doc));
        }
        return result;
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "setTradeRoutes".
     */
    public static String getXMLElementTagName() {
        return "setTradeRoutes";
    }
}
