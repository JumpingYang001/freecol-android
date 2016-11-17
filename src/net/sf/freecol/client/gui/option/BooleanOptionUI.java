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


import org.freecolandroid.repackaged.javax.swing.JCheckBox;

import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.common.option.BooleanOption;


/**
 * This class provides visualization for an {@link
 * net.sf.freecol.common.option.BooleanOption}. In order to enable
 * values to be both seen and changed.
 */
public final class BooleanOptionUI extends OptionUI<BooleanOption>  {

    private JCheckBox box = new JCheckBox();


    /**
    * Creates a new <code>BooleanOptionUI</code> for the given <code>BooleanOption</code>.
     *
    * @param option The <code>BooleanOption</code> to make a user interface for.
    * @param editable boolean whether user can modify the setting
    */
    public BooleanOptionUI(GUI gui, final BooleanOption option, boolean editable) {
        super(gui, option, editable);
        setValue(option.getValue());
        initialize();
    }

    /**
     * Sets the value of this UI's component.
     */
    public void setValue(boolean b) {
        box.setSelected(b);
    }

    /**
     * {@inheritDoc}
     */
    public JCheckBox getComponent() {
        return box;
    }

    /**
     * {@inheritDoc}
     */
    public void updateOption() {
        getOption().setValue(box.isSelected());
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        box.setSelected(getOption().getValue());
    }

}
