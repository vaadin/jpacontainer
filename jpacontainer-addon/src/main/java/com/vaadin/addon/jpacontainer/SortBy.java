/*
 * JPAContainer
 * Copyright (C) 2010-2011 Oy Vaadin Ltd
 *
 * This program is available both under Commercial Vaadin Add-On
 * License 2.0 (CVALv2) and under GNU Affero General Public License (version
 * 3 or later) at your option.
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and CVALv2 along with this program.  If not, see
 * <http://www.gnu.org/licenses/> and <http://vaadin.com/license/cval-2.0>.
 */
package com.vaadin.addon.jpacontainer;

import java.io.Serializable;

/**
 * Data structure class representing a field to sort by and the direction of the
 * sort (ascending or descending). Once created, the instances of this class are
 * immutable.
 * 
 * @author Petter Holmström (IT Mill)
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
	 * @param propertyId the property ID to sort by (must not be null).
	 * @param ascending true to sort ascendingly, false to sort descendingly.
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
