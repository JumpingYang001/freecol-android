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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.freecolandroid.xml.stream.XMLStreamException;
import org.freecolandroid.xml.stream.XMLStreamWriter;

import net.sf.freecol.FreeCol;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.Specification;


/**
 * Option for selecting a language. The possible choices are determined
 * using the available language files in "data/strings".
 */
public class LanguageOption extends AbstractOption<LanguageOption.Language> {

    public static final String AUTO = "automatic";

    private static final Logger logger = Logger.getLogger(LanguageOption.class.getName());

    private static final Map<String, Language> languages = new HashMap<String, Language>();

    private static final Map<String, String> languageNames = new HashMap<String, String>();

    private Language DEFAULT = new Language(AUTO, getLocale(AUTO));

    private Language value;

    static {
        // add non-standard language names here
        languageNames.put("arz", "\u0645\u0635\u0631\u064A");
        languageNames.put("hsb", "Serb\u0161\u0107ina");
        languageNames.put("nds", "Plattd\u00fc\u00fctsch");
        languageNames.put("pms", "Piemont\u00e9s");
        languageNames.put("be-tarask", "\u0411\u0435\u043b\u0430\u0440\u0443\u0441\u043a\u0430\u044f "
                          + "(\u0442\u0430\u0440\u0430\u0448\u043a\u0435\u0432\u0456\u0446\u0430)");
        findLanguages();
    }


    private static Comparator<Language> languageComparator = new Comparator<Language>() {
        public int compare(Language l1, Language l2) {
            if (l1.getKey().equals(AUTO)) {
                if (l2.getKey().equals(AUTO)) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (l2.getKey().equals(AUTO)) {
                return 1;
            } else {
                return l1.toString().compareTo(l2.toString());
            }
        }
    };



    /**
     * Creates a new <code>LanguageOption</code>.
     *
     * @param specification The specification this option belongs
     *     to. May be null.
     */
    public LanguageOption(Specification specification) {
        super(specification);
        languages.put(AUTO, DEFAULT);
    }


    public LanguageOption clone() throws CloneNotSupportedException {
        LanguageOption result = new LanguageOption(getSpecification());
        result.setValues(this);
        return result;
    }

    /**
     * Get the <code>Value</code> value.
     *
     * @return a <code>String</code> value
     */
    public final Language getValue() {
        return value;
    }

    /**
     * Set the <code>Value</code> value.
     *
     * @param newValue The new Value value.
     */
    public final void setValue(final Language newValue) {
        final Language oldValue = this.value;
        this.value = newValue;

        if (!newValue.equals(oldValue)) {
            firePropertyChange(VALUE_TAG, oldValue, value);
        }
    }

    /**
     * Sets the value of this Option from the given string
     * representation. Both parameters must not be null at the same
     * time.
     *
     * @param valueString the string representation of the value of
     * this Option
     * @param defaultValueString the string representation of the
     * default value of this Option
     */
    protected void setValue(String valueString, String defaultValueString) {
        setValue(languages.get((valueString != null) ? valueString : AUTO));
    }

    /**
     * Returns a list of the available languages.
     * @return The available languages in a human readable format.
     */
    public Language[] getOptions() {
        List<Language> names = new ArrayList<Language>(languages.values());
        Collections.sort(names, languageComparator);
        return names.toArray(new Language[0]);
    }

    /**
     * Finds the languages available in the default directory.
     */
    private static void findLanguages() {

        File i18nDirectory = new File(FreeCol.getDataDirectory(), Messages.STRINGS_DIRECTORY);
        File[] files = i18nDirectory.listFiles();
        if (files == null) {
            throw new RuntimeException("No language files could be found in the <" + i18nDirectory +
                                       "> folder. Make sure you ran the ant correctly.");
        }
        String prefix = Messages.FILE_PREFIX + "_";
        int prefixLength = prefix.length();
        for (File file : files) {
            if (file.getName() == null) {
                continue;
            }
            if (file.getName().startsWith(prefix)) {
                try {
                    final String languageID =
                        file.getName().substring(prefixLength, file.getName().indexOf("."));
                    // qqq contains explanations only
                    if (!"qqq".equals(languageID)) {
                        languages.put(languageID, new Language(languageID, getLocale(languageID)));
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Exception in findLanguages()", e);
                    continue;
                }
            }
        }
    }


    /**
     * Returns the <code>Locale</code> decided by the given name.
     *
     * @param languageID A String using the same format as
     *         {@link #getValue()}.
     * @return The Locale.
     */
    public static Locale getLocale(String languageID) {
        if (languageID == null || AUTO.equals(languageID)) {
            return Locale.getDefault();
        }

        try {
            String language, country = "", variant = "";
            StringTokenizer st = new StringTokenizer(languageID, "_", true);
            language = st.nextToken();
            if (st.hasMoreTokens()) {
                // Skip _
                st.nextToken();
            }
            if (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (!token.equals("_")) {
                    country = token;
                }
                if (st.hasMoreTokens()) {
                    token = st.nextToken();
                    if (token.equals("_") && st.hasMoreTokens()) {
                        token = st.nextToken();
                    }
                    if (!token.equals("_")) {
                        variant = token;
                    }
                }
            }
            return new Locale(language, country, variant);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Cannot choose locale: " + languageID, e);
            return Locale.getDefault();
        }
    }

    public static class Language {

        /**
         * Describe key here.
         */
        private String key;

        /**
         * Describe locale here.
         */
        private Locale locale;


        public Language(String key, Locale locale) {
            this.key = key;
            this.locale = locale;
        }

        /**
         * Get the <code>Key</code> value.
         *
         * @return a <code>String</code> value
         */
        public final String getKey() {
            return key;
        }

        /**
         * Set the <code>Key</code> value.
         *
         * @param newKey The new Key value.
         */
        public final void setKey(final String newKey) {
            this.key = newKey;
        }

        /**
         * Get the <code>Locale</code> value.
         *
         * @return a <code>Locale</code> value
         */
        public final Locale getLocale() {
            return locale;
        }

        /**
         * Set the <code>Locale</code> value.
         *
         * @param newLocale The new Locale value.
         */
        public final void setLocale(final Locale newLocale) {
            this.locale = newLocale;
        }

        public String toString() {
            if (getKey().equals(AUTO)) {
                return Messages.message("clientOptions.gui.languageOption.autoDetectLanguage");
            } else {
                String name = locale.getDisplayName(locale);
                if (name.equals(key) && languageNames.containsKey(key)) {
                    name = languageNames.get(key);
                }
                return name.substring(0, 1).toUpperCase(locale) + name.substring(1);
            }
        }

        public boolean equals(Object o) {
            if ((o instanceof Language) &&
                ((Language) o).getKey().equals(key)) {
                return true;
            } else {
                return false;
            }
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
    protected void toXMLImpl(XMLStreamWriter out) throws XMLStreamException {
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

        out.writeAttribute(VALUE_TAG, getValue().getKey());
    }

    /**
     * Gets the tag name of the root element representing this object.
     *
     * @return "languageOption".
     */
    public static String getXMLElementTagName() {
        return "languageOption";
    }
}
