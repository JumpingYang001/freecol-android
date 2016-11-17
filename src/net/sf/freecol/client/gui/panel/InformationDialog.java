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

import org.freecolandroid.repackaged.java.awt.Graphics;
import org.freecolandroid.repackaged.java.awt.Image;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JPanel;
import org.freecolandroid.repackaged.javax.swing.JScrollPane;



import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.common.resources.ResourceManager;


public class InformationDialog extends FreeColDialog<Boolean> {

    /**
     * Returns an information dialog that shows the given
     * texts and images, and an "OK" button.
     *
     * @param canvas The parent Canvas.
     * @param text The text to be displayed in the dialog.
     * @param image The image to be displayed in the dialog.
     */
    public InformationDialog(FreeColClient freeColClient, GUI gui, String text, ImageIcon image) {
        this(freeColClient, gui, new String[] { text }, new ImageIcon[] { image });
    }

    /**
     * Returns an information dialog that shows the given
     * texts and images, and an "OK" button.
     *
     * @param parent The parent Canvas.
     * @param texts The texts to be displayed in the dialog.
     * @param images The images to be displayed in the dialog.
     */
    public InformationDialog(FreeColClient freeColClient, GUI gui, String[] texts, ImageIcon[] images) {
        super(freeColClient,  gui);
        setLayout(new MigLayout("wrap 1, insets 200 10 10 10", "[510]", "[242]20[20]"));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        if (images != null) {
            textPanel.setLayout(new MigLayout("wrap 2", "", ""));
        }

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setResponse(Boolean.FALSE);
            }
        });

        if (images == null) {
            for (String text : texts) {
                textPanel.add(getDefaultTextArea(text, 30));
            }
        } else {
            for (int i = 0; i < texts.length; i++) {
                if (images[i] == null) {
                    textPanel.add(getDefaultTextArea(texts[i], 30), "skip");
                } else {
                    textPanel.add(new JLabel(images[i]));
                    textPanel.add(getDefaultTextArea(texts[i], 30));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(textPanel,
                                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // correct way to make scroll pane opaque
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        setBorder(null);

        add(scrollPane);
        add(okButton, "tag ok");

    }

    /**
     * Paints this component.
     *
     * @param g The graphics context in which to paint.
     */
    @Override
    public void paintComponent(Graphics g) {
        Image bgImage = ResourceManager.getImage("InformationDialog.backgroundImage");
        g.drawImage(bgImage, 0, 0, this);
    }

}