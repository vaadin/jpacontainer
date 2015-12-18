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

import com.vaadin.addon.jpacontainer.filter.ISubqueryProvider;
import com.vaadin.data.Container.Filter;

public interface IFilterTool {

    /**
     * Converts a collection of {@link Filter} into a array of {@link Predicate}
     * .
     * 
     * @param filters
     *            Collection of {@link Filter}
     * @return Array of {@link Predicate}
     */
    <X, Y> Predicate[] convertFiltersToArray(Collection<Filter> filters,
            CriteriaBuilder criteriaBuilder, From<X, Y> root,
            ISubqueryProvider subqueryProvider);

    /**
     * Converts a collection of {@link Filter} into a list of {@link Predicate}.
     * 
     * @param filters
     *            Collection of {@link Filter}
     * @return List of {@link Predicate}
     */
    <X, Y> List<Predicate> convertFilters(Collection<Filter> filters,
            CriteriaBuilder criteriaBuilder, From<X, Y> root,
            ISubqueryProvider subqueryProvider);

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
            CriteriaBuilder criteriaBuilder, From<X, Y> root,
            ISubqueryProvider subqueryProvider);

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