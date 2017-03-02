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

package com.vaadin.addon.jpacontainer.filter;

import java.util.List;

import com.vaadin.addon.jpacontainer.util.CollectionUtil;
import com.vaadin.v7.data.Container.Filter;
import com.vaadin.v7.data.util.filter.And;
import com.vaadin.v7.data.util.filter.Between;
import com.vaadin.v7.data.util.filter.Compare.Equal;
import com.vaadin.v7.data.util.filter.Compare.Greater;
import com.vaadin.v7.data.util.filter.Compare.GreaterOrEqual;
import com.vaadin.v7.data.util.filter.Compare.Less;
import com.vaadin.v7.data.util.filter.Compare.LessOrEqual;
import com.vaadin.v7.data.util.filter.IsNull;
import com.vaadin.v7.data.util.filter.Like;
import com.vaadin.v7.data.util.filter.Not;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;

/**
 * Utility class for creating filter instances.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public final class Filters {

    private Filters() {
        // To prevent applications from creating instances of this class.
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is null.
     */
    public static Filter isNull(Object propertyId) {
        return new IsNull(propertyId);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is not null.
     */
    public static Filter isNotNull(Object propertyId) {
        return new Not(isNull(propertyId));
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is empty.
     */
    public static Filter isEmpty(Object propertyId) {
        return new Equal(propertyId, "");
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is not empty.
     */
    public static Filter isNotEmpty(Object propertyId) {
        return new Not(isEmpty(propertyId));
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is equal to <code>value</code>.
     */
    public static Filter eq(Object propertyId, String value,
            boolean caseSensitive) {
        return new SimpleStringFilter(propertyId, value, !caseSensitive, false);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> matches <code>value</code>. The precent-sign (%)
     * may be used as wildcard.
     */
    public static Filter like(Object propertyId, String value,
            boolean caseSensitive) {
        return new Like(propertyId, value, caseSensitive);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is equal to <code>value</code>.
     */
    public static Filter eq(Object propertyId, Object value) {
        return new Equal(propertyId, value);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is greater than or equal to <code>value</code>.
     */
    public static Filter gteq(Object propertyId, Object value) {
        return new GreaterOrEqual(propertyId, value);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is greater than <code>value</code>.
     */
    public static Filter gt(Object propertyId, Object value) {
        return new Greater(propertyId, value);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is less than or equal to <code>value</code>.
     */
    public static Filter lteq(Object propertyId, Object value) {
        return new LessOrEqual(propertyId, value);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is less than <code>value</code>.
     */
    public static Filter lt(Object propertyId, Object value) {
        return new Less(propertyId, value);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is between <code>startingPoint</code> and
     * <code>endingPoint</code>.
     */
    public static Filter between(Object propertyId,
            Comparable<?> startingPoint, Comparable<?> endingPoint) {
        return new Between(propertyId, startingPoint, endingPoint);
    }

    public static Filter between(Object propertyId, Object startingPoint,
            Object endingPoint, boolean includeStartingPoint,
            boolean includeEndingPoint) {
        return new And((includeStartingPoint ? new GreaterOrEqual(propertyId,
                startingPoint) : new Greater(propertyId, startingPoint)),
                (includeEndingPoint ? new LessOrEqual(propertyId, endingPoint)
                        : new Less(propertyId, endingPoint)));
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is between <code>startingPoint</code> (inclusive)
     * and <code>endingPoint</code> (inclusive).
     */
    public static Filter betweenInclusive(Object propertyId,
            Object startingPoint, Object endingPoint) {
        return between(propertyId, startingPoint, endingPoint, true, true);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is between <code>startingPoint</code> (exclusive)
     * and <code>endingPoint</code> (exclusive).
     */
    public static Filter betweenExlusive(Object propertyId,
            Object startingPoint, Object endingPoint) {
        return between(propertyId, startingPoint, endingPoint, false, false);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is outside <code>startingPoint</code> and
     * <code>endingPoint</code>.
     */
    public static Filter outside(Object propertyId, Object startingPoint,
            Object endingPoint, boolean includeStartingPoint,
            boolean includeEndingPoint) {
        return new Or((includeStartingPoint ? new LessOrEqual(propertyId,
                startingPoint) : new Less(propertyId, startingPoint)),
                (includeEndingPoint ? new GreaterOrEqual(propertyId,
                        endingPoint) : new Greater(propertyId, endingPoint)));
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is outside <code>startingPoint</code> (inclusive)
     * and <code>endingPoint</code> (inclusive).
     */
    public static Filter outsideInclusive(Object propertyId,
            Object startingPoint, Object endingPoint) {
        return outside(propertyId, startingPoint, endingPoint, true, true);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is outside <code>startingPoint</code> (exclusive)
     * and <code>endingPoint</code> (exclusive).
     */
    public static Filter outsideExclusive(Object propertyId,
            Object startingPoint, Object endingPoint) {
        return outside(propertyId, startingPoint, endingPoint, false, false);
    }

    /**
     * Creates a filter that negates <code>filter</code>.
     */
    public static Filter not(Filter filter) {
        return new Not(filter);
    }

    /**
     * Creates a filter that groups <code>filters</code> together in a single
     * conjunction.
     */
    public static And and(Filter... filters) {
        return new And(filters);
    }

    /**
     * Creates a filter that groups <code>filters</code> together in a single
     * conjunction.
     */
    public static And and(List<Filter> filters) {
        return new And(CollectionUtil.toArray(Filter.class, filters));
    }

    /**
     * Creates a filter that groups <code>filters</code> together in a single
     * disjunction.
     */
    public static Or or(Filter... filters) {
        return new Or(filters);
    }

    /**
     * Creates a filter that groups <code>filters</code> together in a single
     * disjunction.
     */
    public static Or or(List<Filter> filters) {
        return new Or(CollectionUtil.toArray(Filter.class, filters));
    }

    /**
     * Creates a filter that applies <code>filters</code> (as a conjunction) to
     * the joined property <code>joinProperty</code>. This is only needed for
     * Hibernate, as EclipseLink implicitly joins on nested properties.
     */
    public static JoinFilter joinFilter(String joinProperty, Filter... filters) {
        return new JoinFilter(joinProperty, filters);
    }
}
