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

import org.freecolandroid.repackaged.java.awt.Font;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JToolTip;



import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.AbstractGoods;
import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.ProductionInfo;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.resources.ResourceManager;

/**
 * This panel represents a single building in a Colony.
 */
public class BuildingToolTip extends JToolTip {

    private static final JLabel arrow = new JLabel("\u2192");

    static {
        arrow.setFont(ResourceManager.getFont("SimpleFont", Font.BOLD, 24f));
    }


    private GUI gui;


    /**
     * Creates this BuildingToolTip.
     *
     * @param freeColClient
     * @param building The building to display information from.
     * @param parent a <code>Canvas</code> value
     */
    public BuildingToolTip(FreeColClient freeColClient, Building building, GUI gui) {
        this.gui = gui;

        int workplaces = building.getUnitCapacity();

        String columns = "[align center]";
        for (int index = 0; index < workplaces; index++) {
            columns += "20[]5[]";
        }

        MigLayout layout = new MigLayout("fill, insets 20, wrap " + (2 * workplaces + 1),
                                         columns, "[][][align bottom]");
        setLayout(layout);

        JLabel buildingName = new JLabel(Messages.message(building.getNameKey()));
        buildingName.setFont(ResourceManager.getFont("SimpleFont", Font.BOLD, 16f));
        add(buildingName, "span");

        ProductionInfo info = building.getProductionInfo();
        if (info == null || info.getProduction().isEmpty()) {
            add(new JLabel(), "span");
        } else {
            AbstractGoods production = info.getProduction().get(0);
            AbstractGoods maximumProduction = info.getMaximumProduction().isEmpty()
                ? production : info.getMaximumProduction().get(0);
            ProductionLabel productionOutput = new ProductionLabel(freeColClient, gui, production, maximumProduction);
            if (info.getConsumption().isEmpty()) {
                add(productionOutput, "span");
            } else {
                AbstractGoods consumption = info.getConsumption().get(0);
                if (consumption.getAmount() > 0) {
                    AbstractGoods maximumConsumption = info.getMaximumConsumption().isEmpty()
                        ? consumption: info.getMaximumConsumption().get(0);
                    ProductionLabel productionInput = new ProductionLabel(freeColClient, gui, consumption, maximumConsumption);
                    add(productionInput, "span, split 3");
                    add(arrow);
                    add(productionOutput);
                } else {
                    add(new JLabel(gui.getImageLibrary().getGoodsImageIcon(consumption.getType())),
                        "span, split 3");
                    add(arrow);
                    add(new JLabel(gui.getImageLibrary().getGoodsImageIcon(production.getType())));
                }
            }
        }

        add(new JLabel(new ImageIcon(ResourceManager.getImage(building.getType().getId() + ".image"))));

        for (Unit unit : building.getUnitList()) {
            UnitLabel unitLabel = new UnitLabel(freeColClient, unit, gui, false);
            if (building.canTeach() && unit.getStudent() != null) {
                JLabel progress = new JLabel(unit.getTurnsOfTraining() + "/" +
                                             unit.getNeededTurnsOfTraining());
                UnitLabel studentLabel = new UnitLabel(freeColClient, unit.getStudent(), gui, true);
                studentLabel.setIgnoreLocation(true);
                add(unitLabel);
                add(progress, "split 2, flowy");
                add(studentLabel);
            } else  {
                add(unitLabel, "span 2");
            }
        }

        int diff = building.getUnitCapacity() - building.getUnitCount();
        for (int index = 0; index < diff; index++) {
            add(new JLabel(new ImageIcon(ResourceManager.getImage("placeholder.image"))), "span 2");
        }

        GoodsType output = building.getGoodsOutputType();
        int breedingNumber = (output == null) ? GoodsType.INFINITY
            : output.getBreedingNumber();
        if (breedingNumber < GoodsType.INFINITY
            && breedingNumber > building.getColony().getGoodsCount(output)) {
            StringTemplate t = StringTemplate.template("buildingToolTip.breeding")
                .addAmount("%number%", breedingNumber)
                .add("%goods%", output.getNameKey());
            add(new JLabel(Messages.message(t)));
        }

        setPreferredSize(layout.preferredLayoutSize(this));

    }

}


