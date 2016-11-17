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



package net.sf.freecol.client.gui.action;


import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;

/**
 * An action for selecting one of several options.
 */
public abstract class SelectableAction extends MapboardAction {

    public static final String id = "selectableAction";

    private String optionId;

    protected boolean selected = false;

    /**
     * Creates this action.
     * @param freeColClient The main controller object for the client
     * @param id a <code>String</code> value
     * @param optionId the id of a boolean client option
     */
    protected SelectableAction(FreeColClient freeColClient, GUI gui, String id, String optionId) {
        super(freeColClient, gui, id);
        this.optionId = optionId;
        setSelected(shouldBeSelected());
    }

    /**
     * Updates the "enabled" status with the value returned by {@link
     * #shouldBeEnabled} and the "selected" status with the value
     * returned by {@link #shouldBeSelected}.
     */
    @Override
    public void update() {
        super.update();

        final Game game = getFreeColClient().getGame();
        final Player player = getFreeColClient().getMyPlayer();
        if (game != null && player != null && !player.getNewModelMessages().isEmpty()) {
            enabled = false;
        }
        setSelected(shouldBeSelected());
    }

    /**
     * Returns whether the action is selected.
     *
     * @return <code>true</code> if the map controls is selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets whether the action is selected.
     *
     * @param b a <code>boolean</code> value
     */
    public void setSelected(boolean b) {
        this.selected = b;
    }

    /**
     * Returns true if this action should be selected.
     *
     * @return a <code>boolean</code> value
     */
    protected boolean shouldBeSelected() {
        if (freeColClient.getClientOptions() == null) {
            return false;
        } else {
            return freeColClient.getClientOptions().getBoolean(optionId);
        }
    }

    protected void updateOption(boolean value) {
        freeColClient.getClientOptions().setBoolean(optionId, value);
    }

}
