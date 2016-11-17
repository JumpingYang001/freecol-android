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
import net.sf.freecol.common.model.Goods;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message sent when purchasing at an IndianSettlement.
 */
public class BuyMessage extends DOMMessage {
    /**
     * The ID of the unit that is buying.
     */
    private String unitId;

    /**
     * The ID of the settlement that is selling.
     */
    private String settlementId;

    /**
     * The goods to be bought.
     */
    private Goods goods;

    /**
     * The price to pay.
     */
    private String goldString;

    /**
     * Create a new <code>BuyMessage</code>.
     *
     * @param unit The <code>Unit</code> that is buying.
     * @param settlement The <code>Settlement</code> that is trading.
     * @param goods The <code>Goods</code> to buy.
     * @param gold The price of the goods.
     */
    public BuyMessage(Unit unit, Settlement settlement, Goods goods, int gold) {
        this.unitId = unit.getId();
        this.settlementId = settlement.getId();
        this.goods = goods;
        this.goldString = Integer.toString(gold);
    }

    /**
     * Create a new <code>BuyMessage</code> from a
     * supplied element.
     *
     * @param game The <code>Game</code> this message belongs to.
     * @param element The <code>Element</code> to use to create the message.
     */
    public BuyMessage(Game game, Element element) {
        this.unitId = element.getAttribute("unit");
        this.settlementId = element.getAttribute("settlement");
        this.goods = new Goods(game,
            DOMMessage.getChildElement(element, Goods.getXMLElementTagName()));
        this.goldString = element.getAttribute("gold");
    }

    /**
     * Handle a "buy"-message.
     *
     * @param server The <code>FreeColServer</code> handling the message.
     * @param player The <code>Player</code> the message applies to.
     * @param connection The <code>Connection</code> message was received on.
     *
     * @return Null.
     * @throws IllegalStateException if there is problem with the arguments.
     */
    public Element handle(FreeColServer server, Player player, Connection connection) {
        ServerPlayer serverPlayer = server.getPlayer(connection);
        Unit unit;
        IndianSettlement settlement;
        try {
            unit = server.getUnitSafely(unitId, serverPlayer);
            settlement = server.getAdjacentIndianSettlementSafely(settlementId, unit);
        } catch (Exception e) {
            return DOMMessage.clientError(e.getMessage());
        }
        // Make sure we are trying to buy something that is there
        if (goods.getLocation() != settlement) {
            return DOMMessage.createError("server.trade.noGoods", "Goods "
                + goods.getId() + " is not at settlement " + settlementId);
        }
        int gold;
        try {
            gold = Integer.parseInt(goldString);
        } catch (NumberFormatException e) {
            return DOMMessage.clientError("Bad gold: " + goldString);
        }

        // Try to buy.
        return server.getInGameController()
            .buyFromSettlement(serverPlayer, unit, settlement, goods, gold);
    }

    /**
     * Convert this BuyMessage to XML.
     *
     * @return The XML representation of this message.
     */
    public Element toXMLElement() {
        Element result = createNewRootElement(getXMLElementTagName());
        result.setAttribute("unit", unitId);
        result.setAttribute("settlement", settlementId);
        result.appendChild(goods.toXMLElement(null, result.getOwnerDocument()));
        result.setAttribute("gold", goldString);
        return result;
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "buy".
     */
    public static String getXMLElementTagName() {
        return "buy";
    }
}
