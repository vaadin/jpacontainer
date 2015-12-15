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

import com.vaadin.addon.jpacontainer.AdvancedFilterable;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Compare.Greater;
import com.vaadin.data.util.filter.IsNull;

/**
 * Converts {@link Compare} filters ({@link Equal}, {@link Greater}, etc).
 */
@SuppressWarnings("serial")
public class CompareConverter implements IFilterConverter {

    @Override
    public boolean canConvert(Filter filter) {
        return filter instanceof Compare;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
            From<X, Y> root, AdvancedFilterable filterableSupport) {
        Compare compare = (Compare) filter;
        Expression propertyExpr = filterableSupport.getPropertyPath(root,
                compare.getPropertyId());
        if (Compare.Operation.EQUAL == compare.getOperation()
                && compare.getValue() == null) {
            // Make an IS NULL instead if "= null" is passed
            return filterableSupport.convertFilter(
                    new IsNull(compare.getPropertyId()), cb, root);
        }
        Expression valueExpr = cb.literal(compare.getValue());
        switch (compare.getOperation()) {
            case EQUAL :
                return cb.equal(propertyExpr, valueExpr);
            case GREATER :
                return cb.greaterThan(propertyExpr, valueExpr);
            case GREATER_OR_EQUAL :
                return cb.greaterThanOrEqualTo(propertyExpr, valueExpr);
            case LESS :
                return cb.lessThan(propertyExpr, valueExpr);
            case LESS_OR_EQUAL :
                return cb.lessThanOrEqualTo(propertyExpr, valueExpr);
            default : // Shouldn't happen
                return null;
        }
    }
}