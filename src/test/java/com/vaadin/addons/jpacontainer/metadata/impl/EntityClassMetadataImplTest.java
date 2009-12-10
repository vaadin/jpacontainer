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
import org.junit.Test;
import static com.vaadin.addons.jpacontainer.metadata.impl.TestClasses.*;
import static org.junit.Assert.*;

/**
 * Unit test for {@link EntityClassMetadataImpl}.
 *
 * @author Petter Holmström (IT Mill)
 */
public class EntityClassMetadataImplTest {

    @Test
    public void testGetEntityName() {
        EntityClassMetadataImpl metadata = new EntityClassMetadataImpl(
                Person_F.class,
                "entityName");
        assertEquals("entityName", metadata.getEntityName());
    }

    @Test
    public void testIdentifierProperty() throws Exception {
        EntityClassMetadataImpl metadata = new EntityClassMetadataImpl(
                Person_F.class,
                "entityName");
        PropertyMetadata idProp = metadata.addProperty("id", Integer.class, BaseEntity_F.class.
                getDeclaredField("id"), null, null);
        metadata.setIdentifierProperty("id");

        assertTrue(metadata.hasIdentifierProperty());
        assertFalse(metadata.hasEmbeddedIdentifier());
        assertSame(idProp, metadata.getIdentifierProperty());

        assertFalse(metadata.hasVersionProperty());
    }

    @Test
    public void testIdentifierProperty_Embedded() throws Exception {
        EntityClassMetadataImpl metadata = new EntityClassMetadataImpl(
                EmbeddedIdEntity_F.class, "entityName");
        ClassMetadataImpl addressMetadata = new ClassMetadataImpl(
                Address_F.class);
        addressMetadata.addProperty("street", String.class, Address_F.class.
                getDeclaredField("street"), null, null);
        PropertyMetadata idProp =
                metadata.addEmbeddedProperty("address", addressMetadata,
                EmbeddedIdEntity_F.class.getDeclaredField("address"), null, null);
        metadata.setIdentifierProperty("address");

        assertTrue(metadata.hasIdentifierProperty());
        assertTrue(metadata.hasEmbeddedIdentifier());
        assertSame(idProp, metadata.getIdentifierProperty());
        assertEquals(1, metadata.getEmbeddedIdentifierProperties().size());
        assertEquals("address.street", metadata.getEmbeddedIdentifierProperties().
                iterator().next().getName());

        assertFalse(metadata.hasVersionProperty());
    }

    @Test
    public void testVersionProperty() throws Exception {
        EntityClassMetadataImpl metadata = new EntityClassMetadataImpl(
                Person_F.class,
                "entityName");
        PropertyMetadata vProp = metadata.addProperty("version", Integer.class, BaseEntity_F.class.
                getDeclaredField("version"), null, null);
        metadata.setVersionProperty("version");

        assertTrue(metadata.hasVersionProperty());
        assertSame(vProp, metadata.getVersionProperty());

        assertFalse(metadata.hasIdentifierProperty());
        assertFalse(metadata.hasEmbeddedIdentifier());
    }
}
