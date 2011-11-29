/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.persistence.Version;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test case for {@link PersistentPropertyMetadata}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class PersistentPropertyMetadataTest {

	@Version
	public int dummyField;

	@Version
	public Integer getDummyField() {
		return dummyField;
	}

	public void setDummyField(Integer dummyField) {
		this.dummyField = dummyField;
	}

	@Test
	public void testFieldProperty() throws Exception {
		Field field = getClass().getDeclaredField("dummyField");
		PersistentPropertyMetadata prop = new PersistentPropertyMetadata(
				"dummy", Integer.class,
				PropertyKind.SIMPLE, field);
		assertEquals("dummy", prop.getName());
		assertSame(Integer.class, prop.getType());
		assertNull(prop.getTypeMetadata());
		assertEquals(PropertyKind.SIMPLE, prop
				.getPropertyKind());
		assertEquals(PersistentPropertyMetadata.AccessType.FIELD, prop
				.getAccessType());
		assertTrue(prop.isWritable());
		assertSame(field, prop.field);
		assertNull(prop.getter);
		assertNull(prop.setter);
		assertArrayEquals(field.getAnnotations(), prop.getAnnotations());
		assertNotNull(prop.getAnnotation(Version.class));
	}

	@Test
	public void testMethodProperty() throws Exception {
		Method getter = getClass().getDeclaredMethod("getDummyField");
		Method setter = getClass().getDeclaredMethod("setDummyField",
				Integer.class);
		PersistentPropertyMetadata prop = new PersistentPropertyMetadata(
				"dummy", Integer.class,
				PropertyKind.ONE_TO_MANY, getter,
				setter);
		assertEquals("dummy", prop.getName());
		assertSame(Integer.class, prop.getType());
		assertNull(prop.getTypeMetadata());
		assertEquals(PropertyKind.ONE_TO_MANY, prop
				.getPropertyKind());
		assertEquals(PersistentPropertyMetadata.AccessType.METHOD, prop
				.getAccessType());
		assertTrue(prop.isWritable());
		assertNull(prop.field);
		assertSame(getter, prop.getter);
		assertSame(setter, prop.setter);
		assertArrayEquals(getter.getAnnotations(), prop.getAnnotations());
		assertNotNull(prop.getAnnotation(Version.class));
	}

	@Test
	public void testFieldPropertyWithMetadata() throws Exception {
		Field field = getClass().getDeclaredField("dummyField");
		ClassMetadata<Integer> cmd = new ClassMetadata<Integer>(Integer.class);
		// It does not matter that Integer is not embeddable nor a reference, it
		// will work anyway
		PersistentPropertyMetadata prop = new PersistentPropertyMetadata(
				"dummy", cmd,
				PropertyKind.MANY_TO_ONE, field);
		assertEquals("dummy", prop.getName());
		assertSame(Integer.class, prop.getType());
		assertSame(cmd, prop.getTypeMetadata());
		assertEquals(PropertyKind.MANY_TO_ONE, prop
				.getPropertyKind());
		assertEquals(PersistentPropertyMetadata.AccessType.FIELD, prop
				.getAccessType());
		assertTrue(prop.isWritable());
		assertSame(field, prop.field);
		assertNull(prop.getter);
		assertNull(prop.setter);
		assertArrayEquals(field.getAnnotations(), prop.getAnnotations());
		assertNotNull(prop.getAnnotation(Version.class));
	}

	@Test
	public void testMethodPropertyWithMetadata() throws Exception {
		Method getter = getClass().getDeclaredMethod("getDummyField");
		Method setter = getClass().getDeclaredMethod("setDummyField",
				Integer.class);
		ClassMetadata<Integer> cmd = new ClassMetadata<Integer>(Integer.class);
		// It does not matter that Integer is not embeddable nor a reference, it
		// will work anyway
		PersistentPropertyMetadata prop = new PersistentPropertyMetadata(
				"dummy", cmd, PropertyKind.EMBEDDED,
				getter, setter);
		assertEquals("dummy", prop.getName());
		assertSame(Integer.class, prop.getType());
		assertSame(cmd, prop.getTypeMetadata());
		assertEquals(PropertyKind.EMBEDDED, prop
				.getPropertyKind());
		assertEquals(PersistentPropertyMetadata.AccessType.METHOD, prop
				.getAccessType());
		assertTrue(prop.isWritable());
		assertNull(prop.field);
		assertSame(getter, prop.getter);
		assertSame(setter, prop.setter);
		assertArrayEquals(getter.getAnnotations(), prop.getAnnotations());
		assertNotNull(prop.getAnnotation(Version.class));
	}

	// TODO Add test for equals() and hashCode() + serialization
}
