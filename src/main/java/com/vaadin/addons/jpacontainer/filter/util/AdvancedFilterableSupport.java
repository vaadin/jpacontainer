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
package com.vaadin.addons.jpacontainer.filter.util;

import com.vaadin.addons.jpacontainer.filter.AdvancedFilterable;
import com.vaadin.addons.jpacontainer.filter.CompositeFilter;
import com.vaadin.addons.jpacontainer.filter.Filter;
import com.vaadin.addons.jpacontainer.filter.PropertyFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class that implements the filtering methods defined in
 * {@link AdvancedFilterable} and can be either extended or used as a
 * delegate.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class AdvancedFilterableSupport {

    /**
     * Listener interface to be implemented by classes that want to be
     * notified when the filters are applied.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public static interface Listener {

        /**
         * Called when the filters have been applied.
         * 
         * @param sender the sender of the event.
         */
        public void filtersApplied(AdvancedFilterableSupport sender);
    }

    /**
     * Adds <code>listener</code> to the list of listeners to be notified when
     * the filters are applied. The listener will be notified as many times
     * as it has been added.
     *
     * @param listener the listener to add (must not be null).
     */
    public void addListener(Listener listener) {
        assert listener != null : "listener must not be null";
        listeners.add(listener);
    }

    /**
     * Removes <code>listener</code> from the list of listeners. If the listener
     * has been added more than once, it will be notified one less time. If the
     * listener has not been added at all, nothing happens.
     * 
     * @param listener the listener to remove (must not be null).
     */
    public void removeListener(Listener listener) {
        assert listener != null : "listener must not be null";
        listeners.remove(listener);
    }

    protected void fireListeners() {
        LinkedList<Listener> listenerList = (LinkedList<Listener>) listeners.
                clone();
        for (Listener l : listenerList) {
            l.filtersApplied(this);
        }
    }

    private Collection<Object> filterablePropertyIds;

    private LinkedList<Listener> listeners = new LinkedList<Listener>();

    private List<Filter> filters = new LinkedList<Filter>();

    private boolean applyFiltersImmediately = true;

    private boolean unappliedFilters = false;

    /**
     * @see AdvancedFilterable#getFilterablePropertyIds() 
     */
    public Collection<Object> getFilterablePropertyIds() {
        if (filterablePropertyIds == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableCollection(filterablePropertyIds);
        }
    }

    /**
     * Sets the filterable property IDs.
     * @param propertyIds the property IDs to set (must not be null).
     */
    public void setFilterablePropertyIds(Collection<Object> propertyIds) {
        assert propertyIds != null : "propertyIds must not be null";
        this.filterablePropertyIds = propertyIds;
    }

    /**
     * Sets the filterable property IDs.
     * @param propertyIds the property IDs to set (must not be null).
     */
    public void setFilterablePropertyIds(Object... propertyIds) {
        assert propertyIds != null : "propertyIds must not be null";
        setFilterablePropertyIds(Arrays.asList(propertyIds));
    }

    /**
     * @see AdvancedFilterable#isFilterable(java.lang.Object)
     */
    public boolean isFilterable(Object propertyId) {
        return getFilterablePropertyIds().contains(propertyId);
    }

    /**
     * Checks if <code>filter</code> is a valid filter, i.e. that all
     * the properties that the filter restricts are filterable.
     *
     * @param filter the filter to check (must not be null).
     * @return true if the filter is valid, false if it is not.
     */
    public boolean isValidFilter(Filter filter) {
        assert filter != null : "filter must not be null";
        if (filter instanceof PropertyFilter) {
            return isFilterable(((PropertyFilter) filter).getPropertyId());
        } else if (filter instanceof CompositeFilter) {
            for (Filter f : ((CompositeFilter) filter).getFilters()) {
                if (!isValidFilter(f)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @see AdvancedFilterable#addFilter(com.vaadin.addons.jpacontainer.filter.Filter) 
     */
    public void addFilter(Filter filter) throws IllegalArgumentException {
        if (!isValidFilter(filter)) {
            throw new IllegalArgumentException("Invalid filter");
        }
        filters.add(filter);
        if (isApplyFiltersImmediately()) {
            fireListeners();
        } else {
            unappliedFilters = true;
        }
    }

    /**
     * @see AdvancedFilterable#removeFilter(com.vaadin.addons.jpacontainer.filter.Filter)
     */
    public void removeFilter(Filter filter) {
        assert filter != null : "filter must not be null";
        if (filters.remove(filter)) {
            if (isApplyFiltersImmediately()) {
                fireListeners();
            } else {
                unappliedFilters = true;
            }
        }
    }

    /**
     * @see AdvancedFilterable#removeAllFilters() 
     */
    public void removeAllFilters() {
        filters.clear();
        if (isApplyFiltersImmediately()) {
            fireListeners();
        } else {
            unappliedFilters = true;
        }
    }

    /**
     * @see AdvancedFilterable#getFilters()
     */
    public List<Filter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    /**
     * @see AdvancedFilterable#setApplyFiltersImmediately(boolean)
     */
    public void setApplyFiltersImmediately(boolean applyFiltersImmediately) {
        this.applyFiltersImmediately = applyFiltersImmediately;
    }

    /**
     * @see AdvancedFilterable#isApplyFiltersImmediately() 
     */
    public boolean isApplyFiltersImmediately() {
        return applyFiltersImmediately;
    }

    /**
     * @see AdvancedFilterable#applyFilters()
     */
    public void applyFilters() {
        fireListeners();
        unappliedFilters = false;
    }

    /**
     * @see AdvancedFilterable#hasUnappliedFilters() 
     */
    public boolean hasUnappliedFilters() {
        return unappliedFilters;
    }
}
