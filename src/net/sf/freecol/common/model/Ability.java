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
 * The <code>Ability</code> class encapsulates a bonus or penalty
 * that can be applied to any action within the game, most obviously
 * combat.
 */
public final class Ability extends Feature {

    /**
     * The ability to add the current tax as a bonus to the production
     * of bells. Provided by the {@link FoundingFather} Thomas Paine.
     */
    public static final String ADD_TAX_TO_BELLS = "model.ability.addTaxToBells";

    /**
     * The ability of certain buildings (e.g. the stables) to produce
     * goods even if no units are present.
     */
    public static final String AUTO_PRODUCTION =
        "model.ability.autoProduction";

    /**
     * The ability of certain buildings (e.g. the stables) to avoid
     * producing more goods than the colony can store, which would
     * normally go to waste.
     */
    public static final String AVOID_EXCESS_PRODUCTION =
        "model.ability.avoidExcessProduction";

    /**
     * The ability to bombard other units, i.e. attack them from a
     * distance without risking damage.
     */
    public static final String BOMBARD = "model.ability.bombard";

    /**
     * The ability to be born in a Colony. Only Units with this
     * ability can be produced by a Colony.
     */
    public static final String BORN_IN_COLONY = "model.ability.bornInColony";

    /**
     * The ability to be born in an IndianSettlement. Only Units with
     * this ability can be produced by an IndianSettlement.
     */
    public static final String BORN_IN_INDIAN_SETTLEMENT =
        "model.ability.bornInIndianSettlement";

    /**
     * The ability to build {@link BuildableType}s, such as units. The
     * shipyard provides the ability to build ships, for example.
     */
    public static final String BUILD = "model.ability.build";

    /**
     * The ability of certain unarmed units to be captured by another
     * player's units. Units lacking this ability (e.g. braves) will
     * be destroyed instead.
     */
    public static final String CAN_BE_CAPTURED = "model.ability.canBeCaptured";

    /**
     * The ability of certain units to be equipped with tools, muskets, etc.
     */
    public static final String CAN_BE_EQUIPPED = "model.ability.canBeEquipped";

    /**
     * The ability of some buildings (e.g. the schoolhouse) to teach
     * skills.
     */
    public static final String CAN_TEACH = "model.ability.teach";

    /**
     * The ability of certain units (e.g. privateers) to capture goods
     * carried by another player's units.
     */
    public static final String CAPTURE_GOODS = "model.ability.captureGoods";

    /**
     * The ability of certain armed units to capture another player's
     * units.
     */
    public static final String CAPTURE_UNITS = "model.ability.captureUnits";

    /**
     * The ability of certain units (e.g. wagon trains) to carry goods.
     */
    public static final String CARRY_GOODS = "model.ability.carryGoods";

    /**
     * The ability of certain units (e.g. ships) to carry other units.
     */
    public static final String CARRY_UNITS = "model.ability.carryUnits";

    /**
     * The ability of certain units (e.g. treasure trains) to carry
     * treasures.
     */
    public static final String CARRY_TREASURE = "model.ability.carryTreasure";

    /**
     * The ability of certain consumers (e.g. BuildQueues) to consume
     * a large amount of goods at once instead of turn by turn.
     */
    public static final String CONSUME_ALL_OR_NOTHING =
        "model.ability.consumeAllOrNothing";

    /**
     * The ability of certain units to work as missionaries more effectively.
     */
    public static final String EXPERT_MISSIONARY = "model.ability.expertMissionary";

    /**
     * The ability of certain units to build TileImprovements faster.
     */
    public static final String EXPERT_PIONEER = "model.ability.expertPioneer";

    /**
     * The ability of certain units to work as scouts more effectively.
     */
    public static final String EXPERT_SCOUT = "model.ability.expertScout";

    /**
     * The ability of certain units to work as soldiers more effectively.
     */
    public static final String EXPERT_SOLDIER = "model.ability.expertSoldier";

    /**
     * The somewhat controversial ability of expert units in factory
     * level buildings to produce a certain amount of goods even when
     * no raw materials are available. Allegedly, this is a feature of
     * the original game.
     */
    public static final String EXPERTS_USE_CONNECTIONS =
        "model.ability.expertsUseConnections";

    /**
     * The ability to export goods to Europe directly.
     */
    public static final String EXPORT = "model.ability.export";


    /**
     * The ability of ships to move across water tiles.
     */
    public static final String NAVAL_UNIT = "model.ability.navalUnit";

    /**
     * The ability of certain units (e.g. privateers) to attack and
     * plunder another player's units without causing war.
     */
    public static final String PIRACY = "model.ability.piracy";

    /**
     * The ability to repair certain units.
     */
    public static final String REPAIR_UNITS = "model.ability.repairUnits";

    /**
     * The ability to produce goods (e.g. fish) on water tiles.
     */
    public static final String PRODUCE_IN_WATER =
        "model.ability.produceInWater";


    private boolean value = true;

    /**
     * Creates a new <code>Ability</code> instance.
     *
     * @param id a <code>String</code> value
     */
    public Ability(String id) {
        this(id, null, true);
    }

    /**
     * Creates a new <code>Ability</code> instance.
     *
     * @param id a <code>String</code> value
     * @param value a <code>boolean</code> value
     */
    public Ability(String id, boolean value) {
        this(id, null, value);
    }

    /**
     * Creates a new <code>Ability</code> instance.
     *
     * @param id a <code>String</code> value
     * @param source a <code>FreeColGameObjectType</code> value
     * @param value a <code>boolean</code> value
     */
    public Ability(String id, FreeColGameObjectType source, boolean value) {
        setId(id);
        setSource(source);
        this.value = value;
    }

    /**
     * Creates a new <code>Ability</code> instance.
     *
     * @param template an <code>Ability</code> value
     */
    public Ability(Ability template) {
        super.copy(template);
        this.value = template.value;
    }


    /**
     * Creates a new <code>Ability</code> instance.
     *
     * @param in a <code>XMLStreamReader</code> value
     * @param specification a <code>Specification</code> value
     * @exception XMLStreamException if an error occurs
     */
    public Ability(XMLStreamReader in, Specification specification)
        throws XMLStreamException {
        readFromXMLImpl(in, specification);
    }

    /**
     * Get the <code>Value</code> value.
     *
     * @return a <code>boolean</code> value
     */
    public boolean getValue() {
        return value;
    }

    /**
     * Set the <code>Value</code> value.
     *
     * @param newValue The new Value value.
     */
    public void setValue(final boolean newValue) {
        this.value = newValue;
    }


    public int hashCode() {
        int hash = super.hashCode();
        hash += (value ? 1 : 0);
        return hash;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Ability) {
            return super.equals(o) && (value == ((Ability) o).value);
        } else {
            return false;
        }
    }


    /**
     * This method writes an XML-representation of this object to
     * the given stream.
     *
     * @param out The target stream.
     * @throws XMLStreamException if there are any problems writing
     *      to the stream.
     */
    public void toXMLImpl(XMLStreamWriter out) throws XMLStreamException {
        super.toXML(out, getXMLElementTagName());
    }

    /**
     * Write the attributes of this object to a stream.
     *
     * @param out The target stream.
     * @throws XMLStreamException if there are any problems writing to
     *     the stream.
     */
    @Override
    protected void writeAttributes(XMLStreamWriter out)
        throws XMLStreamException {
        super.writeAttributes(out);

        out.writeAttribute(VALUE_TAG, String.valueOf(value));
    }

    /**
     * Reads the attributes of this object from an XML stream.
     *
     * @param in The XML input stream.
     * @param specification A <code>Specification</code> to use.
     * @throws XMLStreamException if a problem was encountered
     *     during parsing.
     */
    @Override
    protected void readAttributes(XMLStreamReader in,
                                  Specification specification)
        throws XMLStreamException {
        super.readAttributes(in, specification);

        value = getAttribute(in, VALUE_TAG, true);
    }

    @Override
    public String toString() {
        return getId() + (getSource() == null ? " "
            : " (" + getSource().getId() + ") ")
            + " " + value;
    }

    /**
     * Returns the XML tag name for this element.
     *
     * @return "ability".
     */
    public static String getXMLElementTagName() {
        return "ability";
    }
}
