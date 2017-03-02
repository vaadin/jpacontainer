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
package com.vaadin.addon.jpacontainer.filter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.vaadin.addon.jpacontainer.filter.JoinFilter;
import com.vaadin.addon.jpacontainer.util.CollectionUtil;
import com.vaadin.v7.data.Container.Filter;
import com.vaadin.v7.data.util.filter.And;
import com.vaadin.v7.data.util.filter.Between;
import com.vaadin.v7.data.util.filter.Compare;
import com.vaadin.v7.data.util.filter.Compare.Equal;
import com.vaadin.v7.data.util.filter.Compare.Greater;
import com.vaadin.v7.data.util.filter.IsNull;
import com.vaadin.v7.data.util.filter.Like;
import com.vaadin.v7.data.util.filter.Not;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;

/**
 * Converts a Vaadin 6.6 container filter into a JPA criteria predicate.
 * 
 * @param filter
 *            Vaadin 6.6 {@link Filter}
 * @return {@link Predicate}
 * 
 * @since 2.0
 */
public class FilterConverter {

    /**
     * Interface for a converter that can convert a certain kind of
     * {@link Filter} to a {@link Predicate}.
     */
    private interface Converter {
        public boolean canConvert(Filter filter);

        public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                From<X, Y> root);
    }

    /**
     * Converts {@link And} filters.
     */
    private static class AndConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof And;
        }

        public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                From<X, Y> root) {
            return cb.and(convertFiltersToArray(((And) filter).getFilters(),
                    cb, root));
        }
    }

    /**
     * Converts {@link Or} filters.
     */
    private static class OrConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof Or;
        }

        public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                From<X, Y> root) {
            return cb.or(convertFiltersToArray(((Or) filter).getFilters(), cb,
                    root));
        }
    }

    /**
     * Converts {@link Compare} filters ({@link Equal}, {@link Greater}, etc).
     */
    private static class CompareConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof Compare;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                From<X, Y> root) {
            Compare compare = (Compare) filter;
            Expression propertyExpr = AdvancedFilterableSupport
                    .getPropertyPath(root, compare.getPropertyId());
            if (Compare.Operation.EQUAL == compare.getOperation()
                    && compare.getValue() == null) {
                // Make an IS NULL instead if "= null" is passed
                return convertFilter(new IsNull(compare.getPropertyId()), cb,
                        root);
            }
            Expression valueExpr = cb.literal(compare.getValue());
            switch (compare.getOperation()) {
            case EQUAL:
                return cb.equal(propertyExpr, valueExpr);
            case GREATER:
                return cb.greaterThan(propertyExpr, valueExpr);
            case GREATER_OR_EQUAL:
                return cb.greaterThanOrEqualTo(propertyExpr, valueExpr);
            case LESS:
                return cb.lessThan(propertyExpr, valueExpr);
            case LESS_OR_EQUAL:
                return cb.lessThanOrEqualTo(propertyExpr, valueExpr);
            default: // Shouldn't happen
                return null;
            }
        }
    }

    /**
     * Converts {@link IsNull} filters.
     */
    private static class IsNullConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof IsNull;
        }

        public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                From<X, Y> root) {
            return cb.isNull(AdvancedFilterableSupport.getPropertyPath(root,
                    ((IsNull) filter).getPropertyId()));
        }
    }

    /**
     * Converts {@link SimpleStringFilter} filters.
     */
    private static class SimpleStringFilterConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof SimpleStringFilter;
        }

        public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                From<X, Y> root) {
            SimpleStringFilter stringFilter = (SimpleStringFilter) filter;
            String filterString = stringFilter.getFilterString();
            if (stringFilter.isOnlyMatchPrefix()) {
                filterString = filterString + "%";
            } else {
                filterString = "%" + filterString + "%";
            }
            if (stringFilter.isIgnoreCase()) {
                return cb.like(cb.upper(AdvancedFilterableSupport
                        .getPropertyPath(root, stringFilter.getPropertyId()
                                .toString())), cb.upper(cb
                        .literal(filterString)));
            } else {
                return cb.like(AdvancedFilterableSupport.getPropertyPath(root,
                        stringFilter.getPropertyId().toString()), cb
                        .literal(filterString));
            }
        }
    }

    /**
     * Converts {@link Like} filters.
     */
    private static class LikeConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof Like;
        }

        public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                From<X, Y> root) {
            Like like = (Like) filter;
            if (like.isCaseSensitive()) {
                return cb.like(AdvancedFilterableSupport.getPropertyPath(root,
                        like.getPropertyId().toString()), cb.literal(like
                        .getValue()));
            } else {
                return cb.like(
                        cb.upper(AdvancedFilterableSupport.getPropertyPath(
                                root, like.getPropertyId().toString())), cb
                                .upper(cb.literal(like.getValue())));
            }
        }
    }

    private static class BetweenConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof Between;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                From<X, Y> root) {
            Between between = (Between) filter;
            Expression<? extends Comparable> field = AdvancedFilterableSupport
                    .getPropertyPath(root, between.getPropertyId());
            Expression<? extends Comparable> from = cb.literal(between
                    .getStartValue());
            Expression<? extends Comparable> to = cb.literal(between
                    .getEndValue());
            return cb.between(field, from, to);
        }
    }

    private static class JoinFilterConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof JoinFilter;
        }

        public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                From<X, Y> root) {
            JoinFilter hibernateJoin = (JoinFilter) filter;
            From<X, Y> join = root.join(hibernateJoin.getJoinProperty());
            return cb.and(convertFiltersToArray(hibernateJoin.getFilters(), cb,
                    join));
        }

    }

    private static class NotFilterConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof Not;
        }

        public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                From<X, Y> root) {
            Not not = (Not) filter;
            return cb.not(convertFilter(not.getFilter(), cb, root));
        }
    }

    private static Collection<Converter> converters;
    static {
        converters = Collections.unmodifiableCollection(Arrays.asList(
                new AndConverter(), new OrConverter(), new CompareConverter(),
                new IsNullConverter(), new SimpleStringFilterConverter(),
                new LikeConverter(), new BetweenConverter(),
                new JoinFilterConverter(), new NotFilterConverter()));
    }

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
    public static <X, Y> Predicate convertFilter(Filter filter,
            CriteriaBuilder criteriaBuilder, From<X, Y> root) {
        assert filter != null : "filter must not be null";

        for (Converter c : converters) {
            if (c.canConvert(filter)) {
                return c.toPredicate(filter, criteriaBuilder, root);
            }
        }

        throw new IllegalStateException("Cannot find any converters for "
                + filter.getClass().getSimpleName() + " filters!");
    }

    /**
     * Converts a collection of {@link Filter} into a list of {@link Predicate}.
     * 
     * @param filters
     *            Collection of {@link Filter}
     * @return List of {@link Predicate}
     */
    public static <X, Y> List<Predicate> convertFilters(
            Collection<Filter> filters, CriteriaBuilder criteriaBuilder,
            From<X, Y> root) {
        List<Predicate> result = new ArrayList<Predicate>();
        for (com.vaadin.v7.data.Container.Filter filter : filters) {
            result.add(convertFilter(filter, criteriaBuilder, root));
        }
        return result;
    }

    private static <X, Y> Predicate[] convertFiltersToArray(
            Collection<Filter> filters, CriteriaBuilder criteriaBuilder,
            From<X, Y> root) {
        return CollectionUtil.toArray(Predicate.class,
                convertFilters(filters, criteriaBuilder, root));
    }
}
