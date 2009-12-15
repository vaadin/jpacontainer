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
}
