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

import com.vaadin.addons.jpacontainer.testdata.Address;
import com.vaadin.addons.jpacontainer.testdata.Person;
import com.vaadin.data.Property.ReadOnlyException;
import java.util.Collection;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test case for {@link EntityItem}.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class EntityItemTest {

    private EntityItem<Person> item;
    private Person entity;
    private JPAContainer<Person> container;

    @Before
    public void setUp() {
        container = new JPAContainer<Person>(Person.class);
        container.addNestedContainerProperty("address.street");
        container.addNestedContainerProperty("address.fullAddress");
        entity = new Person();
        entity.setAddress(new Address());
        item = new EntityItem<Person>(container, entity);
    }

    @Test
    public void testGetItemPropertyIds() {
        Collection<String> propertyIds = (Collection<String>) item.getItemPropertyIds();
        assertEquals(container.getPropertyList().getAllAvailablePropertyNames(), propertyIds);
    }

    @Test
    public void testGetItemProperty() {
        assertNotNull(item.getItemProperty("firstName"));
        assertNull(item.getItemProperty("nonexistent"));
    }

    @Test
    public void testPropertyType() {
        assertEquals(String.class, item.getItemProperty("firstName").getType());
        assertEquals(Date.class, item.getItemProperty("dateOfBirth").getType());
        assertEquals(String.class, item.getItemProperty("address.street").getType());
        assertEquals(Address.class, item.getItemProperty("address").getType());
    }

    @Test
    public void testPropertyReadOnly() {
        assertFalse(item.getItemProperty("firstName").isReadOnly());
        assertFalse(item.getItemProperty("address.street").isReadOnly());
        assertTrue(item.getItemProperty("fullName").isReadOnly());
        assertTrue(item.getItemProperty("address.fullAddress").isReadOnly());
    }

    @Test
    public void testPropertyValuesAndModified() {
        assertFalse(item.isModified());
        assertNull(item.getItemProperty("firstName").getValue());
        item.getItemProperty("firstName").setValue("Hello");
        assertEquals("Hello", item.getItemProperty("firstName").getValue());
        assertEquals("Hello", entity.getFirstName());
        assertTrue(item.isModified());

        item.setModified(false);

        assertFalse(item.isModified());
        assertNull(item.getItemProperty("address.street").getValue());
        item.getItemProperty("address.street").setValue("World");
        assertEquals("World", item.getItemProperty("address.street").getValue());
        assertEquals("World", entity.getAddress().getStreet());
        assertTrue(item.isModified());
    }

    @Test
    public void testPropertyValues_ReadOnly() {
        entity.setFirstName("Joe");
        entity.setLastName("Cool");
        try {
            item.getItemProperty("fullName").setValue("Blah");
            fail("No exception thrown");
        } catch (ReadOnlyException e) {
            assertEquals("Joe Cool", item.getItemProperty("fullName").getValue());
        }
    }
}
