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

package com.vaadin.addon.jpacontainer.filter.util;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.v7.data.Container.Filter;

/**
 * Test case for {@link AdvancedFilterableSupport}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class AdvancedFilterableSupportTest {

    private AdvancedFilterableSupport testObject;

    private AdvancedFilterableSupport.ApplyFiltersListener listenerMock;

    private Filter filterMock;

    @Before
    public void setUp() {
        testObject = new AdvancedFilterableSupport();
        listenerMock = createMock(AdvancedFilterableSupport.ApplyFiltersListener.class);
        testObject.addListener(listenerMock);
        filterMock = createMock(Filter.class);
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
