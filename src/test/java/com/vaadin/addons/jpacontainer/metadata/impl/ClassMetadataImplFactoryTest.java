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

import com.vaadin.addons.jpacontainer.metadata.ClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link ClassMetadataImpl}.
 * 
 * @author Petter Holmström (IT Mill)
 */
public class ClassMetadataImplFactoryTest {

    /*
     * Test classes that use field annotations
     */
    
    @MappedSuperclass
    static abstract class BaseEntity_F implements Serializable {

        @Id
        Integer id;

        @Version
        Integer version;
    }

    @Entity
    static class Person_F extends BaseEntity_F {

        String firstName;

        String lastName;

        @Embedded
        Address_F address;

        transient String transientField;

        @Transient
        String transientField2;

        @OneToMany(mappedBy = "parent")
        Collection<Person_F> children;

        @ManyToOne
        Person_F parent;
    }

    @Embeddable
    static class Address_F implements Serializable {

        String street;

        String postalCode;
    }

    /*
     * Test classes that use method annotations
     */

    @MappedSuperclass
    static abstract class BaseEntity_M implements Serializable {

        @Id
        public Integer getId() {
            return null;
        }

        public void setId(Integer id) {
        }

        @Version
        public Integer getVersion() {
            return null;
        }

        public void setVersion(Integer version) {
        }
    }

    @Entity
    static class Person_M extends BaseEntity_M {

        public String getFirstName() {
            return null;
        }

        public void setFirstName(String firstName) {
        }

        public String getLastName() {
            return null;
        }

        public void setLastName(String lastName) {
        }

        @Embedded
        public Address_M getAddress() {
            return null;
        }

        public void setAddress(Address_M address) {
        }

        public String getTransientField() {
            return null;
        }

        @Transient
        public String getTransientField2() {
            return null;
        }

        public void setTransientField2(String value) {
        }

        @OneToMany(mappedBy = "parent")
        public Collection<Person_M> getChildren() {
            return null;
        }

        public void setChildren(Collection<Person_M> children) {
        }

        @ManyToOne
        public Person_M getParent() {
            return null;
        }

        public void setParent(Person_M parent) {
        }
    }

    @Embeddable
    static class Address_M implements Serializable {

        public String getStreet() {
            return null;
        }

        public void setStreet(String street) {
        }

        public String getPostalCode() {
            return null;
        }

        public void setPostalCode(String postalCode) {
        }
    }

    /*
     * Test classes for embedded IDs
     */

    @Entity
    static class EmbeddedIdEntity_F implements Serializable {

        @EmbeddedId
        private Address_F address;
    }

    @Entity
    static class EmbeddedIdEntity_M implements Serializable {

        @EmbeddedId
        public Address_M getAddress() {
            return null;
        }

        public void setAddress(Address_M address) {
        }
    }

    private MetadataFactory factory;

    @Before
    public void setUp() {
        factory = new ClassMetadataImplFactory();
    }

    @Test
    public void testGetMetadataFromFields() {
        ClassMetadata<Person_F> metadata = factory.getClassMetadata(
                Person_F.class);

        // Basic information
        Assert.assertEquals("Person_F", metadata.getEntityName());
        Assert.assertEquals(Person_F.class, metadata.getEntityClass());
        Assert.assertTrue(metadata.hasIdentifierProperty());
        {
            PropertyMetadata id = metadata.getIdentifierProperty();
            Assert.assertEquals("id", id.getName());
            Assert.assertEquals(Integer.class, id.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, id.
                    getAccessType());
            Assert.assertNotNull(id.getAnnotation(Id.class));
        }

        Assert.assertTrue(metadata.hasVersionProperty());
        {
            PropertyMetadata version = metadata.getVersionProperty();
            Assert.assertEquals("version", version.getName());
            Assert.assertEquals(Integer.class, version.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, version.
                    getAccessType());
            Assert.assertNotNull(version.getAnnotation(Version.class));
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
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("address.street");
            Assert.assertEquals("address.street", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertTrue(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty(
                    "address.postalCode");
            Assert.assertEquals("address.postalCode", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertTrue(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
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
        }

        Assert.assertNull(metadata.getMappedProperty("transientField"));
        Assert.assertNull(metadata.getMappedProperty("transientField2"));
    }

    @Test
    public void testGetMetadataFromMethods() {
        ClassMetadata<Person_M> metadata = factory.getClassMetadata(
                Person_M.class);

        // Basic information
        Assert.assertEquals("Person_M", metadata.getEntityName());
        Assert.assertEquals(Person_M.class, metadata.getEntityClass());
        Assert.assertTrue(metadata.hasIdentifierProperty());
        {
            PropertyMetadata id = metadata.getIdentifierProperty();
            Assert.assertEquals("id", id.getName());
            Assert.assertEquals(Integer.class, id.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, id.
                    getAccessType());
            Assert.assertNotNull(id.getAnnotation(Id.class));
        }

        Assert.assertTrue(metadata.hasVersionProperty());
        {
            PropertyMetadata version = metadata.getVersionProperty();
            Assert.assertEquals("version", version.getName());
            Assert.assertEquals(Integer.class, version.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, version.
                    getAccessType());
            Assert.assertNotNull(version.getAnnotation(Version.class));
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
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty("address.street");
            Assert.assertEquals("address.street", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertTrue(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty(
                    "address.postalCode");
            Assert.assertEquals("address.postalCode", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertTrue(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
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
        }

        Assert.assertNull(metadata.getMappedProperty("transientField"));
        Assert.assertNull(metadata.getMappedProperty("transientField2"));
    }

    @Test
    public void testGetEmbeddedIdFromFields() {
        ClassMetadata<EmbeddedIdEntity_F> metadata = factory.getClassMetadata(
                EmbeddedIdEntity_F.class);
        Assert.assertFalse(metadata.hasIdentifierProperty());
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
            Assert.assertTrue(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().
                    contains(prop));
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty(
                    "address.postalCode");
            Assert.assertEquals("address.postalCode", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.FIELD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertTrue(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().
                    contains(prop));
        }
    }

    @Test
    public void testGetEmbeddedIdFromMethods() {
        ClassMetadata<EmbeddedIdEntity_M> metadata = factory.getClassMetadata(
                EmbeddedIdEntity_M.class);
        Assert.assertFalse(metadata.hasIdentifierProperty());
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
            Assert.assertTrue(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().
                    contains(prop));
        }
        {
            PropertyMetadata prop = metadata.getMappedProperty(
                    "address.postalCode");
            Assert.assertEquals("address.postalCode", prop.getName());
            Assert.assertEquals(String.class, prop.getType());
            Assert.assertEquals(PropertyMetadata.AccessType.METHOD, prop.
                    getAccessType());
            Assert.assertFalse(prop.isCollection());
            Assert.assertTrue(prop.isEmbedded());
            Assert.assertFalse(prop.isReference());
            Assert.assertTrue(metadata.getEmbeddedIdentifierProperties().
                    contains(prop));
        }
    }
}
