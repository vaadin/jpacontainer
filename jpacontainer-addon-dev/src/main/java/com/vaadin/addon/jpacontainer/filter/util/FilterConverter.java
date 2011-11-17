package com.vaadin.addon.jpacontainer.filter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.vaadin.addon.jpacontainer.util.CollectionUtil;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Compare.Greater;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;

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

        public <T> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                Root<T> root);
    }

    /**
     * Converts {@link And} filters.
     */
    private static class AndConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof And;
        }

        public <T> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                Root<T> root) {
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

        public <T> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                Root<T> root) {
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
        public <T> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                Root<T> root) {
            Compare compare = (Compare) filter;
            Expression propertyExpr = AdvancedFilterableSupport
                    .getPropertyPath(root, compare.getPropertyId());
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

        public <T> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                Root<T> root) {
            return cb.isNull(AdvancedFilterableSupport.getPropertyPath(
                    root, ((IsNull) filter).getPropertyId()));
        }
    }

    /**
     * Converts {@link SimpleStringFilter} filters.
     */
    private static class SimpleStringFilterConverter implements Converter {
        public boolean canConvert(Filter filter) {
            return filter instanceof SimpleStringFilter;
        }

        public <T> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                Root<T> root) {
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

        public <T> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
                Root<T> root) {
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

    private static Collection<Converter> converters;
    static {
        converters = Collections.unmodifiableCollection(Arrays.asList(
                new AndConverter(), new OrConverter(), new CompareConverter(),
                new IsNullConverter(), new SimpleStringFilterConverter(),
                new LikeConverter()));
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
    public static <T> Predicate convertFilter(Filter filter,
            CriteriaBuilder criteriaBuilder, Root<T> root) {
        assert filter != null : "filter must not be null";

        for (Converter c : converters) {
            if (c.canConvert(filter)) {
                return c.toPredicate(filter, criteriaBuilder, root);
            }
        }

        return null;
    }

    /**
     * Converts a collection of {@link Filter} into a list of {@link Predicate}.
     * 
     * @param filters
     *            Collection of {@link Filter}
     * @return List of {@link Predicate}
     */
    public static <T> List<Predicate> convertFilters(
            Collection<Filter> filters, CriteriaBuilder criteriaBuilder,
            Root<T> root) {
        List<Predicate> result = new ArrayList<Predicate>();
        for (com.vaadin.data.Container.Filter filter : filters) {
            result.add(convertFilter(filter, criteriaBuilder, root));
        }
        return result;
    }

    private static <T> Predicate[] convertFiltersToArray(
            Collection<Filter> filters, CriteriaBuilder criteriaBuilder,
            Root<T> root) {
        return CollectionUtil.toArray(Predicate.class,
                convertFilters(filters, criteriaBuilder, root));
    }
}
