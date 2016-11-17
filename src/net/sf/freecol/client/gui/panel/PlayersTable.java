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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Component;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.java.awt.event.MouseAdapter;
import org.freecolandroid.repackaged.java.awt.event.MouseEvent;
import org.freecolandroid.repackaged.javax.swing.AbstractCellEditor;
import org.freecolandroid.repackaged.javax.swing.BorderFactory;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JButton;
import org.freecolandroid.repackaged.javax.swing.JComboBox;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JList;
import org.freecolandroid.repackaged.javax.swing.JTable;
import org.freecolandroid.repackaged.javax.swing.ListCellRenderer;
import org.freecolandroid.repackaged.javax.swing.table.AbstractTableModel;
import org.freecolandroid.repackaged.javax.swing.table.DefaultTableCellRenderer;
import org.freecolandroid.repackaged.javax.swing.table.JTableHeader;
import org.freecolandroid.repackaged.javax.swing.table.TableCellEditor;
import org.freecolandroid.repackaged.javax.swing.table.TableCellRenderer;
import org.freecolandroid.repackaged.javax.swing.table.TableColumn;


import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.control.PreGameController;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.ImageLibrary;
import net.sf.freecol.client.gui.action.ColopediaAction.PanelType;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.EuropeanNationType;
import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.NationOptions;
import net.sf.freecol.common.model.NationOptions.NationState;
import net.sf.freecol.common.model.NationType;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.resources.ResourceManager;


public final class PlayersTable extends JTable {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(PlayersTable.class.getName());

    public static final int NATION_COLUMN = 0, AVAILABILITY_COLUMN = 1, ADVANTAGE_COLUMN = 2,
        COLOR_COLUMN = 3, PLAYER_COLUMN = 4;

    private static final String[] columnNames = {
        Messages.message("nation"),
        Messages.message("availability"),
        Messages.message("advantage"),
        Messages.message("color"),
        Messages.message("player")
    };


    private static final NationState[] allStates = new NationState[] {
        NationState.AVAILABLE,
        NationState.AI_ONLY,
        NationState.NOT_AVAILABLE
    };

    private static final NationState[] aiStates = new NationState[] {
        NationState.AI_ONLY,
        NationState.NOT_AVAILABLE
    };

    private final ImageLibrary library;

    /**
     * The constructor that will add the items to this panel.
     * @param gui 
     *
     * @param canvas a <code>Canvas</code> value
     * @param nationOptions a <code>NationOptions</code> value
     * @param myPlayer a <code>Player</code> value
     */
    public PlayersTable(final FreeColClient freeColClient, final GUI gui, NationOptions nationOptions, Player myPlayer) {
        super();

        library = gui.getImageLibrary();

        setModel(new PlayersTableModel(freeColClient.getPreGameController(), nationOptions, myPlayer));
        setRowHeight(47);

        JButton nationButton = new JButton(Messages.message("nation"));
        JLabel availabilityLabel = new JLabel(Messages.message("availability"));
        JButton advantageButton = new JButton(Messages.message("advantage"));
        JLabel colorLabel = new JLabel(Messages.message("color"));
        JLabel playerLabel = new JLabel(Messages.message("player"));

        nationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    gui.showColopediaPanel(PanelType.NATIONS.toString());
                }
            });

        advantageButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    gui.showColopediaPanel(PanelType.NATION_TYPES.toString());
                }
            });

        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setOpaque(false);

        HeaderRenderer renderer = new HeaderRenderer(nationButton, availabilityLabel,
                                                     advantageButton, colorLabel, playerLabel);
        JTableHeader header = getTableHeader();
        header.addMouseListener(new HeaderListener(header, renderer));

        TableColumn nationColumn = getColumnModel().getColumn(NATION_COLUMN);
        nationColumn.setCellRenderer(new NationCellRenderer());
        nationColumn.setHeaderRenderer(renderer);

        TableColumn availableColumn = getColumnModel().getColumn(AVAILABILITY_COLUMN);
        availableColumn.setCellRenderer(new AvailableCellRenderer());
        availableColumn.setCellEditor(new AvailableCellEditor());

        TableColumn advantagesColumn = getColumnModel().getColumn(ADVANTAGE_COLUMN);
        if (nationOptions.getNationalAdvantages() == NationOptions.Advantages.SELECTABLE) {
            advantagesColumn.setCellEditor(new AdvantageCellEditor(freeColClient.getGame().getSpecification().getEuropeanNationTypes()));
        }
        advantagesColumn.setCellRenderer(new AdvantageCellRenderer(nationOptions.getNationalAdvantages()));
        advantagesColumn.setHeaderRenderer(renderer);

        TableColumn colorsColumn = getColumnModel().getColumn(COLOR_COLUMN);
        colorsColumn.setCellRenderer(new ColorCellRenderer(true));

        TableColumn playerColumn = getColumnModel().getColumn(PLAYER_COLUMN);
        playerColumn.setCellEditor(new PlayerCellEditor());
        playerColumn.setCellRenderer(new PlayerCellRenderer());

    }

    public void update() {
        ((PlayersTableModel) getModel()).update();
    }

    private class HeaderRenderer implements TableCellRenderer {

        private static final int NO_COLUMN = -1;
        private int pressedColumn = NO_COLUMN;
        private Component[] components;

        public HeaderRenderer(Component... components) {
            this.components = components;
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            if (components[column] instanceof JButton) {
                boolean isPressed = (column == pressedColumn);
                ((JButton) components[column]).getModel().setPressed(isPressed);
                ((JButton) components[column]).getModel().setArmed(isPressed);
            }
            return components[column];
        }

        public void setPressedColumn(int column) {
            pressedColumn = column;
        }
    }

    private class HeaderListener extends MouseAdapter {
        JTableHeader header;

        HeaderRenderer renderer;

        HeaderListener(JTableHeader header, HeaderRenderer renderer) {
            this.header = header;
            this.renderer = renderer;
        }

        public void mousePressed(MouseEvent e) {
            int col = header.columnAtPoint(e.getPoint());
            renderer.setPressedColumn(col);
            header.repaint();
        }

        public void mouseReleased(MouseEvent e) {
            renderer.setPressedColumn(HeaderRenderer.NO_COLUMN);
            header.repaint();
        }
    }


    class NationCellRenderer extends JLabel implements TableCellRenderer {

        /**
         * Returns the component used to render the cell's value.
         * @param table The table whose cell needs to be rendered.
         * @param value The value of the cell being rendered.
         * @param hasFocus Indicates whether or not the cell in question has focus.
         * @param row The row index of the cell that is being rendered.
         * @param column The column index of the cell that is being rendered.
         * @return The component used to render the cell's value.
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {

            Nation nation = (Nation) value;
            setText(Messages.message(nation.getNameKey()));
            setIcon(new ImageIcon(library.getCoatOfArmsImage(nation, 0.5)));
            return this;
        }
    }

    class AvailableCellRenderer implements TableCellRenderer {

        private JComboBox box = new JComboBox(allStates);

        public AvailableCellRenderer() {
            box.setRenderer(new NationStateRenderer());
        }

        /**
         * Returns the component used to render the cell's value.
         * @param table The table whose cell needs to be rendered.
         * @param value The value of the cell being rendered.
         * @param hasFocus Indicates whether or not the cell in question has focus.
         * @param row The row index of the cell that is being rendered.
         * @param column The column index of the cell that is being rendered.
         * @return The component used to render the cell's value.
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            box.setSelectedItem(value);
            return box;
        }
    }

    class NationStateRenderer extends JLabel implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(Messages.message("nationState." + ((NationState) value).toString()));
            return this;
        }
    }

    public final class AvailableCellEditor extends AbstractCellEditor implements TableCellEditor {

        private JComboBox aiStateBox = new JComboBox(aiStates);
        private JComboBox allStateBox = new JComboBox(allStates);
        private JComboBox activeBox;

        private ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stopCellEditing();
                }
            };

        public AvailableCellEditor() {
            aiStateBox.setRenderer(new NationStateRenderer());
            aiStateBox.addActionListener(listener);
            allStateBox.setRenderer(new NationStateRenderer());
            allStateBox.addActionListener(listener);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            NationType nationType = ((Nation) getValueAt(row, NATION_COLUMN)).getType();
            if (nationType instanceof EuropeanNationType) {
                activeBox = allStateBox;
            } else {
                activeBox = aiStateBox;
            }
            return activeBox;
        }

        public Object getCellEditorValue() {
            return activeBox.getSelectedItem();
        }
    }

    class PlayerCellRenderer implements TableCellRenderer {

        JLabel label = new JLabel();
        JButton button = new JButton(Messages.message("select"));

        public PlayerCellRenderer() {
            label.setHorizontalAlignment(JLabel.CENTER);
            button.setBorder(BorderFactory
                             .createCompoundBorder(BorderFactory
                                                   .createEmptyBorder(5, 10, 5, 10),
                                                   button.getBorder()));
        }

        /**
         * Returns the component used to render the cell's value.
         * @param table The table whose cell needs to be rendered.
         * @param value The value of the cell being rendered.
         * @param hasFocus Indicates whether or not the cell in question has focus.
         * @param row The row index of the cell that is being rendered.
         * @param column The column index of the cell that is being rendered.
         * @return The component used to render the cell's value.
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {

            Player player = (Player) value;
            if (player == null) {
                NationType nationType = (NationType) table.getValueAt(row, ADVANTAGE_COLUMN);
                if (nationType instanceof EuropeanNationType) {
                    NationState nationState = (NationState) table
                        .getValueAt(row, AVAILABILITY_COLUMN);
                    if (nationState == NationState.AVAILABLE) {
                        return button;
                    }
                }
                Nation nation = (Nation) table.getValueAt(row, NATION_COLUMN);
                label.setText(Messages.message(nation.getRulerNameKey()));
            } else {
                label.setText(player.getDisplayName());
            }
            return label;
        }
    }

    public final class PlayerCellEditor extends AbstractCellEditor implements TableCellEditor {

        private JButton button = new JButton(Messages.message("select"));

        public PlayerCellEditor() {
            button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        fireEditingStopped();
                    }
                });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            return button;
        }

        public Object getCellEditorValue() {
            return true;
        }

    }


    /**
     * The TableModel for the players table.
     */
    private class PlayersTableModel extends AbstractTableModel {

        private List<Nation> nations;

        private Map<Nation, Player> players;

        private Player thisPlayer;

        private final PreGameController preGameController;

        private NationOptions nationOptions;

        /**
         * A standard constructor.
         *
         * @param pgc The PreGameController to use when updates need to be notified
         *            across the network.
         * @param nationOptions a <code>NationOptions</code> value
         * @param owningPlayer a <code>Player</code> value
         */
        public PlayersTableModel(PreGameController pgc, NationOptions nationOptions, Player owningPlayer) {
            nations = new ArrayList<Nation>();
            players = new HashMap<Nation, Player>();
            for (Nation nation : owningPlayer.getSpecification().getNations()) {
                NationState state = nationOptions.getNations().get(nation);
                if (state != null) {
                    nations.add(nation);
                    players.put(nation, null);
                }
            }
            thisPlayer = owningPlayer;
            players.put(thisPlayer.getNation(), thisPlayer);
            preGameController = pgc;
            this.nationOptions = nationOptions;
        }

        public void update() {
            for (Nation nation : nations) {
                players.put(nation, null);
            }
            for (Player player : thisPlayer.getGame().getPlayers()) {
                players.put(player.getNation(), player);
            }
            fireTableDataChanged();
        }

        /**
         * Returns the Class of the objects in the given column.
         *
         * @param column The column to return the Class of.
         * @return The Class of the objects in the given column.
         */
        public Class<?> getColumnClass(int column) {
            switch(column) {
            case NATION_COLUMN:
                return Nation.class;
            case AVAILABILITY_COLUMN:
                return NationOptions.NationState.class;
            case ADVANTAGE_COLUMN:
                return NationType.class;
            case COLOR_COLUMN:
                return Color.class;
            case PLAYER_COLUMN:
                return Player.class;
            }
            return String.class;
        }

        /**
         * Returns the amount of columns in this statesTable.
         *
         * @return The amount of columns in this statesTable.
         */
        public int getColumnCount() {
            return columnNames.length;
        }

        /**
         * Returns the name of the specified column.
         *
         * @return The name of the specified column.
         */
        public String getColumnName(int column) {
            return columnNames[column];
        }

        /**
         * Returns the amount of rows in this statesTable.
         *
         * @return The amount of rows in this statesTable.
         */
        public int getRowCount() {
            return nations.size();
        }

        /**
         * Returns the value at the requested location.
         *
         * @param row The requested row.
         * @param column The requested column.
         * @return The value at the requested location.
         */
        public Object getValueAt(int row, int column) {
            if ((row < getRowCount()) && (column < getColumnCount()) && (row >= 0) && (column >= 0)) {
                Nation nation = nations.get(row);
                switch (column) {
                case NATION_COLUMN:
                    return nation;
                case AVAILABILITY_COLUMN:
                    return nationOptions.getNationState(nation);
                case ADVANTAGE_COLUMN:
                    if (players.get(nation) == null) {
                        return nation.getType();
                    } else {
                        return players.get(nation).getNationType();
                    }
                case COLOR_COLUMN:
                    return ResourceManager.getColor(nation.getId() + ".color");
                case PLAYER_COLUMN:
                    return players.get(nation);
                }
            }
            return null;
        }

        /**
         * Returns 'true' if the specified cell is editable, 'false' otherwise.
         *
         * @param row The specified row.
         * @param column The specified column.
         * @return 'true' if the specified cell is editable, 'false' otherwise.
         */
        public boolean isCellEditable(int row, int column) {
            if ((row >= 0) && (row < nations.size())) {
                Nation nation = nations.get(row);
                boolean ownRow = (thisPlayer == players.get(nation) && !thisPlayer.isReady());
                switch(column) {
                case AVAILABILITY_COLUMN:
                    return (!ownRow && thisPlayer.isAdmin());
                case ADVANTAGE_COLUMN:
                case COLOR_COLUMN:
                    return (nation.getType() instanceof EuropeanNationType && ownRow);
                case PLAYER_COLUMN:
                    return (nation.getType() instanceof EuropeanNationType && players.get(nation) == null);
                }
            }
            return false;
        }

        /**
         * Sets the value at the specified location.
         *
         * @param value The new value.
         * @param row The specified row.
         * @param column The specified column.
         */
        public void setValueAt(Object value, int row, int column) {
            if ((row < getRowCount()) && (column < getColumnCount()) && (row >= 0) && (column >= 0)) {
                // Column 0 can't be updated.

                switch(column) {
                case ADVANTAGE_COLUMN:
                    preGameController.setNationType((NationType) value);
                    break;
                case AVAILABILITY_COLUMN:
                    preGameController.setAvailable(nations.get(row), (NationState) value);
                    update();
                    break;
                case PLAYER_COLUMN:
                    Nation nation = nations.get(row);
                    if (nationOptions.getNationState(nation) == NationState.AVAILABLE) {
                        preGameController.setNation(nation);
                        preGameController.setNationType(nation.getType());
                        update();
                    }
                    break;
                }

                fireTableCellUpdated(row, column);
            }
        }

    }
}