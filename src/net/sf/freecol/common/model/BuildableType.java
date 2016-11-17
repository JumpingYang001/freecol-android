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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freecolandroid.xml.stream.XMLStreamException;
import org.freecolandroid.xml.stream.XMLStreamReader;
import org.freecolandroid.xml.stream.XMLStreamWriter;


/**
 * Contains information on buildable types.
 */
public abstract class BuildableType extends FreeColGameObjectType {

    public static final String NOTHING = "model.buildableType.nothing";

    /**
     * The minimum population that a Colony needs in order to build
     * this type.
     */
    private int populationRequired = 1;

    /**
     * Limits on the production of this type.
     */
    private List<Limit> limits;

    /**
     * A list of AbstractGoods required to build this type.
     */
    private List<AbstractGoods> goodsRequired = new ArrayList<AbstractGoods>();

    /**
     * Stores the abilities required by this Type.
     */
    private final Map<String, Boolean> requiredAbilities = new HashMap<String, Boolean>();


    public BuildableType(String id, Specification specification) {
        super(id, specification);
    }


    /**
     * Get the <code>GoodsRequired</code> value.
     *
     * @return a <code>List<AbstractGoods></code> value
     */
    public final List<AbstractGoods> getGoodsRequired() {
        return goodsRequired;
    }

    /**
     * Get amount required of given <code>GoodsType</code>
     */
    public final int getAmountRequiredOf(GoodsType type){
    	for (AbstractGoods goods : this.goodsRequired){
            if (goods.getType() == type){
                return goods.getAmount();
            }
    	}
    	return 0;
    }

    /**
     * Set the <code>GoodsRequired</code> value.
     *
     * @param newGoodsRequired The new GoodsRequired value.
     */
    public final void setGoodsRequired(final List<AbstractGoods> newGoodsRequired) {
        this.goodsRequired = newGoodsRequired;
    }

    /**
     * Does this buildable need goods to build?
     */
    public boolean needsGoodsToBuild() {
        return !goodsRequired.isEmpty();
    }

    /**
     * Get the <code>PopulationRequired</code> value.
     *
     * @return an <code>int</code> value
     */
    public int getPopulationRequired() {
        return populationRequired;
    }

    /**
     * Set the <code>PopulationRequired</code> value.
     *
     * @param newPopulationRequired The new PopulationRequired value.
     */
    public void setPopulationRequired(final int newPopulationRequired) {
        this.populationRequired = newPopulationRequired;
    }

    /**
     * Get the <code>Limits</code> value.
     *
     * @return a <code>List<Limit></code> value
     */
    public final List<Limit> getLimits() {
        return limits;
    }

    /**
     * Set the <code>Limits</code> value.
     *
     * @param newLimits The new Limits value.
     */
    public final void setLimits(final List<Limit> newLimits) {
        this.limits = newLimits;
    }

    /**
     * Returns the abilities required by this Type.
     *
     * @return the abilities required by this Type.
     */
    public Map<String, Boolean> getAbilitiesRequired() {
        return requiredAbilities;
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

        if (populationRequired > 1) {
            out.writeAttribute("required-population",
                Integer.toString(populationRequired));
        }
    }

    /**
     * Write the children of this object to a stream.
     *
     * @param out The target stream.
     * @throws XMLStreamException if there are any problems writing to
     *     the stream.
     */
    @Override
    protected void writeChildren(XMLStreamWriter out)
        throws XMLStreamException {
        super.writeChildren(out);

        if (limits != null) {
            for (Limit limit : limits) {
                limit.toXMLImpl(out);
            }
        }
        for (Map.Entry<String, Boolean> entry
                 : getAbilitiesRequired().entrySet()) {
            out.writeStartElement("required-ability");
            out.writeAttribute(ID_ATTRIBUTE_TAG, entry.getKey());
            out.writeAttribute(VALUE_TAG, Boolean.toString(entry.getValue()));
            out.writeEndElement();
        }
        if (getGoodsRequired() != null) {
            for (AbstractGoods goods : getGoodsRequired()) {
                out.writeStartElement("required-goods");
                out.writeAttribute(ID_ATTRIBUTE_TAG, goods.getType().getId());
                out.writeAttribute(VALUE_TAG, Integer.toString(goods.getAmount()));
                out.writeEndElement();
            }
        }
    }

    /**
     * Reads a child object.
     *
     * @param in The XML stream to read.
     * @exception XMLStreamException if an error occurs
     */
    @Override
    protected void readChild(XMLStreamReader in) throws XMLStreamException {
        String childName = in.getLocalName();
        if (Limit.getXMLElementTagName().equals(childName)) {
            if (limits == null) {
                limits = new ArrayList<Limit>();
            }
            Limit limit = new Limit(getSpecification());
            limit.readFromXML(in);
            if (limit.getLeftHandSide().getType() == null) {
                limit.getLeftHandSide().setType(getId());
            }
            limits.add(limit);
        } else if ("required-ability".equals(childName)) {
            String abilityId = in.getAttributeValue(null, ID_ATTRIBUTE_TAG);
            boolean value = getAttribute(in, VALUE_TAG, true);
            getAbilitiesRequired().put(abilityId, value);
            getSpecification().addAbility(abilityId);
            in.nextTag(); // close this element
        } else if ("required-goods".equals(childName)) {
            GoodsType type = getSpecification().getGoodsType(in.getAttributeValue(null, ID_ATTRIBUTE_TAG));
            int amount = getAttribute(in, VALUE_TAG, 0);
            AbstractGoods requiredGoods = new AbstractGoods(type, amount);
            if (amount > 0) {
                type.setBuildingMaterial(true);
                if (getGoodsRequired() == null) {
                    setGoodsRequired(new ArrayList<AbstractGoods>());
                }
                getGoodsRequired().add(requiredGoods);
            }
            in.nextTag(); // close this element
        } else {
            super.readChild(in);
        }
    }
}
