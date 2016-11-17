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

package net.sf.freecol.client.gui.i18n;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.freecolandroid.xml.stream.XMLInputFactory;
import org.freecolandroid.xml.stream.XMLStreamConstants;
import org.freecolandroid.xml.stream.XMLStreamException;
import org.freecolandroid.xml.stream.XMLStreamReader;

import net.sf.freecol.client.gui.i18n.Number.Category;

/**
 * See the
 * <a href="http://cldr.unicode.org/index/cldr-spec/plural-rules">
 * Common Locale Data Repository</a>.
 */
public class NumberRules {

    private static final Logger logger = Logger.getLogger(NumberRules.class.getName());

    /**
     * A rule that always returns category "other".
     */
    public static final Number OTHER_NUMBER_RULE = new OtherNumberRule();

    /**
     * A rule that assigns 1 to category "one", 2 to category "two"
     * and all other numbers to category "other".
     */
    public static final Number DUAL_NUMBER_RULE = new DualNumberRule();

    /**
     * A rule that assigns 1 to category "one" and all other numbers
     * to category "other".
     */
    public static final Number PLURAL_NUMBER_RULE = new PluralNumberRule();

    /**
     * A rule that assigns 0 and 1 to category "one", and all other
     * number to category "other".
     */
    public static final Number ZERO_ONE_NUMBER_RULE = new ZeroOneNumberRule();


    private static Map<String, Number> numberMap = new HashMap<String, Number>();


    /**
     * Creates a new <code>NumberRules</code> instance from the given
     * input stream, which must contain an XML representation of the
     * CLDR plural rules.
     *
     * @param in an <code>InputStream</code> value
     */
    public NumberRules(InputStream in) {
        load(in);
    }


    /**
     * Returns a rule appropriate for the given language, or the
     * OTHER_NUMBER_RULE if none has been defined.
     *
     * @param lang a <code>String</code> value
     * @return a <code>Number</code> value
     */
    public static Number getNumberForLanguage(String lang) {
        Number number = numberMap.get(lang);
        return number == null ? OTHER_NUMBER_RULE : number;
    }

    /**
     * Describe <code>isInitialized</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public static boolean isInitialized() {
        return !numberMap.isEmpty();
    }


    /**
     * Describe <code>load</code> method here.
     *
     * @param in an <code>InputStream</code> value
     */
    public static void load(InputStream in) {

        try {
            XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(in);
            readFromXML(xsr);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.warning(sw.toString());
            throw new RuntimeException("Error parsing number rules.");
        }
    }

    /**
     * Describe <code>readFromXML</code> method here.
     *
     * @param in a <code>XMLStreamReader</code> value
     * @exception XMLStreamException if an error occurs
     */
    private static void readFromXML(XMLStreamReader in) throws XMLStreamException {
        while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
            String tag = in.getLocalName();
            if ("version".equals(tag)) {
                in.nextTag();
            } else if ("generation".equals(tag)) {
                in.nextTag();
            } else if ("plurals".equals(tag)) {
                while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
                    tag = in.getLocalName();
                    if ("pluralRules".equals(tag)) {
                        readChild(in);
                    }
                }
            }
        }
    }

    /**
     * Describe <code>readChild</code> method here.
     *
     * @param in a <code>XMLStreamReader</code> value
     * @exception XMLStreamException if an error occurs
     */
    private static void readChild(XMLStreamReader in)
        throws XMLStreamException {

        String[] locales = in.getAttributeValue(null, "locales").split(" ");
        if (locales != null) {
            DefaultNumberRule numberRule = new DefaultNumberRule();
            while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
                if ("pluralRule".equals(in.getLocalName())) {
                    Category category = Category.valueOf(in.getAttributeValue(null, "count"));
                    Rule rule = new Rule(in.getElementText());
                    numberRule.addRule(category, rule);
                }
            }
            Number number = null;
            switch(numberRule.countRules()) {
            case 0:
                number = OTHER_NUMBER_RULE;
                break;
            case 1:
                Rule rule = numberRule.getRule(Category.one);
                if (rule != null) {
                    if ("n is 1".equals(rule.toString())) {
                        number = PLURAL_NUMBER_RULE;
                    } else if ("n in 0..1".equals(rule.toString())) {
                        number = ZERO_ONE_NUMBER_RULE;
                    }
                }
                break;
            case 2:
                Rule oneRule = numberRule.getRule(Category.one);
                Rule twoRule = numberRule.getRule(Category.two);
                if (oneRule != null
                    && "n is 1".equals(oneRule.toString())
                    && twoRule != null
                    && "n is 2".equals(twoRule.toString())) {
                    number = DUAL_NUMBER_RULE;
                }
                break;
            default:
                number = numberRule;
            }
            for (String locale : locales) {
                numberMap.put(locale, number);
            }
        }
    }

}