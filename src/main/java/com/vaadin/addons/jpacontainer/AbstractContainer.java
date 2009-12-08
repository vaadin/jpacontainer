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

import com.vaadin.addons.jpacontainer.filter.AdvancedFilterable;
import com.vaadin.addons.jpacontainer.filter.Filter;
import com.vaadin.addons.jpacontainer.filter.util.AdvancedFilterableSupport;
import com.vaadin.data.Container;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An abstract base class for containers that implement some of the more general,
 * non-datasource related functions.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class AbstractContainer implements Container.Sortable,
        AdvancedFilterable, Container.ItemSetChangeNotifier,
        AdvancedFilterableSupport.Listener {

    private LinkedList<ItemSetChangeListener> listeners;

    private List<SortBy> sortByList;

    private Collection<Object> containerPropertyIds;

    private Collection<Object> sortableContainerPropertyIds;

    private AdvancedFilterableSupport filterSupport =
            new AdvancedFilterableSupport();

    /**
     * Gets all the properties that the items should be sorted by, if any.
     *
     * @return an unmodifiable, possible empty list of <code>SortBy</code>
     *      instances (never null).
     */
    protected List<SortBy> getSortBy() {
        if (sortByList == null) {
            return Collections.emptyList();
        } else {
            return sortByList;
        }
    }

    @Override
    public Collection<Object> getFilterablePropertyIds() {
        return filterSupport.getFilterablePropertyIds();
    }

    /**
     * @see AdvancedFilterableSupport#setFilterablePropertyIds(java.lang.Object[]) 
     */
    protected void setFilterablePropertyIds(Object... propertyIds) {
        filterSupport.setFilterablePropertyIds(propertyIds);
    }

    /**
     * @see AdvancedFilterableSupport#setFilterablePropertyIds(java.util.Collection)
     */
    protected void setFilterablePropertyIds(Collection<Object> propertyIds) {
        filterSupport.setFilterablePropertyIds(propertyIds);
    }

    @Override
    public boolean isFilterable(Object propertyId) {
        return filterSupport.isFilterable(propertyId);
    }

    @Override
    public void addFilter(Filter filter) throws IllegalArgumentException {
        filterSupport.addFilter(filter);
    }

    @Override
    public void removeFilter(Filter filter) {
        filterSupport.removeFilter(filter);
    }

    @Override
    public List<Filter> getFilters() {
        return filterSupport.getFilters();
    }

    @Override
    public void removeAllFilters() {
        filterSupport.removeAllFilters();
    }

    @Override
    public void setApplyFiltersImmediately(boolean applyFiltersImmediately) {
        filterSupport.setApplyFiltersImmediately(applyFiltersImmediately);
    }

    @Override
    public boolean isApplyFiltersImmediately() {
        return filterSupport.isApplyFiltersImmediately();
    }

    @Override
    public List<Filter> getAppliedFilters() {
        return filterSupport.getAppliedFilters();
    }

    @Override
    public void applyFilters() {
        filterSupport.applyFilters();
    }

    @Override
    public boolean hasUnappliedFilters() {
        return filterSupport.hasUnappliedFilters();
    }

    @Override
    public void filtersApplied(AdvancedFilterableSupport sender) {
        if (sender == filterSupport) {
            fireContainerItemSetChange(new ContainerFilteredEvent());
        }
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        assert propertyId != null : "propertyId must not be null";
        assert ascending != null : "ascending must not be null";
        assert propertyId.length == ascending.length :
                "propertyId and ascending must have the same length";
        sortByList = new LinkedList<SortBy>();
        for (int i = 0; i < propertyId.length; ++i) {
            if (!getSortableContainerPropertyIds().contains(propertyId[i])) {
                throw new IllegalArgumentException(
                        "No such sortable property ID: " + propertyId[i]);
            }
            sortByList.add(new SortBy(propertyId[i], ascending[i]));
        }
        fireContainerItemSetChange(new ContainerSortedEvent());
        sortByList = Collections.unmodifiableList(sortByList);
    }

    @Override
    public void addListener(ItemSetChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listeners == null) {
            listeners = new LinkedList<ItemSetChangeListener>();
        }
        listeners.add(listener);
    }

    @Override
    public void removeListener(ItemSetChangeListener listener) {
        if (listener != null && listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Publishes <code>event</code> to all registered
     * <code>ItemSetChangeListener</code>s.
     *
     * @param event the event to publish (must not be null).
     */
    protected void fireContainerItemSetChange(final ItemSetChangeEvent event) {
        assert event != null : "event must not be null";
        LinkedList<ItemSetChangeListener> list =
                (LinkedList<ItemSetChangeListener>) listeners.clone();
        for (ItemSetChangeListener l : list) {
            l.containerItemSetChange(event);
        }
    }

    @Override
    public Collection<Object> getContainerPropertyIds() {
        if (containerPropertyIds == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableCollection(containerPropertyIds);
        }
    }

    /**
     * Sets the IDs of the properties that can be accessed from this container.
     *
     * @param propertyIds the property IDs (must not be null).
     */
    protected void setContainerPropertyIds(Object... propertyIds) {
        assert propertyIds != null : "propertyIds must not be null";
        setContainerPropertyIds(Arrays.asList(propertyIds));
    }

    /**
     * Sets the IDs of the properties that can be accessed from this container.
     *
     * @param propertyIds the property IDs (must not be null).
     */
    protected void setContainerPropertyIds(Collection<Object> propertyIds) {
        assert propertyIds != null : "propertyIds must not be null";
        containerPropertyIds = propertyIds;
    }

    @Override
    public Collection<Object> getSortableContainerPropertyIds() {
        if (sortableContainerPropertyIds == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableCollection(
                    sortableContainerPropertyIds);
        }
    }

    /**
     * Sets the IDs of the properties that are sortable.
     *
     * @param propertyIds the property IDs (must not be null).
     */
    protected void setSortableContainerPropertyIds(Object... propertyIds) {
        assert propertyIds != null : "propertyIds must not be null";
        setSortableContainerPropertyIds(Arrays.asList(propertyIds));
    }

    /**
     * Sets the IDs of the properties that are sortable.
     *
     * @param propertyIds the property IDs (must not be null).
     */
    protected void setSortableContainerPropertyIds(
            Collection<Object> propertyIds) {
        assert propertyIds != null : "propertyIds must not be null";
        sortableContainerPropertyIds = propertyIds;
    }

    /**
     * Event indicating that the container has been resorted.
     * 
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public final class ContainerSortedEvent implements ItemSetChangeEvent {

        protected ContainerSortedEvent() {
        }

        @Override
        public Container getContainer() {
            return AbstractContainer.this;
        }
    }

    /**
     * Event indicating that the container has been filtered.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public final class ContainerFilteredEvent implements ItemSetChangeEvent {

        protected ContainerFilteredEvent() {
        }

        @Override
        public Container getContainer() {
            return AbstractContainer.this;
        }
    }
}
