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

import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Dimension;
import org.freecolandroid.repackaged.java.awt.Graphics;
import org.freecolandroid.repackaged.java.awt.Graphics2D;
import org.freecolandroid.repackaged.java.awt.Image;
import org.freecolandroid.repackaged.java.awt.RenderingHints;
import org.freecolandroid.repackaged.javax.swing.BorderFactory;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JPanel;



import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.ImageLibrary;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.StringTemplate;


/**
 * Implements a simple progress bar suitable for use with
 * FreeCol. Unlike JProgressBar, it also displays the expected
 * increase next turn as well as the estimated time until completion.
 * Used in the colony panel for the building progress.
 */
public class FreeColProgressBar extends JPanel {

    private static final Color PRIMARY_1 = new Color(122, 109, 82);

    // The minimum value of the progress bar
    private int min = 0;

    // The maximum value of the progress bar
    private int max = 100;

    // The current value of the progress bar
    private int value = 0;

    // The expected increase next turn
    private int step = 0;

    private int iconWidth;

    private int iconHeight = 16;

    /**
     * The type of goods this progress bar is for. The default value of null
     * indicates no goods type.
     */
    private GoodsType goodsType = null;


    private Image image;


    /**
     * Creates a new <code>FreeColProgressBar</code> instance.
     *
     * @param parent a <code>Canvas</code> value
     * @param goodsType the type of goods produced
     */
    public FreeColProgressBar(GUI gui, GoodsType goodsType) {
        this(gui, goodsType, 0, 100, 0, 0);
    }

    /**
     * Creates a new <code>FreeColProgressBar</code> instance.
     *
     * @param parent a <code>Canvas</code> value
     * @param goodsType the type of goods produced
     * @param min the minimum value of the progress bar
     * @param max the maximum value of the progress bar
     */
    public FreeColProgressBar(GUI gui, GoodsType goodsType, int min, int max) {
        this(gui, goodsType, min, max, 0, 0);
    }

    /**
     * Creates a new <code>FreeColProgressBar</code> instance.
     *
     * @param parent a <code>Canvas</code> value
     * @param goodsType the type of goods produced
     * @param min the minimum value of the progress bar
     * @param max the maximum value of the progress bar
     * @param value the current value of the progress bar
     * @param step the expected increase next turn
     */
    public FreeColProgressBar(GUI gui, GoodsType goodsType, int min, int max, int value, int step) {
        this.goodsType = goodsType;
        this.min = min;
        this.max = max;
        this.value = value;
        this.step = step;

        setBorder(BorderFactory.createLineBorder(PRIMARY_1));
		if (goodsType != null) {
			ImageIcon icon = gui.getImageLibrary().getGoodsImageIcon(goodsType);
			// scale to a height of 16px, preserving aspect ratio
			image = icon.getImage().getScaledInstance(-1, iconHeight, Image.SCALE_SMOOTH);
			iconWidth = image.getWidth(this);
        }
		setPreferredSize(new Dimension(200, 20));
    }

    /**
     * Upate the data of the progress bar.
     *
     * @param value the current value of the progress bar
     * @param step the expected increase next turn
     */
    public void update(int value, int step) {
        update(min, max, value, step);
    }

    /**
     * Upate the data of the progress bar.
     *
     * @param min the minimum value of the progress bar
     * @param max the maximum value of the progress bar
     * @param value the current value of the progress bar
     * @param step the expected increase next turn
     */
    public void update(int min, int max, int value, int step) {
        this.min = min;
        this.max = max;
        this.value = value;
        this.step = step;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();
        int width = getWidth() - getInsets().left - getInsets().right;
        int height = getHeight() - getInsets().top - getInsets().bottom;

        if (image != null && iconWidth < 0) {
            iconWidth = image.getWidth(this);
        }

        if (isOpaque()) {
            ImageLibrary.drawTiledImage("background.FreeColProgressBar", g, this, getInsets());
        }

        int dvalue = 0;
        if (value >= max) {
            dvalue = width;
        } else if (max > 0) {
            dvalue = width * value / max;
        }
        if (dvalue > 0) {
            if (dvalue > width) {
                dvalue = width;
            }
            g2d.setColor(new Color(0, 0, 0, 70));
            g2d.fillRect(getInsets().left, getInsets().top, dvalue, height);
        }

        int dstep = 0;
        if (max > 0) {
            dstep = width * step / max;
            if (dstep > 0) {
                if (dstep + dvalue > width) {
                    dstep = width - dvalue;
                }
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillRect(getInsets().left + dvalue, getInsets().top, dstep, height);
            }
        }

        String stepSignal = (step < 0) ? "-" : "+";
        String progressString = String.valueOf(value) + stepSignal + Math.abs(step) + "/" + max;
        String turnsString = Messages.message("notApplicable.short");
        if (max <= value) {
            turnsString = "0";
        } else if (step > 0) {
            // There is progress, find how many turns necessary with current production
            int turns = (max - value) / step;
            if ((max - value) % step > 0) {
                turns++;
            }
            turnsString = Integer.toString(turns);
        }
        progressString += " " + Messages.message(StringTemplate.template("turnsToComplete.short")
                                                 .addName("%number%", turnsString));

        int stringWidth = g2d.getFontMetrics().stringWidth(progressString);
        int stringHeight = g2d.getFontMetrics().getAscent() + g2d.getFontMetrics().getDescent();
        int restWidth = getWidth() - stringWidth;

        if (goodsType != null) {
            restWidth -= iconWidth;
            g2d.drawImage(image, restWidth / 2, (getHeight() - iconHeight) / 2, null);
        }

        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawString(progressString, restWidth / 2 + iconWidth, getHeight() / 2 + stringHeight / 4);

        g2d.dispose();
    }

}
