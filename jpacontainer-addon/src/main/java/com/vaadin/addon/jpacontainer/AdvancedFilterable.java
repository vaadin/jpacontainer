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
package com.vaadin.addon.jpacontainer;

import java.util.Collection;
import java.util.List;

import com.vaadin.data.Container;

/**
 * Container that supports a bit more advanced filtering than {@link Filterable}
 * . It has been designed to be used explicitly with JPA (e.g. all filters
 * generate JPA-QL).
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface AdvancedFilterable extends Container {

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
	 * Adds <code>filter</code> to the end of the list of filters to apply. If
	 * it already exists in the list of filters, it will be applied a second
	 * time.
	 * <p>
	 * If {@link #isApplyFiltersImmediately() } returns true, the filter will be
	 * applied immediately and the container updated.
	 * 
	 * @param filter
	 *            the filter to add (must not be null).
	 * @throws IllegalArgumentException
	 *             if the filter could not be added (e.g. due to a nonfilterable
	 *             property ID).
	 */
	public void addFilter(com.vaadin.addon.jpacontainer.Filter filter)
			throws IllegalArgumentException;

	/**
	 * Removes <code>filter</code> from the list of filters to apply. If the
	 * filter has been added several times, the first occurence will be removed.
	 * If the filter has not been added, nothing happens.
	 * <p>
	 * If {@link #isApplyFiltersImmediately() } returns true, the container will
	 * be updated immediately.
	 * 
	 * @param filter
	 *            the filter to remove (must not be null).
	 */
	public void removeFilter(com.vaadin.addon.jpacontainer.Filter filter);

	/**
	 * Removes all filters.
	 * <p>
	 * If {@link #isApplyFiltersImmediately() } returns true, the container will
	 * be updated immediately.
	 */
	public void removeAllFilters();

	/**
	 * Gets the list of filters to apply. The filters will be applied as a
	 * conjunction (i.e. AND) in the order they appear in.
	 * 
	 * @return an unmodifiable list of filters (never null).
	 */
	public List<com.vaadin.addon.jpacontainer.Filter> getFilters();

	/**
	 * Gets the list of filters that are currently applied. If
	 * {@link #isApplyFiltersImmediately() } returns true, this list will be the
	 * same as the one returned by {@link #getFilters() }.
	 * 
	 * @return an unmodifiable list of filters (never null).
	 */
	public List<com.vaadin.addon.jpacontainer.Filter> getAppliedFilters();

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
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	public static class FiltersAppliedEvent implements ItemSetChangeEvent {

		private static final long serialVersionUID = -6988494009645932112L;
		private final AdvancedFilterable container;

		/**
		 * Creates a new <code>FiltersAppliedEvent</code>.
		 * 
		 * @param container
		 *            the filterable container that fired the event.
		 */
		public FiltersAppliedEvent(AdvancedFilterable container) {
			this.container = container;
		}

		public AdvancedFilterable getContainer() {
			return container;
		}
	}
}
