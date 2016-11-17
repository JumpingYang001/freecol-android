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

package net.sf.freecol.common.option;

import java.util.logging.Logger;

import net.sf.freecol.common.model.FreeColObject;
import net.sf.freecol.common.model.Specification;

import org.freecolandroid.xml.stream.XMLStreamConstants;
import org.freecolandroid.xml.stream.XMLStreamException;
import org.freecolandroid.xml.stream.XMLStreamReader;


/**
 * The super class of all options. GUI components making use of this
 * class can refer to its name and shortDescription properties. The
 * complete keys of these properties consist of the id of the option
 * group (if any), followed by a "."  unless the option group is null,
 * followed by the id of the option object, followed by a ".",
 * followed by "name" or "shortDescription".
 */
abstract public class AbstractOption<T> extends FreeColObject
    implements Option<T> {

    private static Logger logger = Logger.getLogger(AbstractOption.class.getName());

    private String optionGroup = "";

    // Determine if the option has been defined
    // When defined an option won't change when a default value is read from an
    // XML file.
    protected boolean isDefined = false;


    /**
     * Creates a new <code>AbstractOption</code>.
     *
     * @param id The identifier for this option. This is used when the object
     *            should be found in an {@link OptionGroup}.
     */
    public AbstractOption(String id) {
        this(id, null);
    }

    /**
     * Creates a new <code>AbstractOption</code>.
     *
     * @param specification The specification this Option refers
     *     to. This may be null, since only some options need access
     *     to the specification.
     */
    public AbstractOption(Specification specification) {
        this(null, specification);
    }

    /**
     * Creates a new <code>AbstractOption</code>.
     *
     * @param id The identifier for this option. This is used when the
     *     object should be found in an {@link OptionGroup}.
     * @param specification The specification this Option refers
     *     to. This may be null, since only some options need access
     *     to the specification.
     */
    public AbstractOption(String id, Specification specification) {
        setId(id);
        setSpecification(specification);
    }

    public abstract AbstractOption<T> clone() throws CloneNotSupportedException;

    protected void setValues(AbstractOption<T> source) {
        setId(source.getId());
        setSpecification(source.getSpecification());
        setValue(source.getValue());
        setGroup(source.getGroup());
        isDefined = source.isDefined;
    }

    /**
     * Returns the string prefix that identifies the group of this
     * <code>Option</code>.
     *
     * @return The string prefix provided by the OptionGroup.
     */
    public String getGroup() {
        return optionGroup;
    }

    /**
     * Set the option group
     *
     * @param group <code>OptionGroup</code> to set
     *
     */
    public void setGroup(String group) {
        if (group == null) {
            optionGroup = "";
        } else {
            optionGroup = group;
        }
    }

    /**
     * Returns the value of this Option.
     *
     * @return the value of this Option
     */
    public abstract T getValue();

    /**
     * Sets the value of this Option.
     *
     * @param value the value of this Option
     */
    public abstract void setValue(T value);

    /**
     * Sets the value of this Option from the given string
     * representation. Both parameters must not be null at the same
     * time. This method does nothing. Override it if the Option has a
     * suitable string representation.
     *
     * @param valueString the string representation of the value of
     * this Option
     * @param defaultValueString the string representation of the
     * default value of this Option
     */
    protected void setValue(String valueString, String defaultValueString) {
        logger.warning("Unsupported method: setValue.");
    }

    /**
     * Returns whether <code>null</code> is an acceptable value for
     * this Option. This method always returns <code>false</code>.
     * Override it where necessary.
     *
     * @return false
     */
    public boolean isNullValueOK() {
        return false;
    }

    /**
     * Generate the choices to provide to the UI. This method does
     * nothing. Override it if the Option needs to determine its
     * choices dynamically.
     */
    public void generateChoices() {
        // do nothing
    }

    /**
     * Initialize this object from an XML-representation of this object.
     * @param in The input stream with the XML.
     * @throws XMLStreamException if a problem was encountered
     *      during parsing.
     */
    protected void readFromXMLImpl(XMLStreamReader in) throws XMLStreamException {
        readAttributes(in);
        while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
            readChild(in);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void readAttributes(XMLStreamReader in) throws XMLStreamException {
        setId(getAttribute(in, ID_ATTRIBUTE_TAG, getId()));
        final String defaultValue = in.getAttributeValue(null, "defaultValue");
        final String value = in.getAttributeValue(null, VALUE_TAG);

        if (!isNullValueOK() && defaultValue == null && value == null) {
            throw new XMLStreamException("invalid option " + getId()
                                         + ": no value nor default value found.");
        }

        setValue(value, defaultValue);
    }

    protected AbstractOption readOption(XMLStreamReader in) throws XMLStreamException {
        String optionType = in.getLocalName();
        AbstractOption option = null;
        if (OptionGroup.getXMLElementTagName().equals(optionType)) {
            option = new OptionGroup(getSpecification());
        } else if (IntegerOption.getXMLElementTagName().equals(optionType)) {
            option = new IntegerOption(getSpecification());
        } else if (BooleanOption.getXMLElementTagName().equals(optionType)) {
            option = new BooleanOption(getSpecification());
        } else if (RangeOption.getXMLElementTagName().equals(optionType)) {
            option = new RangeOption(getSpecification());
        } else if (SelectOption.getXMLElementTagName().equals(optionType)) {
            option = new SelectOption(getSpecification());
        } else if (LanguageOption.getXMLElementTagName().equals(optionType)) {
            option = new LanguageOption(getSpecification());
        } else if (FileOption.getXMLElementTagName().equals(optionType)) {
            option = new FileOption(getSpecification());
        } else if (PercentageOption.getXMLElementTagName().equals(optionType)) {
            option = new PercentageOption(getSpecification());
        } else if (AudioMixerOption.getXMLElementTagName().equals(optionType)) {
            option = new AudioMixerOption(getSpecification());
        } else if (StringOption.getXMLElementTagName().equals(optionType)) {
            option = new StringOption(getSpecification());
        } else if (UnitTypeOption.getXMLElementTagName().equals(optionType)) {
            option = new UnitTypeOption(getSpecification());
        } else if (AbstractUnitOption.getXMLElementTagName().equals(optionType)) {
            option = new AbstractUnitOption(getSpecification());
        } else if (ModOption.getXMLElementTagName().equals(optionType)) {
            option = new ModOption(getSpecification());
        } else if (UnitListOption.getXMLElementTagName().equals(optionType)) {
            option = new UnitListOption(getSpecification());
        } else if (ModListOption.getXMLElementTagName().equals(optionType)) {
            option = new ModListOption(getSpecification());
        } else if ("action".equals(optionType)) {
            logger.finest("Skipping action " + in.getAttributeValue(null, "id"));
            // TODO: load FreeColActions from client options?
            in.nextTag();
            return null;
        } else {
            logger.finest("Parsing of option type '" + optionType + "' is not implemented yet");
            in.nextTag();
            return null;
        }
        option.readFromXML(in);
        return option;
    }



}
