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
package com.vaadin.addon.jpacontainer.filter;

import com.vaadin.addon.jpacontainer.Filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for {@link Junction}-implementations.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class AbstractJunction implements Junction {

	private static final long serialVersionUID = 4635505131153450999L;
	private List<Filter> filters = new ArrayList<Filter>();

	protected AbstractJunction(Filter... filters) {
		assert filters != null : "filters must not be null";

		for (Filter f : filters) {
			this.filters.add(f);
		}
	}

	protected AbstractJunction(List<Filter> filters) {
		assert filters != null : "filters must not be null";
		// We do not copy the filters instance directly, as we have
		// to make sure the internal list instance is writable, etc.
		for (Filter f : filters) {
			this.filters.add(f);
		}
	}

	public Junction add(Filter filter) {
		assert filter != null : "filter must not be null";
		filters.add(filter);
		return this;
	}

	public Junction remove(Filter filter) {
		assert filter != null : "filter must not be null";
		filters.remove(filter);
		return this;
	}

	public List<Filter> getFilters() {
		return Collections.unmodifiableList(filters);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return obj.getClass() == getClass()
				&& ((AbstractJunction) obj).filters.equals(filters);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 7 * filters.hashCode();
	}
}
