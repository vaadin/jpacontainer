/*
 * JPAContainer
 * Copyright (C) 2010-2011 Oy Vaadin Ltd
 *
 * This program is available both under Commercial Vaadin Add-On
 * License 2.0 (CVALv2) and under GNU Affero General Public License (version
 * 3 or later) at your option.
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and CVALv2 along with this program.  If not, see
 * <http://www.gnu.org/licenses/> and <http://vaadin.com/license/cval-2.0>.
 */
package com.vaadin.addon.jpacontainer.metadata;

import static com.vaadin.addon.jpacontainer.metadata.TestClasses.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test case for {@link EntityClassMetadata}.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class EntityClassMetadataTest {

	@Test
	public void testGetEntityName() {
		EntityClassMetadata<Person_F> metadata = new EntityClassMetadata<Person_F>(
				Person_F.class, "entityName");
		assertEquals("entityName", metadata.getEntityName());
	}

	@Test
	public void testIdentifierProperty() throws Exception {
		EntityClassMetadata<Person_F> metadata = new EntityClassMetadata<Person_F>(
				Person_F.class, "entityName");
		PersistentPropertyMetadata idProp = new PersistentPropertyMetadata(
				"id", Integer.class,
				PersistentPropertyMetadata.PropertyKind.SIMPLE,
				BaseEntity_F.class.getDeclaredField("id"));
		metadata.addProperties(idProp);

		assertFalse(metadata.hasIdentifierProperty());
		assertFalse(metadata.hasEmbeddedIdentifier());
		assertNull(metadata.getIdentifierProperty());

		metadata.setIdentifierPropertyName("id");

		assertTrue(metadata.hasIdentifierProperty());
		assertFalse(metadata.hasEmbeddedIdentifier());
		assertSame(idProp, metadata.getIdentifierProperty());
	}

	@Test
	public void testIdentifierProperty_Embedded() throws Exception {
		EntityClassMetadata<Person_F> metadata = new EntityClassMetadata<Person_F>(
				Person_F.class, "entityName");
		/*
		 * It does not matter that 'id' is in fact not embedded, as no
		 * validation is done by these classes.
		 */
		PersistentPropertyMetadata idProp = new PersistentPropertyMetadata(
				"id", new ClassMetadata<Integer>(Integer.class),
				PersistentPropertyMetadata.PropertyKind.EMBEDDED,
				BaseEntity_F.class.getDeclaredField("id"));
		metadata.addProperties(idProp);

		assertFalse(metadata.hasIdentifierProperty());
		assertFalse(metadata.hasEmbeddedIdentifier());
		assertNull(metadata.getIdentifierProperty());

		metadata.setIdentifierPropertyName("id");

		assertTrue(metadata.hasIdentifierProperty());
		assertTrue(metadata.hasEmbeddedIdentifier());
		assertSame(idProp, metadata.getIdentifierProperty());
	}

	@Test
	public void testIdentifierProperty_Invalid() {
		EntityClassMetadata<Person_F> metadata = new EntityClassMetadata<Person_F>(
				Person_F.class, "entityName");
		try {
			metadata.setIdentifierPropertyName("nonexistent");
			fail("Nothing happened");
		} catch (IllegalArgumentException e) {
			assertNull(metadata.getIdentifierProperty());
			assertFalse(metadata.hasIdentifierProperty());
			assertFalse(metadata.hasEmbeddedIdentifier());
		}
	}

	@Test
	public void testIdentifierProperty_Transient() throws Exception {
		EntityClassMetadata<Person_M> metadata = new EntityClassMetadata<Person_M>(
				Person_M.class, "entityName");
		metadata.addProperties(new PropertyMetadata("transientField2",
				String.class, Person_M.class
						.getDeclaredMethod("getTransientField2"),
				Person_M.class.getDeclaredMethod("setTransientField2",
						String.class)));
		assertNotNull(metadata.getProperty("transientField2"));
		try {
			metadata.setIdentifierPropertyName("transientField2");
			fail("Nothing happened");
		} catch (IllegalArgumentException e) {
			assertNull(metadata.getIdentifierProperty());
			assertFalse(metadata.hasIdentifierProperty());
			assertFalse(metadata.hasEmbeddedIdentifier());
		}
	}

	@Test
	public void testIdentifierProperty_Null() throws Exception {
		EntityClassMetadata<Person_F> metadata = new EntityClassMetadata<Person_F>(
				Person_F.class, "entityName");
		metadata.addProperties(new PersistentPropertyMetadata("id",
				Integer.class, PersistentPropertyMetadata.PropertyKind.SIMPLE,
				BaseEntity_F.class.getDeclaredField("id")));
		metadata.setIdentifierPropertyName("id");
		assertNotNull(metadata.getIdentifierProperty());

		metadata.setIdentifierPropertyName(null);

		assertNull(metadata.getIdentifierProperty());
		assertFalse(metadata.hasIdentifierProperty());
		assertFalse(metadata.hasEmbeddedIdentifier());
	}

	@Test
	public void testVersionProperty() throws Exception {
		EntityClassMetadata<Person_F> metadata = new EntityClassMetadata<Person_F>(
				Person_F.class, "entityName");
		PersistentPropertyMetadata verProp = new PersistentPropertyMetadata(
				"version", Integer.class,
				PersistentPropertyMetadata.PropertyKind.SIMPLE,
				BaseEntity_F.class.getDeclaredField("version"));
		metadata.addProperties(verProp);

		assertFalse(metadata.hasVersionProperty());
		assertNull(metadata.getVersionProperty());

		metadata.setVersionPropertyName("version");

		assertTrue(metadata.hasVersionProperty());
		assertSame(verProp, metadata.getVersionProperty());
	}

	@Test
	public void testVersionProperty_Invalid() {
		EntityClassMetadata<Person_F> metadata = new EntityClassMetadata<Person_F>(
				Person_F.class, "entityName");
		try {
			metadata.setVersionPropertyName("nonexistent");
			fail("Nothing happened");
		} catch (IllegalArgumentException e) {
			assertFalse(metadata.hasVersionProperty());
			assertNull(metadata.getVersionProperty());
		}
	}

	@Test
	public void testVersionProperty_Transient() throws Exception {
		EntityClassMetadata<Person_M> metadata = new EntityClassMetadata<Person_M>(
				Person_M.class, "entityName");
		metadata.addProperties(new PropertyMetadata("transientField2",
				String.class, Person_M.class
						.getDeclaredMethod("getTransientField2"),
				Person_M.class.getDeclaredMethod("setTransientField2",
						String.class)));
		assertNotNull(metadata.getProperty("transientField2"));
		try {
			metadata.setVersionPropertyName("transientField2");
			fail("Nothing happened");
		} catch (IllegalArgumentException e) {
			assertFalse(metadata.hasVersionProperty());
			assertNull(metadata.getVersionProperty());
		}
	}

	@Test
	public void testVersionProperty_Null() throws Exception {
		EntityClassMetadata<Person_F> metadata = new EntityClassMetadata<Person_F>(
				Person_F.class, "entityName");
		metadata.addProperties(new PersistentPropertyMetadata("version",
				Integer.class, PersistentPropertyMetadata.PropertyKind.SIMPLE,
				BaseEntity_F.class.getDeclaredField("version")));
		metadata.setVersionPropertyName("version");
		assertNotNull(metadata.getVersionProperty());

		metadata.setVersionPropertyName(null);

		assertNull(metadata.getVersionProperty());
		assertFalse(metadata.hasVersionProperty());
	}

	// TODO Add test for equals() and hashCode()
}
