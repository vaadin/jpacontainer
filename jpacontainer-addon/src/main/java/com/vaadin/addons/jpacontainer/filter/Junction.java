/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.filter;

import com.vaadin.addons.jpacontainer.Filter;

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
