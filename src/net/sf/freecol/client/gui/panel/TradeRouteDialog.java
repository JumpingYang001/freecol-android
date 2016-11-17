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

import org.freecolandroid.repackaged.java.awt.Component;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.javax.swing.DefaultListCellRenderer;
import org.freecolandroid.repackaged.javax.swing.DefaultListModel;
import org.freecolandroid.repackaged.javax.swing.JButton;
import org.freecolandroid.repackaged.javax.swing.JList;
import org.freecolandroid.repackaged.javax.swing.JScrollPane;
import org.freecolandroid.repackaged.javax.swing.event.ListSelectionEvent;
import org.freecolandroid.repackaged.javax.swing.event.ListSelectionListener;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.TradeRoute;
import net.sf.freecol.common.model.Unit;


/**
 * Allows the user to edit trade routes.
 */
public final class TradeRouteDialog extends FreeColDialog<TradeRoute> implements ActionListener {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TradeRouteDialog.class.getName());

    private static enum Action { OK, CANCEL, DEASSIGN, DELETE }

    private final JButton editRouteButton = new JButton(Messages.message("traderouteDialog.editRoute"));
    private final JButton newRouteButton = new JButton(Messages.message("traderouteDialog.newRoute"));
    private final JButton removeRouteButton = new JButton(Messages.message("traderouteDialog.removeRoute"));
    private final JButton deassignRouteButton = new JButton(Messages.message("traderouteDialog.deassignRoute"));

    private final DefaultListModel listModel = new DefaultListModel();
    private final JList tradeRoutes = new JList(listModel);
    private final JScrollPane tradeRouteView = new JScrollPane(tradeRoutes);

    /**
     * The constructor that will add the items to this panel.
     * @param parent The parent of this panel.
     */
    public TradeRouteDialog(FreeColClient freeColClient, final GUI gui, TradeRoute selectedRoute) {

        super(freeColClient, gui);

        deassignRouteButton.addActionListener(this);
        deassignRouteButton.setToolTipText(Messages.message("traderouteDialog.deassign.tooltip"));
        deassignRouteButton.setActionCommand(Action.DEASSIGN.toString());
        enterPressesWhenFocused(deassignRouteButton);

        tradeRoutes.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    updateButtons();
                }
            });

        // button for adding new TradeRoute
        newRouteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Player player = getMyPlayer();
                    TradeRoute newRoute = getController().getNewTradeRoute(player);
                    newRoute.setName(Messages.message("traderouteDialog.newRoute"));
                    if (getGUI().showTradeRouteInputDialog(newRoute)) {
                        listModel.addElement(newRoute);
                        tradeRoutes.setSelectedValue(newRoute, true);
                    }
                }
            });

        // button for editing TradeRoute
        editRouteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getGUI().showTradeRouteInputDialog((TradeRoute) tradeRoutes.getSelectedValue());
                }
            });

        // button for deleting TradeRoute
        removeRouteButton.addActionListener(this);
        removeRouteButton.setActionCommand(Action.DELETE.toString());

        Player player = getMyPlayer();

        List<TradeRoute> theRoutes = new ArrayList<TradeRoute>(player.getTradeRoutes());
        for (TradeRoute tradeRoute : theRoutes) tradeRoute.setCount(0);
        for (Unit u : player.getUnits()) {
            TradeRoute tradeRoute = u.getTradeRoute();
            if (tradeRoute != null) {
                tradeRoute.setCount(1 + tradeRoute.getCount());
            }
        }
        Collections.sort(theRoutes, tradeRouteComparator);
        for (TradeRoute route : theRoutes) {
            listModel.addElement(route);
        }

        tradeRoutes.setCellRenderer(new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(JList list,
                                                              Object value,
                                                              int index,
                                                              boolean selected,
                                                              boolean focus) {
                    Component ret = super.getListCellRendererComponent(list,
                        value, index, selected, focus);
                    TradeRoute tradeRoute = (TradeRoute) value;
                    String name = tradeRoute.getName();
                    int n = tradeRoute.getCount();

                    if (n > 0) {
                        setText(name + "  (" + String.valueOf(n) + ")");
                    } else {
                        setText(name);
                    }
                    return ret;
                }
            });

        if (selectedRoute != null) {
            tradeRoutes.setSelectedValue(selectedRoute, true);
        }
        updateButtons();

        setLayout(new MigLayout("wrap 2", "[fill][fill]"));

        add(getDefaultHeader(Messages.message("traderouteDialog.name")),
            "span, align center");

        add(tradeRouteView, "height 360:400, width 250:");
        add(newRouteButton, "split 4, flowy, growx");
        add(editRouteButton, "growx");
        add(removeRouteButton, "growx");
        add(deassignRouteButton);

        add(okButton, "newline 20, span, split 2, tag ok");
        add(cancelButton, "tag cancel");

        restoreSavedSize(getPreferredSize());

    }

     private static final Comparator<TradeRoute> tradeRouteComparator = new Comparator<TradeRoute>() {
         public int compare(TradeRoute r1, TradeRoute r2) {
             return r1.getName().compareTo(r2.getName());
         }
     };

    public void updateButtons() {
        if (tradeRoutes.getSelectedIndex() == -1) {
            editRouteButton.setEnabled(false);
            removeRouteButton.setEnabled(false);
            deassignRouteButton.setEnabled(false);
        } else {
            editRouteButton.setEnabled(true);
            removeRouteButton.setEnabled(true);
            deassignRouteButton.setEnabled(true);
        }
    }


    /**
     * This function analyses an event and calls the right methods to take
     * care of the user's requests.
     *
     * @param event The incoming ActionEvent.
     */
    public void actionPerformed(ActionEvent event) {
        Action action = Enum.valueOf(Action.class, event.getActionCommand());
        Unit unit = getGUI().getActiveUnit();
        TradeRoute route = (TradeRoute) tradeRoutes.getSelectedValue();
        if (unit != null && route != null) {
            switch (action) {
            case OK:
                getGUI().removeFromCanvas(this);
                ArrayList<TradeRoute> routes = new ArrayList<TradeRoute>();
                for (int index = 0; index < listModel.getSize(); index++) {
                    routes.add((TradeRoute) listModel.getElementAt(index));
                }
                getController().setTradeRoutes(routes);
                unit.setTradeRoute(route);
                setResponse(route);
                return;
            case DEASSIGN:
                getGUI().removeFromCanvas(this);
                getController().clearOrders(unit);
                setResponse(route);
                return;
            case DELETE:
                for (Unit u : route.getAssignedUnits()) {
                    getController().clearOrders(u);
                }
                listModel.removeElementAt(tradeRoutes.getSelectedIndex());
                Player player = getMyPlayer();
                player.getTradeRoutes().remove(route);
                getController().setTradeRoutes(player.getTradeRoutes());
                // Do not set response
                return;
            case CANCEL: default:
                break;
            }
        }
        getGUI().removeFromCanvas(this);
        setResponse(null);
    }
}
