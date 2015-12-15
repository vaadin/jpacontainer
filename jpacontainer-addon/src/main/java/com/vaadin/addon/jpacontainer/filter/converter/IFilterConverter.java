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

import java.io.Serializable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import com.vaadin.addon.jpacontainer.AdvancedFilterable;
import com.vaadin.data.Container.Filter;

/**
 * Interface for a converter that can convert a certain kind of {@link Filter}
 * to a {@link Predicate}.
 */
public interface IFilterConverter extends Serializable {

    public boolean canConvert(Filter filter);

    public <X, Y> Predicate toPredicate(Filter filter, CriteriaBuilder cb,
            From<X, Y> root, AdvancedFilterable filterableSupport);

}