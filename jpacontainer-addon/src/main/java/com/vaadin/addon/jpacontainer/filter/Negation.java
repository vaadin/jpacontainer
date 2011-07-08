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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Filter that negates another filter.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class Negation implements CompositeFilter {

	private static final long serialVersionUID = -8799310376244240397L;
	private Filter filter;
	private List<Filter> filterList = new LinkedList<Filter>();

	protected Negation(Filter filter) {
		assert filter != null : "filter must not be null";
		this.filter = filter;
		this.filterList.add(filter);
		this.filterList = Collections.unmodifiableList(this.filterList);
	}

	public String toQLString() {
		return toQLString(PropertyIdPreprocessor.DEFAULT);
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		return String.format("(not %s)", filter
				.toQLString(propertyIdPreprocessor));
	}

	public List<Filter> getFilters() {
		return filterList;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == getClass()
				&& ((Negation) obj).filter.equals(this.filter);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() + 7 * filter.hashCode();
	}
}
