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

import com.vaadin.addons.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addons.jpacontainer.metadata.NestedPropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import java.util.Collection;
import javax.persistence.Id;
import javax.persistence.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static com.vaadin.addons.jpacontainer.metadata.impl.TestClasses.*;

/**
 * Test case for {@link ClassMetadataImpl}.
 * 
 * @author Petter Holmström (IT Mill)
 */
public class ClassMetadataImplFactoryTest {

    private MetadataFactory factory;

    @Before
    public void setUp() {
        factory = new ClassMetadataImplFactory();
    }

    @Test
    public void testGetMetadataFromFields_EntityClass() {
        EntityClassMetadata metadata = (EntityClassMetadata) factory.
                getEntityClassMetadata(
                Person_F.class);

        // Basic information
        Assert.assertEquals("Person_F", metadata.getEntityName());
        Assert.assertEquals(Person_F.class, metadata.getMappedClass());
        Assert.assertTrue(metadata.hasIdentifierProperty());
        {
            PropertyMetadata id = metadata.getIdentifierProperty();
            Assert.assertEquals("id", id.getName());
            Assert.assertEquals(Integer.class, id.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, id.
                    getAccessType());
            Assert.assertNotNull(id.getAnnotation(Id.class));
            Assert.assertNull(id.getTypeMetadata());
        }

        Assert.assertTrue(metadata.hasVersionProperty());
        {
            PropertyMetadata version = metadata.getVersionProperty();
            Assert.assertEquals("version", version.getName());
            Assert.assertEquals(Integer.class, version.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, version.
                    getAccessType());
            Assert.assertNotNull(version.getAnnotation(Version.class));
            Assert.assertNull(version.getTypeMetadata());
        }

        Assert.assertFalse(metadata.hasEmbeddedIdentifier());
        Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().isEmpty());

        // Properties
        Assert.assertSame(metadata.getIdentifierProperty(), metadata.
                getMappedProperty("id"));
        Assert.assertSame(metadata.getVersionProperty(), metadata.
                getMappedProperty("version"));

        {
            PropertyMetadata prop = metadata.getMappedProperty("firstName");
            Assert.assertEquals("firstName", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("lastName");
            Assert.assertEquals("lastName", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("address");
            Assert.assertEquals("address", prop.getName());
            Assert.assertEquals(Address_F.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertTrue(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNotNull(prop.getTypeMetadata());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("address.street");
            Assert.assertEquals("address.street", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
            Assert.assertTrue(prop instanceof NestedPropertyMetadata);
            Assert.assertSame(metadata.getMappedProperty("address"), ((NestedPropertyMetadata) prop).
                    getParentProperty());
            Assert.assertSame(metadata.getMappedProperty("address").
                    getTypeMetadata(), ((NestedPropertyMetadata) prop).
                    getActualProperty().getOwner());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty(
                    "address.postalCode");
            Assert.assertEquals("address.postalCode", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
            Assert.assertTrue(prop instanceof NestedPropertyMetadata);
            Assert.assertSame(metadata.getMappedProperty("address"), ((NestedPropertyMetadata) prop).
                    getParentProperty());
            Assert.assertSame(metadata.getMappedProperty("address").
                    getTypeMetadata(), ((NestedPropertyMetadata) prop).
                    getActualProperty().getOwner());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("children");
            Assert.assertEquals("children", prop.getName());
            Assert.assertEquals(Collection.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertTrue(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("parent");
            Assert.assertEquals("parent", prop.getName());
            Assert.assertEquals(Person_F.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertTrue(prop.isReference());
            Assert.assertSame(metadata, prop.getTypeMetadata());
        }

        Assert.assertNull(metadata.getMappedProperty("transientField"));
        Assert.assertNull(metadata.getMappedProperty("transientField2"));
    }

    @Test
    public void testGetMetadataFromMethods_EntityClass() {
        EntityClassMetadata metadata = factory.getEntityClassMetadata(
                Person_M.class);

        // Basic information
        Assert.assertEquals("Person_M", metadata.getEntityName());
        Assert.assertEquals(Person_M.class, metadata.getMappedClass());
        Assert.assertTrue(metadata.hasIdentifierProperty());
        {
            PropertyMetadata id = metadata.getIdentifierProperty();
            Assert.assertEquals("id", id.getName());
            Assert.assertEquals(Integer.class, id.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, id.
                    getAccessType());
            Assert.assertNotNull(id.getAnnotation(Id.class));
            Assert.assertNull(id.getTypeMetadata());
        }

        Assert.assertTrue(metadata.hasVersionProperty());
        {
            PropertyMetadata version = metadata.getVersionProperty();
            Assert.assertEquals("version", version.getName());
            Assert.assertEquals(Integer.class, version.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, version.
                    getAccessType());
            Assert.assertNotNull(version.getAnnotation(Version.class));
            Assert.assertNull(version.getTypeMetadata());
        }

        Assert.assertFalse(metadata.hasEmbeddedIdentifier());
        Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().isEmpty());

        // Properties
        Assert.assertSame(metadata.getIdentifierProperty(), metadata.
                getMappedProperty("id"));
        Assert.assertSame(metadata.getVersionProperty(), metadata.
                getMappedProperty("version"));

        {
            PropertyMetadata prop = metadata.getMappedProperty("firstName");
            Assert.assertEquals("firstName", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("lastName");
            Assert.assertEquals("lastName", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("address");
            Assert.assertEquals("address", prop.getName());
            Assert.assertEquals(Address_M.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertTrue(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNotNull(prop.getTypeMetadata());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("address.street");
            Assert.assertEquals("address.street", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
            Assert.assertTrue(prop instanceof NestedPropertyMetadata);
            Assert.assertSame(metadata.getMappedProperty("address"), ((NestedPropertyMetadata) prop).
                    getParentProperty());
            Assert.assertSame(metadata.getMappedProperty("address").
                    getTypeMetadata(), ((NestedPropertyMetadata) prop).
                    getActualProperty().getOwner());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty(
                    "address.postalCode");
            Assert.assertEquals("address.postalCode", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
            Assert.assertTrue(prop instanceof NestedPropertyMetadata);
            Assert.assertSame(metadata.getMappedProperty("address"), ((NestedPropertyMetadata) prop).
                    getParentProperty());
            Assert.assertSame(metadata.getMappedProperty("address").
                    getTypeMetadata(), ((NestedPropertyMetadata) prop).
                    getActualProperty().getOwner());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("children");
            Assert.assertEquals("children", prop.getName());
            Assert.assertEquals(Collection.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertTrue(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("parent");
            Assert.assertEquals("parent", prop.getName());
            Assert.assertEquals(Person_M.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertTrue(prop.isReference());
            Assert.assertSame(metadata, prop.getTypeMetadata());
        }

        Assert.assertNull(metadata.getMappedProperty("transientField"));
        Assert.assertNull(metadata.getMappedProperty("transientField2"));
    }

    @Test
    public void testGetEmbeddedIdFromFields() {
        EntityClassMetadata metadata = factory.getEntityClassMetadata(
                EmbeddedIdEntity_F.class);
        Assert.assertTrue(metadata.hasIdentifierProperty());
        Assert.assertTrue(metadata.hasEmbeddedIdentifier());
        Assert.assertFalse(metadata.hasVersionProperty());
        Assert.assertEquals(2, metadata.getEmbeddedIdentifierProperties().size());
        {
            PropertyMetadata prop = metadata.getMappedProperty("address.street");
            Assert.assertEquals("address.street", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
            Assert.assertTrue(prop instanceof NestedPropertyMetadata);
            Assert.assertSame(metadata.getMappedProperty("address"), ((NestedPropertyMetadata) prop).
                    getParentProperty());
            Assert.assertSame(metadata.getMappedProperty("address").
                    getTypeMetadata(), ((NestedPropertyMetadata) prop).
                    getActualProperty().getOwner());
            Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().
                    contains((NestedPropertyMetadata) prop));
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty(
                    "address.postalCode");
            Assert.assertEquals("address.postalCode", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
            Assert.assertTrue(prop instanceof NestedPropertyMetadata);
            Assert.assertSame(metadata.getMappedProperty("address"), ((NestedPropertyMetadata) prop).
                    getParentProperty());
            Assert.assertSame(metadata.getMappedProperty("address").
                    getTypeMetadata(), ((NestedPropertyMetadata) prop).
                    getActualProperty().getOwner());
            Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().
                    contains((NestedPropertyMetadata) prop));
        }
    }

    @Test
    public void testGetEmbeddedIdFromMethods() {
        EntityClassMetadata metadata = factory.getEntityClassMetadata(
                EmbeddedIdEntity_M.class);
        Assert.assertTrue(metadata.hasIdentifierProperty());
        Assert.assertTrue(metadata.hasEmbeddedIdentifier());
        Assert.assertFalse(metadata.hasVersionProperty());
        Assert.assertEquals(2, metadata.getEmbeddedIdentifierProperties().size());
       {
            PropertyMetadata prop = metadata.getMappedProperty("address.street");
            Assert.assertEquals("address.street", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
            Assert.assertTrue(prop instanceof NestedPropertyMetadata);
            Assert.assertSame(metadata.getMappedProperty("address"), ((NestedPropertyMetadata) prop).
                    getParentProperty());
            Assert.assertSame(metadata.getMappedProperty("address").
                    getTypeMetadata(), ((NestedPropertyMetadata) prop).
                    getActualProperty().getOwner());
            Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().
                    contains((NestedPropertyMetadata) prop));
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty(
                    "address.postalCode");
            Assert.assertEquals("address.postalCode", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertFalse(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertNull(prop.getTypeMetadata());
            Assert.assertTrue(prop instanceof NestedPropertyMetadata);
            Assert.assertSame(metadata.getMappedProperty("address"), ((NestedPropertyMetadata) prop).
                    getParentProperty());
            Assert.assertSame(metadata.getMappedProperty("address").
                    getTypeMetadata(), ((NestedPropertyMetadata) prop).
                    getActualProperty().getOwner());
            Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().
                    contains((NestedPropertyMetadata) prop));
        }
    }
}
