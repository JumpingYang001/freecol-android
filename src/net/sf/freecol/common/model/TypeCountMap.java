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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


// TODO: implement entire Map interface
public class TypeCountMap<T extends FreeColGameObjectType> {

    private Map<T, Integer> values = new HashMap<T, Integer>();

    public Map<T, Integer> getValues() {
        return values;
    }

    public int getCount(T key) {
        Integer value = values.get(key);
        return value == null ? 0 : value.intValue();
    }

    public Integer incrementCount(T key, int newCount) {
        Integer oldValue = values.get(key);
        if (oldValue == null) {
            return values.put(key, new Integer(newCount));
        } else if (oldValue == -newCount) {
            values.remove(key);
            return null;
        } else {
            return values.put(key, oldValue + newCount);
        }
    }

    public void add(TypeCountMap<T> other) {
        for (Map.Entry<T, Integer> entry : other.values.entrySet()) {
            incrementCount(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        values.clear();
    }

    public Set<T> keySet() {
        return values.keySet();
    }

    public Collection<Integer> values() {
        return values.values();
    }

    public boolean containsKey(T key) {
        return values.containsKey(key);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public int size() {
        return values.size();
    }

    public void putAll(TypeCountMap<T> other) {
        values.putAll(other.values);
    }


    @Override
    public String toString() {
        String result = "[" + getClass().getName();
        for (Map.Entry<T, Integer> entry : values.entrySet()) {
            result += " [" + entry.getKey().getIndex()
                + "," + entry.getValue() + "]";
        }
        result += "]";
        return result;
    }
}
