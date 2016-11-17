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


import org.freecolandroid.repackaged.javax.swing.JSpinner;
import org.freecolandroid.repackaged.javax.swing.SpinnerNumberModel;

import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.common.option.IntegerOption;


/**
 * This class provides visualization for an {@link
 * net.sf.freecol.common.option.IntegerOption}. In order to enable
 * values to be both seen and changed.
 */
public final class IntegerOptionUI extends OptionUI<IntegerOption>  {

    private JSpinner spinner = new JSpinner();

    /**
     * Creates a new <code>IntegerOptionUI</code> for the given <code>IntegerOption</code>.
     * @param option The <code>IntegerOption</code> to make a user interface for.
     * @param editable boolean whether user can modify the setting
     */
    public IntegerOptionUI(GUI gui, final IntegerOption option, boolean editable) {
        super(gui, option, editable);

        int value = option.getValue();
        if (editable) {
            int min = option.getMinimumValue();
            int max = option.getMaximumValue();
            if (min > max) {
                int tmp = min;
                min = max;
                max = tmp;
            }
            int stepSize = Math.max(1, Math.min((max - min) / 10, 1000));
            spinner.setModel(new SpinnerNumberModel(value, min, max, stepSize));
        } else {
            spinner.setModel(new SpinnerNumberModel(value, value, value, 1));
        }

        initialize();
    }

    /**
     * {@inheritDoc}
     */
    public JSpinner getComponent() {
        return spinner;
    }

    /**
     * {@inheritDoc}
     */
    public void updateOption() {
        getOption().setValue((Integer) spinner.getValue());
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        spinner.setValue(getOption().getValue());
    }


}
