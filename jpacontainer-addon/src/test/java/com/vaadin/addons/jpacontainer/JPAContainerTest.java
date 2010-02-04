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
package com.vaadin.addons.jpacontainer;

import com.vaadin.addons.jpacontainer.filter.Filters;
import com.vaadin.addons.jpacontainer.testdata.Address;
import com.vaadin.addons.jpacontainer.testdata.Person;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * Test case for {@link JPAContainer}.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class JPAContainerTest {

    private JPAContainer<Person> container;
    private EntityProvider<Person> entityProviderMock;
    private CachingEntityProvider<Person> cachingEntityProviderMock;
    private MutableEntityProvider<Person> mutableEntityProviderMock;
    private BatchableEntityProvider<Person> batchableEntityProviderMock;

    @Before
    public void setUp() throws Exception {
        entityProviderMock = createMock(EntityProvider.class);
        cachingEntityProviderMock = createMock(CachingEntityProvider.class);
        mutableEntityProviderMock = createMock(MutableEntityProvider.class);
        batchableEntityProviderMock = createMock(BatchableEntityProvider.class);

        container = new JPAContainer<Person>(Person.class);
    }

    @Test
    public void testGetEntityClass() {
        assertSame(Person.class, container.getEntityClass());
    }

    @Test
    public void testSetEntityProvider() {
        assertNull(container.getEntityProvider());
        container.setEntityProvider(cachingEntityProviderMock);
        assertSame(cachingEntityProviderMock, container.getEntityProvider());
    }

    @Test
    public void testReadOnly_EntityProvider() {
        // This provider is not writable => readOnly should always be true
        container.setEntityProvider(entityProviderMock);
        assertTrue(container.isReadOnly());
        try {
            container.setReadOnly(false);
            fail("No exception thrown");
        } catch (UnsupportedOperationException e) {
            assertTrue(container.isReadOnly());
        }
    }

    @Test
    public void testGetSortableContainerPropertyIds() {
        assertTrue(container.getSortableContainerPropertyIds().containsAll(container.
                getEntityClassMetadata().getPersistentPropertyNames()));
        assertEquals(container.getSortableContainerPropertyIds().size(), container.
                getEntityClassMetadata().getPersistentPropertyNames().size());
    }

    @Test
    public void testGetContainerPropertyIds() {
        assertTrue(container.getContainerPropertyIds().containsAll(container.
                getEntityClassMetadata().getPropertyNames()));
        assertEquals(container.getContainerPropertyIds().size(), container.
                getEntityClassMetadata().getPropertyNames().size());
    }

    @Test
    public void testGetType() {
        assertEquals(Long.class, container.getType("id"));
        assertEquals(Long.class, container.getType("version"));
        assertEquals(String.class, container.getType("firstName"));
        assertEquals(String.class, container.getType("lastName"));
        assertEquals(String.class, container.getType("fullName"));
        assertEquals(Date.class, container.getType("dateOfBirth"));
        assertEquals(Address.class, container.getType("address"));
        assertEquals(String.class, container.getType("tempData"));
        assertEquals(Address.class, container.getType("transientAddress"));
    }

    @Test
    public void testRemoveContainerProperty() {
        assertTrue(container.getContainerPropertyIds().contains("address"));
        assertTrue(container.getFilterablePropertyIds().contains("address"));
        assertTrue(container.getSortableContainerPropertyIds().contains(
                "address"));

        assertTrue(container.removeContainerProperty("address"));

        assertFalse(container.getContainerPropertyIds().contains("address"));
        assertFalse(container.getFilterablePropertyIds().contains("address"));
        assertFalse(container.getSortableContainerPropertyIds().contains(
                "address"));
    }

    @Test
    public void testAddNestedContainerProperty() {
        container.addNestedContainerProperty("address.*");
        assertTrue(container.removeContainerProperty("address.postOffice"));

        assertTrue(
                container.getContainerPropertyIds().contains("address.street"));
        assertTrue(container.getFilterablePropertyIds().contains(
                "address.street"));
        assertTrue(container.getSortableContainerPropertyIds().contains(
                "address.street"));

        assertTrue(container.getContainerPropertyIds().contains(
                "address.postalCode"));
        assertTrue(container.getFilterablePropertyIds().contains(
                "address.postalCode"));
        assertTrue(container.getSortableContainerPropertyIds().contains(
                "address.postalCode"));

        assertFalse(container.getContainerPropertyIds().contains(
                "address.postOffice"));
        assertFalse(container.getFilterablePropertyIds().contains(
                "address.postOffice"));
        assertFalse(container.getSortableContainerPropertyIds().contains(
                "address.postOffice"));

        assertTrue(container.getContainerPropertyIds().contains(
                "address.fullAddress"));
        assertFalse(container.getFilterablePropertyIds().contains(
                "address.fullAddress"));
        assertFalse(container.getSortableContainerPropertyIds().contains(
                "address.fullAddress"));
    }

    @Test
    public void testGetFilterablePropertyIds() {
        assertEquals(container.getEntityClassMetadata().
                getPersistentPropertyNames().size(), container.
                getFilterablePropertyIds().size());
        assertTrue(container.getFilterablePropertyIds().containsAll(container.
                getEntityClassMetadata().getPersistentPropertyNames()));
    }

    @Test
    public void testApplyFilters_Delayed() {
        final boolean[] listenerCalled = new boolean[1];
        container.addListener(new ItemSetChangeListener() {

            @Override
            public void containerItemSetChange(ItemSetChangeEvent event) {
                assertTrue(event instanceof JPAContainer.FiltersAppliedEvent);
                listenerCalled[0] = true;
            }
        });
        // Applied filters should not result in any direct calls to the entity provider
        replay(entityProviderMock);
        container.setEntityProvider(entityProviderMock);

        container.setApplyFiltersImmediately(false);
        assertFalse(container.isApplyFiltersImmediately());
        assertFalse(listenerCalled[0]);
        container.addFilter(Filters.eq("firstName", "Hello", false));

        assertFalse(listenerCalled[0]);
        assertTrue(container.getFilters().contains(Filters.eq("firstName",
                "Hello", false)));
        assertTrue(container.getAppliedFilters().isEmpty());
        assertTrue(container.hasUnappliedFilters());

        container.applyFilters();
        assertTrue(listenerCalled[0]);
        assertEquals(container.getFilters(), container.getAppliedFilters());
        assertTrue(container.getFilters().contains(Filters.eq("firstName",
                "Hello", false)));
        assertFalse(container.hasUnappliedFilters());

        // Try to remove the filters
        listenerCalled[0] = false;

        container.removeAllFilters();
        assertTrue(container.getFilters().isEmpty());
        assertFalse(container.getAppliedFilters().isEmpty());
        assertTrue(container.hasUnappliedFilters());

        container.applyFilters();
        assertTrue(listenerCalled[0]);
        assertTrue(container.getAppliedFilters().isEmpty());
        assertFalse(container.hasUnappliedFilters());

        verify(entityProviderMock);
    }

    @Test
    public void testApplyFilters_Immediately() {
        final boolean[] listenerCalled = new boolean[1];
        container.addListener(new ItemSetChangeListener() {

            @Override
            public void containerItemSetChange(ItemSetChangeEvent event) {
                assertTrue(event instanceof JPAContainer.FiltersAppliedEvent);
                listenerCalled[0] = true;
            }
        });
        // Applied filters should not result in any direct calls to the entity provider
        replay(entityProviderMock);
        container.setEntityProvider(entityProviderMock);

        assertTrue(container.isApplyFiltersImmediately());
        assertFalse(listenerCalled[0]);
        container.addFilter(Filters.eq("firstName", "Hello", false));

        assertEquals(container.getFilters(), container.getAppliedFilters());
        assertTrue(container.getFilters().contains(Filters.eq("firstName",
                "Hello", false)));
        assertTrue(listenerCalled[0]);
        assertFalse(container.hasUnappliedFilters());

        // Tro to remove all the filters
        listenerCalled[0] = false;

        container.removeAllFilters();
        assertTrue(container.getFilters().isEmpty());
        assertTrue(container.getAppliedFilters().isEmpty());
        assertTrue(listenerCalled[0]);
        assertFalse(container.hasUnappliedFilters());

        verify(entityProviderMock);
    }

    @Test
    public void testReadOnly_MutableEntityProvider() {
        container.setEntityProvider(mutableEntityProviderMock);
        assertFalse(container.isReadOnly());
        container.setReadOnly(true);
        assertTrue(container.isReadOnly());
        container.setReadOnly(false);
        assertFalse(container.isReadOnly());
    }

    @Test
    public void testSize() {
        expect(entityProviderMock.getEntityCount(Filters.and(Filters.eq(
                "firstName", "Hello", false), Filters.eq("lastName", "World",
                false)))).andReturn(123);
        replay(entityProviderMock);

        assertTrue(container.isApplyFiltersImmediately());
        container.addFilter(Filters.eq("firstName", "Hello", false));
        container.addFilter(Filters.eq("lastName", "World", false));
        container.setEntityProvider(entityProviderMock);

        assertEquals(123, container.size());

        verify(entityProviderMock);
    }

    @Test
    public void testIndexOfId() {
        expect(entityProviderMock.getEntityCount(null)).andStubReturn(5);
        expect(entityProviderMock.getEntityIdentifierAt(null,
                new LinkedList<SortBy>(), 0)).andStubReturn("id1");
        expect(entityProviderMock.getEntityIdentifierAt(null,
                new LinkedList<SortBy>(), 1)).andStubReturn("id2");
        expect(entityProviderMock.getEntityIdentifierAt(null,
                new LinkedList<SortBy>(), 2)).andStubReturn("id3");
        expect(entityProviderMock.getEntityIdentifierAt(null,
                new LinkedList<SortBy>(), 3)).andStubReturn("id4");
        expect(entityProviderMock.getEntityIdentifierAt(null,
                new LinkedList<SortBy>(), 4)).andStubReturn(null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        assertEquals(3, container.indexOfId("id4"));
        assertEquals(-1, container.indexOfId("id5"));

        verify(entityProviderMock);
    }

    @Test
    public void testGetIdByIndex() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(entityProviderMock.getEntityIdentifierAt(null,
                new LinkedList<SortBy>(), 1)).andReturn("id1");
        expect(entityProviderMock.getEntityIdentifierAt(null,
                new LinkedList<SortBy>(), 2)).andReturn(null);
        expect(entityProviderMock.getEntityIdentifierAt(Filters.and(Filters.eq(
                "firstName", "Hello", false)), orderby, 3)).andReturn("id3");
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        assertEquals("id1", container.getIdByIndex(1));
        assertNull(container.getIdByIndex(2));

        // Now let's try with a filter and some sorting
        container.addFilter(Filters.eq("firstName", "Hello", false));
        container.sort(new Object[]{"firstName"}, new boolean[]{true});

        assertEquals("id3", container.getIdByIndex(3));

        verify(entityProviderMock);
    }

    @Test
    public void testGetItemIds() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(entityProviderMock.getFirstEntityIdentifier(null, orderby)).andStubReturn("id1");
        expect(entityProviderMock.getNextEntityIdentifier("id1", null, orderby)).andStubReturn("id2");
        expect(entityProviderMock.getNextEntityIdentifier("id2", null, orderby)).andStubReturn("id3");
        expect(entityProviderMock.getNextEntityIdentifier("id3", null, orderby)).andStubReturn("id4");
        expect(entityProviderMock.getNextEntityIdentifier("id4", null, orderby)).andStubReturn(null);

        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);
        container.sort(new Object[] {"firstName"}, new boolean[]{true});

        Collection<Object> ids = container.getItemIds();
        assertEquals(4, ids.size());
        assertTrue(ids.contains("id1"));
        assertTrue(ids.contains("id2"));
        assertTrue(ids.contains("id3"));
        assertTrue(ids.contains("id4"));

        verify(entityProviderMock);
    }

    @Test
    public void testGetItem() {
        Person p = new Person();
        p.setFirstName("Joe");
        p.setLastName("Cool");
        expect(entityProviderMock.getEntity("myId")).andReturn(p);
        expect(entityProviderMock.getEntity("nonExistent")).andReturn(null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        Item item = container.getItem("myId");

        assertEquals("Joe", item.getItemProperty("firstName").getValue());
        assertEquals("Cool", item.getItemProperty("lastName").getValue());

        assertNull(container.getItem("nonExistent"));

        verify(entityProviderMock);
    }

    @Test
    public void testGetContainerProperty() {
        Person p = new Person();
        p.setFirstName("Joe");
        expect(entityProviderMock.getEntity("myId")).andStubReturn(p);
        expect(entityProviderMock.getEntity("nonExistent")).andStubReturn(null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        assertEquals("Joe", container.getContainerProperty("myId", "firstName").
                getValue());
        assertNull(container.getContainerProperty("myId", "nonExistentProperty"));
        assertNull(container.getContainerProperty("nonExistent", "firstName"));

        verify(entityProviderMock);
    }

    @Test
    public void testContainsId() {
        expect(entityProviderMock.containsEntity("id", null)).andReturn(true);
        expect(entityProviderMock.containsEntity("id2", Filters.and(Filters.eq(
                "firstName", "Hello", false)))).andReturn(false);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        assertTrue(container.containsId("id"));
        assertTrue(container.isApplyFiltersImmediately());
        container.addFilter(Filters.eq("firstName", "Hello", false));
        assertFalse(container.containsId("id2"));

        verify(entityProviderMock);
    }

    @Test
    public void testFirstItemIdAndIsFirstId() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(entityProviderMock.getFirstEntityIdentifier(null,
                new LinkedList<SortBy>())).andReturn("id1").times(2);
        expect(entityProviderMock.getFirstEntityIdentifier(null,
                new LinkedList<SortBy>())).andReturn(null).times(2);
        expect(entityProviderMock.getFirstEntityIdentifier(Filters.and(Filters.
                eq("firstName", "Hello", false)), orderby)).andReturn("id2").
                times(3);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        assertEquals("id1", container.firstItemId());
        assertTrue(container.isFirstId("id1"));

        assertNull(container.firstItemId());
        assertFalse(container.isFirstId("id1"));

        // Now let's try with a filter and some sorting
        container.addFilter(Filters.eq("firstName", "Hello", false));
        container.sort(new Object[]{"firstName"}, new boolean[]{true});

        assertEquals("id2", container.firstItemId());
        assertTrue(container.isFirstId("id2"));
        assertFalse(container.isFirstId("id3"));

        verify(entityProviderMock);
    }

    @Test
    public void testLastItemIdAndIsLastId() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(entityProviderMock.getLastEntityIdentifier(null,
                new LinkedList<SortBy>())).andReturn("id1").times(2);
        expect(entityProviderMock.getLastEntityIdentifier(null,
                new LinkedList<SortBy>())).andReturn(null).times(2);
        expect(entityProviderMock.getLastEntityIdentifier(Filters.and(Filters.eq(
                "firstName", "Hello", false)), orderby)).andReturn("id2").times(
                3);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        assertEquals("id1", container.lastItemId());
        assertTrue(container.isLastId("id1"));

        assertNull(container.lastItemId());
        assertFalse(container.isLastId("id1"));

        // Now let's try with a filter and some sorting
        container.addFilter(Filters.eq("firstName", "Hello", false));
        container.sort(new Object[]{"firstName"}, new boolean[]{true});

        assertEquals("id2", container.lastItemId());
        assertTrue(container.isLastId("id2"));
        assertFalse(container.isLastId("id3"));

        verify(entityProviderMock);
    }

    @Test
    public void testNextItemId() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(entityProviderMock.getNextEntityIdentifier("id1", null,
                new LinkedList<SortBy>())).andReturn("id2");
        expect(entityProviderMock.getNextEntityIdentifier("id2", null,
                new LinkedList<SortBy>())).andReturn(null);
        expect(entityProviderMock.getNextEntityIdentifier("id3", Filters.and(
                Filters.eq("firstName", "Hello", false)), orderby)).andReturn(
                "id4");
        expect(entityProviderMock.getNextEntityIdentifier("id4", Filters.and(
                Filters.eq("firstName", "Hello", false)), orderby)).andReturn(
                null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        assertEquals("id2", container.nextItemId("id1"));
        assertNull(container.nextItemId("id2"));

        // Now let's try with a filter and some sorting
        container.addFilter(Filters.eq("firstName", "Hello", false));
        container.sort(new Object[]{"firstName"}, new boolean[]{true});

        assertEquals("id4", container.nextItemId("id3"));
        assertNull(container.nextItemId("id4"));

        verify(entityProviderMock);
    }

    @Test
    public void testPrevItemId() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(entityProviderMock.getPreviousEntityIdentifier("id1", null,
                new LinkedList<SortBy>())).andReturn("id2");
        expect(entityProviderMock.getPreviousEntityIdentifier("id2", null,
                new LinkedList<SortBy>())).andReturn(null);
        expect(entityProviderMock.getPreviousEntityIdentifier("id3", Filters.and(
                Filters.eq("firstName", "Hello", false)), orderby)).andReturn(
                "id4");
        expect(entityProviderMock.getPreviousEntityIdentifier("id4", Filters.and(
                Filters.eq("firstName", "Hello", false)), orderby)).andReturn(
                null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        assertEquals("id2", container.prevItemId("id1"));
        assertNull(container.prevItemId("id2"));

        // Now let's try with a filter and some sorting
        container.addFilter(Filters.eq("firstName", "Hello", false));
        container.sort(new Object[]{"firstName"}, new boolean[]{true});

        assertEquals("id4", container.prevItemId("id3"));
        assertNull(container.prevItemId("id4"));

        verify(entityProviderMock);
    }

    @Test
    public void testSort() {
        final boolean[] listenerCalled = new boolean[1];
        container.addListener(new ItemSetChangeListener() {

            @Override
            public void containerItemSetChange(ItemSetChangeEvent event) {
                assertTrue(event instanceof JPAContainer.ContainerSortedEvent);
                listenerCalled[0] = true;
            }
        });
        // A resort should not result in any direct calls to the entity provider
        replay(entityProviderMock);
        container.setEntityProvider(entityProviderMock);

        assertFalse(listenerCalled[0]);
        assertTrue(container.getSortByList().isEmpty());
        container.sort(new Object[]{"firstName", "lastName"}, new boolean[]{true,
                    false});
        assertTrue(listenerCalled[0]);
        assertEquals(2, container.getSortByList().size());
        assertEquals("firstName", container.getSortByList().get(0).propertyId);
        assertEquals("lastName", container.getSortByList().get(1).propertyId);
        assertTrue(container.getSortByList().get(0).ascending);
        assertFalse(container.getSortByList().get(1).ascending);

        verify(entityProviderMock);
    }

    @Test
    public void testUnsupportedOperations() {
        try {
            container.addItemAfter(null);
            fail("No exception thrown");
        } catch (UnsupportedOperationException ok) {
        }
        try {
            container.addItemAfter(null, null);
            fail("No exception thrown");
        } catch (UnsupportedOperationException ok) {
        }
        try {
            container.addContainerProperty("test", String.class, "");
            fail("No exception thrown");
        } catch (UnsupportedOperationException ok) {
        }
        try {
            container.addItem("id");
            fail("No exception thrown");
        } catch (UnsupportedOperationException ok) {
        }
        try {
            container.addItem();
            fail("No exception thrown");
        } catch (UnsupportedOperationException ok) {
        }
        try {
            container.addItemAt(2);
            fail("No exception thrown");
        } catch (UnsupportedOperationException ok) {
        }
        try {
            container.addItemAt(2, "id");
            fail("No exception thrown");
        } catch (UnsupportedOperationException ok) {
        }
        try {
            container.setReadThrough(true);
            fail("No exception thrown");
        } catch (UnsupportedOperationException ok) {
        }
    }

    @Test
    public void testReadThrough_NoCachingProvider() {
        replay(entityProviderMock);

        // No cache -> read through always true
        container.setEntityProvider(entityProviderMock);
        assertTrue(container.isReadThrough());

        verify(entityProviderMock);
    }

    @Test
    public void testReadThrough_CachingProvider() {
        expect(cachingEntityProviderMock.isCacheInUse()).andReturn(true);
        expect(cachingEntityProviderMock.isCacheInUse()).andReturn(false);
        replay(cachingEntityProviderMock);

        // Caching container -> read through depends on the cache
        container.setEntityProvider(cachingEntityProviderMock);

        assertFalse(container.isReadThrough()); // Cache is on
        assertTrue(container.isReadThrough()); // Cache is off

        verify(cachingEntityProviderMock);
    }

    @Test
    public void testWriteThrough_NoBatchableProvider() {
        replay(mutableEntityProviderMock);
        container.setEntityProvider(mutableEntityProviderMock);

        assertTrue(container.isWriteThrough());
        try {
            container.setWriteThrough(false);
            fail("No exception thrown");
        } catch (UnsupportedOperationException e) {
            assertTrue(container.isWriteThrough());
        }
    }

    public void testWriteThrough_BatchableProvider() {
        // TODO Requires buffered mode
    }

    @Test
    public void testAddEntity_WriteThrough() {
        Person newEntity = new Person();
        Person persistentEntity = new Person();
        persistentEntity.setId(123l);
        persistentEntity.setVersion(1l);
        expect(mutableEntityProviderMock.addEntity(newEntity)).andReturn(
                persistentEntity);
        replay(mutableEntityProviderMock);
        container.setEntityProvider(mutableEntityProviderMock);

        final boolean[] listenerCalled = new boolean[1];
        container.addListener(new ItemSetChangeListener() {

            @Override
            public void containerItemSetChange(ItemSetChangeEvent event) {
                assertTrue(event instanceof JPAContainer.ItemAddedEvent);
                assertEquals(123l, ((JPAContainer.ItemAddedEvent) event).getItemId());
                listenerCalled[0] = true;
            }
        });

        assertFalse(listenerCalled[0]);
        assertEquals(123l, container.addEntity(newEntity));
        assertTrue(listenerCalled[0]);

        verify(mutableEntityProviderMock);
    }

    @Test
    public void testRemoveItem_WriteThrough() {
        expect(mutableEntityProviderMock.containsEntity(123l, null)).andReturn(
                true);
        mutableEntityProviderMock.removeEntity(123l);
        expect(mutableEntityProviderMock.containsEntity(456l, null)).andReturn(
                false);
        replay(mutableEntityProviderMock);
        container.setEntityProvider(mutableEntityProviderMock);

        final boolean[] listenerCalled = new boolean[1];
        container.addListener(new ItemSetChangeListener() {

            @Override
            public void containerItemSetChange(ItemSetChangeEvent event) {
                assertTrue(event instanceof JPAContainer.ItemRemovedEvent);
                assertEquals(123l, ((JPAContainer.ItemRemovedEvent) event).getItemId());
                listenerCalled[0] = true;
            }
        });

        assertFalse(listenerCalled[0]);
        assertTrue(container.removeItem(123l));
        assertTrue(listenerCalled[0]);

        listenerCalled[0] = false;
        assertFalse(container.removeItem(456l));
        assertFalse(listenerCalled[0]);

        verify(mutableEntityProviderMock);
    }

    public void testRemoveAllItems_WriteThrough() {
        
    }

    public void testContainerItemPropertyModified_WriteThrough() {

    }

    public void testContainerItemModified_WriteThrough() {
        
    }

    // TODO Test all buffered mode operations.
}
