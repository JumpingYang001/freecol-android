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

/**
 * An object with a name that can be changed.
 */
public interface Nameable {


    /**
    * Gets the name of this <code>Named</code>.
    * @return The name of the <code>Named</code>.
    */
    public String getName();

    /**
    * Sets the name for this <code>Nameable</code>.
    * @param newName The new name for the <code>Nameable</code>.
    */
    public void setName(String newName);

}
