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

package com.vaadin.addon.jpacontainer.filter.converter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import com.vaadin.addon.jpacontainer.IFilterTool;
import com.vaadin.addon.jpacontainer.filter.ISubqueryProvider;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Between;

@SuppressWarnings("serial")
public class BetweenConverter<T> implements IFilterConverter<T> {

    @Override
    public boolean canConvert(Filter filter) {
        return filter instanceof Between;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <X> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
            From<X, T> root, IFilterTool filterTool, ISubqueryProvider subqueryProvider) {
        Between between = (Between) filter;
        Expression<? extends Comparable> field = filterTool
                .getPropertyPath(root, between.getPropertyId());
        Expression<? extends Comparable> from = cb
                .literal(between.getStartValue());
        Expression<? extends Comparable> to = cb.literal(between.getEndValue());
        return cb.between(field, from, to);
    }
}