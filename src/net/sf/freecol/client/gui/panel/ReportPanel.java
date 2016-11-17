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


import java.util.Comparator;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Component;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JPanel;
import org.freecolandroid.repackaged.javax.swing.JScrollPane;
import org.freecolandroid.repackaged.javax.swing.border.Border;
import org.freecolandroid.repackaged.javax.swing.border.EmptyBorder;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.AbstractUnit;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.FreeColGameObject;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.Role;
import net.sf.freecol.common.model.UnitType;

/**
 * This panel displays a report.
 */
public class ReportPanel extends FreeColPanel implements ActionListener {

    protected static final Logger logger = Logger.getLogger(ReportPanel.class.getName());

    /**
     * Returns a unit type comparator.
     *
     * @return A unit type comparator.
     */
    public static Comparator<Unit> getUnitTypeComparator() {
        return unitTypeComparator;
    }

    protected JPanel reportPanel;

    protected JLabel header;


    protected JScrollPane scrollPane;


    public static final Comparator<Unit> unitTypeComparator = new Comparator<Unit>() {
        public int compare(Unit unit1, Unit unit2) {
            int deltaType = unit2.getType().compareTo(unit1.getType());
            if (deltaType == 0) {
                return unit2.getRole().ordinal() - unit1.getRole().ordinal();
            } else {
                return deltaType;
            }
        }
    };

    /**
     * The constructor that will add the items to this panel.
     *
     * @param parent The parent of this panel.
     * @param title The title to display on the panel.
     */
    public ReportPanel(FreeColClient freeColClient, GUI gui, String title) {
        super(freeColClient, gui);

        setLayout(new MigLayout("wrap 1", "[fill]", "[]30[fill]30[]"));

        header = getDefaultHeader(title);
        add(header, "cell 0 0, align center");

        reportPanel = new JPanel() {
                @Override
                public String getUIClassID() {
                    return "ReportPanelUI";
                }
            };

        reportPanel.setOpaque(true);
        reportPanel.setBorder(createBorder());

        scrollPane = new JScrollPane(reportPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement( 16 );
        add(scrollPane, "cell 0 1, height 100%, width 100%");
        add(okButton, "cell 0 2, tag ok");

        restoreSavedSize(850, 600);
    }

    /**
     * This function analyses an event and calls the right methods to take care
     * of the user's requests.
     *
     * @param event The incoming ActionEvent.
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (OK.equals(command)) {
            getGUI().removeFromCanvas(this);
        } else {
            FreeColGameObject object = getGame().getFreeColGameObject(command);
            if (object instanceof Colony) {
                getGUI().showColonyPanel((Colony) object);
            } else if (object instanceof Europe) {
                getGUI().showEuropePanel();
            } else if (object instanceof Tile) {
                getGUI().setFocus(((Tile) object));
            } else if (object == null) {
                getGUI().showColopediaPanel(command);
            }
        }
    }

    /**
     * Prepares this panel to be displayed.
     */
    public void initialize() {
        reportPanel.removeAll();
        reportPanel.doLayout();
    }

    protected Border createBorder() {
        return new EmptyBorder(20, 20, 20, 20);
    }

    protected JLabel createUnitTypeLabel(AbstractUnit unit) {
        UnitType unitType = unit.getUnitType(getSpecification());
        Role role = unit.getRole();
        int count = unit.getNumber();
        ImageIcon unitIcon = getLibrary().getUnitImageIcon(unitType, role, (count == 0), 0.66);
        JLabel unitLabel = new JLabel(unitIcon);
        unitLabel.setText(String.valueOf(count));
        if (count == 0) {
            unitLabel.setForeground(Color.GRAY);
        }
        unitLabel.setToolTipText(Messages.getLabel(unitType, role, count));
        return unitLabel;
    }

    protected String getLocationNameFor(Unit unit) {
        if (unit.getDestination() instanceof Map) {
            return Messages.message("goingToAmerica");
        } else if (unit.getDestination() instanceof Europe) {
            return Messages.message("goingToEurope");
        } else {
            return Messages.message(unit.getLocation().getLocationNameFor(unit.getOwner()));
        }
    }


    protected void setMainComponent(Component main) {
        remove(scrollPane);
        add(main, "cell 0 1, height 100%, width 100%");
    }
}
