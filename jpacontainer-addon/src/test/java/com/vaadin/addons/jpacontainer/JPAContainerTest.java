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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import org.easymock.Capture;
import org.easymock.IAnswer;
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
		assertTrue(container.getSortableContainerPropertyIds()
				.containsAll(
						container.getEntityClassMetadata()
								.getPersistentPropertyNames()));
		assertEquals(container.getSortableContainerPropertyIds().size(),
				container.getEntityClassMetadata().getPersistentPropertyNames()
						.size());
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
		container.addFilter(Filters.eq("firstName", "Hello", false));

		assertFalse(listenerCalled[0]);
		assertTrue(container.getFilters().contains(
				Filters.eq("firstName", "Hello", false)));
		assertTrue(container.getAppliedFilters().isEmpty());
		assertTrue(container.hasUnappliedFilters());

		container.applyFilters();
		assertTrue(listenerCalled[0]);
		assertEquals(container.getFilters(), container.getAppliedFilters());
		assertTrue(container.getFilters().contains(
				Filters.eq("firstName", "Hello", false)));
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
		container.addFilter(Filters.eq("firstName", "Hello", false));

		assertEquals(container.getFilters(), container.getAppliedFilters());
		assertTrue(container.getFilters().contains(
				Filters.eq("firstName", "Hello", false)));
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
	public void testSize_WriteThrough() {
		expect(
				entityProviderMock.getEntityCount(Filters.and(Filters.eq(
						"firstName", "Hello", false), Filters.eq("lastName",
						"World", false)))).andReturn(123);
		replay(entityProviderMock);

		assertTrue(container.isApplyFiltersImmediately());
		container.addFilter(Filters.eq("firstName", "Hello", false));
		container.addFilter(Filters.eq("lastName", "World", false));
		container.setEntityProvider(entityProviderMock);
		container.setWriteThrough(true);

		assertEquals(123, container.size());

		verify(entityProviderMock);
	}

	@Test
	public void testIndexOfId_WriteThrough() {
		expect(entityProviderMock.getEntityCount(null)).andStubReturn(5);
		expect(
				entityProviderMock.getEntityIdentifierAt(null,
						new LinkedList<SortBy>(), 0)).andStubReturn("id1");
		expect(
				entityProviderMock.getEntityIdentifierAt(null,
						new LinkedList<SortBy>(), 1)).andStubReturn("id2");
		expect(
				entityProviderMock.getEntityIdentifierAt(null,
						new LinkedList<SortBy>(), 2)).andStubReturn("id3");
		expect(
				entityProviderMock.getEntityIdentifierAt(null,
						new LinkedList<SortBy>(), 3)).andStubReturn("id4");
		expect(
				entityProviderMock.getEntityIdentifierAt(null,
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
		expect(batchableEntityProviderMock.getEntityCount(null)).andStubReturn(
				5);
		expect(
				batchableEntityProviderMock.getEntityIdentifierAt(null, sortby,
						0)).andStubReturn("id1");
		expect(
				batchableEntityProviderMock.getEntityIdentifierAt(null, sortby,
						1)).andStubReturn("id2");
		expect(
				batchableEntityProviderMock.getEntityIdentifierAt(null, sortby,
						2)).andStubReturn("id3");
		expect(
				batchableEntityProviderMock.getEntityIdentifierAt(null, sortby,
						3)).andStubReturn("id4");
		expect(
				batchableEntityProviderMock.getEntityIdentifierAt(null, sortby,
						4)).andStubReturn(null);
		expect(batchableEntityProviderMock.containsEntity("id4", null))
				.andStubReturn(true);
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
		// Item should still be there, although it is marked for deletion
		assertEquals(4, container.indexOfId("id4"));

		verify(batchableEntityProviderMock);
	}

	@Test
	public void testGetIdByIndex_WriteThrough() {
		LinkedList<SortBy> orderby = new LinkedList<SortBy>();
		orderby.add(new SortBy("firstName", true));
		expect(
				entityProviderMock.getEntityIdentifierAt(null,
						new LinkedList<SortBy>(), 1)).andReturn("id1");
		expect(
				entityProviderMock.getEntityIdentifierAt(null,
						new LinkedList<SortBy>(), 2)).andReturn(null);
		expect(
				entityProviderMock.getEntityIdentifierAt(Filters.and(Filters
						.eq("firstName", "Hello", false)), orderby, 3))
				.andReturn("id3");
		replay(entityProviderMock);

		container.setEntityProvider(entityProviderMock);
		container.setWriteThrough(true);

		assertEquals("id1", container.getIdByIndex(1));
		assertNull(container.getIdByIndex(2));

		// Now let's try with a filter and some sorting
		container.addFilter(Filters.eq("firstName", "Hello", false));
		container.sort(new Object[] { "firstName" }, new boolean[] { true });

		assertEquals("id3", container.getIdByIndex(3));

		verify(entityProviderMock);
	}

	@Test
	public void testGetIdByIndex_Buffered() {
		LinkedList<SortBy> orderby = new LinkedList<SortBy>();
		orderby.add(new SortBy("firstName", true));
		expect(
				batchableEntityProviderMock.getEntityIdentifierAt(null,
						new LinkedList<SortBy>(), 0)).andStubReturn("id1");
		expect(
				batchableEntityProviderMock.getEntityIdentifierAt(null,
						new LinkedList<SortBy>(), 1)).andStubReturn(null);
		expect(
				batchableEntityProviderMock.getEntityIdentifierAt(Filters
						.and(Filters.eq("firstName", "Hello", false)), orderby,
						2)).andStubReturn("id3");
		expect(batchableEntityProviderMock.containsEntity("id3", null))
				.andStubReturn(true);
		replay(batchableEntityProviderMock);

		container.setEntityProvider(batchableEntityProviderMock);
		container.setWriteThrough(false);

		assertEquals("id1", container.getIdByIndex(0));
		assertNull(container.getIdByIndex(1));

		// Now let's try with a filter and some sorting
		container.addFilter(Filters.eq("firstName", "Hello", false));
		container.sort(new Object[] { "firstName" }, new boolean[] { true });

		assertEquals("id3", container.getIdByIndex(2));

		// Clear filters and sorting
		container.removeAllFilters();
		container.sort(new Object[] {}, new boolean[] {});

		// Add an item
		Object id = container.addEntity(new Person());
		assertEquals(id, container.getIdByIndex(0));
		assertEquals("id1", container.getIdByIndex(1));
		assertNull(container.getIdByIndex(2));

		// Apply filter and sorting again
		container.addFilter(Filters.eq("firstName", "Hello", false));
		container.sort(new Object[] { "firstName" }, new boolean[] { true });

		assertEquals(id, container.getIdByIndex(0));
		assertEquals("id3", container.getIdByIndex(3));

		// Remove last item
		container.removeItem("id3");
		// Should still be in the container
		assertEquals("id3", container.getIdByIndex(3));

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

		expect(entityProviderMock.getAllEntityIdentifiers(null, orderby))
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
				batchableEntityProviderMock.getAllEntityIdentifiers(null,
						orderby)).andStubReturn(idList);
		expect(batchableEntityProviderMock.containsEntity("id4", null))
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
		// Should still be there
		ids = container.getItemIds();
		assertEquals(5, ids.size());
		assertTrue(ids.contains("id4"));

		verify(batchableEntityProviderMock);
	}

	@Test
	public void testGetItem_WriteThrough() {
		Person p = new Person();
		p.setId(123l);
		p.setFirstName("Joe");
		p.setLastName("Cool");
		expect(entityProviderMock.getEntity(123l)).andReturn(p);
		expect(entityProviderMock.getEntity("nonExistent")).andReturn(null);
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

		expect(batchableEntityProviderMock.getEntity(123l)).andStubAnswer(
				new IAnswer<Person>() {

					public Person answer() throws Throwable {
						return p.clone();
					}
				});
		expect(batchableEntityProviderMock.containsEntity(123l, null))
				.andStubReturn(true);
		expect(batchableEntityProviderMock.getEntity("nonExistent"))
				.andStubReturn(null);
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
		expect(entityProviderMock.getEntity("myId")).andStubReturn(p);
		expect(entityProviderMock.getEntity("nonExistent")).andStubReturn(null);
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
		expect(entityProviderMock.containsEntity("id", null)).andReturn(true);
		expect(
				entityProviderMock.containsEntity("id2", Filters.and(Filters
						.eq("firstName", "Hello", false)))).andReturn(false);
		replay(entityProviderMock);

		container.setEntityProvider(entityProviderMock);
		container.setWriteThrough(true);

		assertTrue(container.containsId("id"));
		assertTrue(container.isApplyFiltersImmediately());
		container.addFilter(Filters.eq("firstName", "Hello", false));
		assertFalse(container.containsId("id2"));

		verify(entityProviderMock);
	}

	@Test
	public void testContainsId_Buffered() {
		expect(batchableEntityProviderMock.containsEntity("id", null))
				.andStubReturn(true);
		expect(batchableEntityProviderMock.containsEntity("id2", null))
				.andStubReturn(true);
		expect(
				batchableEntityProviderMock.containsEntity("id", Filters
						.and(Filters.eq("firstName", "Hello", false))))
				.andStubReturn(true);
		expect(
				batchableEntityProviderMock.containsEntity("id2", Filters
						.and(Filters.eq("firstName", "Hello", false))))
				.andStubReturn(false);
		replay(batchableEntityProviderMock);

		container.setEntityProvider(batchableEntityProviderMock);
		container.setWriteThrough(false);

		assertTrue(container.containsId("id"));
		assertTrue(container.isApplyFiltersImmediately());
		container.addFilter(Filters.eq("firstName", "Hello", false));
		assertFalse(container.containsId("id2"));

		// Add an item
		Object id = container.addEntity(new Person());
		assertTrue(container.containsId(id));
		assertTrue(container.containsId("id"));
		assertFalse(container.containsId("id2"));

		// Clear filtering
		container.removeAllFilters();
		// Item should still be there
		assertTrue(container.containsId(id));
		assertTrue(container.containsId("id"));
		assertTrue(container.containsId("id2"));

		// Remove an item
		container.removeItem("id2");
		// should still be there
		assertTrue(container.containsId("id2"));

		verify(batchableEntityProviderMock);
	}

	@Test
	public void testFirstItemIdAndIsFirstId_WriteThrough() {
		LinkedList<SortBy> orderby = new LinkedList<SortBy>();
		orderby.add(new SortBy("firstName", true));
		expect(
				entityProviderMock.getFirstEntityIdentifier(null,
						new LinkedList<SortBy>())).andReturn("id1").times(2);
		expect(
				entityProviderMock.getFirstEntityIdentifier(null,
						new LinkedList<SortBy>())).andReturn(null).times(2);
		expect(
				entityProviderMock.getFirstEntityIdentifier(Filters.and(Filters
						.eq("firstName", "Hello", false)), orderby)).andReturn(
				"id2").times(3);
		replay(entityProviderMock);

		container.setEntityProvider(entityProviderMock);
		container.setWriteThrough(true);

		assertEquals("id1", container.firstItemId());
		assertTrue(container.isFirstId("id1"));

		assertNull(container.firstItemId());
		assertFalse(container.isFirstId("id1"));

		// Now let's try with a filter and some sorting
		container.addFilter(Filters.eq("firstName", "Hello", false));
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
				batchableEntityProviderMock.getFirstEntityIdentifier(null,
						new LinkedList<SortBy>())).andReturn("id1").times(2)
				.andReturn(null).times(2);
		expect(
				batchableEntityProviderMock.getFirstEntityIdentifier(Filters
						.and(Filters.eq("firstName", "Hello", false)), orderby))
				.andReturn("id2").times(3);
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
		container.addFilter(Filters.eq("firstName", "Hello", false));
		container.sort(new Object[] { "firstName" }, new boolean[] { true });

		assertEquals("id2", container.firstItemId());
		assertTrue(container.isFirstId("id2"));
		assertFalse(container.isFirstId("id3"));

		/*
		 * One added item
		 */
		// Reset filtering and sorting
		container.removeAllFilters();
		container.sort(new Object[] {}, new boolean[] {});

		Object id1 = container.addEntity(new Person());

		assertEquals(id1, container.firstItemId());
		assertTrue(container.isFirstId(id1));
		assertFalse(container.isFirstId("id1"));

		// Add filtering and sorting
		container.addFilter(Filters.eq("firstName", "Hello", false));
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
				entityProviderMock.getLastEntityIdentifier(null,
						new LinkedList<SortBy>())).andReturn("id1").times(2);
		expect(
				entityProviderMock.getLastEntityIdentifier(null,
						new LinkedList<SortBy>())).andReturn(null).times(2);
		expect(
				entityProviderMock.getLastEntityIdentifier(Filters.and(Filters
						.eq("firstName", "Hello", false)), orderby)).andReturn(
				"id2").times(3);
		replay(entityProviderMock);

		container.setEntityProvider(entityProviderMock);
		container.setWriteThrough(true);

		assertEquals("id1", container.lastItemId());
		assertTrue(container.isLastId("id1"));

		assertNull(container.lastItemId());
		assertFalse(container.isLastId("id1"));

		// Now let's try with a filter and some sorting
		container.addFilter(Filters.eq("firstName", "Hello", false));
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
				batchableEntityProviderMock.getLastEntityIdentifier(null,
						new LinkedList<SortBy>())).andReturn("id1").times(2)
				.andReturn(null).times(2);
		expect(
				batchableEntityProviderMock.getLastEntityIdentifier(Filters
						.and(Filters.eq("firstName", "Hello", false)), orderby))
				.andReturn("id2").times(6).andReturn(null).times(3);
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
		container.addFilter(Filters.eq("firstName", "Hello", false));
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
				entityProviderMock.getNextEntityIdentifier("id1", null,
						new LinkedList<SortBy>())).andReturn("id2");
		expect(
				entityProviderMock.getNextEntityIdentifier("id2", null,
						new LinkedList<SortBy>())).andReturn(null);
		expect(
				entityProviderMock.getNextEntityIdentifier("id3", Filters
						.and(Filters.eq("firstName", "Hello", false)), orderby))
				.andReturn("id4");
		expect(
				entityProviderMock.getNextEntityIdentifier("id4", Filters
						.and(Filters.eq("firstName", "Hello", false)), orderby))
				.andReturn(null);
		replay(entityProviderMock);

		container.setEntityProvider(entityProviderMock);
		container.setWriteThrough(true);

		assertEquals("id2", container.nextItemId("id1"));
		assertNull(container.nextItemId("id2"));

		// Now let's try with a filter and some sorting
		container.addFilter(Filters.eq("firstName", "Hello", false));
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
				entityProviderMock.getPreviousEntityIdentifier("id1", null,
						new LinkedList<SortBy>())).andReturn("id2");
		expect(
				entityProviderMock.getPreviousEntityIdentifier("id2", null,
						new LinkedList<SortBy>())).andReturn(null);
		expect(
				entityProviderMock.getPreviousEntityIdentifier("id3", Filters
						.and(Filters.eq("firstName", "Hello", false)), orderby))
				.andReturn("id4");
		expect(
				entityProviderMock.getPreviousEntityIdentifier("id4", Filters
						.and(Filters.eq("firstName", "Hello", false)), orderby))
				.andReturn(null);
		replay(entityProviderMock);

		container.setEntityProvider(entityProviderMock);
		container.setWriteThrough(true);

		assertEquals("id2", container.prevItemId("id1"));
		assertNull(container.prevItemId("id2"));

		// Now let's try with a filter and some sorting
		container.addFilter(Filters.eq("firstName", "Hello", false));
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

			@SuppressWarnings("unchecked")
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

			@SuppressWarnings("unchecked")
			public void containerItemSetChange(ItemSetChangeEvent event) {
				assertTrue(event instanceof JPAContainer.ItemAddedEvent);
				assertEquals(123l, ((JPAContainer.ItemAddedEvent) event)
						.getItemId());
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

			@SuppressWarnings("unchecked")
			public void containerItemSetChange(ItemSetChangeEvent event) {
				assertTrue(event instanceof JPAContainer.ItemRemovedEvent);
				assertEquals(123l, ((JPAContainer.ItemRemovedEvent) event)
						.getItemId());
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
				batchableEntityProviderMock.getFirstEntityIdentifier(null,
						orderby)).andStubReturn(122l);
		expect(
				batchableEntityProviderMock.getNextEntityIdentifier(122l, null,
						orderby)).andStubReturn(123l);
		expect(
				batchableEntityProviderMock.getPreviousEntityIdentifier(122l,
						null, orderby)).andStubReturn(null);
		expect(batchableEntityProviderMock.getEntityCount(null)).andStubReturn(
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
		expect(batchableEntityProviderMock.getEntity(123l)).andStubReturn(pp);
		expect(batchableEntityProviderMock.getEntity(anyObject()))
				.andStubReturn(null);
		replay(batchableEntityProviderMock);

		expect(mutableEntityProviderMock.addEntity(p)).andReturn(pp);
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
				batchableEntityProviderMock.getFirstEntityIdentifier(null,
						orderby)).andStubReturn(122l);
		expect(
				batchableEntityProviderMock.getNextEntityIdentifier(122l, null,
						orderby)).andStubReturn(null);
		expect(
				batchableEntityProviderMock.getPreviousEntityIdentifier(122l,
						null, orderby)).andStubReturn(null);
		expect(batchableEntityProviderMock.getEntity(anyObject()))
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

			@SuppressWarnings("unchecked")
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
		assertEquals(container.getEntityClassMetadata(), otherContainer
				.getEntityClassMetadata());
		assertEquals(container.getEntityClass(), otherContainer
				.getEntityClass());
	}
}
