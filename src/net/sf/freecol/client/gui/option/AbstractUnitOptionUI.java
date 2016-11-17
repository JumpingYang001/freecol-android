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


import java.util.Locale;

import org.freecolandroid.repackaged.java.awt.AlphaComposite;
import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Component;
import org.freecolandroid.repackaged.java.awt.Composite;
import org.freecolandroid.repackaged.java.awt.Graphics;
import org.freecolandroid.repackaged.java.awt.Graphics2D;
import org.freecolandroid.repackaged.java.awt.event.ItemEvent;
import org.freecolandroid.repackaged.java.awt.event.ItemListener;
import org.freecolandroid.repackaged.javax.swing.DefaultComboBoxModel;
import org.freecolandroid.repackaged.javax.swing.JComboBox;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JList;
import org.freecolandroid.repackaged.javax.swing.JPanel;
import org.freecolandroid.repackaged.javax.swing.ListCellRenderer;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.client.gui.plaf.FreeColComboBoxRenderer;
import net.sf.freecol.common.model.Ability;
import net.sf.freecol.common.model.AbstractUnit;
import net.sf.freecol.common.model.Unit.Role;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.option.AbstractUnitOption;
import net.sf.freecol.common.option.IntegerOption;
import net.sf.freecol.common.option.StringOption;
import net.sf.freecol.common.option.UnitTypeOption;


/**
 * This class provides visualization for an {@link
 * net.sf.freecol.common.option.AbstractUnitOption}. In order to enable
 * values to be both seen and changed.
 */
public final class AbstractUnitOptionUI extends OptionUI<AbstractUnitOption>
    implements ItemListener {

    private JPanel panel = new JPanel();
    private IntegerOptionUI numberUI;
    private UnitTypeOptionUI typeUI;
    private StringOptionUI roleUI;

    private boolean roleEditable;

    /**
    * Creates a new <code>AbstractUnitOptionUI</code> for the given <code>AbstractUnitOption</code>.
    *
    * @param option The <code>AbstractUnitOption</code> to make a user interface for
    * @param editable boolean whether user can modify the setting
    */
    public AbstractUnitOptionUI(GUI gui, final AbstractUnitOption option, boolean editable) {
        super(gui, option, editable);

        panel.setLayout(new MigLayout());

        IntegerOption numberOption = option.getNumber();
        UnitTypeOption typeOption = option.getUnitType();
        StringOption roleOption = option.getRole();

        boolean numberEditable = editable
            && (numberOption.getMaximumValue() > numberOption.getMinimumValue());
        numberUI = new IntegerOptionUI(gui, numberOption, numberEditable);
        numberUI.getComponent().setToolTipText(Messages.message("report.numberOfUnits"));
        panel.add(numberUI.getComponent(), "width 30%");

        boolean typeEditable = editable
            && typeOption.getChoices().size() > 1;
        typeUI = new UnitTypeOptionUI(gui, typeOption, typeEditable);

        typeUI.getComponent().setToolTipText(Messages.message("model.unit.type"));
        typeUI.getComponent().addItemListener(this);
        panel.add(typeUI.getComponent(), "width 35%");

        roleEditable = editable
            && roleOption.getChoices().size() > 1;
        roleUI = new StringOptionUI(gui,roleOption, roleEditable);
        roleUI.getComponent().setToolTipText(Messages.message("model.unit.role.name"));
        roleUI.getComponent().setRenderer(new RoleRenderer());
        panel.add(roleUI.getComponent(), "width 35%");

        initialize();

    }

    /**
     * {@inheritDoc}
     */
    public JPanel getComponent() {
        return panel;
    }

    /**
     * Updates the value of the {@link net.sf.freecol.common.option.Option} this object keeps.
     */
    public void updateOption() {
        typeUI.updateOption();
        roleUI.updateOption();
        numberUI.updateOption();
        UnitType type = typeUI.getOption().getValue();
        Role role = Role.valueOf(roleUI.getOption().getValue());
        int number = numberUI.getOption().getValue();
        getOption().setValue(new AbstractUnit(type, role, number));
    }

    /**
     * Reset with the value from the option.
     */
    public void reset() {
        typeUI.reset();
        roleUI.reset();
        numberUI.reset();
    }

    public void itemStateChanged(ItemEvent e) {
        JComboBox box = roleUI.getComponent();
        UnitType type = (UnitType) typeUI.getComponent().getSelectedItem();
        if (type.hasAbility(Ability.CAN_BE_EQUIPPED)) {
            box.setModel(new DefaultComboBoxModel(roleUI.getOption().getChoices().toArray(new String[0])));
            box.setEnabled(roleEditable);
        } else {
            box.setModel(new DefaultComboBoxModel(new String[] { Role.DEFAULT.toString() }));
            box.setEnabled(false);
        }
    }

    private class RoleRenderer extends FreeColComboBoxRenderer {

        @Override
        public void setLabelValues(JLabel label, Object value) {
            label.setText(Messages.message("model.unit.role." + ((String) value).toLowerCase(Locale.US)));
        }
    }

    public ListCellRenderer getListCellRenderer() {
        return new AbstractUnitRenderer();
    }

    private class AbstractUnitRenderer implements ListCellRenderer {

        private final JPanel normal = new JPanel();
        private final JPanel selected = new JPanel() {
                public void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    Composite oldComposite = g2d.getComposite();
                    Color oldColor = g2d.getColor();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setComposite(oldComposite);
                    g2d.setColor(oldColor);

                    super.paintComponent(g);
                }
            };

        public AbstractUnitRenderer() {
            super();
            normal.setOpaque(false);
            normal.setLayout(new MigLayout("", "[40, align right][]"));
            selected.setOpaque(false);
            selected.setLayout(new MigLayout("", "[40, align right][]"));
        }


        /**
         * Returns a <code>ListCellRenderer</code> for the given <code>JList</code>.
         *
         * @param list The <code>JList</code>.
         * @param value The list cell.
         * @param index The index in the list.
         * @param isSelected <code>true</code> if the given list cell is selected.
         * @param hasFocus <code>false</code> if the given list cell has the focus.
         * @return The <code>ListCellRenderer</code>
         */
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean hasFocus) {

            JPanel c = isSelected ? selected : normal;
            c.removeAll();
            c.setForeground(list.getForeground());
            c.setFont(list.getFont());
            AbstractUnit unit = (AbstractUnit) ((AbstractUnitOption) value).getValue();
            String key = unit.getId();
            if (unit.getUnitType(getOption().getSpecification())
                .hasAbility(Ability.CAN_BE_EQUIPPED)
                && unit.getRole() != Role.DEFAULT) {
                key = "model.unit." + unit.getRole().toString().toLowerCase(Locale.US);
            }
            StringTemplate template = StringTemplate.template(key + ".name")
                .addAmount("%number%", unit.getNumber())
                .add("%unit%", unit.getId() + ".name");
            /*
            c.add(new JLabel(new ImageIcon(ResourceManager.getImage(unit.getId() + ".image", 0.5))),
                  "width 80, align center");
            */
            c.add(new JLabel(Integer.toString(unit.getNumber())));
            c.add(new JLabel(Messages.message(template)));
            return c;
        }
    }


}
