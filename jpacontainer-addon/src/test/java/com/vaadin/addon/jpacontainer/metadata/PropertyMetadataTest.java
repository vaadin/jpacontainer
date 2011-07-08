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

import java.lang.reflect.Method;
import javax.persistence.Id;
import javax.persistence.Version;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test case for {@link PropertyMetadata}.
 * 
 * @author Petter Holmström (IT Mill)
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
