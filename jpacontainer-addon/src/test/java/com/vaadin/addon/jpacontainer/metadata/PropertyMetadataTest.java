/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.metadata;

import java.lang.reflect.Method;
import javax.persistence.Id;
import javax.persistence.Version;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test case for {@link PropertyMetadata}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class PropertyMetadataTest {

	// Dummy methods for testing

	@SuppressWarnings("unused")
	@Id
	private String getDummy() {
		return "";
	}

	@SuppressWarnings("unused")
	@Version
	private void setDummy(String value) {

	}

	@Test
	public void testReadOnlyProperty() throws Exception {
		Method getter = getClass().getDeclaredMethod("getDummy");
		PropertyMetadata property = new PropertyMetadata("dummy", String.class,
				getter, null);
		assertEquals("dummy", property.getName());
		assertSame(String.class, property.getType());
		assertArrayEquals(getter.getAnnotations(), property.getAnnotations());
		assertNotNull(property.getAnnotation(Id.class));
		assertNull(property.getAnnotation(Version.class));
		assertSame(getter, property.getter);
		assertNull(property.setter);
		assertFalse(property.isWritable());
	}

	@Test
	public void testWritableProperty() throws Exception {
		Method getter = getClass().getDeclaredMethod("getDummy");
		Method setter = getClass().getDeclaredMethod("setDummy", String.class);
		PropertyMetadata property = new PropertyMetadata("dummy", String.class,
				getter, setter);
		assertEquals("dummy", property.getName());
		assertSame(String.class, property.getType());
		assertArrayEquals(getter.getAnnotations(), property.getAnnotations());
		assertNotNull(property.getAnnotation(Id.class));
		assertNull(property.getAnnotation(Version.class));
		assertSame(getter, property.getter);
		assertSame(setter, property.setter);
		assertTrue(property.isWritable());
	}

	// TODO Add test for equals() and hashCode() + serialization
}
