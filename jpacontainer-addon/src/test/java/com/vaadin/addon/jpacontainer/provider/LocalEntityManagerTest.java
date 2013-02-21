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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.SortBy;
import com.vaadin.addon.jpacontainer.testdata.Person;

public class LocalEntityManagerTest {

    private LocalEntityProvider<Person> entityProvider;

    @Before
    public void setUp() {
        entityProvider = new LocalEntityProvider<Person>(Person.class);
    }

    @Test
    public void testAddPrimaryKeyToSortListDoesntAddTheKeyIfAlreadyPresent() {
        List<SortBy> sortBys = new ArrayList<SortBy>();
        sortBys.add(new SortBy("id", true));
        List<SortBy> newSortBys = entityProvider
                .addPrimaryKeyToSortList(sortBys);
        assertEquals(1, newSortBys.size());
    }

    @Test
    public void testAddPrimaryKeyToSortListDoesntAddTheKeyIfAlreadyPresent_moreSortProperties() {
        List<SortBy> sortBys = new ArrayList<SortBy>();
        sortBys.add(new SortBy("id", true));
        sortBys.add(new SortBy("foo", true));
        sortBys.add(new SortBy("bar", true));
        sortBys.add(new SortBy("baz", true));
        List<SortBy> newSortBys = entityProvider
                .addPrimaryKeyToSortList(sortBys);
        assertEquals(4, newSortBys.size());
    }

    @Test
    public void testAddPrimaryKeyToSortListAddsKeyIfNotPresent() {
        List<SortBy> sortBys = new ArrayList<SortBy>();
        sortBys.add(new SortBy("foo", true));
        sortBys.add(new SortBy("bar", true));
        sortBys.add(new SortBy("baz", true));
        List<SortBy> newSortBys = entityProvider
                .addPrimaryKeyToSortList(sortBys);
        assertEquals(4, newSortBys.size());
    }

}
