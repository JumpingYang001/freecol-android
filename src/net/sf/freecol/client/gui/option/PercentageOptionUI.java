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

import java.util.Hashtable;

import org.freecolandroid.repackaged.javax.swing.DefaultBoundedRangeModel;
import org.freecolandroid.repackaged.javax.swing.JComponent;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JSlider;


import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.common.option.PercentageOption;

/**
 * This class provides visualization for an {@link
 * net.sf.freecol.common.option.PercentageOption}. In order to enable
 * values to be both seen and changed.
 */
public final class PercentageOptionUI extends SliderOptionUI<PercentageOption>  {


    /**
     * Creates a new <code>PercentageOptionUI</code> for the given
     * <code>PercentageOption</code>.
     *
     * @param option The <code>PercentageOption</code> to make a user interface for
     * @param editable boolean whether user can modify the setting
     */
    public PercentageOptionUI(GUI gui, final PercentageOption option, boolean editable) {
        super(gui, option, editable);

        JSlider slider = getComponent();

        slider.setModel(new DefaultBoundedRangeModel(option.getValue(), 0, 0, 100));
        Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
        labels.put(new Integer(0), new JLabel("0 %"));
        labels.put(new Integer(25), new JLabel("25 %"));
        labels.put(new Integer(50), new JLabel("50 %"));
        labels.put(new Integer(75), new JLabel("75 %"));
        labels.put(new Integer(100), new JLabel("100 %"));
        slider.setLabelTable(labels);
        slider.setValue(option.getValue());
        slider.setMajorTickSpacing(5);
        slider.setSnapToTicks(false);
    }

}
