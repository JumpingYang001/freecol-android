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

package net.sf.freecol.client.gui.panel;


import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Component;
import org.freecolandroid.repackaged.java.awt.Dimension;
import org.freecolandroid.repackaged.java.awt.Font;
import org.freecolandroid.repackaged.java.awt.Graphics;
import org.freecolandroid.repackaged.java.awt.Graphics2D;
import org.freecolandroid.repackaged.java.awt.Image;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.javax.swing.BorderFactory;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JLabel;


import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.control.InGameController;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.ImageLibrary;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.Ability;
import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.BuildingType;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.WorkLocation;
import net.sf.freecol.common.resources.ResourceManager;


/**
 * This label holds Unit data in addition to the JLabel data, which makes it
 * ideal to use for drag and drop purposes.
 */
public final class UnitLabel extends JLabel implements ActionListener {

    @SuppressWarnings("unused")
        private static Logger logger = Logger.getLogger(UnitLabel.class.getName());

    public static enum UnitAction {
        ASSIGN,
        CLEAR_SPECIALITY,
        ACTIVATE_UNIT,
        FORTIFY,
        SENTRY,
        COLOPEDIA,
        LEAVE_TOWN,
        WORK_TILE,
        WORK_BUILDING,
        CLEAR_ORDERS,
        ASSIGN_TRADE_ROUTE,
    }

    private final Unit unit;


    private boolean selected;

    private boolean isSmall = false;

    private boolean ignoreLocation;

    private InGameController inGameController;

    private FreeColClient freeColClient;

    private GUI gui;


    /**
     * Initializes this JLabel with the given unit data.
     *
     * @param unit The Unit that this JLabel will visually represent.
     * @param parent The parent that knows more than we do.
     */
    public UnitLabel(FreeColClient freeColClient, Unit unit, GUI gui) {
        this.freeColClient = freeColClient;
        this.unit = unit;
        this.gui = gui;
        this.inGameController = freeColClient.getInGameController();

        selected = false;
        setSmall(false);
        setIgnoreLocation(false);

        updateIcon();
    }

    /**
     * Initializes this JLabel with the given unit data.
     *
     * @param unit The Unit that this JLabel will visually represent.
     * @param parent The parent that knows more than we do.
     * @param isSmall The image will be smaller if set to <code>true</code>.
     */
    public UnitLabel(FreeColClient freeColClient, Unit unit, GUI gui, boolean isSmall) {
        this(freeColClient, unit, gui);
        setSmall(isSmall);
        setIgnoreLocation(false);
    }

    /**
     * Initializes this JLabel with the given unit data.
     *
     * @param unit The Unit that this JLabel will visually represent.
     * @param parent The parent that knows more than we do.
     * @param isSmall The image will be smaller if set to <code>true</code>.
     * @param ignoreLocation The image will not include production or state
     *            information if set to <code>true</code>.
     */
    public UnitLabel(FreeColClient freeColClient, Unit unit, GUI gui, boolean isSmall, boolean ignoreLocation) {
        this(freeColClient, unit, gui);
        setSmall(isSmall);
        setIgnoreLocation(ignoreLocation);
    }


    /**
     * Returns this UnitLabel's unit data.
     *
     * @return This UnitLabel's unit data.
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Sets whether or not this unit should be selected.
     *
     * @param b Whether or not this unit should be selected.
     */
    public void setSelected(boolean b) {
        selected = b;
    }

    /**
     * Sets whether or not this unit label should include production and state
     * information.
     *
     * @param b Whether or not this unit label should include production and
     *            state information.
     */
    public void setIgnoreLocation(boolean b) {
        ignoreLocation = b;
    }

    /**
     * Makes a smaller version.
     *
     * @param isSmall The image will be smaller if set to <code>true</code>.
     */
    public void setSmall(boolean isSmall) {
        ImageIcon imageIcon = gui.getImageLibrary().getUnitImageIcon(unit);
        ImageIcon disabledImageIcon = gui.getImageLibrary().getUnitImageIcon(unit, true);
        if (isSmall) {
            setPreferredSize(null);
            // setIcon(new
            // ImageIcon(imageIcon.getImage().getScaledInstance(imageIcon.getIconWidth()
            // / 2, imageIcon.getIconHeight() / 2, Image.SCALE_DEFAULT)));
            setIcon(new ImageIcon(imageIcon.getImage().getScaledInstance((imageIcon.getIconWidth() / 3) * 2,
                                                                         (imageIcon.getIconHeight() / 3) * 2, Image.SCALE_SMOOTH)));

            setDisabledIcon(new ImageIcon(disabledImageIcon.getImage().getScaledInstance(
                                                                                         (imageIcon.getIconWidth() / 3) * 2, (imageIcon.getIconHeight() / 3) * 2, Image.SCALE_SMOOTH)));
            setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
            this.isSmall = true;
        } else {
            if (unit.getLocation() instanceof ColonyTile) {
                TileType tileType = ((ColonyTile) unit.getLocation()).getTile().getType();
                setSize(new Dimension(gui.getImageLibrary().getTerrainImageWidth(tileType) / 2,
                                      imageIcon.getIconHeight()));
            } else {
                setPreferredSize(null);
            }

            setIcon(imageIcon);
            setDisabledIcon(disabledImageIcon);
            if (unit.getLocation() instanceof ColonyTile) {
                setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
            } else {
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            }
            this.isSmall = false;
        }
    }

    /**
     * Gets the description label.
     *
     * The description label is a tooltip with the unit name and description of
     * the terrain its on if applicable *
     *
     * @return This UnitLabel's description label.
     */
    public String getDescriptionLabel() {
        return getToolTipText();
    }

    /**
     * Sets the description label.
     *
     * The description label is a tooltip with the unit name and description of
     * the terrain its on if applicable
     *
     * @param label The string to set the label to.
     */
    public void setDescriptionLabel(String label) {
        setToolTipText(label);

    }

    /**
     * Paints this UnitLabel.
     *
     * @param g The graphics context in which to do the painting.
     */
    public void paintComponent(Graphics g) {

        if (ignoreLocation || selected
            || (!unit.isCarrier() && unit.getState() != Unit.UnitState.SENTRY)) {
            setEnabled(true);
        } else if (unit.getOwner() != freeColClient.getMyPlayer()
            && unit.getColony() == null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }

        super.paintComponent(g);
        if (ignoreLocation)
            return;

        if (unit.getLocation() instanceof ColonyTile) {
            GoodsType workType = unit.getWorkType();
            int production = ((ColonyTile) unit.getLocation()).getProductionOf(workType);

            ProductionLabel pl = new ProductionLabel(freeColClient, gui, workType, production);
            g.translate(0, 10);
            pl.paintComponent(g);
            g.translate(0, -10);
        } else if (getParent() instanceof ColonyPanel.OutsideColonyPanel ||
                   getParent() instanceof ColonyPanel.InPortPanel ||
                   getParent() instanceof EuropePanel.InPortPanel ||
                   getParent() instanceof EuropePanel.DocksPanel ||
                   getParent().getParent() instanceof ReportPanel) {
            g.drawImage(gui.getMapViewer().getOccupationIndicatorImage(g, unit), 0, 0, null);

            if (unit.isUnderRepair()) {
                String underRepair = Messages.message(StringTemplate.template("underRepair")
                                                      .addAmount("%turns%", unit.getTurnsForRepair()));
                String underRepair1 = underRepair.substring(0, underRepair.indexOf('(')).trim();
                String underRepair2 = underRepair.substring(underRepair.indexOf('(')).trim();
                Font font = ResourceManager.getFont("NormalFont", 14f);
                Image repairImage1 = gui.getMapViewer()
                    .createStringImage((Graphics2D)g, underRepair1, Color.RED, font);
                Image repairImage2 = gui.getMapViewer()
                    .createStringImage((Graphics2D)g, underRepair2, Color.RED, font);
                int textHeight = repairImage1.getHeight(null) + repairImage2.getHeight(null);
                int leftIndent = Math.min(5, Math.min(getWidth() - repairImage1.getWidth(null),
                                                      getWidth() - repairImage2.getWidth(null)));
                g.drawImage(repairImage1,
                            leftIndent, // indent from left side of label (icon is placed at the left side)
                            ((getHeight() - textHeight) / 2),
                            null);
                g.drawImage(repairImage2,
                            leftIndent,
                            ((getHeight() - textHeight) / 2) + repairImage1.getHeight(null),
                            null);
            }
        }
    }

    /**
     * Analyzes an event and calls the right external methods to take care of
     * the user's request.
     *
     * @param event The incoming action event
     */
    public void actionPerformed(ActionEvent event) {
        String commandString = event.getActionCommand();
        String arg = null;
        int index = commandString.indexOf(':');
        if (index > 0) {
            arg = commandString.substring(index + 1);
            commandString = commandString.substring(0, index);
        }
        UnitAction command = Enum.valueOf(UnitAction.class, commandString.toUpperCase());
        switch(command) {
        case ASSIGN:
            Unit teacher = (Unit) unit.getGame().getFreeColGameObject(arg);
            inGameController.assignTeacher(unit, teacher);
            Component uc = getParent();
            while (uc != null) {
                /*
                  if (uc instanceof ColonyPanel) {
                  ((ColonyPanel) uc).reinitialize();
                  break;
                  }
                */
                uc = uc.getParent();
            }
            break;
        case WORK_TILE:
            GoodsType goodsType = freeColClient.getGame().getSpecification().getGoodsType(arg);
            // Change workType first for the benefit of change listeners
            inGameController.changeWorkType(unit, goodsType);
            // Move unit to best producing ColonyTile
            ColonyTile bestTile = unit.getColony().getVacantColonyTileFor(unit, false, goodsType);
            if (bestTile == null
                || (unit.getLocation() instanceof WorkLocation
                    && (((WorkLocation)unit.getLocation())
                        .getProductionOf(unit, goodsType)
                        > bestTile.getProductionOf(unit, goodsType)))) break;
            inGameController.work(unit, bestTile);
            break;
        case WORK_BUILDING:
            BuildingType buildingType = freeColClient.getGame().getSpecification().getBuildingType(arg);
            Building building = unit.getColony().getBuilding(buildingType);
            if (building != unit.getLocation()) {
                inGameController.work(unit, building);
            }
            break;
        case ACTIVATE_UNIT:
            inGameController.changeState(unit, Unit.UnitState.ACTIVE);
            gui.setActiveUnit(unit);
            break;
        case FORTIFY:
            inGameController.changeState(unit, Unit.UnitState.FORTIFYING);
            break;
        case SENTRY:
            inGameController.changeState(unit, Unit.UnitState.SENTRY);
            break;
        case COLOPEDIA:
            gui.showColopediaPanel(unit.getType().getId());
            break;
        case LEAVE_TOWN:
            inGameController.putOutsideColony(unit);
            break;
        case CLEAR_SPECIALITY:
            inGameController.clearSpeciality(unit);
            break;
        case CLEAR_ORDERS:
            inGameController.clearOrders(unit);
            break;
        case ASSIGN_TRADE_ROUTE:
            inGameController.assignTradeRoute(unit);
            break;
        }
        updateIcon();
    }


    public void updateIcon() {
        ImageLibrary lib = gui.getImageLibrary();
        setIcon(lib.getUnitImageIcon(unit));
        setDisabledIcon(lib.getUnitImageIcon(unit, true));
        setDescriptionLabel(Messages.message(Messages.getLabel(unit)));
        StringTemplate label = unit.getEquipmentLabel();
        if (label != null) {
            setDescriptionLabel(getDescriptionLabel() + " ("
                                + Messages.message(label) + ")");
        }
        setSmall(isSmall);

        Component uc = getParent();
        while (uc != null) {
            if (uc instanceof ColonyPanel) {
                if (unit.getColony() == null) {
                    gui.removeFromCanvas(uc);
                    freeColClient.getActionManager().update();
                } else {
                    // ((ColonyPanel) uc).reinitialize();
                }

                break;
            } else if (uc instanceof EuropePanel) {
                break;
            }

            uc = uc.getParent();
        }

        // repaint(0, 0, getWidth(), getHeight());
        // uc.refresh();
    }

    public boolean canUnitBeEquipedWith(JLabel data){
        if(!getUnit().hasAbility(Ability.CAN_BE_EQUIPPED)){
            return false;
        }

        if(data instanceof GoodsLabel && ((GoodsLabel)data).isToEquip()){
            return true;
        }

        if(data instanceof MarketLabel && ((MarketLabel)data).isToEquip()){
            return true;
        }

        return false;
    }
}
