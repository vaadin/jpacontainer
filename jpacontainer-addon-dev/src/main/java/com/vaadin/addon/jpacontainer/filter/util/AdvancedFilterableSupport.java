/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import com.vaadin.addon.jpacontainer.AdvancedFilterable;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.AbstractJunctionFilter;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.SimpleStringFilter;

/**
 * Helper class that implements the filtering methods defined in
 * {@link AdvancedFilterable} and can be either extended or used as a delegate.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class AdvancedFilterableSupport implements AdvancedFilterable,
        Serializable {

    private static final long serialVersionUID = 398382431841547719L;

    /**
     * ApplyFiltersListener interface to be implemented by classes that want to
     * be notified when the filters are applied.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public static interface ApplyFiltersListener extends Serializable {

        /**
         * Called when the filters have been applied.
         * 
         * @param sender
         *            the sender of the event.
         */
        public void filtersApplied(AdvancedFilterableSupport sender);
    }

    /**
     * Adds <code>listener</code> to the list of listeners to be notified when
     * the filters are applied. The listener will be notified as many times as
     * it has been added.
     * 
     * @param listener
     *            the listener to add (must not be null).
     */
    public void addListener(ApplyFiltersListener listener) {
        assert listener != null : "listener must not be null";
        listeners.add(listener);
    }

    /**
     * Removes <code>listener</code> from the list of listeners. If the listener
     * has been added more than once, it will be notified one less time. If the
     * listener has not been added at all, nothing happens.
     * 
     * @param listener
     *            the listener to remove (must not be null).
     */
    public void removeListener(ApplyFiltersListener listener) {
        assert listener != null : "listener must not be null";
        listeners.remove(listener);
    }

    @SuppressWarnings("unchecked")
    protected void fireListeners() {
        LinkedList<ApplyFiltersListener> listenerList = (LinkedList<ApplyFiltersListener>) listeners
                .clone();
        for (ApplyFiltersListener l : listenerList) {
            l.filtersApplied(this);
        }
    }

    private Collection<Object> filterablePropertyIds;

    private LinkedList<ApplyFiltersListener> listeners = new LinkedList<ApplyFiltersListener>();

    private LinkedList<Filter> appliedFilters = new LinkedList<Filter>();

    private LinkedList<Filter> filters = new LinkedList<Filter>();

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
     * 
     * @param propertyIds
     *            the property IDs to set (must not be null).
     */
    @SuppressWarnings("unchecked")
    public void setFilterablePropertyIds(Collection<?> propertyIds) {
        assert propertyIds != null : "propertyIds must not be null";
        filterablePropertyIds = (Collection<Object>) propertyIds;
    }

    /**
     * Sets the filterable property IDs.
     * 
     * @param propertyIds
     *            the property IDs to set (must not be null).
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
     * Checks if <code>filter</code> is a valid filter, i.e. that all the
     * properties that the filter restricts are filterable.
     * 
     * @param filter
     *            the filter to check (must not be null).
     * @return true if the filter is valid, false if it is not.
     */
    public boolean isValidFilter(Filter filter) {
        assert filter != null : "filter must not be null";
        if (filter instanceof Between) {
            return isFilterable(((Between) filter).getPropertyId());
        } else if (filter instanceof Compare) {
            return isFilterable(((Compare) filter).getPropertyId());
        } else if (filter instanceof IsNull) {
            return isFilterable(((IsNull) filter).getPropertyId());
        } else if (filter instanceof Like) {
            return isFilterable(((Like) filter).getPropertyId());
        } else if (filter instanceof SimpleStringFilter) {
            return isFilterable(((SimpleStringFilter) filter).getPropertyId());
        } else if (filter instanceof AbstractJunctionFilter) {
            for (Filter f : ((AbstractJunctionFilter) filter).getFilters()) {
                if (!isValidFilter(f)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @see AdvancedFilterable#getFilters()
     */
    public List<Filter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    /**
     * @see AdvancedFilterable#getAppliedFilters()
     */
    public List<Filter> getAppliedFilters() {
        return isApplyFiltersImmediately() ? getFilters() : Collections
                .unmodifiableList(appliedFilters);
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
        unappliedFilters = false;
        appliedFilters.clear();
        appliedFilters.addAll(filters);
        fireListeners();
    }

    /**
     * @see AdvancedFilterable#hasUnappliedFilters()
     */
    public boolean hasUnappliedFilters() {
        return unappliedFilters;
    }

    @SuppressWarnings("unchecked")
    public static Path<String> getPropertyPath(Root<?> root, Object propertyId) {
        return (Path<String>) getPropertyPathTyped(root, propertyId);
    }

    public static <T> Path<T> getPropertyPathTyped(Root<T> root,
            Object propertyId) {
        String pid = propertyId.toString();
        String[] idStrings = pid.split("\\.");
        Path<T> path = root.get(idStrings[0]);
        for (int i = 1; i < idStrings.length; i++) {
            path = path.get(idStrings[i]);
        }
        return path;
    }

    /**
     * @param filter
     */
    public void addFilter(Filter filter) {
        filters.add(filter);
        unappliedFilters = true;
        if (isApplyFiltersImmediately()) {
            applyFilters();
        }
    }

    /**
     * @param filter
     */
    public void removeFilter(Filter filter) {
        filters.remove(filter);
        unappliedFilters = true;
        if (isApplyFiltersImmediately()) {
            applyFilters();
        }
    }

    public void removeAllFilters() {
        filters.clear();
        unappliedFilters = true;
        if (isApplyFiltersImmediately()) {
            applyFilters();
        }
    }
}
