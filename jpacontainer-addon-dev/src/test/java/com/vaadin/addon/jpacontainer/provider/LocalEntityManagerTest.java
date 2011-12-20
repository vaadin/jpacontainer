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
