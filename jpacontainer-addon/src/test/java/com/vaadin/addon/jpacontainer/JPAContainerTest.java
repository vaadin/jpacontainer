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
package com.vaadin.addon.jpacontainer;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.JPAContainer.AllItemsRefreshedEvent;
import com.vaadin.addon.jpacontainer.testdata.Address;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Container.Filter;
import com.vaadin.v7.data.Container.ItemSetChangeEvent;
import com.vaadin.v7.data.Container.ItemSetChangeListener;
import com.vaadin.v7.data.util.filter.And;
import com.vaadin.v7.data.util.filter.Compare.Equal;
import com.vaadin.v7.data.util.filter.IsNull;

/**
 * Test case for {@link JPAContainer}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@SuppressWarnings("serial")
public class JPAContainerTest {

    private JPAContainer<Person> container;
    private EntityProvider<Person> entityProviderMock;
    private CachingEntityProvider<Person> cachingEntityProviderMock;
    private MutableEntityProvider<Person> mutableEntityProviderMock;
    private BatchableEntityProvider<Person> batchableEntityProviderMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        entityProviderMock = createMock(EntityProvider.class);
        expect(entityProviderMock.getLazyLoadingDelegate()).andStubReturn(null);

        cachingEntityProviderMock = createMock(CachingEntityProvider.class);
        expect(cachingEntityProviderMock.getLazyLoadingDelegate())
                .andStubReturn(null);

        mutableEntityProviderMock = createMock(MutableEntityProvider.class);
        expect(mutableEntityProviderMock.getLazyLoadingDelegate())
                .andStubReturn(null);

        batchableEntityProviderMock = createMock(BatchableEntityProvider.class);
        expect(batchableEntityProviderMock.getLazyLoadingDelegate())
                .andStubReturn(null);

        container = new JPAContainer<Person>(Person.class);
    }

    @Test
    public void testGetEntityClass() {
        assertSame(Person.class, container.getEntityClass());
    }

    @Test
    public void testCreateEntityItem() {
        /*
         * It should be possible to create new items without having an entity
         * provider.
         */
        assertNull(container.getEntityProvider());
        Person tmp = new Person();
        EntityItem<Person> tmpItem = container.createEntityItem(tmp);

        assertSame(container, tmpItem.getContainer());
        assertSame(tmp, tmpItem.getEntity());
        assertNull(tmpItem.getItemId());
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
        assertTrue(container.getEntityClassMetadata()
                .getPersistentPropertyNames()
                .containsAll(container.getSortableContainerPropertyIds()));
        // address, manager and skills not sortable
        assertEquals(container.getSortableContainerPropertyIds().size(),
                container.getEntityClassMetadata().getPersistentPropertyNames()
                        .size() - 3);
    }

    @Test
    public void testGetContainerPropertyIds() {
        assertTrue(container.getContainerPropertyIds().containsAll(
                container.getEntityClassMetadata().getPropertyNames()));
        assertEquals(container.getContainerPropertyIds().size(), container
                .getEntityClassMetadata().getPropertyNames().size());
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

        assertTrue(container.removeContainerProperty("address"));

        assertFalse(container.getContainerPropertyIds().contains("address"));
        assertFalse(container.getFilterablePropertyIds().contains("address"));
    }

    @Test
    public void testAddNestedContainerProperty() {
        container.addNestedContainerProperty("address.*");
        assertTrue(container.removeContainerProperty("address.postOffice"));

        assertTrue(container.getContainerPropertyIds().contains(
                "address.street"));
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
        assertEquals(container.getEntityClassMetadata()
                .getPersistentPropertyNames().size(), container
                .getFilterablePropertyIds().size());
        assertTrue(container.getFilterablePropertyIds()
                .containsAll(
                        container.getEntityClassMetadata()
                                .getPersistentPropertyNames()));

        container.setAdditionalFilterablePropertyIds("hello", "world");
        assertTrue(container.getFilterablePropertyIds()
                .containsAll(
                        container.getEntityClassMetadata()
                                .getPersistentPropertyNames()));
        assertTrue(container.getFilterablePropertyIds().contains("hello"));
        assertTrue(container.getFilterablePropertyIds().contains("world"));
    }

    @Test
    public void testApplyFilters_Delayed() {
        final boolean[] listenerCalled = new boolean[1];
        container.addListener(new ItemSetChangeListener() {

            public void containerItemSetChange(ItemSetChangeEvent event) {
                assertTrue(event instanceof JPAContainer.FiltersAppliedEvent);
                listenerCalled[0] = true;
            }
        });
        // Applied filters should not result in any direct calls to the entity
        // provider
        replay(entityProviderMock);
        container.setEntityProvider(entityProviderMock);

        container.setApplyFiltersImmediately(false);
        assertFalse(container.isApplyFiltersImmediately());
        assertFalse(listenerCalled[0]);
        container.addContainerFilter(new Equal("firstName", "Hello"));

        assertFalse(listenerCalled[0]);
        assertTrue(container.getFilters().contains(
                new Equal("firstName", "Hello")));
        assertTrue(container.getAppliedFilters().isEmpty());
        assertTrue(container.hasUnappliedFilters());

        container.applyFilters();
        assertTrue(listenerCalled[0]);
        assertEquals(container.getFilters(), container.getAppliedFilters());
        assertTrue(container.getFilters().contains(
                new Equal("firstName", "Hello")));
        assertFalse(container.hasUnappliedFilters());

        // Try to remove the filters
        listenerCalled[0] = false;

        container.removeAllContainerFilters();
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

            public void containerItemSetChange(ItemSetChangeEvent event) {
                assertTrue(event instanceof JPAContainer.FiltersAppliedEvent);
                listenerCalled[0] = true;
            }
        });
        // Applied filters should not result in any direct calls to the entity
        // provider
        replay(entityProviderMock);
        container.setEntityProvider(entityProviderMock);

        assertTrue(container.isApplyFiltersImmediately());
        assertFalse(listenerCalled[0]);
        container.addContainerFilter(new Equal("firstName", "Hello"));

        assertEquals(container.getFilters(), container.getAppliedFilters());
        assertTrue(container.getFilters().contains(
                new Equal("firstName", "Hello")));
        assertTrue(listenerCalled[0]);
        assertFalse(container.hasUnappliedFilters());

        // Tro to remove all the filters
        listenerCalled[0] = false;

        container.removeAllContainerFilters();
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
    public void testSize_WriteThrough() {
        expect(
                entityProviderMock.getEntityCount(container, new And(new Equal(
                        "firstName", "Hello"), new Equal("lastName", "World"))))
                .andReturn(123);
        replay(entityProviderMock);

        assertTrue(container.isApplyFiltersImmediately());
        container.addContainerFilter(new Equal("firstName", "Hello"));
        container.addContainerFilter(new Equal("lastName", "World"));
        container.setEntityProvider(entityProviderMock);
        container.setWriteThrough(true);

        assertEquals(123, container.size());

        verify(entityProviderMock);
    }

    @Test
    public void testIndexOfId_WriteThrough() {
        expect(entityProviderMock.getEntityCount(container, null)).andStubReturn(5);
        expect(
                entityProviderMock.getEntityIdentifierAt(container, null,
                        new LinkedList<SortBy>(), 0)).andStubReturn("id1");
        expect(
                entityProviderMock.getEntityIdentifierAt(container, null,
                        new LinkedList<SortBy>(), 1)).andStubReturn("id2");
        expect(
                entityProviderMock.getEntityIdentifierAt(container, null,
                        new LinkedList<SortBy>(), 2)).andStubReturn("id3");
        expect(
                entityProviderMock.getEntityIdentifierAt(container, null,
                        new LinkedList<SortBy>(), 3)).andStubReturn("id4");
        expect(
                entityProviderMock.getEntityIdentifierAt(container, null,
                        new LinkedList<SortBy>(), 4)).andStubReturn(null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);
        container.setWriteThrough(true);

        assertEquals(3, container.indexOfId("id4"));
        assertEquals(-1, container.indexOfId("id5"));

        verify(entityProviderMock);
    }

    @Test
    public void testIndexOfId_Buffered() {
        LinkedList<SortBy> sortby = new LinkedList<SortBy>();
        expect(batchableEntityProviderMock.getEntityCount(container, null)).andStubReturn(
                5);
        expect(
                batchableEntityProviderMock.getEntityIdentifierAt(container, null, sortby,
                        0)).andStubReturn("id1");
        expect(
                batchableEntityProviderMock.getEntityIdentifierAt(container, null, sortby,
                        1)).andStubReturn("id2");
        expect(
                batchableEntityProviderMock.getEntityIdentifierAt(container, null, sortby,
                        2)).andStubReturn("id3");
        expect(
                batchableEntityProviderMock.getEntityIdentifierAt(container, null, sortby,
                        3)).andStubReturn("id4");
        expect(
                batchableEntityProviderMock.getEntityIdentifierAt(container, null, sortby,
                        4)).andStubReturn(null);
        expect(batchableEntityProviderMock.containsEntity(container, "id4", null))
                .andStubReturn(true);
        expect(
                batchableEntityProviderMock.getAllEntityIdentifiers(container, null,
                        sortby)).andReturn(
                Arrays.asList(new Object[] { "id1", "id2", "id3", "id4" }));
        replay(batchableEntityProviderMock);

        container.setEntityProvider(batchableEntityProviderMock);
        container.setWriteThrough(false);

        assertEquals(3, container.indexOfId("id4"));
        assertEquals(-1, container.indexOfId("id5"));

        // Add an item
        Object id = container.addEntity(new Person());
        assertEquals(0, container.indexOfId(id));
        assertEquals(4, container.indexOfId("id4"));
        assertEquals(-1, container.indexOfId("id5"));

        // Delete last item
        container.removeItem("id4");
        // Item should not be there, marked for deletion
        assertEquals(-1, container.indexOfId("id4"));

        verify(batchableEntityProviderMock);
    }

    @Test
    public void testGetIdByIndex_WriteThrough() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(
                entityProviderMock.getEntityIdentifierAt(container, null,
                        new LinkedList<SortBy>(), 1)).andReturn("id1");
        expect(
                entityProviderMock.getEntityIdentifierAt(container, null,
                        new LinkedList<SortBy>(), 2)).andReturn(null);
        expect(
                entityProviderMock.getEntityIdentifierAt(container, new Equal("firstName",
                        "Hello"), orderby, 3)).andReturn("id3");
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);
        container.setWriteThrough(true);

        assertEquals("id1", container.getIdByIndex(1));
        assertNull(container.getIdByIndex(2));

        // Now let's try with a filter and some sorting
        container.addContainerFilter(new Equal("firstName", "Hello"));
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertEquals("id3", container.getIdByIndex(3));

        verify(entityProviderMock);
    }

    @Test
    public void testGetIdByIndex_Buffered() {
        Equal filter = new Equal("firstName", "Hello");
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(
                batchableEntityProviderMock.getEntityIdentifierAt(container, null,
                        new LinkedList<SortBy>(), 0)).andStubReturn("id1");
        expect(
                batchableEntityProviderMock.getEntityIdentifierAt(container, null,
                        new LinkedList<SortBy>(), 1)).andStubReturn(null);
        expect(
                batchableEntityProviderMock.getEntityIdentifierAt(container, new Equal(
                        "firstName", "Hello"), orderby, 2))
                .andStubReturn("id3");
        expect(batchableEntityProviderMock.containsEntity(container, "id3", null))
                .andStubReturn(true);
        expect(
                batchableEntityProviderMock.getAllEntityIdentifiers(container, filter,
                        orderby)).andStubReturn(
                Arrays.asList(new Object[] { "id1", "id2", "id3" }));
        expect(batchableEntityProviderMock.containsEntity(container, "id3", filter))
                .andStubReturn(true);
        replay(batchableEntityProviderMock);

        container.setEntityProvider(batchableEntityProviderMock);
        container.setWriteThrough(false);

        assertEquals("id1", container.getIdByIndex(0));
        assertNull(container.getIdByIndex(1));

        // Now let's try with a filter and some sorting
        container.addContainerFilter(filter);
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertEquals("id3", container.getIdByIndex(2));

        // Clear filters and sorting
        container.removeAllContainerFilters();
        container.sort(new Object[] {}, new boolean[] {});

        // Add an item
        Object id = container.addEntity(new Person());
        assertEquals(id, container.getIdByIndex(0));
        assertEquals("id1", container.getIdByIndex(1));
        assertNull(container.getIdByIndex(2));

        // Apply filter and sorting again
        container.addContainerFilter(new Equal("firstName", "Hello"));
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertEquals(id, container.getIdByIndex(0));
        assertEquals("id3", container.getIdByIndex(3));

        // Remove last item
        container.removeItem("id3");
        // Should not exist in the container
        assertFalse(container.containsId("id3"));

        verify(batchableEntityProviderMock);
    }

    @Test
    public void testGetItemIds_WriteThrough() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));

        LinkedList<Object> idList = new LinkedList<Object>();
        idList.add("id1");
        idList.add("id2");
        idList.add("id3");
        idList.add("id4");

        expect(entityProviderMock.getAllEntityIdentifiers(container, null, orderby))
                .andStubReturn(idList);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);
        container.setWriteThrough(true);
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        Collection<Object> ids = container.getItemIds();
        assertEquals(4, ids.size());
        assertTrue(ids.contains("id1"));
        assertTrue(ids.contains("id2"));
        assertTrue(ids.contains("id3"));
        assertTrue(ids.contains("id4"));

        verify(entityProviderMock);
    }

    @Test
    public void testGetItemIds_Buffered() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));

        LinkedList<Object> idList = new LinkedList<Object>();
        idList.add("id1");
        idList.add("id2");
        idList.add("id3");
        idList.add("id4");

        expect(
                batchableEntityProviderMock.getAllEntityIdentifiers(container, null,
                        orderby)).andStubReturn(idList);
        expect(batchableEntityProviderMock.containsEntity(container, "id4", null))
                .andStubReturn(true);
        replay(batchableEntityProviderMock);

        container.setEntityProvider(batchableEntityProviderMock);
        container.setWriteThrough(false);
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        Collection<Object> ids = container.getItemIds();
        assertEquals(4, ids.size());
        assertTrue(ids.contains("id1"));
        assertTrue(ids.contains("id2"));
        assertTrue(ids.contains("id3"));
        assertTrue(ids.contains("id4"));

        // Add an item
        Object id = container.addEntity(new Person());
        ids = container.getItemIds();
        assertEquals(5, ids.size());
        assertTrue(ids.contains(id));

        // Remove last item
        container.removeItem("id4");
        // WAS: Should still be there
        ids = container.getItemIds();
        assertEquals(4, ids.size());
        assertFalse(ids.contains("id4"));

        verify(batchableEntityProviderMock);
    }

    @Test
    public void testGetItem_Null() {
        assertNull(container.getItem(null));
    }

    @Test
    public void testGetItem_WriteThrough() {
        Person p = new Person();
        p.setId(123l);
        p.setFirstName("Joe");
        p.setLastName("Cool");
        expect(entityProviderMock.getEntity(container, 123l)).andReturn(p);
        expect(entityProviderMock.getEntity(container, "nonExistent")).andReturn(null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);
        container.setWriteThrough(true);

        EntityItem<Person> item = container.getItem(123l);

        assertEquals("Joe", item.getItemProperty("firstName").getValue());
        assertEquals("Cool", item.getItemProperty("lastName").getValue());
        assertTrue(item.isPersistent());
        assertFalse(item.isDeleted());
        assertFalse(item.isDirty());
        assertFalse(item.isModified());
        assertEquals(123l, item.getItemId());

        assertNull(container.getItem("nonExistent"));

        verify(entityProviderMock);
    }

    @Test
    public void testGetItem_Buffered() {
        final Person p = new Person();
        p.setId(123l);
        p.setFirstName("Joe");
        p.setLastName("Cool");

        expect(batchableEntityProviderMock.getEntity(container, 123l)).andStubAnswer(
                new IAnswer<Person>() {

                    public Person answer() throws Throwable {
                        return p.clone();
                    }
                });
        expect(batchableEntityProviderMock.containsEntity(container, 123l, null))
                .andStubReturn(true);
        expect(batchableEntityProviderMock.getEntity(container, "nonExistent"))
                .andStubReturn(null);
        expect(
                batchableEntityProviderMock.getAllEntityIdentifiers(container, null,
                        Collections.EMPTY_LIST)).andReturn(
                Collections.EMPTY_LIST);
        replay(batchableEntityProviderMock);

        container.setEntityProvider(batchableEntityProviderMock);

        EntityItem<Person> item = container.getItem(123l);
        assertEquals(p, item.getEntity());
        assertFalse(item.isDirty());
        assertTrue(item.isPersistent());
        assertFalse(item.isDeleted());
        assertFalse(item.isModified());

        assertNull(container.getItem("nonExistent"));

        // Add an item
        Person p2 = new Person();
        p2.setFirstName("Maxwell");
        p2.setLastName("Smart");

        Object id = container.addEntity(p2);
        item = container.getItem(id);
        assertEquals(p2, item.getEntity());
        assertFalse(item.isPersistent());
        assertFalse(item.isDirty());
        assertFalse(item.isModified());
        assertFalse(item.isDeleted());
        assertEquals(id, item.getItemId());

        assertNull(container.getItem("nonExistent"));

        // Update an item
        item = container.getItem(123l);
        item.getItemProperty("firstName").setValue("Jim"); // Set to cache
        assertTrue(item.isPersistent());
        assertTrue(item.isDirty());
        assertFalse(item.isModified());
        assertFalse(item.isDeleted());

        // Fetch it again
        item = container.getItem(123l);
        assertEquals("Jim", item.getItemProperty("firstName").getValue()); // Fetched
                                                                           // from
                                                                           // cache
        assertTrue(item.isPersistent());
        assertTrue(item.isDirty());
        assertFalse(item.isModified());
        assertFalse(item.isDeleted());

        // Delete it
        container.removeItem(123l);

        // Fetch it again
        item = container.getItem(123l);
        assertEquals("Joe", item.getItemProperty("firstName").getValue()); // Fetched
                                                                           // from
                                                                           // provider
        assertTrue(item.isPersistent());
        assertFalse(item.isDirty());
        assertFalse(item.isModified());
        assertTrue(item.isDeleted());

        verify(batchableEntityProviderMock);
    }

    @Test
    public void testGetContainerProperty() {
        Person p = new Person();
        p.setFirstName("Joe");
        expect(entityProviderMock.getEntity(container, "myId")).andStubReturn(p);
        expect(entityProviderMock.getEntity(container, "nonExistent")).andStubReturn(null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        assertEquals("Joe", container.getContainerProperty("myId", "firstName")
                .getValue());
        assertNull(container
                .getContainerProperty("myId", "nonExistentProperty"));
        assertNull(container.getContainerProperty("nonExistent", "firstName"));

        verify(entityProviderMock);
    }

    @Test
    public void testContainsId_WriteThrough() {
        expect(entityProviderMock.containsEntity(container, "id", null)).andReturn(true);
        expect(
                entityProviderMock.containsEntity(container, "id2", new Equal("firstName",
                        "Hello"))).andReturn(false);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);
        container.setWriteThrough(true);

        assertTrue(container.containsId("id"));
        assertTrue(container.isApplyFiltersImmediately());
        container.addContainerFilter(new Equal("firstName", "Hello"));
        assertFalse(container.containsId("id2"));

        verify(entityProviderMock);
    }

    @Test
    public void testContainsId_Buffered() {
        boolean[] id2InContainer = new boolean[] {true};
        
        expect(batchableEntityProviderMock.containsEntity(container, "id", null))
                .andStubReturn(true);
        expect(batchableEntityProviderMock.containsEntity(container, "id2", null))
                .andStubReturn(id2InContainer[0]);
        expect(
                batchableEntityProviderMock.containsEntity(container, "id", new Equal(
                        "firstName", "Hello"))).andStubReturn(true);
        expect(
                batchableEntityProviderMock.containsEntity(container, "id2", new Equal(
                        "firstName", "Hello"))).andStubReturn(false);
        expect(
                batchableEntityProviderMock.getAllEntityIdentifiers(container, null,
                        Collections.EMPTY_LIST)).andStubReturn(
                Collections.EMPTY_LIST);
        replay(batchableEntityProviderMock);

        container.setEntityProvider(batchableEntityProviderMock);
        container.setWriteThrough(false);

        assertTrue(container.containsId("id"));
        assertTrue(container.isApplyFiltersImmediately());
        container.addContainerFilter(new Equal("firstName", "Hello"));
        assertFalse(container.containsId("id2"));

        // Add an item
        Object id = container.addEntity(new Person());
        assertTrue(container.containsId(id));
        assertTrue(container.containsId("id"));
        assertFalse(container.containsId("id2"));

        // Clear filtering
        container.removeAllContainerFilters();
        // Item should still be there
        assertTrue(container.containsId(id));
        assertTrue(container.containsId("id"));
        assertTrue(container.containsId("id2"));

        // Remove an item
        container.removeItem("id2");
        id2InContainer[0] = false;
        // should not be there
        assertFalse(container.containsId("id2"));

        verify(batchableEntityProviderMock);
    }

    @Test
    public void testFirstItemIdAndIsFirstId_WriteThrough() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(
                entityProviderMock.getFirstEntityIdentifier(container, null,
                        new LinkedList<SortBy>())).andReturn("id1").times(2);
        expect(
                entityProviderMock.getFirstEntityIdentifier(container, null,
                        new LinkedList<SortBy>())).andReturn(null).times(2);
        expect(
                entityProviderMock.getFirstEntityIdentifier(container, new Equal(
                        "firstName", "Hello"), orderby)).andReturn("id2")
                .times(3);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);
        container.setWriteThrough(true);

        assertEquals("id1", container.firstItemId());
        assertTrue(container.isFirstId("id1"));

        assertNull(container.firstItemId());
        assertFalse(container.isFirstId("id1"));

        // Now let's try with a filter and some sorting
        container.addContainerFilter(new Equal("firstName", "Hello"));
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertEquals("id2", container.firstItemId());
        assertTrue(container.isFirstId("id2"));
        assertFalse(container.isFirstId("id3"));

        verify(entityProviderMock);
    }

    @Test
    public void testFirstItemIdAndIsFirstId_Buffered() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(
                batchableEntityProviderMock.getFirstEntityIdentifier(container, null,
                        new LinkedList<SortBy>())).andReturn("id1").times(2)
                .andReturn(null).times(2);
        expect(
                batchableEntityProviderMock.getFirstEntityIdentifier(container, new Equal(
                        "firstName", "Hello"), orderby)).andReturn("id2")
                .times(3);
        replay(batchableEntityProviderMock);

        container.setEntityProvider(batchableEntityProviderMock);
        container.setWriteThrough(false);

        /*
         * No added items, should behave as write-through
         */

        assertEquals("id1", container.firstItemId());
        assertTrue(container.isFirstId("id1"));

        assertNull(container.firstItemId());
        assertFalse(container.isFirstId("id1"));

        // Now let's try with a filter and some sorting
        container.addContainerFilter(new Equal("firstName", "Hello"));
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertEquals("id2", container.firstItemId());
        assertTrue(container.isFirstId("id2"));
        assertFalse(container.isFirstId("id3"));

        /*
         * One added item
         */
        // Reset filtering and sorting
        container.removeAllContainerFilters();
        container.sort(new Object[] {}, new boolean[] {});

        Object id1 = container.addEntity(new Person());

        assertEquals(id1, container.firstItemId());
        assertTrue(container.isFirstId(id1));
        assertFalse(container.isFirstId("id1"));

        // Add filtering and sorting
        container.addContainerFilter(new Equal("firstName", "Hello"));
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertEquals(id1, container.firstItemId()); // Added item still on top
        assertTrue(container.isFirstId(id1));
        assertFalse(container.isFirstId("id2"));

        verify(batchableEntityProviderMock);
    }

    @Test
    public void testLastItemIdAndIsLastId_WriteThrough() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(
                entityProviderMock.getLastEntityIdentifier(container, null,
                        new LinkedList<SortBy>())).andReturn("id1").times(2);
        expect(
                entityProviderMock.getLastEntityIdentifier(container, null,
                        new LinkedList<SortBy>())).andReturn(null).times(2);
        expect(
                entityProviderMock.getLastEntityIdentifier(container, new Equal(
                        "firstName", "Hello"), orderby)).andReturn("id2")
                .times(3);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);
        container.setWriteThrough(true);

        assertEquals("id1", container.lastItemId());
        assertTrue(container.isLastId("id1"));

        assertNull(container.lastItemId());
        assertFalse(container.isLastId("id1"));

        // Now let's try with a filter and some sorting
        container.addContainerFilter(new Equal("firstName", "Hello"));
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertEquals("id2", container.lastItemId());
        assertTrue(container.isLastId("id2"));
        assertFalse(container.isLastId("id3"));

        verify(entityProviderMock);
    }

    @Test
    public void testLastItemIdAndIsLastId_Buffered() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(
                batchableEntityProviderMock.getLastEntityIdentifier(container, null,
                        new LinkedList<SortBy>())).andReturn("id1").times(2)
                .andReturn(null).times(2);
        expect(
                batchableEntityProviderMock.getLastEntityIdentifier(container, new Equal(
                        "firstName", "Hello"), orderby)).andReturn("id2")
                .times(6).andReturn(null).times(3);
        replay(batchableEntityProviderMock);

        container.setEntityProvider(batchableEntityProviderMock);
        container.setWriteThrough(false);

        /*
         * No added items, should behave as write-through
         */

        assertEquals("id1", container.lastItemId());
        assertTrue(container.isLastId("id1"));

        assertNull(container.lastItemId());
        assertFalse(container.isLastId("id1"));

        // Now let's try with a filter and some sorting
        container.addContainerFilter(new Equal("firstName", "Hello"));
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertEquals("id2", container.lastItemId());
        assertTrue(container.isLastId("id2"));
        assertFalse(container.isLastId("id3"));

        /*
         * One added item, should still behave as write-through
         */

        Object id = container.addEntity(new Person());

        assertEquals("id2", container.lastItemId());
        assertTrue(container.isLastId("id2"));
        assertFalse(container.isLastId("id3"));

        /*
         * One added item, no items from entity manager
         */
        assertEquals(id, container.lastItemId());
        assertTrue(container.isLastId(id));
        assertFalse(container.isLastId("id2"));

        verify(batchableEntityProviderMock);
    }

    @Test
    public void testNextItemId_WriteThrough() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(
                entityProviderMock.getNextEntityIdentifier(container, "id1", null,
                        new LinkedList<SortBy>())).andReturn("id2");
        expect(
                entityProviderMock.getNextEntityIdentifier(container, "id2", null,
                        new LinkedList<SortBy>())).andReturn(null);
        expect(
                entityProviderMock.getNextEntityIdentifier(container, "id3", new Equal(
                        "firstName", "Hello"), orderby)).andReturn("id4");
        expect(
                entityProviderMock.getNextEntityIdentifier(container, "id4", new Equal(
                        "firstName", "Hello"), orderby)).andReturn(null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);
        container.setWriteThrough(true);

        assertEquals("id2", container.nextItemId("id1"));
        assertNull(container.nextItemId("id2"));

        // Now let's try with a filter and some sorting
        container.addContainerFilter(new Equal("firstName", "Hello"));
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertEquals("id4", container.nextItemId("id3"));
        assertNull(container.nextItemId("id4"));

        verify(entityProviderMock);
    }

    public void testNextItemId_Buffered() {
        // TODO write test
    }

    @Test
    public void testPrevItemId_WriteThrough() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));
        expect(
                entityProviderMock.getPreviousEntityIdentifier(container, "id1", null,
                        new LinkedList<SortBy>())).andReturn("id2");
        expect(
                entityProviderMock.getPreviousEntityIdentifier(container, "id2", null,
                        new LinkedList<SortBy>())).andReturn(null);
        expect(
                entityProviderMock.getPreviousEntityIdentifier(container, "id3",
                        new Equal("firstName", "Hello"), orderby)).andReturn(
                "id4");
        expect(
                entityProviderMock.getPreviousEntityIdentifier(container, "id4",
                        new Equal("firstName", "Hello"), orderby)).andReturn(
                null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);
        container.setWriteThrough(true);

        assertEquals("id2", container.prevItemId("id1"));
        assertNull(container.prevItemId("id2"));

        // Now let's try with a filter and some sorting
        container.addContainerFilter(new Equal("firstName", "Hello"));
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertEquals("id4", container.prevItemId("id3"));
        assertNull(container.prevItemId("id4"));

        verify(entityProviderMock);
    }

    public void testPrevItemId_Buffered() {
        // TODO Write test
    }

    @Test
    public void testSort() {
        final boolean[] listenerCalled = new boolean[1];
        container.addListener(new ItemSetChangeListener() {

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
        container.sort(new Object[] { "firstName", "lastName" }, new boolean[] {
                true, false });
        assertTrue(listenerCalled[0]);
        assertEquals(2, container.getSortByList().size());
        assertEquals("firstName", container.getSortByList().get(0)
                .getPropertyId());
        assertEquals("lastName", container.getSortByList().get(1)
                .getPropertyId());
        assertTrue(container.getSortByList().get(0).isAscending());
        assertFalse(container.getSortByList().get(1).isAscending());

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
        expect(cachingEntityProviderMock.isCacheEnabled()).andReturn(true);
        expect(cachingEntityProviderMock.isCacheEnabled()).andReturn(false);
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

            public void containerItemSetChange(ItemSetChangeEvent event) {
                assertTrue(event instanceof JPAContainer.ItemAddedEvent);
                assertEquals(123l,
                        ((JPAContainer.ItemAddedEvent) event).getItemId());
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
        expect(mutableEntityProviderMock.containsEntity(container, 123l, null)).andReturn(
                true);
        mutableEntityProviderMock.removeEntity(123l);
        expect(mutableEntityProviderMock.containsEntity(container, 456l, null)).andReturn(
                false);
        replay(mutableEntityProviderMock);
        container.setEntityProvider(mutableEntityProviderMock);

        final boolean[] listenerCalled = new boolean[1];
        container.addListener(new ItemSetChangeListener() {

            public void containerItemSetChange(ItemSetChangeEvent event) {
                assertTrue(event instanceof JPAContainer.ItemRemovedEvent);
                assertEquals(123l,
                        ((JPAContainer.ItemRemovedEvent) event).getItemId());
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
        // TODO Write test
    }

    public void testContainerItemPropertyModified_WriteThrough() {

        // TODO Write test
    }

    public void testContainerItemModified_WriteThrough() {
        // TODO Write test
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddEntity_Buffered_Commit() {
        // Setup test data
        Person p = new Person();
        Person pp = new Person();
        pp.setId(123l);
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));

        // Instruct mocks
        final Capture<BatchableEntityProvider.BatchUpdateCallback> callbackCapture = new Capture<BatchableEntityProvider.BatchUpdateCallback>();
        expect(
                batchableEntityProviderMock.getFirstEntityIdentifier(container, null,
                        orderby)).andStubReturn(122l);
        expect(
                batchableEntityProviderMock.getNextEntityIdentifier(container, 122l, null,
                        orderby)).andStubReturn(123l);
        expect(
                batchableEntityProviderMock.getPreviousEntityIdentifier(container, 122l,
                        null, orderby)).andStubReturn(null);
        expect(batchableEntityProviderMock.getEntityCount(container, null)).andStubReturn(
                1);
        batchableEntityProviderMock.batchUpdate(capture(callbackCapture));
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                // Check the callback object...
                assertTrue(callbackCapture.hasCaptured());
                // .. and run it.
                callbackCapture.getValue().batchUpdate(
                        mutableEntityProviderMock);
                return null;
            }
        });
        expect(batchableEntityProviderMock.getEntity(container, 123l)).andStubReturn(pp);
        expect(batchableEntityProviderMock.getEntity((EntityContainer<Person>) anyObject(), anyObject()))
                .andStubReturn(null);
        replay(batchableEntityProviderMock);

        expect(mutableEntityProviderMock.addEntity(EasyMock.isA(Person.class)))
                .andReturn(pp);
        replay(mutableEntityProviderMock);

        // Run test
        container.setEntityProvider(batchableEntityProviderMock);
        container.setWriteThrough(false);
        container.sort(new Object[] { "firstName" }, new boolean[] { true });
        final int[] listenerCalled = new int[1];
        container.addListener(new ItemSetChangeListener() {

            public void containerItemSetChange(ItemSetChangeEvent event) {
                if (listenerCalled[0] == 0) {
                    assertTrue(event instanceof JPAContainer.ItemAddedEvent);
                } else {
                    assertTrue(event instanceof JPAContainer.ChangesCommittedEvent);
                }
                listenerCalled[0]++;
            }
        });

        assertFalse(container.isModified());
        assertEquals(1, container.size());

        Object id = container.addEntity(p);

        assertEquals(1, listenerCalled[0]);
        assertTrue(container.isModified());
        assertEquals(2, container.size());

        // Check that we can access the item using the temporary ID
        EntityItem<Person> item = container.getItem(id);
        assertEquals(id, item.getItemId());
        assertFalse(item.isPersistent());

        // Check that the item shows up in the list
        assertEquals(id, container.firstItemId());
        assertEquals(122l, container.nextItemId(id));
        assertEquals(id, container.prevItemId(122l));

        container.commit();

        assertEquals(2, listenerCalled[0]);
        assertFalse(container.isModified());

        // Check that the item shows up correctly
        assertEquals(122l, container.firstItemId());
        assertEquals(123l, container.nextItemId(122l));
        assertNull(container.getItem(id));
        assertSame(pp, container.getItem(123l).getEntity());

        // Verify mocks
        verify(batchableEntityProviderMock);
        verify(mutableEntityProviderMock);
    }

    @Test
    public void testAddEntity_Buffered_Discard() {
        // Setup test data
        Person p = new Person();
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));

        // Instruct mocks
        expect(
                batchableEntityProviderMock.getFirstEntityIdentifier(container, null,
                        orderby)).andStubReturn(122l);
        expect(
                batchableEntityProviderMock.getNextEntityIdentifier(container, 122l, null,
                        orderby)).andStubReturn(null);
        expect(
                batchableEntityProviderMock.getPreviousEntityIdentifier(container, 122l,
                        null, orderby)).andStubReturn(null);
        expect(batchableEntityProviderMock.getEntity((EntityContainer<Person>) anyObject(), anyObject()))
                .andStubReturn(null);
        replay(batchableEntityProviderMock);

        // Run test
        container.setEntityProvider(batchableEntityProviderMock);
        container.setWriteThrough(false);
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        assertFalse(container.isModified());

        Object id = container.addEntity(p);

        assertTrue(container.isModified());

        // Check that we can access the item using the temporary ID
        EntityItem<Person> item = container.getItem(id);
        assertEquals(id, item.getItemId());
        assertFalse(item.isPersistent());

        // Check that the item shows up in the list
        assertEquals(id, container.firstItemId());
        assertEquals(122l, container.nextItemId(id));
        assertEquals(id, container.prevItemId(122l));

        final int[] listenerCalled = new int[1];
        container.addListener(new ItemSetChangeListener() {

            public void containerItemSetChange(ItemSetChangeEvent event) {
                assertTrue(event instanceof JPAContainer.ChangesDiscardedEvent);
                listenerCalled[0]++;
            }
        });

        container.discard();

        assertEquals(1, listenerCalled[0]);
        assertFalse(container.isModified());

        // Check that the item is not in the list
        assertEquals(122l, container.firstItemId());
        assertNull(container.nextItemId(122l));
        assertNull(container.prevItemId(122l));
        assertNull(container.getItem(id));

        // Verify mocks
        verify(batchableEntityProviderMock);
    }

    // TODO Test all buffered mode operations.
    // TODO Test entity provider change event handling

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialization() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(container);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(is);
        JPAContainer<Person> otherContainer = (JPAContainer<Person>) ois
                .readObject();
        assertNotNull(otherContainer);
        assertEquals(container.getEntityClassMetadata(),
                otherContainer.getEntityClassMetadata());
        assertEquals(container.getEntityClass(),
                otherContainer.getEntityClass());
    }

    @Test
    public void testGetChildren() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));

        LinkedList<Object> result = new LinkedList<Object>();

        // Instruct mocks
        expect(
                entityProviderMock.getAllEntityIdentifiers(container, new Equal(
                        "manager.id", 123l), orderby)).andReturn(result);
        replay(entityProviderMock);

        // Set up container
        container.setParentProperty("manager");
        container.setEntityProvider(entityProviderMock);
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        // Run test
        assertSame(result, container.getChildren(123l));

        // Verify
        verify(entityProviderMock);
    }

    @Test
    public void testGetChildren_Filtered() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));

        LinkedList<Object> result = new LinkedList<Object>();

        // Instruct mocks
        expect(
                entityProviderMock.getAllEntityIdentifiers(container, new And(new Equal(
                        "manager.id", 123l), new Equal("firstName", "blah")),
                        orderby)).andReturn(result);
        replay(entityProviderMock);

        // Set up container
        container.setParentProperty("manager");
        container.setEntityProvider(entityProviderMock);
        container.sort(new Object[] { "firstName" }, new boolean[] { true });
        container.addContainerFilter(new Equal("firstName", "blah"));

        // Run test
        assertSame(result, container.getChildren(123l));

        // Verify
        verify(entityProviderMock);
    }

    @Test
    public void testRootItemIds() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));

        LinkedList<Object> result = new LinkedList<Object>();

        // Instruct mocks
        expect(
                entityProviderMock.getAllEntityIdentifiers(container, 
                        new IsNull("manager"), orderby)).andReturn(result);
        replay(entityProviderMock);

        // Set up container
        container.setParentProperty("manager");
        container.setEntityProvider(entityProviderMock);
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        // Run test
        assertSame(result, container.rootItemIds());

        // Verify
        verify(entityProviderMock);
    }

    @Test
    public void testHasChildren() {
        LinkedList<SortBy> orderby = new LinkedList<SortBy>();
        orderby.add(new SortBy("firstName", true));

        LinkedList<Object> result = new LinkedList<Object>();
        result.add(12l);

        // Instruct mocks
        expect(
                entityProviderMock.getAllEntityIdentifiers(container, new Equal(
                        "manager.id", 123l), orderby)).andReturn(
                Collections.emptyList());
        expect(
                entityProviderMock.getAllEntityIdentifiers(container, new Equal(
                        "manager.id", 123l), orderby)).andReturn(result);
        replay(entityProviderMock);

        // Set up container
        container.setParentProperty("manager");
        container.setEntityProvider(entityProviderMock);
        container.sort(new Object[] { "firstName" }, new boolean[] { true });

        // Run test
        assertFalse(container.hasChildren(123l));
        assertTrue(container.hasChildren(123l));

        // Verify
        verify(entityProviderMock);
    }

    @Test
    public void testGetParent_andIsRoot() {
        Person manager = new Person();
        manager.setId(123l);
        Person person = new Person();
        person.setId(456l);
        person.setManager(manager);

        // Instruct mocks
        expect(entityProviderMock.getEntity(container, 456l)).andStubReturn(person);
        replay(entityProviderMock);

        // Set up container
        container.setParentProperty("manager");
        container.setEntityProvider(entityProviderMock);

        // Run test
        assertEquals(123l, container.getParent(456l));
        assertFalse(container.isRoot(456l));

        // Verify
        verify(entityProviderMock);
    }

    @Test
    public void testGetParent_andIsRoot_Root() {
        Person person = new Person();
        person.setId(456l);

        // Instruct mocks
        expect(entityProviderMock.getEntity(container, 456l)).andStubReturn(person);
        replay(entityProviderMock);

        // Set up container
        container.setParentProperty("manager");
        container.setEntityProvider(entityProviderMock);

        // Run test
        assertNull(container.getParent(456l));
        assertTrue(container.isRoot(456l));

        // Verify
        verify(entityProviderMock);
    }

    @Test
    public void testNoItemSetChangeEventOnPropertyValueChange() {
        Person p = new Person();
        p.setId(123l);
        p.setFirstName("Joe");
        p.setLastName("Cool");
        expect(mutableEntityProviderMock.getEntity(container, 123l)).andReturn(p);
        mutableEntityProviderMock.updateEntityProperty(p.getId(), "firstName",
                "John");
        replay(mutableEntityProviderMock);

        container.setEntityProvider(mutableEntityProviderMock);
        container.setWriteThrough(true);
        TestItemSetChangeListener listener = new TestItemSetChangeListener();
        container.addListener(listener);

        EntityItem<Person> item = container.getItem(123l);
        assertEquals("Joe", item.getItemProperty("firstName").getValue());
        assertEquals("Cool", item.getItemProperty("lastName").getValue());

        item.getItemProperty("firstName").setValue("John");

        /*
         * "item set" has not changed, only a property of an item in that set.
         */
        assertEquals(0, listener.getCalled());

        assertEquals("John", p.getFirstName());

        verify(mutableEntityProviderMock);

    }

    public static class TestItemSetChangeListener implements
            Container.ItemSetChangeListener {

        private int called;
        private ItemSetChangeEvent lastEvent;

        public void containerItemSetChange(ItemSetChangeEvent event) {
            called++;
            lastEvent = event;
        }

        public int getCalled() {
            return called;
        }

        public ItemSetChangeEvent getLastEvent() {
            return lastEvent;
        }

    }

    @Test
    public void testRefreshContainerFiresItemSetChange() {
        ItemSetChangeListener listener = createMock(ItemSetChangeListener.class);
        listener.containerItemSetChange(isA(AllItemsRefreshedEvent.class));
        expectLastCall().once();
        replay(listener);
        container.setEntityProvider(entityProviderMock);
        container.addListener(listener);
        container.refresh();
        verify(listener);
    }

    @Test
    public void testRefreshContainerRefreshesEntityProvider() {
        entityProviderMock.refresh();
        expectLastCall().once();
        replay(entityProviderMock);
        container.setEntityProvider(entityProviderMock);
        container.refresh();
        verify(entityProviderMock);
    }

    @Test
    public void testRefreshContainerClearsBufferingDelegate() {
        container.setEntityProvider(batchableEntityProviderMock);
        container.setWriteThrough(false);
        Person p = new Person();
        p.setId(123l);
        p.setFirstName("Joe");
        p.setLastName("Cool");
        Object id = container.addEntity(p);
        assertEquals(p, container.getItem(id).getEntity());
        container.refresh();
        assertNull(container.getItem(id));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRefreshContainerDiscardsChanges() {
        Person p = new Person();
        p.setId(123l);
        p.setFirstName("Joe");
        p.setLastName("Cool");
        Person p2 = p.clone();

        expect(batchableEntityProviderMock.getLazyLoadingDelegate())
                .andStubReturn(null);
        expect(
                batchableEntityProviderMock.getFirstEntityIdentifier((EntityContainer<Person>) anyObject(), 
                        (Filter) isNull(), isA(List.class)))
                .andStubReturn(123L);
        batchableEntityProviderMock.refresh();
        expectLastCall().once();
        expect(batchableEntityProviderMock.getEntity((EntityContainer<Person>) anyObject(), isA(Object.class)))
                .andStubReturn(p);
        expect(batchableEntityProviderMock.refreshEntity(isA(Person.class)))
                .andStubReturn(p2);
        replay(batchableEntityProviderMock);
        container.setEntityProvider(batchableEntityProviderMock);

        Object id = container.firstItemId();
        JPAContainerItem<Person> item = (JPAContainerItem<Person>) container
                .getItem(id);
        item.getItemProperty("firstName").setValue("foo");
        container.refresh();
        assertEquals("Joe", item.getItemProperty("firstName").getValue());
    }
}
