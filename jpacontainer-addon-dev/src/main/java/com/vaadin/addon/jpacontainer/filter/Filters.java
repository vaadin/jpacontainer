/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

import java.util.List;

import com.vaadin.addon.jpacontainer.Filter;

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
    public static PropertyFilter isNull(Object propertyId) {
        return new IsNullFilter(propertyId);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is not null.
     */
    public static PropertyFilter isNotNull(Object propertyId) {
        return new IsNotNullFilter(propertyId);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is empty.
     */
    public static PropertyFilter isEmpty(Object propertyId) {
        return new IsEmptyFilter(propertyId);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is not empty.
     */
    public static PropertyFilter isNotEmpty(Object propertyId) {
        return new IsNotEmptyFilter(propertyId);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is equal to <code>value</code>.
     */
    public static ValueFilter eq(Object propertyId, String value,
            boolean caseSensitive) {
        return new StringComparisonFilter(propertyId, value, caseSensitive, "=");
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> matches <code>value</code>. The precent-sign (%)
     * may be used as wildcard.
     */
    public static ValueFilter like(Object propertyId, String value,
            boolean caseSensitive) {
        return new StringComparisonFilter(propertyId, value, caseSensitive,
                "like");
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is equal to <code>value</code>.
     */
    public static ValueFilter eq(Object propertyId, Object value) {
        return new ComparisonFilter(propertyId, value, "=");
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is greater than or equal to <code>value</code>.
     */
    public static ValueFilter gteq(Object propertyId, Object value) {
        return new ComparisonFilter(propertyId, value, ">=");
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is greater than <code>value</code>.
     */
    public static ValueFilter gt(Object propertyId, Object value) {
        return new ComparisonFilter(propertyId, value, ">");
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is less than or equal to <code>value</code>.
     */
    public static ValueFilter lteq(Object propertyId, Object value) {
        return new ComparisonFilter(propertyId, value, "<=");
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is less than <code>value</code>.
     */
    public static ValueFilter lt(Object propertyId, Object value) {
        return new ComparisonFilter(propertyId, value, "<");
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is between <code>startingPoint</code> and
     * <code>endingPoint</code>.
     */
    public static IntervalFilter between(Object propertyId,
            Object startingPoint, Object endingPoint,
            boolean includeStartingPoint, boolean includeEndingPoint) {
        return new BetweenFilter(propertyId, startingPoint,
                includeStartingPoint, endingPoint, includeEndingPoint);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is between <code>startingPoint</code> (inclusive)
     * and <code>endingPoint</code> (inclusive).
     */
    public static IntervalFilter betweenInclusive(Object propertyId,
            Object startingPoint, Object endingPoint) {
        return between(propertyId, startingPoint, endingPoint, true, true);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is between <code>startingPoint</code> (exclusive)
     * and <code>endingPoint</code> (exclusive).
     */
    public static IntervalFilter betweenExlusive(Object propertyId,
            Object startingPoint, Object endingPoint) {
        return between(propertyId, startingPoint, endingPoint, false, false);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is outside <code>startingPoint</code> and
     * <code>endingPoint</code>.
     */
    public static IntervalFilter outside(Object propertyId,
            Object startingPoint, Object endingPoint,
            boolean includeStartingPoint, boolean includeEndingPoint) {
        return new OutsideFilter(propertyId, startingPoint,
                includeStartingPoint, endingPoint, includeEndingPoint);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is outside <code>startingPoint</code> (inclusive)
     * and <code>endingPoint</code> (inclusive).
     */
    public static IntervalFilter outsideInclusive(Object propertyId,
            Object startingPoint, Object endingPoint) {
        return outside(propertyId, startingPoint, endingPoint, true, true);
    }

    /**
     * Creates a new filter that accepts all items whose value of
     * <code>propertyId</code> is outside <code>startingPoint</code> (exclusive)
     * and <code>endingPoint</code> (exclusive).
     */
    public static IntervalFilter outsideExclusive(Object propertyId,
            Object startingPoint, Object endingPoint) {
        return outside(propertyId, startingPoint, endingPoint, false, false);
    }

    /**
     * Creates a filter that negates <code>filter</code>.
     */
    public static Filter not(Filter filter) {
        return new Negation(filter);
    }

    /**
     * Creates a filter that groups <code>filters</code> together in a single
     * conjunction.
     */
    public static Junction and(Filter... filters) {
        return new Conjunction(filters);
    }

    /**
     * Creates a filter that groups <code>filters</code> together in a single
     * conjunction.
     */
    public static Junction and(List<Filter> filters) {
        return new Conjunction(filters);
    }

    /**
     * Creates a filter that groups <code>filters</code> together in a single
     * disjunction.
     */
    public static Junction or(Filter... filters) {
        return new Disjunction(filters);
    }

    /**
     * Creates a filter that groups <code>filters</code> together in a single
     * disjunction.
     */
    public static Junction or(List<Filter> filters) {
        return new Disjunction(filters);
    }

    /**
     * Creates a filter that applies <code>filters</code> (as a conjunction) to
     * the joined property <code>joinProperty</code>.
     */
    public static JoinFilter joinFilter(String joinProperty, Filter... filters) {
        return new SimpleJoinFilter(joinProperty, filters);
    }
}
