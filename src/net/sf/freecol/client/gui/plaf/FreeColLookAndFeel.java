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

package net.sf.freecol.client.gui.plaf;


import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Cursor;
import org.freecolandroid.repackaged.java.awt.Dimension;
import org.freecolandroid.repackaged.java.awt.Font;
import org.freecolandroid.repackaged.java.awt.Image;
import org.freecolandroid.repackaged.java.awt.Point;
import org.freecolandroid.repackaged.java.awt.Toolkit;
import org.freecolandroid.repackaged.javax.swing.UIDefaults;
import org.freecolandroid.repackaged.javax.swing.UIManager;
import org.freecolandroid.repackaged.javax.swing.UnsupportedLookAndFeelException;
import org.freecolandroid.repackaged.javax.swing.plaf.ColorUIResource;
import org.freecolandroid.repackaged.javax.swing.plaf.metal.DefaultMetalTheme;
import org.freecolandroid.repackaged.javax.swing.plaf.metal.MetalLookAndFeel;



import net.sf.freecol.common.FreeColException;
import net.sf.freecol.common.resources.ResourceManager;


/**
 * Implements the FreeCol look and feel.
 */
public class FreeColLookAndFeel extends MetalLookAndFeel {

    private static final Logger logger = Logger.getLogger(FreeColLookAndFeel.class.getName());


    /**
     * Initiates a new FreeCol look and feel.
     *
     * @param dataDirectory The home of the FreeCol data files.
     * @param windowSize The size of the application window.
     * @exception FreeColException If the ui directory could not be found.
     */
    public FreeColLookAndFeel(File dataDirectory, Dimension windowSize)
        throws FreeColException {
        super();

//        setCurrentTheme(new DefaultMetalTheme() {
//                protected ColorUIResource getPrimary1() {
//                    return new ColorUIResource(ResourceManager.getColor("lookAndFeel.primary1.color"));
//                }
//
//                protected ColorUIResource getPrimary2() {
//                    return new ColorUIResource(ResourceManager.getColor("lookAndFeel.backgroundSelect.color"));
//                }
//
//                protected ColorUIResource getPrimary3() {
//                    return new ColorUIResource(ResourceManager.getColor("lookAndFeel.primary3.color"));
//                }
//
//                protected ColorUIResource getSecondary1() {
//                    return new ColorUIResource(ResourceManager.getColor("lookAndFeel.secondary1.color"));
//                }
//
//                protected ColorUIResource getSecondary2() {
//                    return new ColorUIResource(ResourceManager.getColor("lookAndFeel.disabled.color"));
//                }
//
//                protected ColorUIResource getSecondary3() {
//                    return new ColorUIResource(ResourceManager.getColor("lookAndFeel.background.color"));
//                }
//
//                public ColorUIResource getMenuDisabledForeground() {
//                    return new ColorUIResource(ResourceManager.getColor("lookAndFeel.disabledMenu.color"));
//                }
//            });
//
//        if (!dataDirectory.isDirectory()) {
//           throw new FreeColException("Data directory is not a directory.");
//        }
    }

    /**
     * Creates the look and feel specific defaults table.
     *
     * @return The defaults table.
     */
    public UIDefaults getDefaults() {
//        UIDefaults u = super.getDefaults();
//
//        try {
//            int offset = "FreeCol".length();
//            for (Class uiClass : new Class[] {
//                    FreeColButtonUI.class,
//                    FreeColCheckBoxUI.class,
//                    FreeColComboBoxUI.class,
//                    FreeColLabelUI.class,
//                    FreeColListUI.class,
//                    FreeColMenuBarUI.class,
//                    FreeColMenuItemUI.class,
//                    FreeColPanelUI.class,
//                    FreeColPopupMenuUI.class,
//                    FreeColRadioButtonUI.class,
//                    FreeColScrollPaneUI.class,
//                    FreeColTableHeaderUI.class,
//                    FreeColTableUI.class,
//                    FreeColTextAreaUI.class,
//                    FreeColTextFieldUI.class,
//                    FreeColToolTipUI.class,
//                    FreeColTransparentPanelUI.class
//                }) {
//                String name = uiClass.getName();
//                int index = name.lastIndexOf("FreeCol");
//                if (index >= 0) {
//                    index += offset;
//                    String shortName = name.substring(index);
//                    u.put(shortName, name);
//                    u.put(name, uiClass);
//                }
//            }
//
//            // Sharing FreeColBrightPanelUI:
//            String brightPanelUI = "net.sf.freecol.client.gui.plaf.FreeColBrightPanelUI";
//            u.put(brightPanelUI, Class.forName(brightPanelUI));
//            u.put("InPortPanelUI", brightPanelUI);
//            u.put("CargoPanelUI", brightPanelUI);
//            u.put("BuildingsPanelUI", brightPanelUI);
//            u.put("OutsideColonyPanelUI", brightPanelUI);
//            u.put("WarehousePanelUI", brightPanelUI);
//            u.put("ConstructionPanelUI", brightPanelUI);
//            u.put("PopulationPanelUI", brightPanelUI);
//            u.put("WarehouseGoodsPanelUI", brightPanelUI);
//            u.put("ReportPanelUI", brightPanelUI);
//            u.put("ColopediaPanelUI", brightPanelUI);
//            u.put("TilePanelUI", brightPanelUI);
//            u.put("OptionGroupUI", brightPanelUI);
//
//            // Sharing FreeColTransparentPanelUI:
//            String transparentPanelUI = "net.sf.freecol.client.gui.plaf.FreeColTransparentPanelUI";
//            u.put(transparentPanelUI, Class.forName(transparentPanelUI));
//            u.put("MarketPanelUI", transparentPanelUI);
//            u.put("EuropeCargoPanelUI", transparentPanelUI);
//            u.put("ToAmericaPanelUI", transparentPanelUI);
//            u.put("ToEuropePanelUI", transparentPanelUI);
//            u.put("EuropeInPortPanelUI", transparentPanelUI);
//            u.put("DocksPanelUI", transparentPanelUI);
//
//            // Add cursors:
//            Image im = ResourceManager.getImage("cursor.go.image");
//            if (im != null) {
//                u.put("cursor.go", Toolkit.getDefaultToolkit()
//                      .createCustomCursor(im, new Point(im.getWidth(null)/2,
//                                                        im.getHeight(null)/2), "go"));
//            } else {
//                u.put("cursor.go", Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//            }
//
//        } catch (ClassNotFoundException e) {
//            logger.log(Level.SEVERE, "Failed to load look and feel!", e);
//            System.exit(-1);
//        }
//
//        return u;
    	return null;
    }

    /**
     * Installs a FreeColLookAndFeel as the default look and feel.
     *
     * @param fclaf The <code>FreeColLookAndFeel</code> to install.
     * @param defaultFont A <code>Font</code> to use by default.
     * @throws FreeColException if the installation fails.
     */
    public static void install(FreeColLookAndFeel fclaf, Font defaultFont)
        throws FreeColException {
//        try {
//            UIManager.setLookAndFeel(fclaf);
//        } catch (UnsupportedLookAndFeelException e) {
//            throw new FreeColException(e.getMessage());
//        }
//
//        // Set the default font in all UI elements.
//        UIDefaults u = UIManager.getDefaults();
//        java.util.Enumeration<Object> keys = u.keys();
//        while (keys.hasMoreElements()) {
//            Object key = keys.nextElement();
//            if (u.get(key) instanceof com.erik.repackaged.javax.swing.plaf.FontUIResource) {
//                u.put(key, defaultFont);
//            }
//        }
    }

    /**
     * Gets a one line description of this Look and Feel.
     *
     * @return "The default Look and Feel for FreeCol"
     */
    public String getDescription() {
        return "The default Look and Feel for FreeCol";
    }


    /**
     * Gets the name of this Look and Feel.
     *
     * @return "FreeCol Look and Feel"
     */
    public String getName() {
        return "FreeCol Look and Feel";
    }
}
