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

package net.sf.freecol.common.model;

import java.util.logging.Logger;


/**
 * Helper container to remember the Europe state prior to some
 * change, and fire off any consequent property changes.
 */
public class EuropeWas {

    private static final Logger logger = Logger.getLogger(EuropeWas.class.getName());

    private Europe europe;
    private int unitCount;


    public EuropeWas(Europe europe) {
        this.europe = europe;
        this.unitCount = europe.getUnitCount();
    }

    /**
     * Fire any property changes resulting from actions in Europe.
     */
    public void fireChanges() {
        int newUnitCount = europe.getUnitCount();

        if (newUnitCount != unitCount) {
            String pc = Europe.UNIT_CHANGE.toString();
            europe.firePropertyChange(pc, unitCount, newUnitCount);
        }
    }
}
