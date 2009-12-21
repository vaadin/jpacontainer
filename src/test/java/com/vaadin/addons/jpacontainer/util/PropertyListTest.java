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
package com.vaadin.addons.jpacontainer.util;

import com.vaadin.addons.jpacontainer.metadata.ClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addons.jpacontainer.testdata.Address;
import com.vaadin.addons.jpacontainer.testdata.Person;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test case for {@link PropertyList}.
 *
 * @author Petter Holmström (IT Mill)
 */
public class PropertyListTest {

    private ClassMetadata<Person> metadata = MetadataFactory.getInstance().
            getEntityClassMetadata(Person.class);
    private PropertyList<Person> propertyList;

    @Before
    public void setUp() {
        propertyList = new PropertyList<Person>(metadata);
    }

    @Test
    public void testInitialPropertyList() {
        assertEquals(6, propertyList.getPersistentPropertyNames().size());
        assertTrue(propertyList.getPersistentPropertyNames().contains("id"));
        assertTrue(propertyList.getPersistentPropertyNames().contains("version"));
        assertTrue(propertyList.getPersistentPropertyNames().contains(
                "firstName"));
        assertTrue(
                propertyList.getPersistentPropertyNames().contains("lastName"));
        assertTrue(propertyList.getPersistentPropertyNames().contains(
                "dateOfBirth"));
        assertTrue(propertyList.getPersistentPropertyNames().contains("address"));

        assertEquals(9, propertyList.getPropertyNames().size());
        assertTrue(propertyList.getPropertyNames().containsAll(propertyList.
                getPersistentPropertyNames()));
        assertTrue(propertyList.getPropertyNames().contains("fullName"));
        assertTrue(propertyList.getPropertyNames().contains("transientAddress"));
        assertTrue(propertyList.getPropertyNames().contains("tempData"));
    }

    @Test
    public void testRemoveProperty() {
        assertTrue(propertyList.getPropertyNames().contains("fullName"));
        propertyList.removeProperty("fullName");
        assertFalse(propertyList.getPropertyNames().contains("fullName"));

        assertTrue(propertyList.getPropertyNames().contains("firstName"));
        assertTrue(propertyList.getPersistentPropertyNames().contains(
                "firstName"));
        propertyList.removeProperty("firstName");
        assertFalse(propertyList.getPropertyNames().contains("firstName"));
        assertFalse(propertyList.getPersistentPropertyNames().contains(
                "firstName"));
    }

    @Test
    public void testAddSinglePersistentNestedProperty() {
        propertyList.addNestedProperty("address.street");
        assertTrue(propertyList.getPropertyNames().contains("address.street"));
        assertTrue(propertyList.getPersistentPropertyNames().contains(
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

        // Transient property of a persistent embedded property
        propertyList.addNestedProperty("address.fullAddress");
        assertTrue(propertyList.getPropertyNames().contains(
                "address.fullAddress"));
        assertFalse(propertyList.getPersistentPropertyNames().contains(
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
            assertFalse(propertyList.getPersistentPropertyNames().contains(
                    "address.nonexistent"));
        }
        try {
            propertyList.addNestedProperty("nonexistent.street");
            fail("Did not throw exception even though nested property was nonexistent");
        } catch (IllegalArgumentException e) {
            assertFalse(propertyList.getPropertyNames().contains(
                    "nonexistent.street"));
            assertFalse(propertyList.getPersistentPropertyNames().contains(
                    "nonexistent.street"));
        }
        try {
            propertyList.addNestedProperty("firstName.nonexistent");
            fail("Did not throw exception even though nested property was nonexistent");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    @Test
    public void testAddNestedPersistentPropertyWithWildcards() {
        propertyList.addNestedProperty("address.*");

        // Persistent properties
        assertTrue(propertyList.getPropertyNames().contains("address.street"));
        assertTrue(propertyList.getPersistentPropertyNames().contains(
                "address.street"));
        assertTrue(
                propertyList.getPropertyNames().contains("address.postOffice"));
        assertTrue(propertyList.getPersistentPropertyNames().contains(
                "address.postOffice"));
        assertTrue(
                propertyList.getPropertyNames().contains("address.postalCode"));
        assertTrue(propertyList.getPersistentPropertyNames().contains(
                "address.postalCode"));

        // Transient properties
        assertTrue(propertyList.getPropertyNames().contains(
                "address.fullAddress"));
        assertFalse(propertyList.getPersistentPropertyNames().contains(
                "address.fullAddress"));
    }

    @Test
    public void testAddNestedTransientPropertyWithWildcards() {
        propertyList.addNestedProperty("transientAddress.*");

        assertTrue(propertyList.getPropertyNames().contains(
                "transientAddress.street"));
        assertFalse(propertyList.getPersistentPropertyNames().contains(
                "transientAddress.street"));
        assertTrue(
                propertyList.getPropertyNames().contains(
                "transientAddress.postOffice"));
        assertFalse(propertyList.getPersistentPropertyNames().contains(
                "transientAddress.postOffice"));
        assertTrue(
                propertyList.getPropertyNames().contains(
                "transientAddress.postalCode"));
        assertFalse(propertyList.getPersistentPropertyNames().contains(
                "transientAddress.postalCode"));
        assertTrue(propertyList.getPropertyNames().contains(
                "transientAddress.fullAddress"));
        assertFalse(propertyList.getPersistentPropertyNames().contains(
                "transientAddress.fullAddress"));
    }

    @Test
    public void testAddNestedPropertyWithWildcards_Illegal() {
        int oldSize = propertyList.getPropertyNames().size();
        int oldPersistentSize = propertyList.getPersistentPropertyNames().size();

        try {
            propertyList.addNestedProperty("nonexistent.*");
            fail("Did not throw exception even though nested property was nonexistent");
        } catch (IllegalArgumentException e) {
            assertEquals(oldSize, propertyList.getPropertyNames().size());
            assertEquals(oldPersistentSize, propertyList.
                    getPersistentPropertyNames().size());
        }

        try {
            propertyList.addNestedProperty("address.*.nothing");
            fail("Did not throw exception even though nested property was nonexistent");
        } catch (IllegalArgumentException e) {
            assertEquals(oldSize, propertyList.getPropertyNames().size());
            assertEquals(oldPersistentSize, propertyList.
                    getPersistentPropertyNames().size());
        }
    }

    @Test
    public void testGetPropertyValue_TransientProperty() {
        Person p = new Person();
        p.setFirstName("Joe");
        p.setLastName("Cool");
        assertEquals("Joe Cool", propertyList.getPropertyValue(p, "fullName"));
    }

    @Test
    public void testGetPropertyValue_PersistentProperty() {
        Person p = new Person();
        p.setFirstName("Joe");
        assertEquals("Joe", propertyList.getPropertyValue(p, "firstName"));
    }

    @Test
    public void testGetPropertyValue_NestedTransientProperty() {
        Person p = new Person();
        propertyList.addNestedProperty("transientAddress.street");
        propertyList.addNestedProperty("address.fullAddress");
        assertNull(propertyList.getPropertyValue(p, "transientAddress.street"));
        assertNull(propertyList.getPropertyValue(p, "address.fullAddress"));

        // transientAddress and address return the same value
        p.setAddress(new Address());
        p.getAddress().setStreet("Street");
        p.getAddress().setPostalCode("Code");
        p.getAddress().setPostOffice("Office");

        assertEquals("Street", propertyList.getPropertyValue(p, "transientAddress.street"));
        assertEquals("Street Code Office", propertyList.getPropertyValue(p,
                "address.fullAddress"));
    }

    @Test
    public void testGetPropertyValue_NestedPersistentProperty() {
        Person p = new Person();
        propertyList.addNestedProperty("address.street");
        assertNull(propertyList.getPropertyValue(p, "address.street"));

        p.setAddress(new Address());
        p.getAddress().setStreet("Hello World");
        assertEquals("Hello World", propertyList.getPropertyValue(p, "address.street"));
    }

    @Test
    public void testGetPropertyValue_Invalid() {
        Person p = new Person();
        // TODO Write test!
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
    }

    @Test
    public void testSetPropertyValue_PersistentProperty() {
        Person p = new Person();
        propertyList.setPropertyValue(p, "firstName", "Joe");
        assertEquals("Joe", p.getFirstName());
    }

    @Test
    public void testSetPropertyValue_NestedTransientProperty() {
        Person p = new Person();
        p.setAddress(new Address());
        propertyList.addNestedProperty("transientAddress.tempData");
        propertyList.setPropertyValue(p, "transientAddress.tempData", "Hello World");
        assertEquals("Hello World", p.getAddress().getTempData());
    }

    @Test
    public void testSetPropertyValue_NestedPersistentProperty() {
        Person p = new Person();
        p.setAddress(new Address());
        propertyList.addNestedProperty("address.street");
        propertyList.setPropertyValue(p, "address.street", "Street");
        assertEquals("Street", p.getAddress().getStreet());
    }

    @Test
    public void testSetPropertyValue_Invalid() {
        // TODO Write test
        Person p = new Person();
        // TODO Write test!
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
    
}
