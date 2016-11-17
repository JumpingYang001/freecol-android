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

import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Dimension;
import org.freecolandroid.repackaged.javax.swing.BorderFactory;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JSlider;



import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.common.option.Option;

/**
 * This class provides a JSlider as visualization for an {@link
 * net.sf.freecol.common.option.IntegerOption} or one of its
 * subclasses.
 */
public class SliderOptionUI<T extends Option<Integer>> extends OptionUI<T>  {

    private JSlider slider = new JSlider();

    /**
     * Creates a new <code>SliderOptionUI</code> for the given
     * <code>IntegerOption</code>.
     *
     * @param option The <code>IntegerOption</code> to make a user interface for
     * @param editable boolean whether user can modify the setting
     */
    public SliderOptionUI(GUI gui, final T option, boolean editable) {
        super(gui, option, editable);

        slider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),
                                                          super.getLabel().getText()));
        slider.setOrientation(JSlider.HORIZONTAL);
        slider.setPreferredSize(new Dimension(500, 50));
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setExtent(0);

        initialize();
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
    public JSlider getComponent() {
        return slider;
    }

    /**
     * {@inheritDoc}
     */
    public void updateOption() {
        getOption().setValue(slider.getValue());
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        slider.setValue(getOption().getValue());
    }

}
