package com.vaadin.addon.jpacontainer;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;

import com.vaadin.data.Container.Filter;

/**
 * The QueryModifierDelegate interface defines methods that will be called at
 * the different stages of {@link CriteriaQuery} generation. Implement this
 * interface and call
 * {@link EntityProvider#setQueryModifierDelegate(QueryModifierDelegate)} to
 * receive calls while the {@link CriteriaQuery} is being built. The methods are
 * allowed to modify the CriteriaQuery.
 * 
 * @since 2.0
 */
public interface QueryModifierDelegate extends Serializable {
    /**
     * This method is called after the {@link CriteriaQuery} instance (
     * <code>query</code>) has been instantiated, but before any state has been
     * set.
     * 
     * Operations and configuration may be performed on the query instance.
     * 
     * @param criteriaBuilder
     *            the {@link CriteriaBuilder} used to build the query
     * @param query
     *            the {@link CriteriaQuery} being built
     */
    public void queryWillBeBuilt(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query);

    /**
     * This method is called after the {@link CriteriaQuery} instance has been
     * completely built (configured).
     * 
     * Any operations may be performed on the query instance.
     * 
     * @param criteriaBuilder
     *            the {@link CriteriaBuilder} used to build the query
     * @param query
     *            the {@link CriteriaQuery} being built
     */
    public void queryHasBeenBuilt(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query);

    /**
     * This method is called after filters (in the form of {@link Filter}) have
     * been translated into instances of {@link Predicate}, but before the
     * resulting predicates have been added to <code>query</code>.
     * 
     * The contents of the <code>predicates</code> list may be modified at this
     * point. Any operations may be performed on the query instance.
     * 
     * @param criteriaBuilder
     *            the {@link CriteriaBuilder} used to build the query
     * @param query
     *            the {@link CriteriaQuery} being built
     * @param predicates
     *            the list of predicates ({@link Predicate}) to be applied. The
     *            contents of this list may be modified.
     */
    public void filtersWillBeAdded(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query, List<Predicate> predicates);

    /**
     * This method is called after all filters have been applied to the query.
     * 
     * Any operations may be performed on the query instance.
     * 
     * @param criteriaBuilder
     *            the {@link CriteriaBuilder} used to build the query
     * @param query
     *            the {@link CriteriaQuery} being built
     */
    public void filtersWereAdded(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query);

    /**
     * This method is called after all {@link SortBy} instances have been
     * translated into {@link Order} instances, but before they have been
     * applied to the query.
     * 
     * The contents of the <code>orderBy</code> list may be modified at this
     * point. Any operations may be performed on the query instance.
     * 
     * @param criteriaBuilder
     *            the {@link CriteriaBuilder} used to build the query
     * @param query
     *            the {@link CriteriaQuery} being built
     * @param orderBy
     *            the list of order by rules ({@link Order}) to be applied. The
     *            contents of this list may be modified.
     */
    public void orderByWillBeAdded(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query, List<Order> orderBy);

    /**
     * This method is called after the order by has been applied for the query.
     * 
     * Any operations may be performed on the query instance.
     * 
     * @param criteriaBuilder
     *            the {@link CriteriaBuilder} used to build the query
     * @param query
     *            the {@link CriteriaQuery} being built
     */
    public void orderByWasAdded(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query);
}
