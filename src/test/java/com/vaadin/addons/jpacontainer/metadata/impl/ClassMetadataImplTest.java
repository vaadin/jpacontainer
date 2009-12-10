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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.metadata.impl;

import com.vaadin.addons.jpacontainer.metadata.NestedPropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import java.util.Collection;
import org.junit.Test;
import static com.vaadin.addons.jpacontainer.metadata.impl.TestClasses.*;
import static org.junit.Assert.*;

/**
 * Test case for {@link ClassMetadataImpl}.
 *
 * @author Petter Holmström (IT Mill)
 */
public class ClassMetadataImplTest {

    @Test
    public void testAddProperty() throws Exception {
        ClassMetadataImpl metadata = new ClassMetadataImpl(
                Person_F.class);

        PropertyMetadata pm = metadata.addProperty("firstName", String.class, Person_F.class.
                getDeclaredField("firstName"), null, null);
        assertFalse(pm.isCollection());
        assertFalse(pm.isEmbedded());
        assertFalse(pm.isReference());
        assertEquals("firstName", pm.getName());
        assertSame(String.class, pm.getType());
        assertNull(pm.getTypeMetadata());
        assertEquals(PropertyMetadata.AccessType.FIELD, pm.getAccessType());
        assertSame(metadata, pm.getOwner());
    }

    @Test
    public void testAddCollectionProperty() throws Exception {
        ClassMetadataImpl metadata = new ClassMetadataImpl(
                Person_F.class);

        PropertyMetadata pm = metadata.addCollectionProperty("children",
                Collection.class, Person_F.class.getDeclaredField("children"),
                null, null);
        assertTrue(pm.isCollection());
        assertFalse(pm.isEmbedded());
        assertFalse(pm.isReference());
        assertEquals("children", pm.getName());
        assertSame(Collection.class, pm.getType());
        assertNull(pm.getTypeMetadata());
        assertEquals(PropertyMetadata.AccessType.FIELD, pm.getAccessType());
        assertSame(metadata, pm.getOwner());
    }

    @Test
    public void testAddReferenceProperty() throws Exception {
        ClassMetadataImpl metadata = new ClassMetadataImpl(
                Person_F.class);

        PropertyMetadata pm = metadata.addReferenceProperty("parent", metadata, Person_F.class.
                getDeclaredField("parent"),
                null, null);
        assertFalse(pm.isCollection());
        assertFalse(pm.isEmbedded());
        assertTrue(pm.isReference());
        assertEquals("parent", pm.getName());
        assertSame(Person_F.class, pm.getType());
        assertSame(metadata, pm.getTypeMetadata());
        assertEquals(PropertyMetadata.AccessType.FIELD, pm.getAccessType());
        assertSame(metadata, pm.getOwner());
    }

    @Test
    public void testAddEmbeddedProperty() throws Exception {
        ClassMetadataImpl metadata = new ClassMetadataImpl(
                Person_F.class);
        ClassMetadataImpl addressMetadata = new ClassMetadataImpl(
                Address_F.class);
        addressMetadata.addProperty("street", String.class, Address_F.class.
                getDeclaredField("street"), null, null);

        PropertyMetadata pm = metadata.addEmbeddedProperty("address",
                addressMetadata, Person_F.class.getDeclaredField("address"),
                null, null);
        assertFalse(pm.isCollection());
        assertTrue(pm.isEmbedded());
        assertFalse(pm.isReference());
        assertEquals("address", pm.getName());
        assertSame(Address_F.class, pm.getType());
        assertSame(addressMetadata, pm.getTypeMetadata());
        assertEquals(PropertyMetadata.AccessType.FIELD, pm.getAccessType());
        assertSame(metadata, pm.getOwner());

        NestedPropertyMetadata nested = (NestedPropertyMetadata) metadata.
                getMappedProperty("address.street");
        assertNotNull(nested);
        assertEquals("address.street", nested.getName());
        assertSame(pm, nested.getParentProperty());
        assertSame(metadata, nested.getOwner());
    }

    @Test
    public void testGetPropertyValue_Field() throws Exception {
        ClassMetadataImpl metadata = new ClassMetadataImpl(
                Person_F.class);
        metadata.addProperty("firstName", String.class, Person_F.class.
                getDeclaredField("firstName"), null, null);

        Person_F person = new Person_F();
        person.firstName = "Hello World";
        assertEquals("Hello World", metadata.getPropertyValue(person,
                "firstName"));
    }

    @Test
    public void testSetPropertyValue_Field() throws Exception {
        ClassMetadataImpl metadata = new ClassMetadataImpl(
                Person_F.class);
        metadata.addProperty("firstName", String.class, Person_F.class.
                getDeclaredField("firstName"), null, null);

        Person_F person = new Person_F();
        metadata.setPropertyValue(person, "firstName", "Hello World");
        assertEquals("Hello World", person.firstName);
    }

    @Test
    public void testGetPropertyValue_Method() throws Exception {
        ClassMetadataImpl metadata = new ClassMetadataImpl(
                Person_M.class);
        metadata.addProperty("firstName", String.class, null, Person_M.class.
                getDeclaredMethod("getFirstName"), Person_M.class.
                getDeclaredMethod("setFirstName", String.class));

        Person_M person = new Person_M();
        person.setFirstName("Hello World");
        assertEquals("Hello World", metadata.getPropertyValue(person,
                "firstName"));
    }

    @Test
    public void testSetPropertyValue_Method() throws Exception {
        ClassMetadataImpl metadata = new ClassMetadataImpl(
                Person_M.class);
        metadata.addProperty("firstName", String.class, null, Person_M.class.
                getDeclaredMethod("getFirstName"), Person_M.class.
                getDeclaredMethod("setFirstName", String.class));

        Person_M person = new Person_M();
        metadata.setPropertyValue(person, "firstName", "Hello World");
        assertEquals("Hello World", person.getFirstName());
    }

    @Test
    public void testGetNestedPropertyValue() throws Exception {
        ClassMetadataImpl metadata = new ClassMetadataImpl(
                Person_F.class);
        ClassMetadataImpl addressMetadata = new ClassMetadataImpl(
                Address_F.class);
        addressMetadata.addProperty("street", String.class, Address_F.class.
                getDeclaredField("street"), null, null);
        metadata.addEmbeddedProperty("address",
                addressMetadata, Person_F.class.getDeclaredField("address"),
                null, null);

        Person_F person = new Person_F();
        person.address = new Address_F();
        person.address.street = "Hello World";
        assertEquals("Hello World", metadata.getPropertyValue(person,
                "address.street"));
    }

    @Test
    public void testSetNestedPropertyValue() throws Exception {
        ClassMetadataImpl metadata = new ClassMetadataImpl(
                Person_F.class);
        ClassMetadataImpl addressMetadata = new ClassMetadataImpl(
                Address_F.class);
        addressMetadata.addProperty("street", String.class, Address_F.class.
                getDeclaredField("street"), null, null);
        metadata.addEmbeddedProperty("address",
                addressMetadata, Person_F.class.getDeclaredField("address"),
                null, null);

        Person_F person = new Person_F();
        person.address = new Address_F();
        metadata.setPropertyValue(person, "address.street", "Hello World");
        assertEquals("Hello World", person.address.street);
    }
}
