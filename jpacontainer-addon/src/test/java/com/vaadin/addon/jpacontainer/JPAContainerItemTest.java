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

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.testdata.Address;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;

/**
 * Test case for {@link JPAContainerItem}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@SuppressWarnings("serial")
public class JPAContainerItemTest {

    private JPAContainerItem<Person> item;
    private Person entity;
    private JPAContainer<Person> container;
    private JPAContainerItem<Person> modifiedItem;
    private String modifiedPropertyId;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        container = new JPAContainer<Person>(Person.class) {

            /*
             * The following two methods are used to verify that the item
             * notifies the container when items or item properties are changed.
             * 
             * The assertions in the beginning of each method make sure that the
             * test fails if any of the methods are called twice in a row
             * without the modifiedItem and modifiedPropertyId values being
             * explicitly set to null in between.
             */
            @Override
            protected void containerItemPropertyModified(
                    JPAContainerItem<Person> item, String propertyId) {
                assertNull("modifiedItem was not null", modifiedItem);
                assertNull("modifiedPropertyId was not null",
                        modifiedPropertyId);
                modifiedItem = item;
                modifiedPropertyId = propertyId;
            }

            @Override
            protected void containerItemModified(JPAContainerItem<Person> item) {
                assertNull("modifiedItem was not null", modifiedItem);
                assertNull("modifiedPropertyId was not null",
                        modifiedPropertyId);
                modifiedItem = item;
            }
        };
        EntityProvider<Person> entityProviderMock = createNiceMock(EntityProvider.class);
        expect(entityProviderMock.getLazyLoadingDelegate()).andStubReturn(null);
        replay(entityProviderMock);

        container.setEntityProvider(entityProviderMock);

        modifiedItem = null;
        modifiedPropertyId = null;
        container.addNestedContainerProperty("address.street");
        container.addNestedContainerProperty("address.fullAddress");
        entity = new Person();
        entity.setId(123l);
        entity.setAddress(new Address());

        Person person = new Person();
        person.setId(124l);
        entity.setManager(person);

        item = new JPAContainerItem<Person>(container, entity);
    }

    @Test
    public void testPrimitiveTypesWithFields() {

        CheckBox checkBox = new CheckBox();
        TextField textField = new TextField();
        textField.setConverter(new StringToDoubleConverter());

        EntityItemProperty maleProperty = item.getItemProperty("male");
        EntityItemProperty doubleProperty = item
                .getItemProperty("primitiveDouble");

        checkBox.setPropertyDataSource(maleProperty);
        textField.setPropertyDataSource(doubleProperty);

        Boolean originalMaleValue = (Boolean) checkBox.getValue();
        Double originalDoubleValue = Double.parseDouble((String) textField
                .getValue());

        assertEquals(false, originalMaleValue);
        assertEquals(0.0, originalDoubleValue.doubleValue(), 0.000001);

        checkBox.setValue(true);

        modifiedItem = null;
        modifiedPropertyId = null;

        textField.setValue("3.55");

        modifiedItem = null;
        modifiedPropertyId = null;

        Boolean newMaleValue = (Boolean) checkBox.getValue();
        Double newDoubleValue = Double.parseDouble((String) textField
                .getValue());

        assertEquals(true, newMaleValue);
        assertEquals((Double) 3.55, newDoubleValue);

        Person p = item.getEntity();
        assertEquals(true, p.isMale());
        assertEquals(3.55, p.getPrimitiveDouble(), 0.0000001);
    }

    @Test
    public void testGetItemPropertyIds() {
        Collection<String> propertyIds = item.getItemPropertyIds();
        assertEquals(
                container.getPropertyList().getAllAvailablePropertyNames(),
                propertyIds);
    }

    @Test
    public void testGetItemProperty() {
        assertNotNull(item.getItemProperty("firstName"));
        assertNull(item.getItemProperty("nonexistent"));
    }

    @Test
    public void testIsPersistent() {
        assertTrue(item.isPersistent());
        item.setPersistent(false);
        assertFalse(item.isPersistent());

        // Try the other constructor
        item = new JPAContainerItem<Person>(container, entity, null, true);
        assertFalse(item.isPersistent());
    }

    @Test
    public void testIsDirty_NonPersistent() {
        item.setPersistent(false);
        item.setDirty(true);
        assertFalse(item.isDirty());
    }

    @Test
    public void testGetItemId() {
        assertEquals(123l, item.getItemId());
        // Try the other constructor
        item = new JPAContainerItem<Person>(container, entity, null, true);
        assertNull(item.getItemId());
    }

    @Test
    public void testGetEntity() {
        assertSame(entity, item.getEntity());
    }

    @Test
    public void testGetContainer() {
        assertSame(container, item.getContainer());
    }

    @Test
    public void testPropertyType() {
        assertEquals(String.class, item.getItemProperty("firstName").getType());
        assertEquals(Date.class, item.getItemProperty("dateOfBirth").getType());
        assertEquals(String.class, item.getItemProperty("address.street")
                .getType());
        assertEquals(Address.class, item.getItemProperty("address").getType());

        assertEquals(Person.class, item.getItemProperty("manager").getType());

        // should report wrapper types for beans primitive types
        assertEquals(Boolean.class, item.getItemProperty("male").getType());
        assertEquals(Double.class, item.getItemProperty("primitiveDouble")
                .getType());
    }

    @Test
    public void testPropertyReadOnly() {
        assertFalse(item.getItemProperty("firstName").isReadOnly());
        assertFalse(item.getItemProperty("address.street").isReadOnly());
        assertTrue(item.getItemProperty("fullName").isReadOnly());
        assertTrue(item.getItemProperty("address.fullAddress").isReadOnly());
    }

    @Test
    public void testAddNestedProperty() {
        item.addNestedContainerProperty("address.postalCode");
        assertNotNull(item.getItemProperty("address.postalCode"));
    }

    @Test
    public void testRemoveProperty() {
        item.addNestedContainerProperty("address.postalCode");

        assertFalse(item.removeItemProperty("firstName"));
        assertTrue(item.removeItemProperty("address.postalCode"));

        assertNotNull(item.getItemProperty("firstName"));
        assertNull(item.getItemProperty("address.postalCode"));
    }

    @Test
    public void testPropertyValue_Unbuffered() {
        final Property prop = item.getItemProperty("firstName");
        final boolean[] listenerCalled = new boolean[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0] = true;
                    }
                });

        assertTrue(item.isReadThrough());
        assertTrue(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertFalse(listenerCalled[0]);

        prop.setValue("Hello");

        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertEquals("Hello", item.getEntity().getFirstName());
        assertFalse(item.isModified());
        assertTrue(item.isDirty());
        assertTrue(listenerCalled[0]);
        assertEquals("firstName", modifiedPropertyId);
        assertSame(item, modifiedItem);
    }

    @Test
    public void testPropertyValueFromString_Unbuffered() {
        final Property prop = item.getItemProperty("id");
        assertEquals(123l, prop.getValue());
        prop.setValue("1234");
        assertEquals(1234l, prop.getValue());
    }

    @Test
    public void testNestedPropertyValue_Unbuffered() {
        final Property prop = item.getItemProperty("address.street");
        final boolean[] listenerCalled = new boolean[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0] = true;
                    }
                });

        assertTrue(item.isReadThrough());
        assertTrue(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertFalse(listenerCalled[0]);

        prop.setValue("World");

        assertEquals("World", prop.getValue());
        assertEquals("World", prop.toString());
        assertEquals("World", item.getEntity().getAddress().getStreet());
        assertFalse(item.isModified());
        assertTrue(item.isDirty());
        assertTrue(listenerCalled[0]);
        assertEquals("address.street", modifiedPropertyId);
        assertSame(item, modifiedItem);
    }

    @Test
    public void testLocalNestedPropertyValue_Unbuffered() {
        item.addNestedContainerProperty("address.postalCode");
        final Property prop = item.getItemProperty("address.postalCode");
        final boolean[] listenerCalled = new boolean[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0] = true;
                    }
                });

        assertTrue(item.isReadThrough());
        assertTrue(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertFalse(listenerCalled[0]);

        prop.setValue("World");

        assertEquals("World", prop.getValue());
        assertEquals("World", prop.toString());
        assertEquals("World", item.getEntity().getAddress().getPostalCode());
        assertFalse(item.isModified());
        assertTrue(item.isDirty());
        assertTrue(listenerCalled[0]);
        assertEquals("address.postalCode", modifiedPropertyId);
        assertSame(item, modifiedItem);
    }

    @Test(expected = ReadOnlyException.class)
    public void testPropertyValue_Unbuffered_ReadOnly_throwsException() {
        entity.setFirstName("Joe");
        entity.setLastName("Cool");
        item.getItemProperty("fullName").setValue("Blah");
    }

    @Test(expected = ReadOnlyException.class)
    public void testPropertyValue_Unbuffered_ReadOnly_valueChangeNotFired() {
        entity.setFirstName("Joe");
        entity.setLastName("Cool");
        final Property prop = item.getItemProperty("fullName");
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        fail("No listener should be called");
                    }
                });

        prop.setValue("Blah");
    }

    @Test
    public void testPropertyValue_Unbuffered_ReadOnly_nothingChanges() {
        entity.setFirstName("Joe");
        entity.setLastName("Cool");
        final Property prop = item.getItemProperty("fullName");

        try {
            prop.setValue("Blah");
        } catch (ReadOnlyException e) {
            assertEquals("Joe Cool", prop.getValue());
            assertFalse(item.isDirty());
            assertFalse(item.isModified());
        }

        assertNull(modifiedPropertyId);
        assertNull(modifiedItem);
    }

    @Test
    public void testNestedPropertyValue_Unbuffered_ReadOnly() {
        entity.getAddress().setStreet("Street");
        entity.getAddress().setPostalCode("1234");
        entity.getAddress().setPostOffice("Office");
        final Property prop = item.getItemProperty("address.fullAddress");
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        fail("No listener should be called");
                    }
                });
        try {
            prop.setValue("Blah");
            fail("No exception thrown");
        } catch (ReadOnlyException e) {
            assertEquals("Street 1234 Office", prop.toString());
            assertFalse(item.isDirty());
            assertFalse(item.isModified());
        }
        assertNull(modifiedPropertyId);
        assertNull(modifiedItem);
    }

    @Test
    public void testPropertyValue_Buffered_NoReadThrough_Commit() {
        item.setWriteThrough(false);

        final Property prop = item.getItemProperty("firstName");
        final int[] listenerCalled = new int[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0]++;
                    }
                });

        assertTrue(item.isReadThrough());
        assertFalse(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertEquals(0, listenerCalled[0]);

        prop.setValue("Hello");

        // Write through is false, so we should always get the cached value
        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getFirstName());
        assertTrue(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(1, listenerCalled[0]);

        item.commit();

        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertEquals("Hello", item.getEntity().getFirstName());
        assertFalse(item.isModified());
        assertTrue(item.isDirty());
        assertEquals(1, listenerCalled[0]);
        assertNull(modifiedPropertyId);
        assertSame(item, modifiedItem);
    }

    @Test
    public void testNestedPropertyValue_Buffered_NoReadThrough_Commit() {
        item.setWriteThrough(false);

        final Property prop = item.getItemProperty("address.street");
        final int[] listenerCalled = new int[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0]++;
                    }
                });

        assertTrue(item.isReadThrough());
        assertFalse(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertEquals(0, listenerCalled[0]);

        prop.setValue("Hello");

        // Read through is false, so we should get the cached value
        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getAddress().getStreet());
        assertTrue(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(1, listenerCalled[0]);

        item.commit();

        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertEquals("Hello", item.getEntity().getAddress().getStreet());
        assertFalse(item.isModified());
        assertTrue(item.isDirty());
        assertEquals(1, listenerCalled[0]);
        assertNull(modifiedPropertyId);
        assertSame(item, modifiedItem);
    }

    @Test
    public void testLocalNestedPropertyValue_Buffered_NoReadThrough_Commit() {
        item.setWriteThrough(false);

        item.addNestedContainerProperty("address.postalCode");
        final Property prop = item.getItemProperty("address.postalCode");
        final int[] listenerCalled = new int[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0]++;
                    }
                });

        assertTrue(item.isReadThrough());
        assertFalse(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertEquals(0, listenerCalled[0]);

        prop.setValue("Hello");

        // Read through is false, so we should get the cached value
        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getAddress().getPostalCode());
        assertTrue(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(1, listenerCalled[0]);

        item.commit();

        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertEquals("Hello", item.getEntity().getAddress().getPostalCode());
        assertFalse(item.isModified());
        assertTrue(item.isDirty());
        assertEquals(1, listenerCalled[0]);
        assertNull(modifiedPropertyId);
        assertSame(item, modifiedItem);
    }

    @Test
    public void testPropertyValue_Buffered_NoReadThrough_Discard() {
        item.setWriteThrough(false);

        final Property prop = item.getItemProperty("firstName");
        final int[] listenerCalled = new int[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0]++;
                    }
                });

        assertTrue(item.isReadThrough());
        assertFalse(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertEquals(0, listenerCalled[0]);

        prop.setValue("Hello");

        // Read through is false, so we should get the cached value
        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getFirstName());
        assertTrue(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(1, listenerCalled[0]);

        item.discard();

        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertNull(item.getEntity().getFirstName());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(2, listenerCalled[0]);
        assertNull(modifiedPropertyId);
        assertNull(modifiedItem);
    }

    @Test
    public void testNestedPropertyValue_Buffered_NoReadThrough_Discard() {
        item.setWriteThrough(false);

        final Property prop = item.getItemProperty("address.street");
        final int[] listenerCalled = new int[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0]++;
                    }
                });

        assertTrue(item.isReadThrough());
        assertFalse(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertEquals(0, listenerCalled[0]);

        prop.setValue("Hello");

        // Read through is false, so we should get the cached value
        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getAddress().getStreet());
        assertTrue(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(1, listenerCalled[0]);

        item.discard();

        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertNull(item.getEntity().getAddress().getStreet());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(2, listenerCalled[0]);
        assertNull(modifiedPropertyId);
        assertNull(modifiedItem);
    }

    @Test
    public void testLocalNestedPropertyValue_Buffered_NoReadThrough_Discard() {
        item.addNestedContainerProperty("address.postalCode");
        item.setWriteThrough(false);

        final Property prop = item.getItemProperty("address.postalCode");
        final int[] listenerCalled = new int[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0]++;
                    }
                });

        assertTrue(item.isReadThrough());
        assertFalse(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertEquals(0, listenerCalled[0]);

        prop.setValue("Hello");

        // Read through is false, so we should get the cached value
        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getAddress().getPostalCode());
        assertTrue(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(1, listenerCalled[0]);

        item.discard();

        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertNull(item.getEntity().getAddress().getPostalCode());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(2, listenerCalled[0]);
        assertNull(modifiedPropertyId);
        assertNull(modifiedItem);
    }

    @Test
    public void testPropertyValue_Buffered_ReadThrough_Commit() {
        item.setWriteThrough(false);
        item.setReadThrough(true);

        final Property prop = item.getItemProperty("firstName");
        final int[] listenerCalled = new int[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0]++;
                    }
                });

        assertTrue(item.isReadThrough());
        assertFalse(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertEquals(0, listenerCalled[0]);

        prop.setValue("Hello");

        // Write through is false, so we should get the buffered value
        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getFirstName());
        assertTrue(item.isModified());
        assertFalse(item.isDirty());
        // ValueChanges should always be fired if the value is changed whether
        // buffered or not.
        assertEquals(1, listenerCalled[0]);

        // Now, we temporarily turn off read through
        item.setReadThrough(false);

        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getFirstName());
        assertEquals(1, listenerCalled[0]); // Should not have been triggered
                                            // again.

        // Now, we turn on read through again
        item.setReadThrough(true);

        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getFirstName());
        assertEquals(1, listenerCalled[0]); // Should not have been triggered
                                            // again

        item.commit();

        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertEquals("Hello", item.getEntity().getFirstName());
        assertFalse(item.isModified());
        assertTrue(item.isDirty());
        assertEquals(1, listenerCalled[0]); // Should not have been triggered
                                            // again
        assertNull(modifiedPropertyId);
        assertSame(item, modifiedItem);
    }

    @Test
    public void testPropertyValue_Buffered_ReadThrough_Discard() {
        item.setWriteThrough(false);
        item.setReadThrough(true);

        final Property prop = item.getItemProperty("firstName");
        final int[] listenerCalled = new int[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0]++;
                    }
                });

        assertTrue(item.isReadThrough());
        assertFalse(item.isWriteThrough());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertEquals(0, listenerCalled[0]);

        prop.setValue("Hello");

        // Read through is true, so we should still get the real value
        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getFirstName());
        assertTrue(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(1, listenerCalled[0]);

        // Now, we temporarily turn off read through
        item.setReadThrough(false);

        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getFirstName());
        assertEquals(1, listenerCalled[0]); // Should not have been called again

        // Now, we turn on read through again
        item.setReadThrough(true);

        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", prop.toString());
        assertNull(item.getEntity().getFirstName());
        assertEquals(1, listenerCalled[0]); // Should not have been called again

        item.discard();

        assertNull(prop.getValue());
        assertNull(prop.toString());
        assertNull(item.getEntity().getFirstName());
        assertFalse(item.isModified());
        assertFalse(item.isDirty());
        assertEquals(2, listenerCalled[0]); // The value changes back when
                                            // changes are discarded

        assertNull(modifiedPropertyId);
        assertNull(modifiedItem);
    }

    @Test
    public void testTurnOnWriteThrough() {
        item.setWriteThrough(false);

        final Property prop = item.getItemProperty("address.street");
        final int[] listenerCalled = new int[1];
        ((Property.ValueChangeNotifier) prop)
                .addListener(new Property.ValueChangeListener() {

                    public void valueChange(ValueChangeEvent event) {
                        assertSame(prop, event.getProperty());
                        listenerCalled[0]++;
                    }
                });

        prop.setValue("Hello");
        assertEquals(1, listenerCalled[0]);

        item.setWriteThrough(true);

        assertEquals("Hello", prop.getValue());
        assertEquals("Hello", item.getEntity().getAddress().getStreet());
        assertFalse(item.isModified());
        assertTrue(item.isDirty());
        assertEquals(1, listenerCalled[0]); // The value has not changed, only
                                            // written to the entity
        assertTrue(item.isWriteThrough());
        assertTrue(item.isReadThrough()); // Is the default
        assertNull(modifiedPropertyId);
        assertSame(item, modifiedItem);
    }

    @Test
    public void testTurnOffReadThroughWithWriteThroughActive() {
        try {
            item.setReadThrough(false);
        } catch (IllegalStateException e) {
            assertTrue(item.isReadThrough());
        }
    }

    @Test
    public void testWriteTroughOffReadThroughOn() {
        item.setReadThrough(true);
        item.setWriteThrough(false);

        // sets the buffered value
        item.getItemProperty("firstName").setValue("foo");

        // gets the buffered value
        assertEquals("foo", item.getItemProperty("firstName").getValue());

        // make sure the value has not been written through
        assertFalse("foo".equals(item.getEntity().getFirstName()));

        // after commit the entity should be updated.
        item.commit();
        assertEquals("foo", item.getEntity().getFirstName());
    }

    // TODO Test registering property listeners through item
}
