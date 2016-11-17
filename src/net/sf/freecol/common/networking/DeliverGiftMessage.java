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
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message sent when delivering a gift to a Settlement.
 */
public class DeliverGiftMessage extends DOMMessage {
    /**
     * The ID of the unit that is delivering the gift.
     */
    private String unitId;

    /**
     * The ID of the settlement the gift is going to.
     */
    private String settlementId;

    /**
     * The goods to be delivered.
     */
    private Goods goods;

    /**
     * Create a new <code>DeliverGiftMessage</code>.
     *
     * @param unit The <code>Unit</code> that is trading.
     * @param settlement The <code>Settlement</code> that is trading.
     * @param goods The <code>Goods</code> to deliverGift.
     */
    public DeliverGiftMessage(Unit unit, Settlement settlement, Goods goods) {
        this.unitId = unit.getId();
        this.settlementId = settlement.getId();
        this.goods = goods;
    }

    /**
     * Create a new <code>DeliverGiftMessage</code> from a
     * supplied element.
     *
     * @param game The <code>Game</code> this message belongs to.
     * @param element The <code>Element</code> to use to create the message.
     */
    public DeliverGiftMessage(Game game, Element element) {
        this.unitId = element.getAttribute("unit");
        this.settlementId = element.getAttribute("settlement");
        this.goods = new Goods(game,
            DOMMessage.getChildElement(element, Goods.getXMLElementTagName()));
    }

    /**
     * Get the <code>Unit</code> which is delivering the gift.
     * This is a helper routine to be called in-client as it blindly trusts
     * its field.
     *
     * @return The unit, or null if none.
     */
    public Unit getUnit() {
        try {
            return (Unit) goods.getGame().getFreeColGameObject(unitId);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Get the <code>Settlement</code> which is receiving the gift.
     * This is a helper routine to be called in-client as it blindly trusts
     * its field.
     *
     * @return The settlement, or null if none.
     */
    public Settlement getSettlement() {
        try {
            return (Settlement) goods.getGame().getFreeColGameObject(settlementId);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Get the <code>Goods</code> delivered as a gift.
     * This is a helper routine to be called in-client as it blindly trusts
     * its field.
     *
     * @return The goods, or null if none.
     */
    public Goods getGoods() {
        return goods;
    }

    /**
     * Handle a "deliverGift"-message.
     *
     * @param server The <code>FreeColServer</code> handling the message.
     * @param player The <code>Player</code> the message applies to.
     * @param connection The <code>Connection</code> message was received on.
     *
     * @return An update containing the unit and settlement,
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
        Settlement settlement;
        try {
            settlement = server.getAdjacentSettlementSafely(settlementId, unit);
        } catch (Exception e) {
            return DOMMessage.clientError(e.getMessage());
        }

        // Make sure we are trying to deliver something that is there
        if (goods.getLocation() != unit) {
            return DOMMessage.createError("server.trade.noGoods",
                "deliverGift of non-existent goods");
        }

        // Proceed to deliver.
        return server.getInGameController()
            .deliverGiftToSettlement(serverPlayer, unit, settlement, goods);
    }

    /**
     * Convert this DeliverGiftMessage to XML.
     *
     * @return The XML representation of this message.
     */
    public Element toXMLElement() {
        Element result = createNewRootElement(getXMLElementTagName());
        result.setAttribute("unit", unitId);
        result.setAttribute("settlement", settlementId);
        result.appendChild(goods.toXMLElement(null, result.getOwnerDocument()));
        return result;
    }

    /**
     * The tag name of the root element representing this object.
     *
     * @return "deliverGift".
     */
    public static String getXMLElementTagName() {
        return "deliverGift";
    }
}
