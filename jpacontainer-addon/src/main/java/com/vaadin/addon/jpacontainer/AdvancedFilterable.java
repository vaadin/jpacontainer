/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vaadin.addon.jpacontainer;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.vaadin.addon.jpacontainer.filter.converter.IFilterConverter;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Container.ItemSetChangeEvent;

/**
 * Container that supports a bit more advanced filtering than {@link Filterable}
 * . It has been designed to be used explicitly with JPA.
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

    /**
     * Add converter that can convert a certain kind of {@link Filter} to a
     * {@link Predicate}
     * 
     * @param filterConverter
     *            converter that can convert a certain kind of {@link Filter} to
     *            a {@link Predicate}
     */
    void addFilterConverter(IFilterConverter filterConverter);

    /**
     * Remove converter that can convert a certain kind of {@link Filter} to a
     * {@link Predicate}
     * 
     * @param filterConverter
     *            converter that can convert a certain kind of {@link Filter} to
     *            a {@link Predicate}
     */
    void removeFilterConverter(IFilterConverter filterConverter);

    /**
     * Check if this object contains {@code filterConverter}.
     * 
     * @param filterConverter
     * @return true, if this object contains {@code filterConverter}.
     */
    boolean containsFilterConverter(IFilterConverter filterConverter);

    /**
     * Converts a collection of {@link Filter} into a array of {@link Predicate}
     * .
     * 
     * @param filters
     *            Collection of {@link Filter}
     * @return Array of {@link Predicate}
     */
    <X, Y> Predicate[] convertFiltersToArray(Collection<Filter> filters,
            CriteriaBuilder criteriaBuilder, From<X, Y> root);

    /**
     * Converts a collection of {@link Filter} into a list of {@link Predicate}.
     * 
     * @param filters
     *            Collection of {@link Filter}
     * @return List of {@link Predicate}
     */
    <X, Y> List<Predicate> convertFilters(Collection<Filter> filters,
            CriteriaBuilder criteriaBuilder, From<X, Y> root);

    /**
     * Convert a single {@link Filter} to a criteria {@link Predicate}.
     * 
     * @param filter
     *            the {@link Filter} to convert
     * @param criteriaBuilder
     *            the {@link CriteriaBuilder} to use when creating the
     *            {@link Predicate}
     * @param root
     *            the {@link CriteriaQuery} {@link Root} to use for finding
     *            fields.
     * @return a {@link Predicate} representing the {@link Filter} or null if
     *         conversion failed.
     */
    <X, Y> Predicate convertFilter(Filter filter,
            CriteriaBuilder criteriaBuilder, From<X, Y> root);

    /**
     * Get property path
     * 
     * @param root
     * @param propertyId
     * @return
     */
    <X, Y> Path<X> getPropertyPathTyped(From<X, Y> root, Object propertyId);

    /**
     * Get property path
     * 
     * @param root
     * @param propertyId
     * @return
     */
    Path<String> getPropertyPath(From<?, ?> root, Object propertyId);

}
