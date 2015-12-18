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

package com.vaadin.addon.jpacontainer.filter;

import javax.persistence.criteria.Subquery;

/**
 * @author indvd00m (gotoindvdum[at]gmail[dot]com)
 * @date Dec 18, 2015 5:39:12 PM
 *
 */
public interface ISubqueryProvider {

    /**
     * Create a subquery of the query.
     * 
     * @param type
     *            the subquery result type
     * @return subquery
     */
    <U> Subquery<U> subquery(Class<U> type);

}
