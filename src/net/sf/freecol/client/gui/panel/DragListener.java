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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Font;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.java.awt.event.MouseAdapter;
import org.freecolandroid.repackaged.java.awt.event.MouseEvent;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JComponent;
import org.freecolandroid.repackaged.javax.swing.JMenuItem;
import org.freecolandroid.repackaged.javax.swing.JPopupMenu;
import org.freecolandroid.repackaged.javax.swing.TransferHandler;


import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.control.InGameController;
import net.sf.freecol.client.gui.Canvas;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.ImageLibrary;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.client.gui.panel.UnitLabel.UnitAction;
import net.sf.freecol.common.model.Ability;
import net.sf.freecol.common.model.AbstractGoods;
import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.EquipmentType;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.GameOptions;
import net.sf.freecol.common.model.Goods;
import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.Role;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.UnitTypeChange.ChangeType;
import net.sf.freecol.common.model.WorkLocation;


/**
 * A DragListener should be attached to Swing components that have a
 * TransferHandler attached. The DragListener will make sure that the Swing
 * component to which it is attached is draggable (moveable to be precise).
 */
public final class DragListener extends MouseAdapter {

    private static final Logger logger = Logger.getLogger(DragListener.class.getName());

    private final FreeColPanel parentPanel;
    private final Canvas canvas;

    private FreeColClient freeColClient;

    private GUI gui;

    /**
     * The constructor to use.
     * @param freeColClient 
     *
     * @param parentPanel The layered pane that contains the components to which
     *            a DragListener might be attached.
     */
    public DragListener(FreeColClient freeColClient, GUI gui, FreeColPanel parentPanel) {
        this.freeColClient = freeColClient;
        this.gui = gui;
        this.parentPanel = parentPanel;
        this.canvas = parentPanel.getCanvas();
    }

    /**
     * Gets called when the mouse was pressed on a Swing component that has this
     * object as a MouseListener.
     *
     * @param e The event that holds the information about the mouse click.
     */
    public void mousePressed(MouseEvent e) {
        JComponent comp = (JComponent) e.getSource();

        // Does not work on some platforms:
        // if (e.isPopupTrigger() && (comp instanceof UnitLabel)) {
        if ((e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger())) {
            // Popup mustn't be shown when panel is not editable
            if (parentPanel.isEditable()) {
                JPopupMenu menu = null;
                if (comp instanceof UnitLabel) {
                    menu = getUnitMenu((UnitLabel) comp);
                } else if (comp instanceof GoodsLabel) {
                    menu = getGoodsMenu((GoodsLabel) comp);
                } else if (comp instanceof MarketLabel
                           && parentPanel instanceof EuropePanel) {
                    GoodsType goodsType = ((MarketLabel) comp).getType();
                    if (freeColClient.getInGameController()
                        .payArrears(goodsType)) {
                        ((EuropePanel) parentPanel).revalidate();
                        ((EuropePanel) parentPanel).refresh();
                    }
                }
                if (menu != null) {
                    int elements = menu.getSubElements().length;
                    if (elements > 0) {
                        int lastIndex = menu.getComponentCount() - 1;
                        if (menu.getComponent(lastIndex) instanceof JPopupMenu.Separator) {
                            menu.remove(lastIndex);
                        }
                        if (gui.isWindowed()
                            && System.getProperty("os.name").startsWith("Windows")) {
                            // work-around: JRE on Windows is unable
                            // to display popup menus that extend
                            // beyond the canvas
                            menu.show(canvas, 0, 0);
                        } else {
                            menu.show(comp, e.getX(), e.getY());
                        }
                    }
                }
            }
        } else {
            TransferHandler handler = comp.getTransferHandler();

            if (e.isShiftDown()) {
                if (comp instanceof GoodsLabel) {
                    ((GoodsLabel) comp).setPartialChosen(true);
                } else if (comp instanceof MarketLabel) {
                    ((MarketLabel) comp).setPartialChosen(true);
                }
            } else if(e.isAltDown()){
                if (comp instanceof GoodsLabel) {
                    ((GoodsLabel) comp).toEquip(true);
                } else if (comp instanceof MarketLabel) {
                    ((MarketLabel) comp).toEquip(true);
                }
            } else {
                if (comp instanceof GoodsLabel) {
                    ((GoodsLabel) comp).setPartialChosen(false);
                } else if (comp instanceof MarketLabel) {
                    ((MarketLabel) comp).setPartialChosen(false);
                    ((MarketLabel) comp).setAmount(GoodsContainer.CARGO_SIZE);
                }
            }

            if ((comp instanceof UnitLabel) && (((UnitLabel) comp).getUnit().isCarrier())) {
                Unit u = ((UnitLabel) comp).getUnit();
                if (parentPanel instanceof EuropePanel) {
                    if (!u.isAtSea()) {
                        ((EuropePanel) parentPanel).setSelectedUnitLabel((UnitLabel) comp);
                    }
                } else if (parentPanel instanceof ColonyPanel) {
                    ColonyPanel colonyPanel = (ColonyPanel) parentPanel;
                    if(colonyPanel.getSelectedUnit() != u){
                        colonyPanel.setSelectedUnit(u);
                        colonyPanel.updateInPortPanel();
                    }
                }
            }

            if (handler != null) {
                handler.exportAsDrag(comp, e, TransferHandler.COPY);
            }
        }
    }


    public JPopupMenu getUnitMenu(final UnitLabel unitLabel) {
        ImageLibrary imageLibrary = parentPanel.getLibrary();
        final Unit tempUnit = unitLabel.getUnit();
        JPopupMenu menu = new JPopupMenu("Unit");
        ImageIcon unitIcon = imageLibrary.getUnitImageIcon(tempUnit, 0.66);

        JMenuItem name = new JMenuItem(Messages.message(tempUnit.getLabel()) + " (" +
                                       Messages.message("menuBar.colopedia") + ")",
                                       unitIcon);
        name.setActionCommand(UnitAction.COLOPEDIA.toString());
        name.addActionListener(unitLabel);
        menu.add(name);
        menu.addSeparator();

        if (tempUnit.isCarrier()) {
            if (addCarrierItems(unitLabel, menu)) {
                menu.addSeparator();
            }
        }

        if (tempUnit.getLocation().getTile() != null &&
            tempUnit.getLocation().getTile().getColony() != null) {
            if (addWorkItems(unitLabel, menu)) {
                menu.addSeparator();
            }
            if (addEducationItems(unitLabel, menu)) {
                menu.addSeparator();
            }
            if (tempUnit.getLocation() instanceof WorkLocation) {
                if (tempUnit.getColony().canReducePopulation()) {
                    JMenuItem menuItem = new JMenuItem(Messages.message("leaveTown"));
                    menuItem.setActionCommand(UnitAction.LEAVE_TOWN.toString());
                    menuItem.addActionListener(unitLabel);
                    menu.add(menuItem);
                }
            } else {
                if (addCommandItems(unitLabel, menu)) {
                    menu.addSeparator();
                }
            }
        } else if (tempUnit.getLocation() instanceof Europe) {
            if (addCommandItems(unitLabel, menu)) {
                menu.addSeparator();
            }
        }

        if (tempUnit.hasAbility(Ability.CAN_BE_EQUIPPED)) {
            if (addEquipmentItems(unitLabel, menu)) {
                menu.addSeparator();
            }
        }

        return menu;
    }

    private boolean addCarrierItems(final UnitLabel unitLabel, final JPopupMenu menu) {
        final Unit tempUnit = unitLabel.getUnit();

        if (tempUnit.getSpaceLeft() < tempUnit.getType().getSpace()) {
            JMenuItem cargo = new JMenuItem(Messages.message("cargoOnCarrier"));
            menu.add(cargo);

            for (Unit passenger : tempUnit.getUnitList()) {
                JMenuItem menuItem = new JMenuItem("    " + Messages.message(passenger.getLabel()));
                menuItem.setFont(menuItem.getFont().deriveFont(Font.ITALIC));
                menu.add(menuItem);
            }
            for (Goods goods : tempUnit.getGoodsList()) {
                JMenuItem menuItem = new JMenuItem("    " + Messages.message(goods.getLabel(true)));
                menuItem.setFont(menuItem.getFont().deriveFont(Font.ITALIC));
                menu.add(menuItem);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean addWorkItems(final UnitLabel unitLabel, final JPopupMenu menu) {

        final Unit tempUnit = unitLabel.getUnit();
        ImageLibrary imageLibrary = parentPanel.getLibrary();
        Colony colony = tempUnit.getLocation().getColony();
        boolean separatorNeeded = false;
        ColonyTile also = (tempUnit.getWorkLocation2() instanceof ColonyTile)
            ? (ColonyTile) tempUnit.getWorkLocation2()
            : null;

        List<GoodsType> farmedGoods = freeColClient.getGame().getSpecification().getFarmedGoodsTypeList();
        // Work in Field - automatically find the best location
        for (GoodsType goodsType : farmedGoods) {
            ColonyTile bestTile = colony.getVacantColonyTileFor(tempUnit, false, goodsType);
            if (also != null) {
                int alsoProd = also.getProductionOf(tempUnit, goodsType);
                if (bestTile == null) {
                    if (alsoProd > 0) bestTile = also;
                } else {
                    if (alsoProd > bestTile.getProductionOf(tempUnit, goodsType))
                        bestTile = also;
                }
            }
            if (bestTile != null) {
                int maxpotential = bestTile.getProductionOf(tempUnit, goodsType);
                String text = Messages.message(StringTemplate.template(goodsType.getId() + ".workAs")
                                               .addAmount("%amount%", maxpotential));
                JMenuItem menuItem = new JMenuItem(text,
                                                   imageLibrary.getScaledGoodsImageIcon(goodsType, 0.66f));
                menuItem.setActionCommand(UnitAction.WORK_TILE.toString() + ":" + goodsType.getId());
                menuItem.addActionListener(unitLabel);
                menu.add(menuItem);
                separatorNeeded = true;
            }
        }

        // Work at Building - show both max potential and realistic projection
        for (Building building : colony.getBuildings()) {
            switch (building.getNoAddReason(tempUnit)) {
            case NONE: case ALREADY_PRESENT:
                GoodsType goodsType = building.getGoodsOutputType();
                String locName = Messages.message(building.getNameKey());
                JMenuItem menuItem = new JMenuItem(locName);
                if (goodsType != null) {
                    menuItem.setIcon(imageLibrary.getScaledGoodsImageIcon(goodsType, 0.66f));
                    StringTemplate t = StringTemplate.template("model.goods.goodsAmount")
                        .addAmount("%amount%", building.getAdditionalProductionNextTurn(tempUnit))
                        .addName("%goods%", goodsType);
                    locName += " (" + Messages.message(t) +")";
                    menuItem.setText(locName);
                }
                menuItem.setActionCommand(UnitAction.WORK_BUILDING.toString() + ":" +
                    building.getType().getId());
                menuItem.addActionListener(unitLabel);
                menu.add(menuItem);
                separatorNeeded = true;
                break;
            default:
                break;
            }
        }

        if (tempUnit.getWorkTile() != null) {
            JMenuItem menuItem = new JMenuItem(Messages.message("showProduction"));
            menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        gui.showWorkProductionPanel(tempUnit);
                    }
                });
            menu.add(menuItem);
            separatorNeeded = true;
        } else if (tempUnit.getWorkLocation() != null) {
            JMenuItem menuItem = new JMenuItem(Messages.message("showProductivity"));
            menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        gui.showWorkProductionPanel(tempUnit);
                    }
                });
            menu.add(menuItem);
            separatorNeeded = true;
        }

        return separatorNeeded;
    }

    private boolean addEducationItems(final UnitLabel unitLabel, final JPopupMenu menu) {
        boolean separatorNeeded = false;
        Unit unit = unitLabel.getUnit();
        ImageLibrary imageLibrary = parentPanel.getLibrary();

        if (unit.getSpecification().getBoolean(GameOptions.ALLOW_STUDENT_SELECTION)) {
            for (Unit teacher : unit.getColony().getTeachers()) {
                if (unit.canBeStudent(teacher) && (unit.getLocation() instanceof WorkLocation)) {
                    JMenuItem menuItem = null;
                    ImageIcon teacherIcon = imageLibrary.getUnitImageIcon(teacher, 0.5);
                    if (teacher.getStudent() != unit) {
                        menuItem = new JMenuItem(Messages.message("assignToTeacher"), teacherIcon);
                        menuItem.setActionCommand(UnitAction.ASSIGN.toString() + ":" + teacher.getId());
                        menuItem.addActionListener(unitLabel);
                    } else {
                        String teacherName = Messages.message(teacher.getType().getNameKey());
                        menuItem = new JMenuItem(Messages.message(StringTemplate.template("menu.unit.apprentice")
                                                                  .addName("%unit%", teacherName)),
                                                 teacherIcon);
                        menuItem.setEnabled(false);
                    }
                    menu.add(menuItem);
                    separatorNeeded = true;
                }
            }
        }

        if ((unit.getTurnsOfTraining() > 0) && (unit.getStudent() != null)) {
            JMenuItem menuItem = new JMenuItem(Messages.message("menuBar.teacher") +
                                               ": " + unit.getTurnsOfTraining() +
                                               "/" + unit.getNeededTurnsOfTraining());
            menuItem.setEnabled(false);
            menu.add(menuItem);
            separatorNeeded = true;
        }

        int experience = unit.getExperience();
        GoodsType goods = unit.getExperienceType();
        if (experience > 0 && goods != null) {
            UnitType expertType = freeColClient.getGame().getSpecification().getExpertForProducing(goods);
            if (unit.getType().canBeUpgraded(expertType, ChangeType.EXPERIENCE)) {
                int maxExperience = unit.getType().getMaximumExperience();
                double probability = unit.getType().getUnitTypeChange(expertType)
                    .getProbability(ChangeType.EXPERIENCE) * experience / (double) maxExperience;
                String jobName = Messages.message(goods.getWorkingAsKey());
                ImageIcon expertIcon = imageLibrary.getUnitImageIcon(expertType, 0.5);
                JMenuItem experienceItem = new JMenuItem(Messages.message(StringTemplate.template("menu.unit.experience")
                                                                          .addName("%job%", jobName))
                                                         + " " + experience + "/" + maxExperience + " ("
                                                         + FreeColPanel.getModifierFormat().format(probability) + "%)",
                                                         expertIcon);
                experienceItem.setEnabled(false);
                menu.add(experienceItem);
                separatorNeeded = true;
            }
        }

        return separatorNeeded;
    }


    private boolean addCommandItems(final UnitLabel unitLabel, final JPopupMenu menu) {
        final Unit tempUnit = unitLabel.getUnit();
        final boolean isUnitAtSea = tempUnit.isAtSea();

        JMenuItem menuItem = new JMenuItem(Messages.message("activateUnit"));
        menuItem.setActionCommand(UnitAction.ACTIVATE_UNIT.toString());
        menuItem.addActionListener(unitLabel);
        menuItem.setEnabled(tempUnit.getState() != Unit.UnitState.ACTIVE
                                && !isUnitAtSea);
        menu.add(menuItem);

        if (!(tempUnit.getLocation() instanceof Europe)) {
            menuItem = new JMenuItem(Messages.message("fortifyUnit"));
            menuItem.setActionCommand(UnitAction.FORTIFY.toString());
            menuItem.addActionListener(unitLabel);
            menuItem.setEnabled((tempUnit.getMovesLeft() > 0)
                && !(tempUnit.getState() == Unit.UnitState.FORTIFIED
                    || tempUnit.getState() == Unit.UnitState.FORTIFYING));
            menu.add(menuItem);
        }

        UnitState unitState = tempUnit.getState();
        menuItem = new JMenuItem(Messages.message("sentryUnit"));
        menuItem.setActionCommand(UnitAction.SENTRY.toString());
        menuItem.addActionListener(unitLabel);
        menuItem.setEnabled(unitState != Unit.UnitState.SENTRY
            && !isUnitAtSea);
        menu.add(menuItem);

        boolean hasTradeRoute = tempUnit.getTradeRoute() != null;
        menuItem = new JMenuItem(Messages.message("clearUnitOrders"));
        menuItem.setActionCommand(UnitAction.CLEAR_ORDERS.toString());
        menuItem.addActionListener(unitLabel);
        menuItem.setEnabled((unitState != Unit.UnitState.ACTIVE
                || hasTradeRoute)
            && !isUnitAtSea);
        menu.add(menuItem);

        menuItem = new JMenuItem(Messages.message("assignTradeRoute"));
        menuItem.setActionCommand(UnitAction.ASSIGN_TRADE_ROUTE.toString());
        menuItem.addActionListener(unitLabel);
        menuItem.setEnabled(tempUnit.isCarrier() && !hasTradeRoute);
        menu.add(menuItem);

        if (tempUnit.canCarryTreasure() && tempUnit.canCashInTreasureTrain()) {
            menuItem = new JMenuItem(Messages.message("cashInTreasureTrain.order"));
            menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        freeColClient.getInGameController()
                            .checkCashInTreasureTrain(tempUnit);
                    }
                });
            menu.add(menuItem);
        }
        return true;
    }


    private boolean addEquipmentItems(final UnitLabel unitLabel, final JPopupMenu menu) {
        final Unit tempUnit = unitLabel.getUnit();
        final InGameController igc = freeColClient
            .getInGameController();
        ImageLibrary imageLibrary = parentPanel.getLibrary();
        boolean separatorNeeded = false;
        if (tempUnit.getEquipment().size() > 1) {
            JMenuItem newItem = new JMenuItem(Messages.message("model.equipment.removeAll"));
            newItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Map<EquipmentType, Integer> equipment =
                            new HashMap<EquipmentType, Integer>(tempUnit.getEquipment().getValues());
                        for (Map.Entry<EquipmentType, Integer> entry: equipment.entrySet()) {
                            igc.equipUnit(tempUnit, entry.getKey(), -entry.getValue());
                        }
                        unitLabel.updateIcon();
                    }
                });
            menu.add(newItem);
        }

        EquipmentType horses = null;
        EquipmentType muskets = null;
        for (EquipmentType equipmentType : freeColClient.getGame().getSpecification().getEquipmentTypeList()) {
            int count = tempUnit.getEquipment().getCount(equipmentType);
            if (count > 0) {
                // "remove current equipment" action
                JMenuItem newItem = new JMenuItem(Messages.message(equipmentType.getId() + ".remove"));
                if (!equipmentType.getGoodsRequired().isEmpty()) {
                    GoodsType goodsType = equipmentType.getGoodsRequired().get(0).getType();
                    newItem.setIcon(imageLibrary.getScaledGoodsImageIcon(goodsType, 0.66f));
                }
                final int items = count;
                final EquipmentType type = equipmentType;
                newItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            igc.equipUnit(tempUnit, type, -items);
                            unitLabel.updateIcon();
                        }
                    });
                menu.add(newItem);
            }
            if (tempUnit.canBeEquippedWith(equipmentType) && count == 0) {
                // "add new equipment" action
                JMenuItem newItem = null;
                count = equipmentType.getMaximumCount() - count;
                if (equipmentType.getGoodsRequired().isEmpty()) {
                    newItem = new JMenuItem();
                    newItem.setText(Messages.message(equipmentType.getId() + ".add"));
                } else if (tempUnit.isInEurope() &&
                           tempUnit.getOwner().getEurope().canBuildEquipment(equipmentType)) {
                    int price = 0;
                    newItem = new JMenuItem();
                    for (AbstractGoods goodsRequired : equipmentType.getGoodsRequired()) {
                        price += tempUnit.getOwner().getMarket().getBidPrice(goodsRequired.getType(),
                                                                             goodsRequired.getAmount());
                        newItem.setIcon(imageLibrary.getScaledGoodsImageIcon(goodsRequired.getType(), 0.66f));
                    }
                    while (!tempUnit.getOwner().checkGold(count * price)) {
                        count--;
                    }
                    newItem.setText(Messages.message(equipmentType.getId() + ".add") + " (" +
                                    Messages.message(StringTemplate.template("goldAmount")
                                                     .addAmount("%amount%", count * price)) +
                                    ")");
                } else if (tempUnit.getColony() != null &&
                           tempUnit.getColony().canBuildEquipment(equipmentType)) {
                    newItem = new JMenuItem();
                    for (AbstractGoods goodsRequired : equipmentType.getGoodsRequired()) {
                        int present = tempUnit.getColony().getGoodsCount(goodsRequired.getType()) /
                            goodsRequired.getAmount();
                        if (present < count) {
                            count = present;
                        }
                        newItem.setIcon(imageLibrary.getScaledGoodsImageIcon(goodsRequired.getType(), 0.66f));
                    }
                    newItem.setText(Messages.message(equipmentType.getId() + ".add"));
                }
                if (newItem != null) {
                    // for convenience menu only
                    if ("model.equipment.horses".equals(equipmentType.getId())) {
                        horses = equipmentType;
                    } else if ("model.equipment.muskets".equals(equipmentType.getId())) {
                        muskets = equipmentType;
                    }
                    final int items = count;
                    final EquipmentType type = equipmentType;
                    newItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                igc.equipUnit(tempUnit, type, items);
                                unitLabel.updateIcon();
                            }
                        });
                    menu.add(newItem);
                }
            }
        }
        
        // convenience menu for equipping dragoons
        if (horses != null && muskets != null && horses.isCompatibleWith(muskets)) {
            final EquipmentType horseType = horses;
            final EquipmentType musketType = muskets;
            JMenuItem newItem = new JMenuItem(Messages.message("model.equipment.dragoon"),
                imageLibrary.getUnitImageIcon(tempUnit.getType(), Role.DRAGOON, 1.0/3));
            newItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        igc.equipUnit(tempUnit, horseType, 1);
                        igc.equipUnit(tempUnit, musketType, 1);
                        unitLabel.updateIcon();
                    }
                });
            menu.add(newItem);
        }

        separatorNeeded = true;

        if (separatorNeeded) {
            menu.addSeparator();
            separatorNeeded = false;
        }

        UnitType newUnitType = tempUnit.getType().getTargetType(ChangeType.CLEAR_SKILL, tempUnit.getOwner());
        if (newUnitType != null) {
            JMenuItem menuItem = new JMenuItem(Messages.message("clearSpeciality"),
                imageLibrary.getUnitImageIcon(newUnitType, 1.0/3));
            menuItem.setActionCommand(UnitAction.CLEAR_SPECIALITY.toString());
            menuItem.addActionListener(unitLabel);
            menu.add(menuItem);
            if(tempUnit.getLocation() instanceof Building &&
               !((Building)tempUnit.getLocation()).canAdd(newUnitType)){
                    menuItem.setEnabled(false);
            }
            separatorNeeded = true;
        }
        return separatorNeeded;
    }


    public JPopupMenu getGoodsMenu(final GoodsLabel goodsLabel) {

        final Goods goods = goodsLabel.getGoods();
        final InGameController inGameController = freeColClient
            .getInGameController();
        ImageLibrary imageLibrary = parentPanel.getLibrary();
        JPopupMenu menu = new JPopupMenu("Cargo");
        JMenuItem name = new JMenuItem(Messages.message(goods.getNameKey()) + " (" +
                                       Messages.message("menuBar.colopedia") + ")",
                                       imageLibrary.getScaledGoodsImageIcon(goods.getType(), 0.66f));
        name.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    gui.showColopediaPanel(goods.getType().getId());
                }
            });
        menu.add(name);

        if (!(goods.getLocation() instanceof Colony)) {
            if (freeColClient.getMyPlayer().canTrade(goods)) {
                JMenuItem unload = new JMenuItem(Messages.message("unload"));
                unload.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            inGameController.unloadCargo(goods, false);
                            if (parentPanel instanceof CargoPanel) {
                                CargoPanel cargoPanel = (CargoPanel) parentPanel;
                                cargoPanel.initialize();
                                /*
                                  if (cargoPanel.getParentPanel() instanceof ColonyPanel) {
                                  ((ColonyPanel) cargoPanel.getParentPanel()).updateWarehouse();
                                  }
                                */
                            }
                            parentPanel.revalidate();
                        }
                    });
                menu.add(unload);
            } else {
                if (goods.getLocation() instanceof Unit
                    && ((Unit)goods.getLocation()).isInEurope()) {
                    JMenuItem pay = new JMenuItem(Messages.message("boycottedGoods.payArrears"));
                    pay.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                inGameController.payArrears(goods.getType());
                                if (parentPanel instanceof CargoPanel) {
                                    CargoPanel cargoPanel = (CargoPanel) parentPanel;
                                    cargoPanel.initialize();
                                }
                                parentPanel.revalidate();
                            }
                        });
                    menu.add(pay);
                }
            }
            JMenuItem dump = new JMenuItem(Messages.message("dumpCargo"));
            dump.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    inGameController.unloadCargo(goods, true);
                    if (parentPanel instanceof CargoPanel) {
                        ((CargoPanel) parentPanel).initialize();
                    }
                    parentPanel.revalidate();
                }
            });
            menu.add(dump);
        }

        return menu;
    }
}