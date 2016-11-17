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

package net.sf.freecol.common.model;

import org.freecolandroid.xml.stream.XMLStreamException;
import org.freecolandroid.xml.stream.XMLStreamReader;
import org.freecolandroid.xml.stream.XMLStreamWriter;


/**
 * This class contains the last sale a player has made, by Settlement
 * and GoodsType.
 */
public final class LastSale extends FreeColObject {

    // When a sale was made.
    private Turn when;

    // The price per unit returned from the sale.
    private int price;


    /**
     * Empty constructor for Player.
     */
    public LastSale() {
    }

    /**
     * Make a new LastSale record.
     *
     * @param where The <code>Location</code> of the sale.
     * @param what The <code>GoodsType</code> sold.
     * @param when In which <code>Turn</code> the sale occurred.
     * @param price The per-unit price of the sale.
     */
    public LastSale(Location where, GoodsType what,
                    Turn when, int price) {
        this(makeKey(where, what), when, price);
    }

    /**
     * Make a new LastSale record.
     *
     * @param id The identifier (encoding Settlement and GoodsType).
     * @param when In which <code>Turn</code> the sale occurred.
     * @param price The per-unit price of the sale.
     */
    public LastSale(String id, Turn when, int price) {
        setId(id);
        this.when = when;
        this.price = price;
    }

    /**
     * Get the <code>Turn</code> when the sale was made.
     *
     * @return The <code>Turn</code> when the sale was made.
     */
    public Turn getWhen() {
        return when;
    }

    /**
     * Get the price from the sale.
     *
     * @return The price from the sale.
     */
    public int getPrice() {
        return price;
    }

    /**
     * Make a String to be used as a key for looking up sales.
     *
     * @param where The <code>Location</code> of the sale.
     * @param what The <code>GoodsType</code> sold.
     *
     * @return A key string.
     */
    public static String makeKey(Location where, GoodsType what) {
        return where.getId() + "-" + what.getId();
    }


    /**
     * This method writes an XML-representation of this object to
     * the given stream.
     *
     * @param out The target stream.
     * @throws XMLStreamException if there are any problems with the stream.
     */
    protected void toXMLImpl(XMLStreamWriter out) throws XMLStreamException {
        super.toXML(out, getXMLElementTagName());
    }

    /**
     * Write the children of this object to a stream.
     *
     * @param out The target stream.
     * @throws XMLStreamException if there are any problems writing
     *     to the stream.
     */
    @Override
    protected void writeAttributes(XMLStreamWriter out)
        throws XMLStreamException {
        super.writeAttributes(out);

        out.writeAttribute("when", Integer.toString(when.getNumber()));
        out.writeAttribute("price", Integer.toString(price));
    }

    /**
     * Initialize this object from an XML-representation of this object.
     *
     * @param in The input stream with the XML.
     * @throws XMLStreamException if there are any problems with the stream.
     */
    public void readFromXMLImpl(XMLStreamReader in) throws XMLStreamException {
        super.readAttributes(in);
        when = new Turn(getAttribute(in, "when", 0));
        price = getAttribute(in, "price", 0);
        super.readChildren(in);
    }

    /**
     * Debug helper.
     */
    @Override
    public String toString() {
        return getXMLElementTagName() + "-" + getId() + "-"
            + when.toString() + "-" + Integer.toString(price);
    }

    /**
     * Returns the tag name of the root element representing this object.
     *
     * @return "lastSale"
     */
    public static String getXMLElementTagName() {
        return "lastSale";
    }
}
