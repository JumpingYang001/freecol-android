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

package net.sf.freecol.client.gui.option;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.javax.swing.BorderFactory;
import org.freecolandroid.repackaged.javax.swing.DefaultListModel;
import org.freecolandroid.repackaged.javax.swing.JButton;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JList;
import org.freecolandroid.repackaged.javax.swing.JPanel;
import org.freecolandroid.repackaged.javax.swing.JScrollPane;
import org.freecolandroid.repackaged.javax.swing.event.ListSelectionEvent;
import org.freecolandroid.repackaged.javax.swing.event.ListSelectionListener;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.client.gui.panel.FreeColDialog;
import net.sf.freecol.common.option.AbstractOption;
import net.sf.freecol.common.option.ListOption;
import net.sf.freecol.common.option.Option;

/**
 * This class provides visualization for a List of {@link
 * net.sf.freecol.common.option.AbstractOption<T>}s. In order to
 * enable values to be both seen and changed.
 *
 */
public final class ListOptionUI<T> extends OptionUI<ListOption<T>>
    implements ListSelectionListener {

    private static Logger logger = Logger.getLogger(ListOptionUI.class.getName());

    private JPanel panel = new JPanel();
    private JList list;
    private DefaultListModel model;

    private JButton editButton = new JButton(Messages.message("list.edit"));
    private JButton addButton = new JButton(Messages.message("list.add"));
    private JButton removeButton = new JButton(Messages.message("list.remove"));
    private JButton upButton = new JButton(Messages.message("list.up"));
    private JButton downButton = new JButton(Messages.message("list.down"));

    private JButton[] buttons = new JButton[] {
        editButton, addButton, removeButton, upButton, downButton
    };
    private FreeColClient freeColClient;


    /**
     * Creates a new <code>ListOptionUI</code> for the given
     * <code>ListOption</code>.
     *
     * @param option
     * @param editable boolean whether user can modify the setting
     */
    public ListOptionUI(FreeColClient freeColClient, final GUI gui, final ListOption<T> option, boolean editable) {
        super(gui, option, editable);

        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),
                                                         super.getLabel().getText()));

        panel.setLayout(new MigLayout("wrap 2, fill", "[fill, grow]20[fill]"));

        model = new DefaultListModel();
        for (AbstractOption<T> o : option.getValue()) {
            try {
                model.addElement(o.clone());
            } catch(CloneNotSupportedException e) {
                logger.warning(e.toString());
            }
        }
        list = new JList(model);
        AbstractOption<T> o = option.getValue().isEmpty()
            ? option.getTemplate()
            : option.getValue().get(0);
        if (o != null) {
            OptionUI ui = OptionUI.getOptionUI(freeColClient, gui, o, editable);
            if (ui != null && ui.getListCellRenderer() != null) {
                list.setCellRenderer(ui.getListCellRenderer());
            }
        }
        list.setVisibleRowCount(4);
        JScrollPane pane = new JScrollPane(list);
        panel.add(pane, "grow, spany 5");

        for (JButton button : buttons) {
            button.setEnabled(editable);
            panel.add(button);
        }

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Option oldValue = (Option) list.getSelectedValue();
                if (oldValue == null) {
                    oldValue = getOption().getTemplate();
                }
                try {
                    Option value = oldValue.clone();
                    if (showEditDialog(gui, value)) {
                        model.addElement(value);
                        list.setSelectedValue(value, true);
                        list.repaint();
                    }
                } catch(CloneNotSupportedException ex) {
                    logger.warning(ex.toString());
                }
            }
        });
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object object = list.getSelectedValue();
                if (object != null) {
                    if (showEditDialog(gui, (Option) object)) {
                        list.repaint();
                    }
                }
            }
        });
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.removeElementAt(list.getSelectedIndex());
            }
        });
        upButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list.getSelectedIndex() == 0) {
                    return;
                }
                final int index = list.getSelectedIndex();
                final Object temp = model.getElementAt(index);
                model.setElementAt(model.getElementAt(index-1), index);
                model.setElementAt(temp, index-1);
                list.setSelectedIndex(index-1);
            }
        });
        downButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list.getSelectedIndex() == model.getSize() - 1) {
                    return;
                }
                final int index = list.getSelectedIndex();
                final Object temp = model.getElementAt(index);
                model.setElementAt(model.getElementAt(index+1), index);
                model.setElementAt(temp, index+1);
                list.setSelectedIndex(index+1);
            }
        });

        editButton.setEnabled(false);
        removeButton.setEnabled(false);
        upButton.setEnabled(false);
        downButton.setEnabled(false);
        list.addListSelectionListener(this);
        initialize();
    }

    private boolean showEditDialog(GUI gui, Option option) {
        final EditDialog editDialog = new EditDialog(freeColClient, gui, option);
        boolean result = gui.getCanvas().showFreeColDialog(editDialog);
        editDialog.requestFocus();
        return result;
    }

    private class EditDialog extends FreeColDialog<Boolean> {

        private OptionUI ui;

        public EditDialog(FreeColClient freeColClient, GUI gui, Option option) {
            super(freeColClient, gui);
            setLayout(new MigLayout());
            ui = OptionUI.getOptionUI(freeColClient, gui, option, editable);
            if (ui.getLabel() == null) {
                add(ui.getLabel(), "split 2");
            }
            add(ui.getComponent());

            add(okButton, "newline, split 2, tag ok");
            add(cancelButton, "tag cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (OK.equals(command)) {
                ui.updateOption();
                setResponse(true);
            } else {
                setResponse(false);
            }
        }
    }

    /**
     * Returns <code>null</code>, since this OptionUI does not require
     * an external label.
     *
     * @return null
     */
    @Override
    public final JLabel getLabel() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public JPanel getComponent() {
        return panel;
    }

    /**
     * Updates the value of the {@link net.sf.freecol.common.getOption().Option} this object keeps.
     */
    public void updateOption() {
        getOption().setValue(getValue());
    }

    @SuppressWarnings("unchecked")
    private List<AbstractOption<T>> getValue() {
        List<AbstractOption<T>> result = new ArrayList<AbstractOption<T>>();
        for (Enumeration e = model.elements(); e.hasMoreElements();) {
            result.add((AbstractOption<T>) e.nextElement());
        }
        return result;
    }

    /**
     * Reset with the value from the Option.
     */
    public void reset() {
        model.clear();
        for (AbstractOption<T> o : getOption().getValue()) {
            model.addElement(o);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
            boolean enabled = (isEditable() && list.getSelectedValue() != null);
            editButton.setEnabled(enabled);
            removeButton.setEnabled(enabled);
            upButton.setEnabled(enabled);
            downButton.setEnabled(enabled);
        }
    }



}
