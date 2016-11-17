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


import java.util.List;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.BorderLayout;
import org.freecolandroid.repackaged.java.awt.Dimension;
import org.freecolandroid.repackaged.java.awt.Font;
import org.freecolandroid.repackaged.java.awt.Graphics;
import org.freecolandroid.repackaged.java.awt.Graphics2D;
import org.freecolandroid.repackaged.java.awt.Image;
import org.freecolandroid.repackaged.java.awt.event.MouseAdapter;
import org.freecolandroid.repackaged.java.awt.event.MouseEvent;
import org.freecolandroid.repackaged.java.awt.image.BufferedImage;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JButton;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JPanel;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.ViewMode;
import net.sf.freecol.client.gui.action.EndTurnAction;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.client.gui.panel.MapEditorTransformPanel.MapTransform;
import net.sf.freecol.common.model.AbstractGoods;
import net.sf.freecol.common.model.EquipmentType;
import net.sf.freecol.common.model.Goods;
import net.sf.freecol.common.model.Modifier;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.resources.ResourceManager;

/**
 * This is the panel that shows more details about the currently selected unit
 * and the tile it stands on. It also shows the amount of gold the player has
 * left and stuff like that.
 */
public final class InfoPanel extends FreeColPanel {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(InfoPanel.class.getName());

    private static final int PANEL_WIDTH = 256;

    public static final int PANEL_HEIGHT = 128;

    private final EndTurnPanel endTurnPanel;

    private final UnitInfoPanel unitInfoPanel;

    private final TileInfoPanel tileInfoPanel = new TileInfoPanel();

    private final JPanel mapEditorPanel;

    private Image skin = ResourceManager.getImage("InfoPanel.skin");

    private boolean useSkin = true;


    /**
     * The constructor that will add the items to this panel.
     *
     * @param freeColClient The main controller object for the client.
     * @param gui a <code>GUI</code> value
     */
    public InfoPanel(final FreeColClient freeColClient, final GUI gui) {
        this(freeColClient, gui, true);
    }


    /**
     * The constructor that will add the items to this panel.
     *
     * @param freeColClient The main controller object for the client.
     * @param gui a <code>GUI</code> value
     * @param useSkin a <code>boolean</code> value
     */
    public InfoPanel(final FreeColClient freeColClient, final GUI gui, boolean useSkin) {
        super(freeColClient, gui);
        this.useSkin = useSkin;
        this.endTurnPanel = new EndTurnPanel(gui);

        unitInfoPanel = new UnitInfoPanel();
        setLayout(null);

        int internalPanelTop = 0;
        int internalPanelHeight = 128;
        if (useSkin && skin != null) {
            setBorder(null);
            setSize(skin.getWidth(null), skin.getHeight(null));
            setOpaque(false);
            internalPanelTop = 75;
            internalPanelHeight = 128;
        } else {
            setSize(PANEL_WIDTH, PANEL_HEIGHT);
        }

        mapEditorPanel = new JPanel(null);
        mapEditorPanel.setSize(130, 100);
        mapEditorPanel.setOpaque(false);

        add(unitInfoPanel, internalPanelTop, internalPanelHeight);
        add(endTurnPanel, internalPanelTop, internalPanelHeight);
        add(tileInfoPanel, internalPanelTop, internalPanelHeight);
        add(mapEditorPanel, internalPanelTop, internalPanelHeight);

        addMouseListener(new MouseAdapter() {
           @Override
           public void mousePressed(MouseEvent e) {
              Unit activeUnit = gui.getActiveUnit();
              if (activeUnit != null && activeUnit.getTile() != null) {
                  gui.setFocus(activeUnit.getTile());
              }
          }
            });
    }

    /**
     * Adds a panel to show information
     */
    private void add(JPanel panel, int internalPanelTop, int internalPanelHeight) {
        panel.setVisible(false);
        panel.setLocation((getWidth() - panel.getWidth()) / 2, internalPanelTop
                + (internalPanelHeight - panel.getHeight()) / 2);
        add(panel);
    }

    /**
     * Updates this <code>InfoPanel</code>.
     *
     * @param unit The displayed unit (or null if none)
     */
    public void update(Unit unit) {
        unitInfoPanel.update(unit);
    }

    /**
     * Updates this <code>InfoPanel</code>.
     *
     * @param mapTransform The current MapTransform.
     */
    public void update(MapTransform mapTransform) {
        if (mapTransform != null) {
            final JPanel p = mapTransform.getDescriptionPanel();
            if (p != null) {
                p.setOpaque(false);
                final Dimension d = p.getPreferredSize();
                p.setBounds(0, (mapEditorPanel.getHeight() - d.height)/2, mapEditorPanel.getWidth(), d.height);
                mapEditorPanel.removeAll();
                mapEditorPanel.add(p, BorderLayout.CENTER);
                mapEditorPanel.validate();
                mapEditorPanel.revalidate();
                mapEditorPanel.repaint();
            }
        }
    }


    /**
     * Updates this <code>InfoPanel</code>.
     *
     * @param tile The displayed tile (or null if none)
     */
    public void update(Tile tile) {
        tileInfoPanel.update(tile);
    }

    /**
     * Gets the <code>Unit</code> in which this <code>InfoPanel</code> is
     * displaying information about.
     *
     * @return The <code>Unit</code> or <i>null</i> if no <code>Unit</code>
     *         applies.
     */
    public Unit getUnit() {
        return unitInfoPanel.getUnit();
    }

    /**
     * Gets the <code>Tile</code> in which this <code>InfoPanel</code> is
     * displaying information about.
     *
     * @return The <code>Tile</code> or <i>null</i> if no <code>Tile</code>
     *         applies.
     */
    public Tile getTile() {
        return tileInfoPanel.getTile();
    }

    /**
     * Paints this component.
     *
     * @param graphics The Graphics context in which to draw this component.
     */
    @Override
    public void paintComponent(Graphics graphics) {
        int viewMode = getGUI().getCurrentViewMode();
        mapEditorPanel.setVisible(false);
        unitInfoPanel.setVisible(false);
        endTurnPanel.setVisible(false);
        tileInfoPanel.setVisible(false);
        if (getFreeColClient().isMapEditor()) {
            mapEditorPanel.setVisible(true);
        } else if (viewMode == ViewMode.VIEW_TERRAIN_MODE) {
            tileInfoPanel.setVisible(true);
        } else if (unitInfoPanel.getUnit() != null) {
            unitInfoPanel.setVisible(true);
        } else if (getMyPlayer() != null
                   && !getMyPlayer().hasNextActiveUnit()) {
            endTurnPanel.setVisible(true);
        }

        if (useSkin && skin != null) {
            graphics.drawImage(skin, 0, 0, null);
        }

        super.paintComponent(graphics);
    }


    /**
     * Panel for displaying <code>Tile</code>-information.
     */
    public class TileInfoPanel extends JPanel {

        private Tile tile;
        private Font font = new JLabel().getFont().deriveFont(9f);

        public TileInfoPanel() {
            super(null);

            setSize(226, 128);
            setOpaque(false);
            setLayout(new MigLayout("fill, wrap 5, gap 2 2"));
        }

        /**
         * Updates this <code>InfoPanel</code>.
         *
         * @param tile The displayed tile (or null if none)
         */
        public void update(Tile tile) {

            this.tile = tile;

            removeAll();

            if (tile != null) {
                int width = getLibrary().getTerrainImageWidth(tile.getType());
                int height = getLibrary().getTerrainImageHeight(tile.getType());
                int compoundHeight = getLibrary().getCompoundTerrainImageHeight(tile.getType());
                BufferedImage image = new BufferedImage(width, compoundHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = image.createGraphics();
                g.translate(0, compoundHeight - height);
                getGUI().getMapViewer().displayTerrain(g, tile);
                if (tile.isExplored()) {
                    StringTemplate items = StringTemplate.label(", ");
                    items.add(tile.getNameKey());
                    for (TileImprovement tileImprovement : tile.getCompletedTileImprovements()) {
                        items.add(tileImprovement.getType().getDescriptionKey());
                    }
                    add(new JLabel(Messages.message(items)), "span, align center");

                    add(new JLabel(new ImageIcon(image)), "spany");
                    if (tile.getOwner() != null) {
                        JLabel ownerLabel = localizedLabel(tile.getOwner().getNationName());
                        ownerLabel.setFont(font);
                        add(ownerLabel, "span 4");
                    }

                    int defenceBonus = (int) tile.getType().getFeatureContainer()
                        .applyModifier(100, Modifier.DEFENCE) - 100;
                    JLabel defenceLabel = new JLabel(Messages.message("colopedia.terrain.defenseBonus") +
                                                     " " + defenceBonus + "%");
                    defenceLabel.setFont(font);
                    add(defenceLabel, "span 4");
                    JLabel moveLabel = new JLabel(Messages.message("colopedia.terrain.movementCost") +
                                                  " " + String.valueOf(tile.getType().getBasicMoveCost()/3));
                    moveLabel.setFont(font);
                    add(moveLabel, "span 4");

                    List<AbstractGoods> production = tile.getType().getProduction();
                    for (AbstractGoods goods : production) {
                        JLabel goodsLabel = new JLabel(String.valueOf(tile.potential(goods.getType(), null)),
                                                       getLibrary().getScaledGoodsImageIcon(goods.getType(), 0.50f),
                                                       JLabel.RIGHT);
                        goodsLabel.setToolTipText(Messages.message(goods.getType().getNameKey()));
                        goodsLabel.setFont(font);
                        add(goodsLabel);
                    }
                } else {
                    add(new JLabel(Messages.message("unexplored")), "span, align center");
                    add(new JLabel(new ImageIcon(image)), "spany");
                }
                revalidate();
                repaint();
            }
        }

        /**
         * Gets the <code>Tile</code> in which this <code>InfoPanel</code>
         * is displaying information about.
         *
         * @return The <code>Tile</code> or <i>null</i> if no
         *         <code>Tile</code> applies.
         */
        public Tile getTile() {
            return tile;
        }
    }

    /**
     * Panel for displaying <code>Unit</code>-information.
     */
    public class UnitInfoPanel extends JPanel {

        private Unit unit;

        public UnitInfoPanel() {

            super(new MigLayout("wrap 6, fill, gap 0 0", "", ""));

            setSize(226, 100);
            setOpaque(false);
        }

        /**
         * Updates this <code>InfoPanel</code>.
         *
         * @param unit The displayed unit (or null if none)
         */
        public void update(Unit unit) {
            this.unit = unit;

            removeAll();
            if (unit != null) {
                add(new JLabel(getLibrary().getUnitImageIcon(unit)), "spany, gapafter 5px");
                String name = Messages.message(Messages.getLabel(unit));
                // TODO: this is too brittle!
                int index = name.indexOf(" (");
                if (index < 0) {
                    add(new JLabel(name), "span");
                } else {
                    add(new JLabel(name.substring(0, index)), "span");
                    add(new JLabel(name.substring(index + 1)), "span");
                }
                add(new JLabel(Messages.message("moves") + " " + unit.getMovesAsString()), "span");

                // Handle the special cases. TODO: make this more generic
                if (unit.canCarryTreasure()) {
                    add(new JLabel(unit.getTreasureAmount() + " " + Messages.message("gold")), "span");
                } else if (unit.isCarrier()) {
                    for (Goods goods : unit.getGoodsList()) {
                        JLabel goodsLabel = new JLabel(getLibrary().getScaledGoodsImageIcon(goods.getType(), 0.66f));
                        goodsLabel.setToolTipText(Messages.message(StringTemplate.template("model.goods.goodsAmount")
                                                                   .addAmount("%amount%", goods.getAmount())
                                                                   .add("%goods%", goods.getNameKey())));
                        add(goodsLabel);
                    }
                    for (Unit carriedUnit : unit.getUnitList()) {
                        ImageIcon unitIcon = getLibrary().getUnitImageIcon(carriedUnit, 0.5);
                        JLabel unitLabel = new JLabel(unitIcon);
                        unitLabel.setToolTipText(Messages.message(carriedUnit.getLabel()));
                        add(unitLabel);
                    }
                } else {
                    for (EquipmentType equipment : unit.getEquipment().keySet()) {
                        for (AbstractGoods goods : equipment.getGoodsRequired()) {
                            int amount = goods.getAmount() * unit.getEquipment().getCount(equipment);
                            JLabel equipmentLabel =
                                new JLabel(Integer.toString(amount),
                                           getLibrary().getScaledGoodsImageIcon(goods.getType(), 0.66f),
                                           JLabel.CENTER);
                            equipmentLabel
                                .setToolTipText(Messages.message(StringTemplate.template("model.goods.goodsAmount")
                                                                 .addAmount("%amount%", amount)
                                                                 .add("%goods%", goods.getNameKey())));
                            add(equipmentLabel);
                        }
                    }
                }
            }
            revalidate();
            repaint();
        }

        /**
         * Gets the <code>Unit</code> in which this <code>InfoPanel</code>
         * is displaying information about.
         *
         * @return The <code>Unit</code> or <i>null</i> if no
         *         <code>Unit</code> applies.
         */
        public Unit getUnit() {
            return unit;
        }

    }

    /**
     * Panel for ending the turn.
     */
    public class EndTurnPanel extends JPanel {

        public EndTurnPanel(GUI gui) {
            super(new MigLayout("wrap 1, center", "[center]", ""));

            String labelString = Messages.message("infoPanel.endTurnPanel.text");
            int width = getFontMetrics(getFont()).stringWidth(labelString);
            if (width > 150 ) {
                int index = Messages.getBreakingPoint(labelString);
                if (index > 0) {
                    add(new JLabel(labelString.substring(0, index)));
                    add(new JLabel(labelString.substring(index + 1)));
                } else {
                    add(new JLabel(labelString));
                }
            } else {
                add(new JLabel(labelString));
            }

            add(new JButton(getFreeColClient().getActionManager()
                    .getFreeColAction(EndTurnAction.id)));
            setOpaque(false);
            setSize(getPreferredSize());

        }
    }
}
