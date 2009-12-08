/*
 * JPAContainer
 * Copyright (C) 2009 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer;

import java.io.Serializable;

/**
 * Internal data structure class representing a sort instruction.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public final class SortBy implements Serializable {

    /**
     * The property ID to sort by.
     */
    public final Object propertyId;

    /**
     * True to sort ascendingly, false to sort descendingly.
     */
    public final boolean ascending;

    protected SortBy(Object propertyId, boolean ascending) {
        this.propertyId = propertyId;
        this.ascending = ascending;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == getClass()) {
            SortBy o = (SortBy) obj;
            return o.propertyId.equals(propertyId) && o.ascending == ascending;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = propertyId.hashCode();
        hash = hash * 7 + new Boolean(ascending).hashCode();
        return hash;
    }
}
