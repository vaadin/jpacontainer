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
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import com.vaadin.addon.jpacontainer.IFilterTool;
import com.vaadin.addon.jpacontainer.filter.ISubqueryProvider;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.SimpleStringFilter;

/**
 * Converts {@link SimpleStringFilter} filters.
 */
@SuppressWarnings("serial")
public class SimpleStringFilterConverter<T> implements IFilterConverter<T> {

    @Override
    public boolean canConvert(Filter filter) {
        return filter instanceof SimpleStringFilter;
    }

    @Override
    public <X> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
            From<X, T> root, IFilterTool filterTool, ISubqueryProvider subqueryProvider) {
        SimpleStringFilter stringFilter = (SimpleStringFilter) filter;
        String filterString = stringFilter.getFilterString();
        if (stringFilter.isOnlyMatchPrefix()) {
            filterString = filterString + "%";
        } else {
            filterString = "%" + filterString + "%";
        }
        if (stringFilter.isIgnoreCase()) {
            return cb.like(
                    cb.upper(
                            cb.concat(
                                    filterTool.getPropertyPath(root,
                                            stringFilter.getPropertyId()
                                                    .toString()),
                            cb.literal(""))),
                    cb.literal(filterString.toUpperCase()));
        } else {
            return cb.like(cb.concat(
                    filterTool.getPropertyPath(root,
                            stringFilter.getPropertyId().toString()),
                    cb.literal("")), cb.literal(filterString));
        }
    }
}