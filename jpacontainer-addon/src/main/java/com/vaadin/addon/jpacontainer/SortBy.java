/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vaadin.addon.jpacontainer;

import java.io.Serializable;

/**
 * Data structure class representing a field to sort by and the direction of the
 * sort (ascending or descending). Once created, the instances of this class are
 * immutable.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public final class SortBy implements Serializable {

    private static final long serialVersionUID = -6308560006578484770L;

    /**
     * The property ID to sort by.
     */
    private final Object propertyId;

    /**
     * True to sort ascendingly, false to sort descendingly.
     */
    private final boolean ascending;

    /**
     * Gets the property ID to sort by.
     */
    public Object getPropertyId() {
        return propertyId;
    }

    /**
     * Returns true to sort ascendingly, false to sort descendingly.
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Creates a new <code>SortBy</code> instance.
     * 
     * @param propertyId
     *            the property ID to sort by (must not be null).
     * @param ascending
     *            true to sort ascendingly, false to sort descendingly.
     */
    public SortBy(Object propertyId, boolean ascending) {
        assert propertyId != null : "propertyId must not be null";
        this.propertyId = propertyId;
        this.ascending = ascending;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
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
