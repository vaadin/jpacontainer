/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;

import javax.persistence.Id;
import javax.persistence.Version;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.metadata.TestClasses.Address_F;
import com.vaadin.addon.jpacontainer.metadata.TestClasses.Address_M;
import com.vaadin.addon.jpacontainer.metadata.TestClasses.Data_D;
import com.vaadin.addon.jpacontainer.metadata.TestClasses.EmbeddedIdEntity_F;
import com.vaadin.addon.jpacontainer.metadata.TestClasses.EmbeddedIdEntity_M;
import com.vaadin.addon.jpacontainer.metadata.TestClasses.Integer_ConcreteId_M;
import com.vaadin.addon.jpacontainer.metadata.TestClasses.Person_F;
import com.vaadin.addon.jpacontainer.metadata.TestClasses.Person_M;
import com.vaadin.addon.jpacontainer.testdata.Data;

/**
 * Test case for {@link MetadataFactory}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class MetadataFactoryTest {

    private MetadataFactory factory;

    @Before
    public void setUp() {
        factory = MetadataFactory.getInstance();
    }

    @Test
    public void testGetMetadataFromFields_EntityClass() {
        EntityClassMetadata<Person_F> metadata = factory
                .getEntityClassMetadata(Person_F.class);

        // Basic information
        assertEquals("Person_F", metadata.getEntityName());
        assertEquals(Person_F.class, metadata.getMappedClass());
        assertTrue(metadata.hasIdentifierProperty());
        {
            PersistentPropertyMetadata id = metadata.getIdentifierProperty();
            assertEquals("id", id.getName());
            assertEquals(Integer.class, id.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.FIELD,
                    id.getAccessType());
            assertNotNull(id.getAnnotation(Id.class));
            assertNull(id.getTypeMetadata());
            assertTrue(id.isWritable());
        }

        assertTrue(metadata.hasVersionProperty());
        {
            PersistentPropertyMetadata version = metadata.getVersionProperty();
            assertEquals("version", version.getName());
            assertEquals(Integer.class, version.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.FIELD,
                    version.getAccessType());
            assertNotNull(version.getAnnotation(Version.class));
            assertNull(version.getTypeMetadata());
            assertTrue(version.isWritable());
        }

        assertFalse(metadata.hasEmbeddedIdentifier());

        // Properties
        assertSame(metadata.getIdentifierProperty(), metadata.getProperty("id"));
        assertSame(metadata.getVersionProperty(),
                metadata.getProperty("version"));

        {
            PersistentPropertyMetadata prop = (PersistentPropertyMetadata) metadata
                    .getProperty("firstName");
            assertEquals("firstName", prop.getName());
            assertEquals(String.class, prop.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.FIELD,
                    prop.getAccessType());
            assertEquals(PropertyKind.SIMPLE, prop.getPropertyKind());
            assertNull(prop.getTypeMetadata());
            assertTrue(prop.isWritable());
        }
        {
            PersistentPropertyMetadata prop = (PersistentPropertyMetadata) metadata
                    .getProperty("lastName");
            assertEquals("lastName", prop.getName());
            assertEquals(String.class, prop.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.FIELD,
                    prop.getAccessType());
            assertEquals(PropertyKind.SIMPLE, prop.getPropertyKind());
            assertNull(prop.getTypeMetadata());
            assertTrue(prop.isWritable());
        }
        {
            PersistentPropertyMetadata prop = (PersistentPropertyMetadata) metadata
                    .getProperty("address");
            assertEquals("address", prop.getName());
            assertEquals(Address_F.class, prop.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.FIELD,
                    prop.getAccessType());
            assertEquals(PropertyKind.EMBEDDED, prop.getPropertyKind());
            assertNotNull(prop.getTypeMetadata());
            assertTrue(prop.isWritable());
        }
        {
            PersistentPropertyMetadata prop = (PersistentPropertyMetadata) metadata
                    .getProperty("children");
            assertEquals("children", prop.getName());
            assertEquals(Collection.class, prop.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.FIELD,
                    prop.getAccessType());
            assertEquals(PropertyKind.ONE_TO_MANY, prop.getPropertyKind());
            assertNull(prop.getTypeMetadata());
            assertTrue(prop.isWritable());
        }
        {
            PersistentPropertyMetadata prop = (PersistentPropertyMetadata) metadata
                    .getProperty("parent");
            assertEquals("parent", prop.getName());
            assertEquals(Person_F.class, prop.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.FIELD,
                    prop.getAccessType());
            assertEquals(PropertyKind.MANY_TO_ONE, prop.getPropertyKind());
            assertSame(metadata, prop.getTypeMetadata());
            assertTrue(prop.isWritable());
        }

        // These fields do not have getter methods, hence they cannot be present
        // in the metadata
        assertNull(metadata.getProperty("transientField"));
        assertNull(metadata.getProperty("transientField2"));

        {
            PropertyMetadata prop = metadata.getProperty("transientBaseField");
            assertEquals("transientBaseField", prop.getName());
            assertEquals(Integer.class, prop.getType());
            assertFalse(prop.isWritable());
        }
        {
            PropertyMetadata prop = metadata.getProperty("transientField3");
            assertEquals("transientField3", prop.getName());
            assertEquals(String.class, prop.getType());
            assertFalse(prop.isWritable());
        }
        {
            PropertyMetadata prop = metadata.getProperty("transientField4");
            assertEquals("transientField4", prop.getName());
            assertEquals(String.class, prop.getType());
            assertTrue(prop.isWritable());
        }
        {
            PropertyMetadata prop = metadata.getProperty("transientAddress");
            assertEquals("transientAddress", prop.getName());
            assertEquals(Address_M.class, prop.getType());
            assertTrue(prop.isWritable());
        }

        assertEquals(11, metadata.getProperties().size());
    }

    @Test
    public void testGetMetadataFromMethods_EntityClass() {
        EntityClassMetadata<Person_M> metadata = factory
                .getEntityClassMetadata(Person_M.class);

        // Basic information
        assertEquals("Person_M", metadata.getEntityName());
        assertEquals(Person_M.class, metadata.getMappedClass());
        assertTrue(metadata.hasIdentifierProperty());
        {
            PersistentPropertyMetadata id = metadata.getIdentifierProperty();
            assertEquals("id", id.getName());
            assertEquals(Integer.class, id.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.METHOD,
                    id.getAccessType());
            assertNotNull(id.getAnnotation(Id.class));
            assertNull(id.getTypeMetadata());
            assertTrue(id.isWritable());
        }

        assertTrue(metadata.hasVersionProperty());
        {
            PersistentPropertyMetadata version = metadata.getVersionProperty();
            assertEquals("version", version.getName());
            assertEquals(Integer.class, version.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.METHOD,
                    version.getAccessType());
            assertNotNull(version.getAnnotation(Version.class));
            assertNull(version.getTypeMetadata());
            assertTrue(version.isWritable());
        }

        assertFalse(metadata.hasEmbeddedIdentifier());

        // Properties
        assertSame(metadata.getIdentifierProperty(), metadata.getProperty("id"));
        assertSame(metadata.getVersionProperty(),
                metadata.getProperty("version"));

        {
            PersistentPropertyMetadata prop = (PersistentPropertyMetadata) metadata
                    .getProperty("firstName");
            assertEquals("firstName", prop.getName());
            assertEquals(String.class, prop.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.METHOD,
                    prop.getAccessType());
            assertEquals(PropertyKind.SIMPLE, prop.getPropertyKind());
            assertNull(prop.getTypeMetadata());
            assertTrue(prop.isWritable());
        }
        {
            PersistentPropertyMetadata prop = (PersistentPropertyMetadata) metadata
                    .getProperty("lastName");
            assertEquals("lastName", prop.getName());
            assertEquals(String.class, prop.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.METHOD,
                    prop.getAccessType());
            assertEquals(PropertyKind.SIMPLE, prop.getPropertyKind());
            assertNull(prop.getTypeMetadata());
            assertTrue(prop.isWritable());
        }
        {
            PersistentPropertyMetadata prop = (PersistentPropertyMetadata) metadata
                    .getProperty("address");
            assertEquals("address", prop.getName());
            assertEquals(Address_M.class, prop.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.METHOD,
                    prop.getAccessType());
            assertEquals(PropertyKind.EMBEDDED, prop.getPropertyKind());
            assertNotNull(prop.getTypeMetadata());
            assertTrue(prop.isWritable());
        }
        {
            PersistentPropertyMetadata prop = (PersistentPropertyMetadata) metadata
                    .getProperty("children");
            assertEquals("children", prop.getName());
            assertEquals(Collection.class, prop.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.METHOD,
                    prop.getAccessType());
            assertEquals(PropertyKind.ONE_TO_MANY, prop.getPropertyKind());
            assertNull(prop.getTypeMetadata());
            assertTrue(prop.isWritable());
        }
        {
            PersistentPropertyMetadata prop = (PersistentPropertyMetadata) metadata
                    .getProperty("parent");
            assertEquals("parent", prop.getName());
            assertEquals(Person_M.class, prop.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.METHOD,
                    prop.getAccessType());
            assertEquals(PropertyKind.MANY_TO_ONE, prop.getPropertyKind());
            assertSame(metadata, prop.getTypeMetadata());
            assertTrue(prop.isWritable());
        }
        {
            PropertyMetadata prop = metadata.getProperty("transientBaseField");
            assertEquals("transientBaseField", prop.getName());
            assertEquals(Integer.class, prop.getType());
            assertFalse(prop.isWritable());
        }
        {
            PropertyMetadata prop = metadata.getProperty("transientField");
            assertEquals("transientField", prop.getName());
            assertEquals(String.class, prop.getType());
            assertFalse(prop.isWritable());
        }
        {
            PropertyMetadata prop = metadata.getProperty("transientField2");
            assertEquals("transientField2", prop.getName());
            assertEquals(String.class, prop.getType());
            assertTrue(prop.isWritable());
        }
        assertEquals(10, metadata.getProperties().size());
    }

    @Test
    public void testGetMetadataFromMethods_InheritanceOrder() {
        EntityClassMetadata<Integer_ConcreteId_M> metadata = factory
                .getEntityClassMetadata(Integer_ConcreteId_M.class);

        // Basic information
        assertEquals("Integer_ConcreteId_M", metadata.getEntityName());
        assertEquals(Integer_ConcreteId_M.class, metadata.getMappedClass());
        assertTrue(metadata.hasIdentifierProperty());
        {
            PersistentPropertyMetadata id = metadata.getIdentifierProperty();
            assertEquals("id", id.getName());
            assertEquals(Integer.class, id.getType());
            assertEquals(PersistentPropertyMetadata.AccessType.METHOD,
                    id.getAccessType());
            assertNotNull(id.getAnnotation(Id.class));
            assertNull(id.getTypeMetadata());
            assertTrue(id.isWritable());
        }

        // Properties
        assertSame(metadata.getIdentifierProperty(), metadata.getProperty("id"));
        assertEquals(1, metadata.getProperties().size());
    }

    @Test
    public void testGetEmbeddedIdFromFields() {
        EntityClassMetadata<EmbeddedIdEntity_F> metadata = factory
                .getEntityClassMetadata(EmbeddedIdEntity_F.class);
        assertTrue(metadata.hasIdentifierProperty());
        assertTrue(metadata.hasEmbeddedIdentifier());
        assertFalse(metadata.hasVersionProperty());
        assertSame(factory.getClassMetadata(Address_F.class,
                PersistentPropertyMetadata.AccessType.FIELD), metadata
                .getIdentifierProperty().getTypeMetadata());
    }

    @Test
    public void testGetEmbeddedIdFromMethods() {
        EntityClassMetadata<EmbeddedIdEntity_M> metadata = factory
                .getEntityClassMetadata(EmbeddedIdEntity_M.class);
        assertTrue(metadata.hasIdentifierProperty());
        assertTrue(metadata.hasEmbeddedIdentifier());
        assertFalse(metadata.hasVersionProperty());
        assertSame(factory.getClassMetadata(Address_M.class,
                PersistentPropertyMetadata.AccessType.METHOD), metadata
                .getIdentifierProperty().getTypeMetadata());
    }

    @Test
    public void testBuildMetadataForEntityWithTargetEntityAnnotatedField() {
        EntityClassMetadata<Data_D> metadata = factory
                .getEntityClassMetadata(Data_D.class);
    }

    @Test
    public void testCollectionTypeWithTargetEntityAnnotation() {
        EntityClassMetadata<Data> metadata = factory
                .getEntityClassMetadata(Data.class);
        Class<?> type = metadata.getProperty("manyToMany").getType();
        assertEquals(Set.class, type);
    }
}
