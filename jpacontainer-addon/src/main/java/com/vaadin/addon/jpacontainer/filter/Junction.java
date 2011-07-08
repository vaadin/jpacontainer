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

/**
 * A filter that groups other filters together using some associative logical
 * operator.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface Junction extends CompositeFilter {

	/**
	 * Adds <code>filter</code> to the end of the list of filters. If it has
	 * already been added, it will be added again.
	 * 
	 * @param filter
	 *            the filter to add (must not be null).
	 * @return <code>this</code>, to allow chaining.
	 */
	public Junction add(Filter filter);

	/**
	 * Removes <code>filter</code> from the list of filters. If it has been
	 * added more than once, only the first occurence will be remoed. If it has
	 * never been added, nothing happens.
	 * 
	 * @param filter
	 *            the filter to remove (must not be null).
	 * @return <code>this</code>, to allow chaining.
	 */
	public Junction remove(Filter filter);
}
