/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import java.util.Collection;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.ItemSetChangeEvent;

/**
 * Container that supports a bit more advanced filtering than {@link Filterable}
 * . It has been designed to be used explicitly with JPA (e.g. all filters
 * generate JPA-QL).
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface AdvancedFilterable {

    /**
     * Gets the IDs of all the properties that are filterable.
     * 
     * @return an unmodifiable collection of property IDs (never null).
     */
    public Collection<Object> getFilterablePropertyIds();

    /**
     * Checks if <code>propertyId</code> is filterable.
     * 
     * @param propertyId
     *            the property ID to check (must not be null).
     * @return true if the property is filterable, false otherwise.
     */
    public boolean isFilterable(Object propertyId);

    /**
     * Gets the list of filters to apply. The filters will be applied as a
     * conjunction (i.e. AND) in the order they appear in.
     * 
     * @return an unmodifiable list of filters (never null).
     */
    public List<Filter> getFilters();

    /**
     * Gets the list of filters that are currently applied. If
     * {@link #isApplyFiltersImmediately() } returns true, this list will be the
     * same as the one returned by {@link #getFilters() }.
     * 
     * @return an unmodifiable list of filters (never null).
     */
    public List<Filter> getAppliedFilters();

    /**
     * Sets whether the filters should be applied immediately when a filter is
     * added or removed.
     * 
     * @see #isApplyFiltersImmediately()
     * @param applyFiltersImmediately
     *            true to apply filters immediately, false to apply when
     *            {@link #applyFilters() } is called.
     */
    public void setApplyFiltersImmediately(boolean applyFiltersImmediately);

    /**
     * Returns whether the filters should be applied immediately when a filter
     * is added or removed. Default is true. If false, {@link #applyFilters() }
     * has to be called to apply the filters and update the container.
     * 
     * @see #setApplyFiltersImmediately(boolean)
     * @return true if the filters are applied immediately, false otherwise.
     */
    public boolean isApplyFiltersImmediately();

    /**
     * Applies the filters to the data, possibly causing the items in the
     * container to change.
     * 
     * @see FiltersAppliedEvent
     */
    public void applyFilters();

    /**
     * Checks if there are filters that have not yet been applied, or applied
     * filters that have been removed using {@link #removeAllFilters() } or
     * {@link #removeFilter(com.vaadin.addon.jpacontainer.Filter) }.
     * <p>
     * If {@link #isApplyFiltersImmediately() } is true, this method always
     * returns false.
     * 
     * @see #applyFilters()
     * @return true if there are unapplied filters, false otherwise.
     */
    public boolean hasUnappliedFilters();

    /**
     * This event indicates that the filters of a {@link AdvancedFilterable}
     * have been applied. If an implementation of {@link AdvancedFilterable}
     * also implements {@link com.vaadin.data.Container$ItemSetChangeNotifier},
     * this event should be fired every time
     * {@link AdvancedFilterable#applyFilters() } has been executed successfully.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public static class FiltersAppliedEvent<C extends Container & AdvancedFilterable>
            implements ItemSetChangeEvent {

        private static final long serialVersionUID = -6988494009645932112L;
        private final C container;

        /**
         * Creates a new <code>FiltersAppliedEvent</code>.
         * 
         * @param container
         *            the filterable container that fired the event.
         */
        public FiltersAppliedEvent(C container) {
            this.container = container;
        }

        public C getContainer() {
            return container;
        }
    }
}
