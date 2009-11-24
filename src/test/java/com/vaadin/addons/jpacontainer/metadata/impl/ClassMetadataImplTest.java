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

import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata.AccessType;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.persistence.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link ClassMetadataImpl}.
 *
 * @author Petter Holmström (IT Mill)
 */
public class ClassMetadataImplTest {

    private Collection<PropertyMetadata> idProperties;

    private Map<String, PropertyMetadata> properties;

    @Before
    public void setUp() {
        properties = new HashMap<String, PropertyMetadata>();
        properties.put("property1", new PropertyMetadataImpl("property1",
                String.class, false, false, false, AccessType.FIELD,
                new Annotation[]{}));
        properties.put("property2", new PropertyMetadataImpl("property2",
                Integer.class, false, false, false, AccessType.FIELD,
                new Annotation[]{}));
        properties.put("property3", new PropertyMetadataImpl("property3",
                Integer.class, false, false, false, AccessType.FIELD,
                new Annotation[]{}));
        idProperties = new LinkedList<PropertyMetadata>();
    }

    @Test
    public void testSimpleGetters() {
        ClassMetadataImpl<Object> metadata = new ClassMetadataImpl<Object>(
                "entityName", Object.class, properties, idProperties);

        Assert.assertEquals("entityName", metadata.getEntityName());
        Assert.assertEquals(Object.class, metadata.getEntityClass());
        Assert.assertFalse(metadata.hasVersionProperty());
        Assert.assertTrue(properties.get("property1") == metadata.
                getMappedProperty("property1"));
        Assert.assertTrue(properties.get("property2") == metadata.
                getMappedProperty("property2"));
        Assert.assertTrue(properties.get("property3") == metadata.
                getMappedProperty("property3"));
        try {
            metadata.getMappedProperties().add(new PropertyMetadataImpl(
                    "property4", Integer.class, false, false, false,
                    AccessType.FIELD, new Annotation[]{}));
            Assert.fail("No exception thrown");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals(3, metadata.getMappedProperties().size());
        }
    }

    @Test
    public void testSingleIdProperty() {
        idProperties.add(properties.get("property1"));
        ClassMetadataImpl<Object> metadata = new ClassMetadataImpl<Object>(
                "entityName", Object.class, properties, idProperties);

        Assert.assertTrue(metadata.hasIdentifierProperty());
        Assert.assertFalse(metadata.hasEmbeddedIdentifier());
        Assert.assertSame(properties.get("property1"), metadata.
                getIdentifierProperty());
        Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().isEmpty());
    }

    @Test
    public void testMultipleIdProperties() {
        idProperties.add(properties.get("property1"));
        idProperties.add(properties.get("property2"));
        ClassMetadataImpl<Object> metadata = new ClassMetadataImpl<Object>(
                "entityName", Object.class, properties, idProperties);

        Assert.assertFalse(metadata.hasIdentifierProperty());
        Assert.assertTrue(metadata.hasEmbeddedIdentifier());
        Assert.assertNull(metadata.getIdentifierProperty());
        Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().contains(properties.
                get("property1")));
        Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().contains(properties.
                get("property2")));
        Assert.assertEquals(2, metadata.getEmbeddedIdentifierProperties().size());
    }

    static class Entity_F {

        @Version
        Integer version;
    }

    static class Entity_M {

        Integer version;

        @Version
        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }
    }

    @Test
    public void testVersionProperty() throws Exception {
        properties.put("version", new PropertyMetadataImpl("version",
                Integer.class, false,
                false, false, AccessType.METHOD, Entity_F.class.getDeclaredField(
                "version").getAnnotations()));
        ClassMetadataImpl<Object> metadata = new ClassMetadataImpl<Object>(
                "entityName", Object.class, properties, idProperties);

        Assert.assertTrue(metadata.hasVersionProperty());
        Assert.assertSame(properties.get("version"), metadata.getVersionProperty());
    }

    @Test
    public void testGetPropertyValue_Field() throws Exception {
        Entity_F entity_f = new Entity_F();
        entity_f.version = 123;

        properties.put("version", new PropertyMetadataImpl("version",
                Integer.class, false,
                false, false, AccessType.METHOD, Entity_F.class.getDeclaredField(
                "version").getAnnotations()));
        ClassMetadataImpl<Entity_F> metadata = new ClassMetadataImpl<Entity_F>(
                "entityName", Entity_F.class, properties, idProperties);

        Assert.assertEquals(123, metadata.getPropertyValue(entity_f, metadata.getVersionProperty()));
    }

    //@Test
    public void testgetPropertyValue_Method() throws Exception {
        Assert.fail("Not implemented");
    }
}
