/*
 * JPAContainer
 * Copyright (C) 2009 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.filter.util;

import com.vaadin.addons.jpacontainer.filter.CompositeFilter;
import com.vaadin.addons.jpacontainer.filter.Filter;
import com.vaadin.addons.jpacontainer.filter.PropertyFilter;
import java.util.Collection;
import java.util.LinkedList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * Test case for {@link AdvancedFilterableSupport}.
 *
 * @author Petter Holmström (IT Mill)
 */
public class AdvancedFilterableSupportTest {

    private AdvancedFilterableSupport testObject;

    private AdvancedFilterableSupport.Listener listenerMock;

    private Filter filterMock;

    private PropertyFilter propertyFilterMock;

    private CompositeFilter compositeFilterMock;

    @Before
    public void setUp() {
        testObject = new AdvancedFilterableSupport();
        listenerMock = createMock(AdvancedFilterableSupport.Listener.class);
        testObject.addListener(listenerMock);
        filterMock = createMock(Filter.class);
        propertyFilterMock = createMock(PropertyFilter.class);
        compositeFilterMock = createMock(CompositeFilter.class);
    }

    @Test
    public void testFilterablePropertyIds() {
        // No property IDs by default
        assertNotNull(testObject.getFilterablePropertyIds());
        assertTrue(testObject.getFilterablePropertyIds().isEmpty());

        // Add some property IDs
        testObject.setFilterablePropertyIds("hello", "world");

        assertEquals(2, testObject.getFilterablePropertyIds().size());
        assertTrue(testObject.getFilterablePropertyIds().contains("hello"));
        assertTrue(testObject.getFilterablePropertyIds().contains("world"));
        assertTrue(testObject.isFilterable("hello"));
        assertTrue(testObject.isFilterable("world"));
        assertFalse(testObject.isFilterable("foo"));

        // Add some property IDs using the collection setter
        Collection<Object> collection = new LinkedList<Object>();
        collection.add("Hello");
        testObject.setFilterablePropertyIds(collection);

        assertEquals(1, testObject.getFilterablePropertyIds().size());
        assertTrue(testObject.getFilterablePropertyIds().contains("Hello"));
    }

    @Test
    public void testFilterablePropertyIds_Unmodifiable() {
        try {
            testObject.getFilterablePropertyIds().add("hello");
            fail("No exception thrown");
        } catch (Exception e) {
            assertTrue(testObject.getFilterablePropertyIds().isEmpty());
        }
    }

    @Test
    public void testIsValidFilter_PropertyFilter() {
        testObject.setFilterablePropertyIds("hello", "world");

        expect(propertyFilterMock.getPropertyId()).andReturn("hello");
        expect(propertyFilterMock.getPropertyId()).andReturn("nonexistent");
        replay(propertyFilterMock);

        assertTrue(testObject.isValidFilter(propertyFilterMock));
        // Second time, the filter should return a different property Id
        assertFalse(testObject.isValidFilter(propertyFilterMock));

        verify(propertyFilterMock);
    }

    @Test
    public void testIsValidFilter_CompositeFilter() {
        testObject.setFilterablePropertyIds("hello", "world");
        LinkedList<Filter> filterList = new LinkedList<Filter>();
        filterList.add(propertyFilterMock);

        expect(compositeFilterMock.getFilters()).andStubReturn(filterList);
        replay(compositeFilterMock);

        expect(propertyFilterMock.getPropertyId()).andReturn("hello");
        expect(propertyFilterMock.getPropertyId()).andReturn("nonexistent");
        replay(propertyFilterMock);

        assertTrue(testObject.isValidFilter(compositeFilterMock));
        // Second time, the filter should return a different property Id
        assertFalse(testObject.isValidFilter(compositeFilterMock));

        verify(compositeFilterMock);
        verify(propertyFilterMock);
    }

    @Test
    public void testIsValid_CustomFilter() {
        assertTrue(testObject.isValidFilter(filterMock));
    }

    @Test
    public void testAddFilter_ApplyImmediately() {
        assertTrue(testObject.isApplyFiltersImmediately());
        assertFalse(testObject.hasUnappliedFilters());

        listenerMock.filtersApplied(testObject);
        replay(listenerMock);

        assertFalse(testObject.getFilters().contains(filterMock));
        testObject.addFilter(filterMock);
        assertTrue(testObject.getFilters().contains(filterMock));
        assertEquals(testObject.getAppliedFilters(), testObject.getFilters());
        assertFalse(testObject.hasUnappliedFilters());

        verify(listenerMock);
    }

    @Test
    public void testAddFilter_ApplyLater() {
        testObject.setApplyFiltersImmediately(false);
        assertFalse(testObject.isApplyFiltersImmediately());
        assertFalse(testObject.hasUnappliedFilters());

        listenerMock.filtersApplied(testObject);
        expectLastCall().once();
        replay(listenerMock);

        assertFalse(testObject.getFilters().contains(filterMock));
        testObject.addFilter(filterMock);
        assertTrue(testObject.getFilters().contains(filterMock));
        assertFalse(testObject.getAppliedFilters().contains(filterMock));
        assertTrue(testObject.hasUnappliedFilters());
        testObject.applyFilters();
        assertFalse(testObject.hasUnappliedFilters());
        assertEquals(testObject.getAppliedFilters(), testObject.getFilters());
        verify(listenerMock);
    }

    @Test
    public void testAddFilter_InvalidFilter() {
        testObject.setFilterablePropertyIds("hello");
        expect(propertyFilterMock.getPropertyId()).andReturn("nonexistent");
        replay(propertyFilterMock);
        replay(listenerMock);

        try {
            testObject.addFilter(propertyFilterMock);
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            assertFalse(testObject.getFilters().contains(propertyFilterMock));
        }

        verify(propertyFilterMock);
        verify(listenerMock);
    }

    @Test
    public void testFilters_Unmodifiable() {
        assertTrue(testObject.getFilters().isEmpty());
        try {
            testObject.getFilters().add(filterMock);
            fail("No exception thrown");
        } catch (Exception e) {
            assertTrue(testObject.getFilters().isEmpty());
        }
    }

    @Test
    public void testRemoveFilter_ApplyImmediately() {
        assertTrue(testObject.isApplyFiltersImmediately());
        assertFalse(testObject.hasUnappliedFilters());

        listenerMock.filtersApplied(testObject);
        expectLastCall().times(2);
        replay(listenerMock);

        testObject.addFilter(filterMock);
        assertTrue(testObject.getFilters().contains(filterMock));
        assertFalse(testObject.hasUnappliedFilters());
        testObject.removeFilter(filterMock);
        assertFalse(testObject.getFilters().contains(filterMock));
        assertFalse(testObject.hasUnappliedFilters());
        assertEquals(testObject.getAppliedFilters(), testObject.getFilters());

        verify(listenerMock);
    }

    @Test
    public void testRemoveFilter_ApplyLater() {
        testObject.setApplyFiltersImmediately(false);
        assertFalse(testObject.isApplyFiltersImmediately());
        assertFalse(testObject.hasUnappliedFilters());

        listenerMock.filtersApplied(testObject);
        expectLastCall().times(2);
        replay(listenerMock);

        testObject.addFilter(filterMock);
        testObject.applyFilters();

        assertTrue(testObject.getFilters().contains(filterMock));
        assertFalse(testObject.hasUnappliedFilters());
        testObject.removeFilter(filterMock);
        assertFalse(testObject.getFilters().contains(filterMock));
        assertTrue(testObject.hasUnappliedFilters());
        assertTrue(testObject.getAppliedFilters().contains(filterMock));
        testObject.applyFilters();
        assertFalse(testObject.hasUnappliedFilters());
        assertEquals(testObject.getAppliedFilters(), testObject.getFilters());

        verify(listenerMock);
    }

    @Test
    public void testRemoveAll_ApplyImmediately() {
        assertTrue(testObject.isApplyFiltersImmediately());
        assertFalse(testObject.hasUnappliedFilters());

        listenerMock.filtersApplied(testObject);
        expectLastCall().times(2);
        replay(listenerMock);

        testObject.addFilter(filterMock);
        assertTrue(testObject.getFilters().contains(filterMock));
        assertFalse(testObject.hasUnappliedFilters());
        testObject.removeAllFilters();
        assertTrue(testObject.getFilters().isEmpty());
        assertFalse(testObject.hasUnappliedFilters());
        assertEquals(testObject.getAppliedFilters(), testObject.getFilters());

        verify(listenerMock);
    }

    @Test
    public void testRemoveAll_ApplyLater() {
        testObject.setApplyFiltersImmediately(false);
        assertFalse(testObject.isApplyFiltersImmediately());
        assertFalse(testObject.hasUnappliedFilters());

        listenerMock.filtersApplied(testObject);
        expectLastCall().times(2);
        replay(listenerMock);

        testObject.addFilter(filterMock);
        testObject.applyFilters();

        assertTrue(testObject.getFilters().contains(filterMock));
        assertFalse(testObject.hasUnappliedFilters());
        testObject.removeAllFilters();
        assertTrue(testObject.getFilters().isEmpty());
        assertFalse(testObject.getAppliedFilters().isEmpty());
        assertTrue(testObject.hasUnappliedFilters());
        testObject.applyFilters();
        assertFalse(testObject.hasUnappliedFilters());
        assertEquals(testObject.getAppliedFilters(), testObject.getFilters());

        verify(listenerMock);
    }
}
