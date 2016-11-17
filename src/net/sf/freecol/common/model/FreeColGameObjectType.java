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

import java.util.Set;

import net.sf.freecol.common.option.OptionGroup;

import org.freecolandroid.xml.stream.XMLStreamConstants;
import org.freecolandroid.xml.stream.XMLStreamException;
import org.freecolandroid.xml.stream.XMLStreamReader;
import org.freecolandroid.xml.stream.XMLStreamWriter;


/**
 * The base class for all types defined by the specification. It can
 * be instantiated in order to provide a source for modifiers and
 * abilities that are provided by the code rather than defined in the
 * specification, such as the "artillery in the open" penalty.
 */
public class FreeColGameObjectType extends FreeColObject {

    private int index = -1;

    /**
     * The default index of Modifiers provided by this type.
     */
    private int modifierIndex = 100;

    /**
     * Whether the type is abstract or can be instantiated.
     */
    private boolean abstractType;

    /**
     * Describe featureContainer here.
     */
    private FeatureContainer featureContainer;


    protected FreeColGameObjectType() {
        // empty constructor
    }

    public FreeColGameObjectType(String id) {
        this(id, null);
    }

    public FreeColGameObjectType(Specification specification) {
        this(null, specification);
    }

    public FreeColGameObjectType(String id, Specification specification) {
        setId(id);
        setSpecification(specification);
        featureContainer = new FeatureContainer();
    }

    /**
     * Get the <code>FeatureContainer</code> value.
     *
     * @return a <code>FeatureContainer</code> value
     */
    public final FeatureContainer getFeatureContainer() {
        return featureContainer;
    }

    /**
     * Set the <code>FeatureContainer</code> value.
     *
     * @param newFeatureContainer The new FeatureContainer value.
     */
    public final void setFeatureContainer(final FeatureContainer newFeatureContainer) {
        this.featureContainer = newFeatureContainer;
    }

    /**
     * Describe <code>setIndex</code> method here.
     *
     * @param index an <code>int</code> value
     */
    protected final void setIndex(final int index) {
        this.index = index;
    }

    /**
     * Returns the index of this FreeColGameObjectType. The index
     * imposes a total ordering consistent with equals on each class
     * extending FreeColGameObjectType, but this ordering is nothing
     * but the order in which the objects of the respective class were
     * defined. It is guaranteed to remain stable only for a
     * particular revision of a particular specification.
     *
     * @return an <code>int</code> value
     */
    protected int getIndex() {
        return index;
    }

    public final String getNameKey() {
        return getId() + ".name";
    }

    public final String getDescriptionKey() {
        return getId() + ".description";
    }

    public boolean hasAbility(String id) {
        return featureContainer.hasAbility(id);
    }

    public boolean hasAbility(String id, FreeColGameObjectType type) {
        return featureContainer.hasAbility(id, type);
    }

    public void addAbility(Ability ability) {
        featureContainer.addAbility(ability);
    }

    public void addModifier(Modifier modifier) {
        featureContainer.addModifier(modifier);
    }

    public Set<Modifier> getModifierSet(String id) {
        return featureContainer.getModifierSet(id);
    }

    /**
     * Applies the difficulty level with the given ID to this
     * FreeColGameObjectType. This method does nothing. If the
     * behaviour of a FreeColGameObjectType depends on difficulty, it
     * must override this method.
     *
     * @param difficulty difficulty level to apply
     */
    public void applyDifficultyLevel(OptionGroup difficulty) {
        // do nothing
    }

    /**
     * Get the <code>ModifierIndex</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getModifierIndex() {
        return modifierIndex;
    }

    /**
     * Get the index for the given Modifier.
     *
     * @param modifier a <code>Modifier</code> value
     * @return an <code>int</code> value
     */
    public int getModifierIndex(Modifier modifier) {
        return modifierIndex;
    }

    /**
     * Set the <code>ModifierIndex</code> value.
     *
     * @param newModifierIndex The new ModifierIndex value.
     */
    public final void setModifierIndex(final int newModifierIndex) {
        this.modifierIndex = newModifierIndex;
    }

    /**
     * Get the <code>Abstract</code> value.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean isAbstractType() {
        return abstractType;
    }

    /**
     * Set the <code>Abstract</code> value.
     *
     * @param newAbstract The new Abstract value.
     */
    public final void setAbstractType(final boolean newAbstract) {
        this.abstractType = newAbstract;
    }


    protected void toXMLImpl(XMLStreamWriter out) throws XMLStreamException {
        // don't use this
    }

    /**
     * This method writes an XML-representation of this object to
     * the given stream.
     *
     * @param out The target stream.
     * @exception XMLStreamException if there are any problems writing
     *      to the stream.
     */
    protected void toXMLImpl(XMLStreamWriter out, String tag)
        throws XMLStreamException {
        super.toXML(out, tag);
    }

    /**
     * Write the children of this object to a stream.
     *
     * @param out The target stream.
     * @throws XMLStreamException if there are any problems writing
     *     to the stream.
     */
    @Override
    protected void writeChildren(XMLStreamWriter out)
        throws XMLStreamException {
        super.writeChildren(out);

        if (featureContainer != null) {
            for (Ability ability: featureContainer.getAbilities()) {
                ability.toXMLImpl(out);
            }
            for (Modifier modifier: featureContainer.getModifiers()) {
                modifier.toXMLImpl(out);
            }
        }
    }

    /**
     * Reads the attributes of this object from an XML stream.
     *
     * @param in The XML input stream.
     * @throws XMLStreamException if a problem was encountered
     *     during parsing.
     */
    @Override
    protected void readAttributes(XMLStreamReader in)
        throws XMLStreamException {
        super.readAttributes(in);

        setAbstractType(getAttribute(in, "abstract", false));
    }

    /**
     * Reads the children of this object from an XML stream.
     *
     * @param in The XML input stream.
     * @exception XMLStreamException if a problem was encountered
     *     during parsing.
     */
    @Override
    protected void readChildren(XMLStreamReader in) throws XMLStreamException {
        while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
            readChild(in);
        }
    }

    /**
     * Reads a common child object, i.e. an Ability or Modifier.
     *
     * @param in The XML input stream.
     * @exception XMLStreamException if an error occurs
     */
    protected void readChild(XMLStreamReader in) throws XMLStreamException {
        String childName = in.getLocalName();
        if (Ability.getXMLElementTagName().equals(childName)) {
            if (getAttribute(in, "delete", false)) {
                String id = in.getAttributeValue(null, ID_ATTRIBUTE_TAG);
                featureContainer.removeAbilities(id);
                in.nextTag();
            } else {
                Ability ability = new Ability(in, getSpecification());
                if (ability.getSource() == null) {
                    ability.setSource(this);
                }
                addAbility(ability); // Ability close the element
                getSpecification().addAbility(ability);
            }
        } else if (Modifier.getXMLElementTagName().equals(childName)) {
            if (getAttribute(in, "delete", false)) {
                String id = in.getAttributeValue(null, ID_ATTRIBUTE_TAG);
                featureContainer.removeModifiers(id);
                in.nextTag();
            } else {
                Modifier modifier = new Modifier(in, getSpecification());
                if (modifier.getSource() == null) {
                    modifier.setSource(this);
                }
                if (modifier.getIndex() < 0) {
                    modifier.setIndex(getModifierIndex(modifier));
                }
                addModifier(modifier); // Modifier close the element
                getSpecification().addModifier(modifier);
            }
        } else {
            logger.warning("Parsing of " + childName
                + " is not implemented yet");
            while (in.nextTag() != XMLStreamConstants.END_ELEMENT
                   || !in.getLocalName().equals(childName)) {
                in.nextTag();
            }
        }
    }

    /**
     * Use only for debugging purposes! A human-readable and localized name is
     * returned by getName().
     */
    @Override
    public String toString() {
        return getId();
    }
}
