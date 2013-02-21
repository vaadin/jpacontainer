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
package com.vaadin.addon.jpacontainer.util;

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
    public void orderByWasAdded(CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> query) {
    }
}
