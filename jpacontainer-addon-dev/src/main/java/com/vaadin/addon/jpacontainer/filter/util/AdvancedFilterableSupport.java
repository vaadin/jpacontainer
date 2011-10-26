/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.addon.jpacontainer.AdvancedFilterable;
import com.vaadin.addon.jpacontainer.Filter;
import com.vaadin.addon.jpacontainer.filter.CompositeFilter;
import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.filter.JoinFilter;
import com.vaadin.addon.jpacontainer.filter.PropertyFilter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;

/**
 * Helper class that implements the filtering methods defined in
 * {@link AdvancedFilterable} and can be either extended or used as a delegate.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class AdvancedFilterableSupport implements Serializable {

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
		if (filter instanceof JoinFilter) {
			return isFilterable(((JoinFilter) filter).getJoinProperty());
		} else if (filter instanceof PropertyFilter) {
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
	 * @see AdvancedFilterable#addFilter(com.vaadin.addon.jpacontainer.Filter)
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
	 * @see AdvancedFilterable#removeFilter(com.vaadin.addon.jpacontainer.Filter)
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

	/**
	 * Converts a Vaadin 6.6 container filter into a JPAContainer filter.
	 * 
	 * @param filter
	 *            Vaadin 6.6 {@link com.vaadin.data.Container.Filter}
	 * @return {@link com.vaadin.addon.jpacontainer.Filter}
	 */
	public static com.vaadin.addon.jpacontainer.Filter convertFilter(
			com.vaadin.data.Container.Filter filter) {
		assert filter != null : "filter must not be null";

		// Handle compound filters
		if (filter instanceof And) {
			return Filters.and(convertFilters(((And) filter).getFilters()));
		}
		if (filter instanceof Or) {
			return Filters.or(convertFilters(((Or) filter).getFilters()));
		}

		if (filter instanceof Compare) {
			Compare compare = (Compare) filter;
			switch (compare.getOperation()) {
			case EQUAL:
				return Filters.eq(compare.getPropertyId(), compare.getValue());
			case GREATER:
				return Filters.gt(compare.getPropertyId(), compare.getValue());
			case GREATER_OR_EQUAL:
				return Filters
						.gteq(compare.getPropertyId(), compare.getValue());
			case LESS:
				return Filters.lt(compare.getPropertyId(), compare.getValue());
			case LESS_OR_EQUAL:
				return Filters
						.lteq(compare.getPropertyId(), compare.getValue());
			}
		}

		if (filter instanceof IsNull) {
			return Filters.isNull(((IsNull) filter).getPropertyId());
		}

		if (filter instanceof SimpleStringFilter) {
			SimpleStringFilter stringFilter = (SimpleStringFilter) filter;
			String filterString = stringFilter.getFilterString();
			if (stringFilter.isOnlyMatchPrefix()) {
				filterString = filterString + "%";
			} else {
				filterString = "%" + filterString + "%";
			}
			return Filters.like(stringFilter.getPropertyId(), filterString,
					!stringFilter.isIgnoreCase());
		}

		return null;
	}

	/**
	 * Converts a collection of {@link com.vaadin.data.Container.Filter} into a
	 * list of {@link com.vaadin.addon.jpacontainer.Filter}.
	 * 
	 * @param filters
	 *            Collection of {@link com.vaadin.data.Container.Filter}
	 * @return List of {@link com.vaadin.addon.jpacontainer.Filter}
	 */
	public static List<Filter> convertFilters(
			Collection<com.vaadin.data.Container.Filter> filters) {
		List<Filter> result = new ArrayList<Filter>();
		for (com.vaadin.data.Container.Filter filter : filters) {
			result.add(convertFilter(filter));
		}
		return result;
	}
}
