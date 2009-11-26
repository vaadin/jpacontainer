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

/**
 * Utility class for creating filter instances.
 *
 * @author Petter Holmström (IT Mill)
 */
public final class Filters {

    private Filters() {
        // To prevent applications from creating instances of this class.
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is null.
     */
    public static PropertyFilter isNull(Object propertyId) {
        return new IsNullFilter(propertyId);
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is not null.
     */
    public static PropertyFilter isNotNull(Object propertyId) {
        return new IsNotNullFilter(propertyId);
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is empty.
     */
    public static PropertyFilter isEmpty(Object propertyId) {
        return new IsEmptyFilter(propertyId);
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is not empty.
     */
    public static PropertyFilter isNotEmpty(Object propertyId) {
        return new IsNotEmptyFilter(propertyId);
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is equal to <code>value</code>.
     */
    public static ValueFilter eq(Object propertyId, String value,
            boolean caseSensitive) {
        return new StringComparisionFilter(propertyId, value, caseSensitive, "=");
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> matches <code>value</code>. The precent-sign (%)
     * may be used as wildcard.
     */
    public static ValueFilter like(Object propertyId, String value,
            boolean caseSensitive) {
        return new StringComparisionFilter(propertyId, value, caseSensitive,
                "like");
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is equal to <code>value</code>.
     */
    public static ValueFilter eq(Object propertyId, Object value) {
        return new ComparisionFilter(propertyId, value, "=");
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is greater than or equal to <code>value</code>.
     */
    public static ValueFilter gteq(Object propertyId, Object value) {
        return new ComparisionFilter(propertyId, value, ">=");
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is greater than <code>value</code>.
     */
    public static ValueFilter gt(Object propertyId, Object value) {
        return new ComparisionFilter(propertyId, value, ">");
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is less than or equal to <code>value</code>.
     */
    public static ValueFilter lteq(Object propertyId, Object value) {
        return new ComparisionFilter(propertyId, value, "<=");
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is less than <code>value</code>.
     */
    public static ValueFilter lt(Object propertyId, Object value) {
        return new ComparisionFilter(propertyId, value, "<");
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is between <code>startingPoint</code> and <code>endingPoint</code>.
     */
    public static IntervalFilter between(Object propertyId, Object startingPoint,
            Object endingPoint, boolean includeStartingPoint,
            boolean includeEndingPoint) {
        return new BetweenFilter(propertyId, startingPoint, includeEndingPoint,
                endingPoint, includeEndingPoint);
    }

    /**
     * Creates a new filter that includes all items whose value of <code>propertyId</code> is outside <code>startingPoint</code> and <code>endingPoint</code>.
     */
    public static IntervalFilter outside(Object propertyId, Object startingPoint,
            Object endingPoint, boolean includeStartingPoint,
            boolean includeEndingPoint) {
        return new OutsideFilter(propertyId, startingPoint, includeStartingPoint,
                endingPoint, includeEndingPoint);
    }

    /**
     * Creates a filter that negates <code>filter</code>.
     */
    public static Filter not(Filter filter) {
        return new Negation(filter);
    }

    /**
     * Creates a filter that groups <code>filters</code> together in a single conjunction.
     */
    public static Junction and(Filter... filters) {
        return new Conjunction(filters);
    }

    /**
     * Creates a filter that groups <code>filters</code> together in a single disjunction.
     */
    public static Junction or(Filter... filters) {
        return new Disjunction(filters);
    }
}
