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
package com.vaadin.addons.jpacontainer.metadata;

import org.junit.Test;
import static com.vaadin.addons.jpacontainer.metadata.TestClasses.*;
import static org.junit.Assert.*;

/**
 * Test case for {@link ClassMetadata}.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class ClassMetadataTest {

    @Test
    public void testAddTransientProperty() throws Exception {
        ClassMetadata<Person_M> metadata = new ClassMetadata<Person_M>(
                Person_M.class);
        PropertyMetadata prop = new PropertyMetadata("transientField2",
                String.class, Person_M.class.getDeclaredMethod(
                "getTransientField2"),
                Person_M.class.getDeclaredMethod("setTransientField2",
                String.class));
        metadata.addProperties(prop);
        assertTrue(metadata.getProperties().contains(prop));
        assertTrue(metadata.getPersistentProperties().isEmpty());
        assertSame(prop, metadata.getProperty("transientField2"));
        assertSame(Person_M.class, metadata.getMappedClass());
    }

    @Test
    public void testAddPersistentProperty() throws Exception {
        ClassMetadata<Person_M> metadata = new ClassMetadata<Person_M>(
                Person_M.class);
        PersistentPropertyMetadata prop = new PersistentPropertyMetadata(
                "firstName", String.class,
                PersistentPropertyMetadata.PropertyKind.SIMPLE, Person_M.class.
                getDeclaredMethod("getFirstName"),
                Person_M.class.getDeclaredMethod("setFirstName", String.class));
        metadata.addProperties(prop);
        assertTrue(metadata.getProperties().contains(prop));
        assertTrue(metadata.getPersistentProperties().contains(prop));
        assertSame(prop, metadata.getProperty("firstName"));
        assertSame(Person_M.class, metadata.getMappedClass());
    }

    @Test
    public void testGetTransientPropertyValue() throws Exception {
        ClassMetadata<Person_M> metadata = new ClassMetadata<Person_M>(
                Person_M.class);
        PropertyMetadata prop = new PropertyMetadata("transientField2",
                String.class, Person_M.class.getDeclaredMethod(
                "getTransientField2"),
                Person_M.class.getDeclaredMethod("setTransientField2",
                String.class));
        metadata.addProperties(prop);

        Person_M person = new Person_M();
        person.setTransientField2("Hello");

        assertEquals("Hello", metadata.getPropertyValue(person,
                "transientField2"));
    }

    @Test
    public void testGetNestedTransientPropertyValue() throws Exception {
        ClassMetadata<Person_F> metadata = new ClassMetadata<Person_F>(
                Person_F.class);
        metadata.addProperties(new PropertyMetadata("transientAddress",
                Address_M.class, Person_F.class.getDeclaredMethod(
                "getTransientAddress"), Person_F.class.getDeclaredMethod(
                "setTransientAddress", Address_M.class)));

        Person_F person = new Person_F();

        assertNull(metadata.getProperty("transientAddress.street"));

        person.setTransientAddress(new Address_M());
        person.transientAddress.setStreet("Street");

        assertEquals("Street", metadata.getPropertyValue(person,
                "transientAddress.street"));
    }

    @Test
    public void testSetTransientPropertyValue() throws Exception {
        ClassMetadata<Person_M> metadata = new ClassMetadata<Person_M>(
                Person_M.class);
        PropertyMetadata prop = new PropertyMetadata("transientField2",
                String.class, Person_M.class.getDeclaredMethod(
                "getTransientField2"),
                Person_M.class.getDeclaredMethod("setTransientField2",
                String.class));
        metadata.addProperties(prop);

        Person_M person = new Person_M();
        assertNull(person.getTransientField2());
        metadata.setPropertyValue(person, "transientField2", "Hello");
        assertEquals("Hello", person.getTransientField2());
    }

    @Test
    public void testSetNestedTransientPropertyValue() throws Exception {
        ClassMetadata<Person_F> metadata = new ClassMetadata<Person_F>(
                Person_F.class);
        metadata.addProperties(new PropertyMetadata("transientAddress",
                Address_M.class, Person_F.class.getDeclaredMethod(
                "getTransientAddress"), Person_F.class.getDeclaredMethod(
                "setTransientAddress", Address_M.class)));

        Person_F person = new Person_F();

        person.setTransientAddress(new Address_M());
        metadata.setPropertyValue(person, "transientAddress.street", "Street");
        assertEquals("Street", person.getTransientAddress().getStreet());
    }

    @Test
    public void testGetPersistentPropertyValue_Field() throws Exception {
        ClassMetadata<Person_F> metadata = new ClassMetadata<Person_F>(
                Person_F.class);
        PersistentPropertyMetadata prop = new PersistentPropertyMetadata(
                "firstName",
                String.class, PersistentPropertyMetadata.PropertyKind.SIMPLE, Person_F.class.
                getDeclaredField("firstName"));
        metadata.addProperties(prop);

        Person_F person = new Person_F();
        person.firstName = "Hello";

        assertEquals("Hello", metadata.getPropertyValue(person,
                "firstName"));
    }

    @Test
    public void testGetNestedPersistentPropertyValue_Field() throws Exception {
        ClassMetadata<Address_F> addressMetadata = new ClassMetadata<Address_F>(
                Address_F.class);
        addressMetadata.addProperties(new PersistentPropertyMetadata("street",
                String.class, PersistentPropertyMetadata.PropertyKind.SIMPLE, Address_F.class.
                getDeclaredField("street")));

        ClassMetadata<Person_F> metadata = new ClassMetadata<Person_F>(
                Person_F.class);
        metadata.addProperties(new PersistentPropertyMetadata("address",
                addressMetadata,
                PersistentPropertyMetadata.PropertyKind.EMBEDDED,
                Person_F.class.getDeclaredField("address")));

        Person_F person = new Person_F();

        assertNull(metadata.getProperty("address.street"));

        person.address = new Address_F();
        person.address.street = "Street";

        assertEquals("Street", metadata.getPropertyValue(person,
                "address.street"));
    }

    @Test
    public void testSetPersistentPropertyValue_Field() throws Exception {
        ClassMetadata<Person_F> metadata = new ClassMetadata<Person_F>(
                Person_F.class);
        PersistentPropertyMetadata prop = new PersistentPropertyMetadata(
                "firstName",
                String.class, PersistentPropertyMetadata.PropertyKind.SIMPLE, Person_F.class.
                getDeclaredField("firstName"));
        metadata.addProperties(prop);

        Person_F person = new Person_F();
        assertNull(person.firstName);
        metadata.setPropertyValue(person, "firstName", "Hello");
        assertEquals("Hello", person.firstName);
    }

    @Test
    public void testSetNestedPersistentPropertyValue_Field() throws Exception {
        ClassMetadata<Address_F> addressMetadata = new ClassMetadata<Address_F>(
                Address_F.class);
        addressMetadata.addProperties(new PersistentPropertyMetadata("street",
                String.class, PersistentPropertyMetadata.PropertyKind.SIMPLE, Address_F.class.
                getDeclaredField("street")));

        ClassMetadata<Person_F> metadata = new ClassMetadata<Person_F>(
                Person_F.class);
        metadata.addProperties(new PersistentPropertyMetadata("address",
                addressMetadata,
                PersistentPropertyMetadata.PropertyKind.EMBEDDED,
                Person_F.class.getDeclaredField("address")));

        Person_F person = new Person_F();
        person.address = new Address_F();
        assertNull(person.address.street);
        metadata.setPropertyValue(person, "address.street", "Hello");
        assertEquals("Hello", person.address.street);
    }

    @Test
    public void testGetPersistentPropertyValue_Method() throws Exception {
        ClassMetadata<Person_M> metadata = new ClassMetadata<Person_M>(
                Person_M.class);
        PersistentPropertyMetadata prop = new PersistentPropertyMetadata(
                "firstName",
                String.class, PersistentPropertyMetadata.PropertyKind.SIMPLE, Person_M.class.
                getDeclaredMethod("getFirstName"),
                Person_M.class.getDeclaredMethod("setFirstName", String.class));
        metadata.addProperties(prop);

        Person_M person = new Person_M();
        person.setFirstName("Hello");

        assertEquals("Hello", metadata.getPropertyValue(person,
                "firstName"));
    }

    @Test
    public void testGetNestedPersistentPropertyValue_Method() throws Exception {
        ClassMetadata<Address_M> addressMetadata = new ClassMetadata<Address_M>(
                Address_M.class);
        addressMetadata.addProperties(new PersistentPropertyMetadata("street",
                String.class, PersistentPropertyMetadata.PropertyKind.SIMPLE, Address_M.class.
                getDeclaredMethod("getStreet"), Address_M.class.
                getDeclaredMethod("setStreet", String.class)));

        ClassMetadata<Person_M> metadata = new ClassMetadata<Person_M>(
                Person_M.class);
        metadata.addProperties(new PersistentPropertyMetadata("address",
                addressMetadata,
                PersistentPropertyMetadata.PropertyKind.EMBEDDED,
                Person_M.class.getDeclaredMethod("getAddress"),
                Person_M.class.getDeclaredMethod("setAddress", Address_M.class)));

        Person_M person = new Person_M();

        assertNull(metadata.getProperty("address.street"));

        person.setAddress(new Address_M());
        person.getAddress().setStreet("Street");

        assertEquals("Street", metadata.getPropertyValue(person,
                "address.street"));
    }

    @Test
    public void testSetPersistentPropertyValue_Method() throws Exception {
        ClassMetadata<Person_M> metadata = new ClassMetadata<Person_M>(
                Person_M.class);
        PersistentPropertyMetadata prop = new PersistentPropertyMetadata(
                "firstName",
                String.class, PersistentPropertyMetadata.PropertyKind.SIMPLE, Person_M.class.
                getDeclaredMethod("getFirstName"),
                Person_M.class.getDeclaredMethod("setFirstName", String.class));
        metadata.addProperties(prop);

        Person_M person = new Person_M();
        assertNull(person.getFirstName());
        metadata.setPropertyValue(person, "firstName", "Hello");
        assertEquals("Hello", person.getFirstName());
    }

    @Test
    public void testSetNestedPersistentPropertyValue_Method() throws Exception {
        ClassMetadata<Address_M> addressMetadata = new ClassMetadata<Address_M>(
                Address_M.class);
        addressMetadata.addProperties(new PersistentPropertyMetadata("street",
                String.class, PersistentPropertyMetadata.PropertyKind.SIMPLE, Address_M.class.
                getDeclaredMethod("getStreet"), Address_M.class.
                getDeclaredMethod("setStreet", String.class)));

        ClassMetadata<Person_M> metadata = new ClassMetadata<Person_M>(
                Person_M.class);
        metadata.addProperties(new PersistentPropertyMetadata("address",
                addressMetadata,
                PersistentPropertyMetadata.PropertyKind.EMBEDDED,
                Person_M.class.getDeclaredMethod("getAddress"),
                Person_M.class.getDeclaredMethod("setAddress", Address_M.class)));

        Person_M person = new Person_M();
        person.setAddress(new Address_M());
        assertNull(person.getAddress().getStreet());
        metadata.setPropertyValue(person, "address.street", "Hello");
        assertEquals("Hello", person.getAddress().getStreet());
    }
}
