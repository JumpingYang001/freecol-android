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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Dimension;
import org.freecolandroid.repackaged.java.awt.Image;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.java.awt.event.ItemEvent;
import org.freecolandroid.repackaged.java.awt.event.ItemListener;
import org.freecolandroid.repackaged.java.awt.event.MouseAdapter;
import org.freecolandroid.repackaged.java.awt.event.MouseEvent;
import org.freecolandroid.repackaged.java.awt.event.MouseListener;
import org.freecolandroid.repackaged.javax.swing.AbstractAction;
import org.freecolandroid.repackaged.javax.swing.Action;
import org.freecolandroid.repackaged.javax.swing.DefaultListModel;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JCheckBox;
import org.freecolandroid.repackaged.javax.swing.JComboBox;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JList;
import org.freecolandroid.repackaged.javax.swing.JScrollPane;
import org.freecolandroid.repackaged.javax.swing.KeyStroke;
import org.freecolandroid.repackaged.javax.swing.event.ChangeEvent;
import org.freecolandroid.repackaged.javax.swing.event.ChangeListener;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.ImageLibrary;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.client.gui.plaf.FreeColComboBoxRenderer;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Goods;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Location;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Market;
import net.sf.freecol.common.model.PathNode;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.UnitTypeChange.ChangeType;
import net.sf.freecol.common.model.pathfinding.CostDeciders;
import net.sf.freecol.common.model.pathfinding.GoalDecider;
import net.sf.freecol.common.util.Utils;


/**
 * Centers the map on a known settlement or colony.
 */
public final class SelectDestinationDialog extends FreeColDialog<Location>
    implements ActionListener, ChangeListener, ItemListener {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SelectDestinationDialog.class.getName());

    private static boolean showOnlyMyColonies = true;

    private static Comparator<Destination> destinationComparator = null;

    private final JCheckBox onlyMyColoniesBox;

    private final JComboBox comparatorBox;

    private final JList destinationList;

    private final List<Destination> destinations = new ArrayList<Destination>();


    /**
     * The constructor to use.
     * @param freeColClient 
     */
    public SelectDestinationDialog(FreeColClient freeColClient, GUI gui, Unit unit) {
        super(freeColClient, gui);

        // Collect the goods the unit is carrying.
        final List<GoodsType> goodsTypes = new ArrayList<GoodsType>();
        for (Goods goods : unit.getGoodsList()) {
            if (!goodsTypes.contains(goods.getType())) {
                goodsTypes.add(goods.getType());
            }
        }

        destinations.clear();
        if (unit.isInEurope()) {
            collectDestinationsFromEurope(unit, goodsTypes);
        } else {
            collectDestinationsFromAmerica(unit, goodsTypes);
        }

        MigLayout layout = new MigLayout("wrap 1, fill", "[align center]", "");
        setLayout(layout);

        JLabel header = new JLabel(Messages.message("selectDestination.text"));
        header.setFont(smallHeaderFont);
        add(header);

        DefaultListModel model = new DefaultListModel();
        destinationList = new JList(model);
        filterDestinations();

        destinationList.setCellRenderer(new LocationRenderer());
        destinationList.setFixedCellHeight(48);

        Action selectAction = new AbstractAction(Messages.message("ok")) {
                public void actionPerformed(ActionEvent e) {
                    Destination d = (Destination) destinationList.getSelectedValue();
                    if (d != null) {
                        setResponse((Location) d.location);
                    }
                    getGUI().removeFromCanvas(SelectDestinationDialog.this);
                }
            };

        Action quitAction = new AbstractAction(Messages.message("selectDestination.cancel")) {
                public void actionPerformed(ActionEvent e) {
                    getGUI().removeFromCanvas(SelectDestinationDialog.this);
                    setResponse(null);
                }
            };

        destinationList.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "select");
        destinationList.getActionMap().put("select", selectAction);
        destinationList.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "quit");
        destinationList.getActionMap().put("quit", quitAction);

        MouseListener mouseListener = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        Destination d = (Destination) destinationList.getSelectedValue();
                        if (d != null) {
                            setResponse((Location) d.location);
                        }
                        getGUI().removeFromCanvas(SelectDestinationDialog.this);
                    }
                }
            };
        destinationList.addMouseListener(mouseListener);

        JScrollPane listScroller = new JScrollPane(destinationList);
        listScroller.setPreferredSize(new Dimension(250, 250));

        add(listScroller, "newline 30, growx, growy");

        onlyMyColoniesBox = new JCheckBox(Messages.message("selectDestination.onlyMyColonies"),
                                          showOnlyMyColonies);
        onlyMyColoniesBox.addChangeListener(this);
        add(onlyMyColoniesBox, "left");

        comparatorBox = new JComboBox(new String[] {
                Messages.message("selectDestination.sortByOwner"),
                Messages.message("selectDestination.sortByName"),
                Messages.message("selectDestination.sortByDistance")
            });
        comparatorBox.addItemListener(this);
        if (destinationComparator instanceof DestinationComparator) {
            comparatorBox.setSelectedIndex(0);
        } else if (destinationComparator instanceof NameComparator) {
            comparatorBox.setSelectedIndex(1);
        } else if (destinationComparator instanceof DistanceComparator) {
            comparatorBox.setSelectedIndex(2);
        }
        add(comparatorBox, "left");

        cancelButton.setAction(quitAction);
        okButton.setAction(selectAction);

        add(okButton, "newline 30, split 2, tag ok");
        add(cancelButton, "tag cancel");

        setSize(getPreferredSize());
    }

    private void collectDestinationsFromEurope(Unit unit,
                                               List<GoodsType> goodsTypes) {
        Game game = getGame();
        Map map = game.getMap();
        int sailTurns = unit.getSailTurns();
        for (Player p : game.getPlayers()) {
            for (Settlement s : p.getSettlements()) {
                if (!s.isConnected()) continue;
                PathNode path = unit.findPathToEurope(s.getTile());
                if (path != null) {
                    String extras = (s.getOwner() != unit.getOwner())
                        ? getExtras(unit, s, goodsTypes) : "";
                    destinations.add(new Destination(s,
                            sailTurns + path.getTurns(), extras));
                }
            }
        }

        if (destinationComparator == null) {
            destinationComparator = new DestinationComparator(getMyPlayer());
        }
        Collections.sort(destinations, destinationComparator);

        destinations.add(0, new Destination(map, sailTurns, ""));
    }

    private void collectDestinationsFromAmerica(Unit unit,
        final List<GoodsType> goodsTypes) {
        final Settlement inSettlement = unit.getSettlement();

        unit.search(unit.getTile(), new GoalDecider() {
                public PathNode getGoal() {
                    return null;
                }

                public boolean check(Unit u, PathNode p) {
                    Settlement settlement = p.getTile().getSettlement();
                    if (settlement != null && settlement != inSettlement) {
                        String extras = (settlement.getOwner() != u.getOwner())
                            ? getExtras(u, settlement, goodsTypes) : "";
                        destinations.add(new Destination(settlement, p.getTurns(), extras));
                    }
                    return false;
                }

                public boolean hasSubGoals() {
                    return false;
                }
            }, CostDeciders.avoidIllegal(), Integer.MAX_VALUE, null);

        if (destinationComparator == null) {
            destinationComparator = new DestinationComparator(getMyPlayer());
        }
        Collections.sort(destinations, destinationComparator);

        if (unit.isNaval() && unit.getOwner().canMoveToEurope()) {
            PathNode path = unit.findPathToEurope();
            int turns = (path != null) ? unit.getSailTurns()
                + path.getTotalTurns()
                : (unit.getTile() != null
                    && unit.getTile().canMoveToEurope()) ? unit.getSailTurns()
                : -1;
            if (turns >= 0) {
                Europe europe = getMyPlayer().getEurope();
                destinations.add(0,
                    new Destination(europe, turns,
                        getExtras(unit, europe, goodsTypes)));
            }
        }
    }

    @Override
    public void requestFocus() {
        destinationList.requestFocus();
    }

    public void stateChanged(ChangeEvent event) {
        showOnlyMyColonies = onlyMyColoniesBox.isSelected();
        filterDestinations();
    }

    public void itemStateChanged(ItemEvent event) {
        switch(comparatorBox.getSelectedIndex()) {
        case 0:
        default:
            destinationComparator = new DestinationComparator(getMyPlayer());
            break;
        case 1:
            destinationComparator = new NameComparator();
            break;
        case 2:
            destinationComparator = new DistanceComparator();
            break;
        }
        Collections.sort(destinations, destinationComparator);
        filterDestinations();
    }

    /**
     * Collected extra annotations of interest to a unit proposing to
     * visit a location.
     *
     * @param unit The <code>Unit</code> proposing to visit.
     * @param loc The <code>Location</code> to visit.
     * @param goodsTypes A list of goods types the unit is carrying.
     * @return A string containing interesting annotations about the visit
     *         or an empty string if nothing is of interest.
     */
    private String getExtras(Unit unit, Location loc, List<GoodsType> goodsTypes) {
        if (loc instanceof Europe && !goodsTypes.isEmpty()) {
            Market market = unit.getOwner().getMarket();
            List<String> sales = new ArrayList<String>();
            for (GoodsType goodsType : goodsTypes) {
                sales.add(Messages.message(goodsType.getNameKey()) + " "
                          + Integer.toString(market.getSalePrice(goodsType, 1)));
            }
            if (!sales.isEmpty()) {
                return "[" + Utils.join(", ", sales) + "]";
            }
        } else if (loc instanceof Settlement
            && ((Settlement)loc).getOwner().atWarWith(unit.getOwner())) {
            return "[" + Messages.message("model.stance.war") + "]";
        } else if (loc instanceof Settlement && !goodsTypes.isEmpty()) {
            List<String> sales = new ArrayList<String>();
            for (GoodsType goodsType : goodsTypes) {
                String sale = unit.getOwner().getLastSaleString((Settlement) loc, goodsType);
                if (sale != null) {
                    sales.add(Messages.message(goodsType.getNameKey())
                              + " " + sale);
                }
            }
            if (!sales.isEmpty()) {
                return "[" + Utils.join(", ", sales) + "]";
            }
        } else if (loc instanceof IndianSettlement) {
            IndianSettlement indianSettlement = (IndianSettlement) loc;
            UnitType skill = indianSettlement.getLearnableSkill();
            if (skill != null
                && unit.getType().canBeUpgraded(skill, ChangeType.NATIVES)) {
                return "[" + Messages.message(skill.getNameKey()) + "]";
            }
        }
        return "";
    }

    private void filterDestinations() {
        DefaultListModel model = (DefaultListModel) destinationList.getModel();
        Object selected = destinationList.getSelectedValue();
        model.clear();
        for (Destination d : destinations) {
            if (showOnlyMyColonies) {
                if (d.location instanceof Europe
                    || d.location instanceof Map
                    || (d.location instanceof Colony
                        && ((Colony) d.location).getOwner() == getMyPlayer())) {
                    model.addElement(d);
                }
            } else {
                model.addElement(d);
            }
        }
        destinationList.setSelectedValue(selected, true);
        if (destinationList.getSelectedIndex() == -1) {
            destinationList.setSelectedIndex(0);
        }
    }

    public int compareNames(Location dest1, Location dest2) {
        Player player = getMyPlayer();
        String name1 = "";
        if (dest1 instanceof Settlement) {
            name1 = ((Settlement) dest1).getNameFor(player);
        } else if (dest1 instanceof Europe || dest1 instanceof Map) {
            return -1;
        }
        String name2 = "";
        if (dest2 instanceof Settlement) {
            name2 = ((Settlement) dest2).getNameFor(player);
        } else if (dest2 instanceof Europe || dest2 instanceof Map) {
            return 1;
        }
        return name1.compareTo(name2);
    }

    private class Destination {
        public Location location;
        public int turns;
        public String extras;

        public Destination(Location location, int turns, String extras) {
            this.location = location;
            this.turns = turns;
            this.extras = extras;
        }
    }

    private class LocationRenderer extends FreeColComboBoxRenderer {

        @Override
        public void setLabelValues(JLabel label, Object value) {

            Destination d = (Destination) value;
            Location location = d.location;
            Player player = getMyPlayer();
            String name = "";
            ImageLibrary lib = getLibrary();
            if (location instanceof Europe) {
                Europe europe = (Europe) location;
                name = Messages.message(europe.getNameKey());
                label.setIcon(new ImageIcon(lib.getCoatOfArmsImage(europe.getOwner().getNation())
                        .getScaledInstance(-1, 48, Image.SCALE_SMOOTH)));
            } else if (location instanceof Map) {
                name = Messages.message(location.getLocationNameFor(player));
                label.setIcon(lib.getMiscImageIcon(ImageLibrary.LOST_CITY_RUMOUR));
            } else if (location instanceof Settlement) {
                Settlement settlement = (Settlement) location;
                name = Messages.message(settlement.getNameFor(player));
                label.setIcon(new ImageIcon(lib.getSettlementImage(settlement)
                        .getScaledInstance(64, -1, Image.SCALE_SMOOTH)));
            }
            label.setText(Messages.message(StringTemplate.template("selectDestination.destinationTurns")
                                           .addName("%location%", name)
                                           .addAmount("%turns%", d.turns)
                                           .addName("%extras%", d.extras)));
        }
    }

    private class DestinationComparator implements Comparator<Destination> {

        private Player owner;

        public DestinationComparator(Player player) {
            this.owner = player;
        }

        public int compare(Destination choice1, Destination choice2) {
            Location dest1 = choice1.location;
            Location dest2 = choice2.location;

            int score1 = 100;
            if (dest1 instanceof Europe || dest1 instanceof Map) {
                score1 = 10;
            } else if (dest1 instanceof Colony) {
                if (((Colony) dest1).getOwner() == owner) {
                    score1 = 20;
                } else {
                    score1 = 30;
                }
            } else if (dest1 instanceof IndianSettlement) {
                score1 = 40;
            }
            int score2 = 100;
            if (dest2 instanceof Europe || dest2 instanceof Map) {
                score2 = 10;
            } else if (dest2 instanceof Colony) {
                if (((Colony) dest2).getOwner() == owner) {
                    score2 = 20;
                } else {
                    score2 = 30;
                }
            } else if (dest2 instanceof IndianSettlement) {
                score2 = 40;
            }

            if (score1 == score2) {
                return compareNames(dest1, dest2);
            } else {
                return score1 - score2;
            }
        }
    }

    private class NameComparator implements Comparator<Destination> {
        public int compare(Destination choice1, Destination choice2) {
            return compareNames(choice1.location, choice2.location);
        }
    }

    private class DistanceComparator implements Comparator<Destination> {
        public int compare(Destination choice1, Destination choice2) {
            int result = choice1.turns - choice2.turns;
            if (result == 0) {
                return compareNames(choice1.location, choice2.location);
            } else {
                return result;
            }
        }
    }

}
