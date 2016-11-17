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
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JButton;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.border.BevelBorder;
import org.freecolandroid.repackaged.javax.swing.border.CompoundBorder;
import org.freecolandroid.repackaged.javax.swing.border.EmptyBorder;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.action.ActionManager;
import net.sf.freecol.client.gui.action.ContinueAction;
import net.sf.freecol.client.gui.action.MapEditorAction;
import net.sf.freecol.client.gui.action.NewAction;
import net.sf.freecol.client.gui.action.OpenAction;
import net.sf.freecol.client.gui.action.PreferencesAction;
import net.sf.freecol.client.gui.action.QuitAction;
import net.sf.freecol.common.resources.ResourceManager;

/**
* A panel filled with 'main' items.
*/
public final class MainPanel extends FreeColPanel {

    private static final Logger logger = Logger.getLogger(MainPanel.class.getName());


    /**
    * The constructor that will add the items to this panel.
     * @param freeColClient 
    * @param parent The parent of this panel.
    */
    public MainPanel(FreeColClient freeColClient, GUI gui) {
        super(freeColClient, gui, new MigLayout("wrap 1, insets n n 20 n", "[center]"));
        boolean canContinue = getFreeColClient().getInGameController()
            .getLastSaveGameFile() != null;

        ActionManager am = getFreeColClient().getActionManager();
        JButton newButton = new JButton(am.getFreeColAction(NewAction.id));
        JButton openButton = new JButton(am.getFreeColAction(OpenAction.id));
        JButton mapEditorButton = new JButton(am.getFreeColAction(MapEditorAction.id));
        JButton optionsButton = new JButton(am.getFreeColAction(PreferencesAction.id));
        JButton quitButton = new JButton(am.getFreeColAction(QuitAction.id));

        setCancelComponent(quitButton);
        okButton.setAction(am.getFreeColAction( canContinue ? ContinueAction.id : NewAction.id));

        enterPressesWhenFocused(okButton);
        enterPressesWhenFocused(newButton);
        enterPressesWhenFocused(mapEditorButton);
        enterPressesWhenFocused(openButton);
        enterPressesWhenFocused(optionsButton);
        enterPressesWhenFocused(quitButton);

        Image tempImage = ResourceManager.getImage("TitleImage");

        if (tempImage != null) {
            JLabel logoLabel = new JLabel(new ImageIcon(tempImage));
            logoLabel.setBorder(new CompoundBorder(new EmptyBorder(2,2,0,2), new BevelBorder(BevelBorder.LOWERED)));
            add(logoLabel);
        }

        add(okButton, "newline 20, width 70%");
        if (canContinue) {
           add(newButton, "width 70%");
        }
        add(openButton, "width 70%");
        add(mapEditorButton, "width 70%");
        add(optionsButton, "width 70%");
        add(quitButton, "width 70%");

        setSize(getPreferredSize());
    }
}
