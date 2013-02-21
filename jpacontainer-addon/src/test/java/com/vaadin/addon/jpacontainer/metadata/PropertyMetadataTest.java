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
