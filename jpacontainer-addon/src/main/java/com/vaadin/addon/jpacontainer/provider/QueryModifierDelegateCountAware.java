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

package com.vaadin.addon.jpacontainer.provider;

import com.vaadin.addon.jpacontainer.QueryModifierDelegate;

public interface QueryModifierDelegateCountAware extends QueryModifierDelegate
{

    /**
     * Normally JPAContainer can not deal with a QueryModifierDelegate that adds
     * a Group by statement when checking the count of records in a query.
     * 
     * This extension to the interface allows the QueryModifierDelegate to know
     * that the query being generated will be used for acquiring a count so that
     * QueryModifierDelegate can the create an appropriate query for a count
     * Operation.
     * 
     * This particularly deals with the case where using a subquery for sorting
     * Necessitates the use of an otherwise unneeded group by clause
     * 
     * in the example below both the group and order can be dropped without side
     * effects and JPAContainer will happily get the count
     * 
     * select c from contact as c join (select max(calldate) as lastcall,
     * call.contactid from call group by contactid ) as calls on calls.contactid
     * = c.contactid group by calls.lastcall order by calls.lastcall
     * 
     * 
     * @param isCountQuery
     */
    void startQueryForCount(boolean isCountQuery);

}
