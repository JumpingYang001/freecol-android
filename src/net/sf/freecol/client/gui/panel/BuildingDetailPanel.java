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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.freecolandroid.repackaged.java.awt.Font;
import org.freecolandroid.repackaged.java.awt.Image;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JButton;
import org.freecolandroid.repackaged.javax.swing.JComponent;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JPanel;
import org.freecolandroid.repackaged.javax.swing.JTextPane;
import org.freecolandroid.repackaged.javax.swing.text.BadLocationException;
import org.freecolandroid.repackaged.javax.swing.text.StyleConstants;
import org.freecolandroid.repackaged.javax.swing.text.StyledDocument;
import org.freecolandroid.repackaged.javax.swing.tree.DefaultMutableTreeNode;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.action.ColopediaAction.PanelType;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.Ability;
import net.sf.freecol.common.model.AbstractGoods;
import net.sf.freecol.common.model.BuildingType;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.Modifier;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.resources.ResourceManager;


/**
 * This panel displays the Colopedia.
 */
public class BuildingDetailPanel extends ColopediaGameObjectTypePanel<BuildingType> {

    private static final Font arrowFont = ResourceManager.getFont("SimpleFont", Font.BOLD, 24f);


    /**
     * Creates a new instance of this ColopediaDetailPanel.
     * @param freeColClient 
     *
     * @param colopediaPanel the ColopediaPanel
     */
    public BuildingDetailPanel(FreeColClient freeColClient, GUI gui, ColopediaPanel colopediaPanel) {
        super(freeColClient, gui, colopediaPanel, PanelType.BUILDINGS.toString(), 0.75);
    }


    /**
     * Adds one or several subtrees for all the objects for which this
     * ColopediaDetailPanel could build a detail panel to the given
     * root node.
     *
     * @param root a <code>DefaultMutableTreeNode</code>
     */
    public void addSubTrees(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode parent =
            new DefaultMutableTreeNode(new ColopediaTreeItem(this, getId(), getName(), null));

        Image buildingImage = ResourceManager.getImage("Colopedia.buildingSection.image");
        ImageIcon buildingIcon = new ImageIcon((buildingImage != null) ? buildingImage : null);

        List<BuildingType> buildingTypes = new ArrayList<BuildingType>();
        Map<BuildingType, DefaultMutableTreeNode> buildingHash =
            new HashMap<BuildingType, DefaultMutableTreeNode>();
        for (BuildingType buildingType : getSpecification().getBuildingTypeList()) {
            if (buildingType.getUpgradesFrom() == null) {
                String name = Messages.message(buildingType.getNameKey());
                DefaultMutableTreeNode item =
                    new DefaultMutableTreeNode(new ColopediaTreeItem(this, buildingType.getId(),
                                                                     name, buildingIcon));
                buildingHash.put(buildingType, item);
                parent.add(item);
            } else {
                buildingTypes.add(buildingType);
            }
        }

        while (!buildingTypes.isEmpty()) {
            for (Iterator<BuildingType> iterator = buildingTypes.iterator(); iterator.hasNext();) {
                BuildingType buildingType = iterator.next();
                DefaultMutableTreeNode node = buildingHash.get(buildingType.getUpgradesFrom());
                if (node != null) {
                    String name = Messages.message(buildingType.getNameKey());
                    DefaultMutableTreeNode item =
                        new DefaultMutableTreeNode(new ColopediaTreeItem(this, buildingType.getId(),
                                                                         name, buildingIcon));
                    node.add(item);
                    buildingHash.put(buildingType, item);
                    iterator.remove();
                }
            }
        }
        root.add(parent);
    }

    /**
     * Builds the details panel for the BuildingType with the given ID.
     *
     * @param id the ID of a BuildingType
     * @param panel the detail panel to build
     */
    public void buildDetail(String id, JPanel panel) {
        if (getId().equals(id)) {
            return;
        }

        BuildingType buildingType = getSpecification().getBuildingType(id);
        panel.setLayout(new MigLayout("wrap 7, gapx 20", "", ""));

        JLabel name = localizedLabel(buildingType.getNameKey());
        name.setFont(smallHeaderFont);
        panel.add(name, "span, align center, wrap 40");

        // Requires - prerequisites to build
        JTextPane textPane = getDefaultTextPane();
        StyledDocument doc = textPane.getStyledDocument();

        try {
            if (buildingType.getUpgradesFrom() != null) {
                StyleConstants.setComponent(doc.getStyle("button"), getButton(buildingType.getUpgradesFrom()));
                doc.insertString(doc.getLength(), " ", doc.getStyle("button"));
                doc.insertString(doc.getLength(), "\n", doc.getStyle("regular"));
            }
            if (buildingType.getPopulationRequired() > 0) {
                StringTemplate template = StringTemplate.template("colonist")
                    .addAmount("%number%", buildingType.getPopulationRequired());
                doc.insertString(doc.getLength(),
                                 Messages.message(template) + "\n",
                                 doc.getStyle("regular"));
            }
            appendRequiredAbilities(doc, buildingType);

            panel.add(localizedLabel("colopedia.buildings.requires"), "top");
            panel.add(textPane, "span, growx");
        } catch(BadLocationException e) {
            //logger.warning(e.toString());
        }

        // Costs to build - Hammers & Tools
        panel.add(localizedLabel("colopedia.buildings.cost"));
        if (buildingType.getGoodsRequired().isEmpty()) {
            panel.add(localizedLabel("colopedia.buildings.autoBuilt"), "span");
        } else {
            AbstractGoods goodsRequired = buildingType.getGoodsRequired().get(0);
            if (buildingType.getGoodsRequired().size() > 1) {
                panel.add(getGoodsButton(goodsRequired.getType(), goodsRequired.getAmount()),
                                "span, split " + buildingType.getGoodsRequired().size());

                for (int index = 1; index < buildingType.getGoodsRequired().size(); index++) {
                    goodsRequired = buildingType.getGoodsRequired().get(index);
                    panel.add(getGoodsButton(goodsRequired.getType(), goodsRequired.getAmount()));
                }
            } else {
                panel.add(getGoodsButton(goodsRequired.getType(), goodsRequired.getAmount()), "span");
            }
        }

        // Production - Needs & Produces
        if (buildingType.hasAbility(Ability.CAN_TEACH)) {
            panel.add(localizedLabel("colopedia.buildings.teaches"), "newline, top");
            int count = 0;
            for (UnitType unitType2 : getSpecification().getUnitTypeList()) {
                if (buildingType.canAdd(unitType2)) {
                    if (count > 0 && count % 3 == 0) {
                        panel.add(getButton(unitType2), "skip, span 2");
                    } else {
                        panel.add(getButton(unitType2), "span 2");
                    }
                    count++;
                }
            }
        } else {

            GoodsType inputType = buildingType.getConsumedGoodsType();
            GoodsType outputType = buildingType.getProducedGoodsType();

            if (outputType != null) {
                panel.add(localizedLabel("colopedia.buildings.production"), "newline");
                if (inputType != null) {
                    panel.add(getGoodsButton(inputType), "span, split 3");
                    JLabel arrow = new JLabel("\u2192");
                    arrow.setFont(arrowFont);
                    panel.add(arrow);
                }
                panel.add(getGoodsButton(outputType));
            }
        }

        int workplaces = buildingType.getWorkPlaces();
        panel.add(localizedLabel("colopedia.buildings.workplaces"), "newline");
        panel.add(new JLabel(Integer.toString(workplaces)), "span");

        // Specialist
        if (workplaces > 0) {
            panel.add(localizedLabel("colopedia.buildings.specialist"), "newline");
            final UnitType unitType = getSpecification()
                .getExpertForProducing(buildingType.getProducedGoodsType());
            if (unitType == null) {
                panel.add(localizedLabel("none"), "span");
            } else {
                panel.add(getUnitButton(unitType), "span");
            }
        }

        Set<Modifier> bonusList = buildingType.getFeatureContainer().getModifiers();
        List<JComponent> labels = new ArrayList<JComponent>();
        for (Modifier productionBonus : bonusList) {
            JComponent component = getModifierComponent(productionBonus);
            if (component instanceof JButton) {
                labels.add(0, component);
            } else {
                labels.add(component);
            }
        }

        Set<Ability> abilities = buildingType.getFeatureContainer().getAbilities();
        for (Ability ability : abilities) {
            JComponent component = getAbilityComponent(ability);
            if (component != null) {
                labels.add(component);
            }
        }

        if (!labels.isEmpty()) {
            panel.add(localizedLabel(StringTemplate.template("colopedia.buildings.modifiers")
                                           .addAmount("%number%", labels.size())),
                            "newline, top");
            int count = 0;
            for (JComponent component : labels) {
                if (count > 0 && count % 2 == 0) {
                    panel.add(component, "skip, span 3");
                } else {
                    panel.add(component, "span 3");
                }
                count++;
            }
        }

        // Notes
        panel.add(localizedLabel("colopedia.buildings.notes"), "newline 20, top");
        panel.add(getDefaultTextArea(Messages.message(buildingType.getDescriptionKey()), 20),
                  "span, growx");
    }

}
