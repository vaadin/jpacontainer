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
package com.vaadin.addons.jpacontainer.filter;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filterable;
import java.util.Collection;
import java.util.List;

/**
 * Container that supports a bit more advanced filtering than {@link Filterable}.
 *
 * @author Petter Holmström (IT Mill)
 */
public interface AdvancedFilterable extends Container {

    // TODO Improve documentation
    
    /**
     * Gets the IDs of all the properties that are filterable.
     *
     * @return an unmodifiable collection of property IDs (never null).
     */
    public Collection<Object> getFilterablePropertyIds();

    /**
     * Checks if <code>propertyId</code> is filterable.
     *
     * @param propertyId the property ID to check (must not be null).
     * @return true if the property is filterable, false otherwise.
     */
    public boolean isFilterable(Object propertyId);

    /**
     * Adds <code>filter</code> to the end of the list of filters to apply.
     * If it already exists in the list of filters, it will be applied a second time.
     *
     * @param filter the filter to add (must not be null).
     * @throws IllegalArgumentException if the filter could not be added (e.g. due to a nonfilterable property ID).
     */
    public void addFilter(Filter filter) throws IllegalArgumentException;

    /**
     * Removes <code>filter</code> from the list of filters to apply. If the filter
     * has been added several times, the first occurence will be removed. If the filter
     * has not been added, nothing happens.
     *
     * @param filter the filter to remove (must not be null).
     */
    public void removeFilter(Filter filter);

    /**
     * Gets the list of filters to apply. The filters will be applied as a conjunction
     * (i.e. AND) in the order they appear in.
     * 
     * @return an unmodifiable list of filters (never null).
     */
    public List<Filter> getFilters();
}
