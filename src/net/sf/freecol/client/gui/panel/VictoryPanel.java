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


import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Image;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JButton;
import org.freecolandroid.repackaged.javax.swing.JLabel;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.resources.ResourceManager;


/**
 * This panel gets displayed to the player who have won the game.
 */
public final class VictoryPanel extends FreeColPanel {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(VictoryPanel.class.getName());

    private static final String CONTINUE = "CONTINUE";

    private JButton continueButton
        = new JButton(Messages.message("victory.continue"));


    /**
     * The constructor that will add the items to this panel.
     * 
     * @param gui The parent of this panel.
     */
    public VictoryPanel(FreeColClient freeColClient, GUI gui) {

        super(freeColClient, gui);
        
        okButton.setText(Messages.message("victory.yes"));

        setLayout(new MigLayout("wrap 1", "", ""));

        add(getDefaultHeader(Messages.message("victory.text")),
            "align center, wrap 20");

        Image tempImage = ResourceManager.getImage("VictoryImage");
        if (tempImage != null) {
            add(new JLabel(new ImageIcon(tempImage)), "align center");
        }

        continueButton.setActionCommand(CONTINUE);
        continueButton.addActionListener(this);
        enterPressesWhenFocused(continueButton);

        if (getFreeColClient().isSingleplayer()) {
            add(okButton, "newline 20, split 2, tag ok");
            add(continueButton);
        } else {
            add(okButton, "newline 20, tag ok");
        }
        setSize(getPreferredSize());

        boolean high = getFreeColClient().askServer().checkHighScore();
        getGUI().showHighScoresPanel((high) ? "highscores.yes"
            : "highscores.no");
    }

    /**
     * This function analyses an event and calls the right methods to
     * take care of the user's requests.
     * 
     * @param event The incoming ActionEvent.
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (OK.equals(command)) {
            getFreeColClient().quit();
        } else {
            getFreeColClient().continuePlaying();
            getGUI().removeFromCanvas(this);
        }
    }
}
