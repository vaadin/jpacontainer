package com.vaadin.addon.jpacontainer.provider;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;

import com.vaadin.addon.jpacontainer.QueryModifierDelegate;

/**
 * A default (empty) implementation of the {@link QueryModifierDelegate}
 * interface. This is provided for the convenience of the developer who needs to
 * implement only a specific delegate method.
 * 
 * @since 2.0
 */
public class DefaultQueryModifierDelegate implements QueryModifierDelegate {

    /**
     * {@inheritDoc}
     * 
     * This default implementation does nothing.
     */
    public void queryWillBeBuilt(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query) {
    }

    /**
     * {@inheritDoc}
     * 
     * This default implementation does nothing.
     */
    public void queryHasBeenBuilt(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query) {
    }

    /**
     * {@inheritDoc}
     * 
     * This default implementation does nothing.
     */
    public void filtersWillBeAdded(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query, List<Predicate> predicates) {
    }

    /**
     * {@inheritDoc}
     * 
     * This default implementation does nothing.
     */
    public void filtersWereAdded(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query) {
    }

    /**
     * {@inheritDoc}
     * 
     * This default implementation does nothing.
     */
    public void orderByWillBeAdded(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query, List<Order> orderBy) {
    }

    /**
     * {@inheritDoc}
     * 
     * This default implementation does nothing.
     */
    public void orderByWereAdded(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query) {
    }
}
