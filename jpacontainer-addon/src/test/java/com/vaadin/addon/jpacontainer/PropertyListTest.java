/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.metadata.ClassMetadata;
import com.vaadin.addon.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addon.jpacontainer.testdata.Address;
import com.vaadin.addon.jpacontainer.testdata.Person;

/**
 * Test case for {@link PropertyList}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class PropertyListTest {

	private ClassMetadata<Person> metadata = MetadataFactory.getInstance()
			.getEntityClassMetadata(Person.class);
	private PropertyList<Person> propertyList;
	private PropertyList<Person> childPropertyList;

	@Before
	public void setUp() {
		propertyList = new PropertyList<Person>(metadata);
		childPropertyList = new PropertyList<Person>(propertyList);
	}

	@Test
	public void testInitialPropertyList() {
		assertEquals(10, propertyList.getPersistentPropertyNames().size());
		assertTrue(propertyList.getPersistentPropertyNames().contains("id"));
		assertTrue(propertyList.getPersistentPropertyNames()
				.contains("version"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"firstName"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"lastName"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"dateOfBirth"));
		assertTrue(propertyList.getPersistentPropertyNames()
				.contains("address"));
		assertTrue(propertyList.getPersistentPropertyNames()
				.contains("manager"));
		assertTrue(propertyList.getPersistentPropertyNames().contains("skills"));
		assertTrue(propertyList.getPersistentPropertyNames().contains("male"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"primitiveDouble"));

		assertEquals(13, propertyList.getPropertyNames().size());
		assertTrue(propertyList.getPropertyNames().containsAll(
				propertyList.getPersistentPropertyNames()));
		assertTrue(propertyList.getPropertyNames().contains("fullName"));
		assertTrue(propertyList.getPropertyNames().contains("transientAddress"));
		assertTrue(propertyList.getPropertyNames().contains("tempData"));
		assertTrue(propertyList.getNestedPropertyNames().isEmpty());
		assertEquals(propertyList.getPropertyNames(),
				propertyList.getAllAvailablePropertyNames());

		// And test the child list
		assertEquals(propertyList.getAllAvailablePropertyNames(),
				childPropertyList.getAllAvailablePropertyNames());
		assertEquals(propertyList.getNestedPropertyNames(),
				childPropertyList.getNestedPropertyNames());
		assertEquals(propertyList.getPropertyNames(),
				childPropertyList.getPropertyNames());
		assertEquals(propertyList.getPersistentPropertyNames(),
				childPropertyList.getPersistentPropertyNames());
		assertSame(propertyList, childPropertyList.getParentList());

		// All the persistent properties should be sortable
		assertFalse(propertyList.getSortablePropertyNames().contains("address"));
		assertFalse(propertyList.getSortablePropertyNames().contains("manager"));
		assertFalse(propertyList.getSortablePropertyNames().contains("skills"));
		assertEquals(7, propertyList.getSortablePropertyNames().size());
		assertTrue(propertyList.getPersistentPropertyNames().containsAll(
				propertyList.getSortablePropertyNames()));
	}

	@Test
	public void testRemoveProperty() {
		assertTrue(propertyList.getPropertyNames().contains("fullName"));
		assertTrue(propertyList.removeProperty("fullName"));
		assertFalse(propertyList.getPropertyNames().contains("fullName"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"fullName"));

		assertTrue(propertyList.getPropertyNames().contains("firstName"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"firstName"));
		assertTrue(propertyList.getSortablePropertyNames()
				.contains("firstName"));
		assertTrue(propertyList.removeProperty("firstName"));
		assertFalse(propertyList.getPropertyNames().contains("firstName"));
		assertFalse(propertyList.getPersistentPropertyNames().contains(
				"firstName"));
		assertFalse(propertyList.getSortablePropertyNames().contains(
				"firstName"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"firstName"));

		assertFalse(propertyList.removeProperty("nonexistent"));

		// And test the child list
		assertEquals(propertyList.getAllAvailablePropertyNames(),
				childPropertyList.getAllAvailablePropertyNames());
		assertEquals(propertyList.getNestedPropertyNames(),
				childPropertyList.getNestedPropertyNames());
		assertEquals(propertyList.getPropertyNames(),
				childPropertyList.getPropertyNames());
		assertEquals(propertyList.getPersistentPropertyNames(),
				childPropertyList.getPersistentPropertyNames());
		assertEquals(propertyList.getSortablePropertyNames(),
				childPropertyList.getSortablePropertyNames());

		assertTrue(childPropertyList.getPropertyNames().contains("lastName"));
		assertFalse(childPropertyList.removeProperty("lastName"));
		assertTrue(childPropertyList.getPropertyNames().contains("lastName"));
	}

	@Test
	public void testAddSinglePersistentNestedProperty() {
		propertyList.addNestedProperty("address.street");
		assertTrue(propertyList.getPropertyNames().contains("address.street"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"address.street"));
		assertTrue(propertyList.getSortablePropertyNames().contains(
				"address.street"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"address.street"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"address.street"));

		// Check child list
		assertTrue(childPropertyList.getPropertyNames().contains(
				"address.street"));
		assertTrue(childPropertyList.getPersistentPropertyNames().contains(
				"address.street"));
		assertTrue(childPropertyList.getSortablePropertyNames().contains(
				"address.street"));
		assertTrue(childPropertyList.getNestedPropertyNames().contains(
				"address.street"));
		assertTrue(childPropertyList.getAllAvailablePropertyNames().contains(
				"address.street"));
	}

	@Test
	public void testAddSinglePersistentNestedProperty_ChildList() {
		childPropertyList.addNestedProperty("address.street");
		assertFalse(propertyList.getAllAvailablePropertyNames().contains(
				"address.street"));

		assertTrue(childPropertyList.getPropertyNames().contains(
				"address.street"));
		assertTrue(childPropertyList.getPersistentPropertyNames().contains(
				"address.street"));
		assertTrue(childPropertyList.getSortablePropertyNames().contains(
				"address.street"));
		assertTrue(childPropertyList.getNestedPropertyNames().contains(
				"address.street"));
		assertTrue(childPropertyList.getAllAvailablePropertyNames().contains(
				"address.street"));
	}

	@Test
	public void testAddSingleTransientNestedProperty() {
		// Transient property of a transient "embedded" property
		propertyList.addNestedProperty("transientAddress.street");
		assertTrue(propertyList.getPropertyNames().contains(
				"transientAddress.street"));
		assertFalse(propertyList.getPersistentPropertyNames().contains(
				"transientAddress.street"));
		assertFalse(propertyList.getSortablePropertyNames().contains(
				"transientAddress.street"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"transientAddress.street"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"transientAddress.street"));

		// Transient property of a persistent embedded property
		propertyList.addNestedProperty("address.fullAddress");
		assertTrue(propertyList.getPropertyNames().contains(
				"address.fullAddress"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"address.fullAddress"));
		assertFalse(propertyList.getPersistentPropertyNames().contains(
				"address.fullAddress"));
		assertFalse(propertyList.getSortablePropertyNames().contains(
				"address.fullAddress"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"address.fullAddress"));
	}

	@Test
	public void testAddSingleTransientNestedProperty_ChildList() {
		// Transient property of a transient "embedded" property
		childPropertyList.addNestedProperty("transientAddress.street");
		assertFalse(propertyList.getAllAvailablePropertyNames().contains(
				"transientAddress.street"));
		assertTrue(childPropertyList.getPropertyNames().contains(
				"transientAddress.street"));
		assertFalse(childPropertyList.getPersistentPropertyNames().contains(
				"transientAddress.street"));
		assertFalse(childPropertyList.getSortablePropertyNames().contains(
				"transientAddress.street"));
		assertTrue(childPropertyList.getNestedPropertyNames().contains(
				"transientAddress.street"));
		assertTrue(childPropertyList.getAllAvailablePropertyNames().contains(
				"transientAddress.street"));

		// Transient property of a persistent embedded property
		childPropertyList.addNestedProperty("address.fullAddress");
		assertFalse(propertyList.getAllAvailablePropertyNames().contains(
				"address.fullAddress"));
		assertTrue(childPropertyList.getPropertyNames().contains(
				"address.fullAddress"));
		assertTrue(childPropertyList.getNestedPropertyNames().contains(
				"address.fullAddress"));
		assertFalse(childPropertyList.getPersistentPropertyNames().contains(
				"address.fullAddress"));
		assertFalse(childPropertyList.getSortablePropertyNames().contains(
				"address.fullAddress"));
		assertTrue(childPropertyList.getAllAvailablePropertyNames().contains(
				"address.fullAddress"));
	}

	@Test
	public void testAddSingleNestedProperty_Illegal() {
		try {
			propertyList.addNestedProperty("address");
			fail("Did not throw exception even though nested property was not nested");
		} catch (IllegalArgumentException e) {
			// OK
		}
		try {
			propertyList.addNestedProperty("address.nonexistent");
			fail("Did not throw exception even though nested property was nonexistent");
		} catch (IllegalArgumentException e) {
			assertFalse(propertyList.getPropertyNames().contains(
					"address.nonexistent"));
			assertFalse(propertyList.getNestedPropertyNames().contains(
					"address.nonexistent"));
			assertFalse(propertyList.getPersistentPropertyNames().contains(
					"address.nonexistent"));
			assertFalse(propertyList.getSortablePropertyNames().contains(
					"address.nonexistent"));
			assertFalse(propertyList.getAllAvailablePropertyNames().contains(
					"address.nonexistent"));
		}
		try {
			propertyList.addNestedProperty("nonexistent.street");
			fail("Did not throw exception even though nested property was nonexistent");
		} catch (IllegalArgumentException e) {
			assertFalse(propertyList.getPropertyNames().contains(
					"nonexistent.street"));
			assertFalse(propertyList.getNestedPropertyNames().contains(
					"nonexistent.street"));
			assertFalse(propertyList.getPersistentPropertyNames().contains(
					"nonexistent.street"));
			assertFalse(propertyList.getSortablePropertyNames().contains(
					"nonexistent.street"));
			assertFalse(propertyList.getAllAvailablePropertyNames().contains(
					"nonexistent.street"));
		}
		try {
			propertyList.addNestedProperty("firstName.nonexistent");
			fail("Did not throw exception even though nested property was nonexistent");
		} catch (IllegalArgumentException e) {
			assertFalse(propertyList.getPropertyNames().contains(
					"firstName.nonexistent"));
			assertFalse(propertyList.getNestedPropertyNames().contains(
					"firstName.nonexistent"));
			assertFalse(propertyList.getPersistentPropertyNames().contains(
					"firstName.nonexistent"));
			assertFalse(propertyList.getSortablePropertyNames().contains(
					"firstName.nonexistent"));
			assertFalse(propertyList.getAllAvailablePropertyNames().contains(
					"firstName.nonexistent"));
		}
	}

	@Test
	public void testAddNestedPersistentPropertyWithWildcards() {
		propertyList.addNestedProperty("address.*");

		// Persistent properties
		assertTrue(propertyList.getPropertyNames().contains("address.street"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"address.street"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"address.street"));
		assertTrue(propertyList.getSortablePropertyNames().contains(
				"address.street"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"address.street"));

		assertTrue(propertyList.getPropertyNames().contains(
				"address.postOffice"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"address.postOffice"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"address.postOffice"));
		assertTrue(propertyList.getSortablePropertyNames().contains(
				"address.postOffice"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"address.postOffice"));

		assertTrue(propertyList.getPropertyNames().contains(
				"address.postalCode"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"address.postalCode"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"address.postalCode"));
		assertTrue(propertyList.getSortablePropertyNames().contains(
				"address.postalCode"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"address.postalCode"));

		// Transient properties
		assertTrue(propertyList.getPropertyNames().contains(
				"address.fullAddress"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"address.fullAddress"));
		assertFalse(propertyList.getPersistentPropertyNames().contains(
				"address.fullAddress"));
		assertFalse(propertyList.getSortablePropertyNames().contains(
				"address.fullAddress"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"address.fullAddress"));
	}

	@Test
	public void testAddNestedTransientPropertyWithWildcards() {
		propertyList.addNestedProperty("transientAddress.*");

		assertTrue(propertyList.getPropertyNames().contains(
				"transientAddress.street"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"transientAddress.street"));
		assertFalse(propertyList.getPersistentPropertyNames().contains(
				"transientAddress.street"));
		assertFalse(propertyList.getSortablePropertyNames().contains(
				"transientAddress.street"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"transientAddress.street"));

		assertTrue(propertyList.getPropertyNames().contains(
				"transientAddress.postOffice"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"transientAddress.postOffice"));
		assertFalse(propertyList.getPersistentPropertyNames().contains(
				"transientAddress.postOffice"));
		assertFalse(propertyList.getSortablePropertyNames().contains(
				"transientAddress.postOffice"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"transientAddress.postOffice"));

		assertTrue(propertyList.getPropertyNames().contains(
				"transientAddress.postalCode"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"transientAddress.postalCode"));
		assertFalse(propertyList.getPersistentPropertyNames().contains(
				"transientAddress.postalCode"));
		assertFalse(propertyList.getSortablePropertyNames().contains(
				"transientAddress.postalCode"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"transientAddress.postalCode"));

		assertTrue(propertyList.getPropertyNames().contains(
				"transientAddress.fullAddress"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"transientAddress.fullAddress"));
		assertFalse(propertyList.getPersistentPropertyNames().contains(
				"transientAddress.fullAddress"));
		assertFalse(propertyList.getSortablePropertyNames().contains(
				"transientAddress.fullAddress"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"transientAddress.fullAddress"));
	}

	@Test
	public void testAddNestedPropertyWithWildcards_Illegal() {
		int oldSize = propertyList.getPropertyNames().size();
		int oldPersistentSize = propertyList.getPersistentPropertyNames()
				.size();

		try {
			propertyList.addNestedProperty("nonexistent.*");
			fail("Did not throw exception even though nested property was nonexistent");
		} catch (IllegalArgumentException e) {
			assertEquals(oldSize, propertyList.getPropertyNames().size());
			assertEquals(oldPersistentSize, propertyList
					.getPersistentPropertyNames().size());
		}

		try {
			propertyList.addNestedProperty("address.*.nothing");
			fail("Did not throw exception even though nested property was nonexistent");
		} catch (IllegalArgumentException e) {
			assertEquals(oldSize, propertyList.getPropertyNames().size());
			assertEquals(oldPersistentSize, propertyList
					.getPersistentPropertyNames().size());
		}
	}

	@Test
	public void testRemoveNestedProperty() {
		propertyList.addNestedProperty("address.street");

		assertTrue(propertyList.getPropertyNames().contains("address.street"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"address.street"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"address.street"));
		assertTrue(propertyList.getSortablePropertyNames().contains(
				"address.street"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"address.street"));

		propertyList.removeProperty("address.street");

		assertFalse(propertyList.getPropertyNames().contains("address.street"));
		assertFalse(propertyList.getNestedPropertyNames().contains(
				"address.street"));
		assertFalse(propertyList.getPersistentPropertyNames().contains(
				"address.street"));
		assertFalse(propertyList.getSortablePropertyNames().contains(
				"address.street"));
		assertFalse(propertyList.getAllAvailablePropertyNames().contains(
				"address.street"));
	}

	@Test
	public void testRemoveNestedProperty_ChildList() {
		propertyList.addNestedProperty("address.street");
		childPropertyList.addNestedProperty("address.postalCode");

		assertTrue(childPropertyList.getAllAvailablePropertyNames().contains(
				"address.street"));
		assertTrue(childPropertyList.getAllAvailablePropertyNames().contains(
				"address.postalCode"));

		assertFalse(childPropertyList.removeProperty("address.street"));
		assertTrue(childPropertyList.getAllAvailablePropertyNames().contains(
				"address.street"));

		assertTrue(childPropertyList.removeProperty("address.postalCode"));
		assertFalse(childPropertyList.getAllAvailablePropertyNames().contains(
				"address.postalCode"));
	}

	@Test
	public void testNonSortablePersistentNestedProperty() {
		propertyList.addNestedProperty("manager.address");

		assertTrue(propertyList.getPropertyNames().contains("manager.address"));
		assertTrue(propertyList.getNestedPropertyNames().contains(
				"manager.address"));
		assertTrue(propertyList.getPersistentPropertyNames().contains(
				"manager.address"));
		assertFalse(propertyList.getSortablePropertyNames().contains(
				"manager.address"));
		assertTrue(propertyList.getAllAvailablePropertyNames().contains(
				"manager.address"));
	}

	@Test
	public void testGetPropertyValue_TransientProperty() {
		Person p = new Person();
		p.setFirstName("Joe");
		p.setLastName("Cool");
		assertEquals("Joe Cool", propertyList.getPropertyValue(p, "fullName"));
		assertEquals("Joe Cool",
				childPropertyList.getPropertyValue(p, "fullName"));
	}

	@Test
	public void testGetPropertyValue_PersistentProperty() {
		Person p = new Person();
		p.setFirstName("Joe");
		assertEquals("Joe", propertyList.getPropertyValue(p, "firstName"));
		assertEquals("Joe", childPropertyList.getPropertyValue(p, "firstName"));
	}

	@Test
	public void testGetPropertyValue_NestedTransientProperty() {
		Person p = new Person();
		propertyList.addNestedProperty("transientAddress.street");
		propertyList.addNestedProperty("address.fullAddress");
		assertNull(propertyList.getPropertyValue(p, "transientAddress.street"));
		assertNull(propertyList.getPropertyValue(p, "address.fullAddress"));
		assertNull(childPropertyList.getPropertyValue(p,
				"transientAddress.street"));
		assertNull(childPropertyList.getPropertyValue(p, "address.fullAddress"));

		// transientAddress and address return the same value
		p.setAddress(new Address());
		p.getAddress().setStreet("Street");
		p.getAddress().setPostalCode("Code");
		p.getAddress().setPostOffice("Office");

		assertEquals("Street",
				propertyList.getPropertyValue(p, "transientAddress.street"));
		assertEquals("Street Code Office",
				propertyList.getPropertyValue(p, "address.fullAddress"));
		assertEquals("Street", childPropertyList.getPropertyValue(p,
				"transientAddress.street"));
		assertEquals("Street Code Office",
				childPropertyList.getPropertyValue(p, "address.fullAddress"));
	}

	@Test
	public void testGetPropertyValue_NestedPersistentProperty() {
		Person p = new Person();
		propertyList.addNestedProperty("address.street");
		assertNull(propertyList.getPropertyValue(p, "address.street"));
		assertNull(childPropertyList.getPropertyValue(p, "address.street"));

		p.setAddress(new Address());
		p.getAddress().setStreet("Hello World");
		assertEquals("Hello World",
				propertyList.getPropertyValue(p, "address.street"));
		assertEquals("Hello World",
				childPropertyList.getPropertyValue(p, "address.street"));
	}

	@Test
	public void testGetPropertyValue_Invalid() {
		Person p = new Person();
		try {
			propertyList.getPropertyValue(p, "nonexistent");
			fail("No exception thrown");
		} catch (IllegalArgumentException e) {
			// OK.
		}

		try {
			// Valid property name, but has not been added
			propertyList.getPropertyValue(p, "address.street");
			fail("No exception thrown");
		} catch (IllegalArgumentException e) {
			// OK.
		}
	}

	@Test
	public void testSetPropertyValue_TransientProperty() {
		Person p = new Person();
		propertyList.setPropertyValue(p, "tempData", "Hello World");
		assertEquals("Hello World", p.getTempData());
		childPropertyList.setPropertyValue(p, "tempData", "World Hello");
		assertEquals("World Hello", p.getTempData());
	}

	@Test
	public void testSetPropertyValue_PersistentProperty() {
		Person p = new Person();
		propertyList.setPropertyValue(p, "firstName", "Joe");
		assertEquals("Joe", p.getFirstName());
		childPropertyList.setPropertyValue(p, "firstName", "Max");
		assertEquals("Max", p.getFirstName());
	}

	@Test
	public void testSetPropertyValue_NestedTransientProperty() {
		Person p = new Person();
		p.setAddress(new Address());
		propertyList.addNestedProperty("transientAddress.tempData");
		propertyList.setPropertyValue(p, "transientAddress.tempData",
				"Hello World");
		assertEquals("Hello World", p.getAddress().getTempData());
		childPropertyList.setPropertyValue(p, "transientAddress.tempData",
				"World Hello");
		assertEquals("World Hello", p.getAddress().getTempData());
	}

	@Test
	public void testSetPropertyValue_NestedPersistentProperty() {
		Person p = new Person();
		p.setAddress(new Address());
		propertyList.addNestedProperty("address.street");
		propertyList.setPropertyValue(p, "address.street", "Street");
		assertEquals("Street", p.getAddress().getStreet());
		childPropertyList.setPropertyValue(p, "address.street", "Road");
		assertEquals("Road", p.getAddress().getStreet());
	}

	@Test
	public void testSetPropertyValue_Invalid() {
		Person p = new Person();
		try {
			propertyList.setPropertyValue(p, "nonexistent", "Hello");
			fail("No exception thrown");
		} catch (IllegalArgumentException e) {
			// OK.
		}

		try {
			// Valid property name, but has not been added
			propertyList.setPropertyValue(p, "address.street", "Hello");
			fail("No exception thrown");
		} catch (IllegalArgumentException e) {
			// OK.
		}
	}

	@Test
	public void testSetPropertyValue_NestedProperty_NullValueInChain() {
		Person p = new Person();
		propertyList.addNestedProperty("address.street");
		try {
			propertyList.setPropertyValue(p, "address.street", "Street");
			fail("No exception thrown");
		} catch (IllegalStateException e) {
			assertNull(p.getAddress());
			// OK.
		}
	}

	@Test
	public void testGetPropertyType_SingleProperty() {
		assertSame(Date.class, propertyList.getPropertyType("dateOfBirth"));
		assertSame(Date.class, childPropertyList.getPropertyType("dateOfBirth"));
	}

	@Test
	public void testGetPropertyType_NestedProperty() {
		propertyList.addNestedProperty("address.street");
		assertSame(String.class, propertyList.getPropertyType("address.street"));
		assertSame(String.class,
				childPropertyList.getPropertyType("address.street"));
	}

	@Test
	public void testIsPropertyWritable_SingleProperty() {
		assertTrue(propertyList.isPropertyWritable("dateOfBirth"));
		assertFalse(propertyList.isPropertyWritable("fullName"));
		assertTrue(childPropertyList.isPropertyWritable("dateOfBirth"));
		assertFalse(childPropertyList.isPropertyWritable("fullName"));
	}

	@Test
	public void testIsPropertyWritable_NestedProperty() {
		propertyList.addNestedProperty("address.street");
		propertyList.addNestedProperty("address.fullAddress");
		assertTrue(propertyList.isPropertyWritable("address.street"));
		assertFalse(propertyList.isPropertyWritable("address.fullAddress"));
		assertTrue(childPropertyList.isPropertyWritable("address.street"));
		assertFalse(childPropertyList.isPropertyWritable("address.fullAddress"));
	}
}
