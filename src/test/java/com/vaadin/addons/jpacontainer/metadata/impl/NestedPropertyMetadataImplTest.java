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

import com.vaadin.addons.jpacontainer.metadata.NestedPropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import java.lang.annotation.Annotation;
import javax.persistence.Version;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Unit test for {@link NestedPropertyMetadataImpl}.
 *
 * @author Petter Holmström (IT Mill)
 */
public class NestedPropertyMetadataImplTest {

    private PropertyMetadata actualProperty;

    private PropertyMetadata parentProperty;

    private NestedPropertyMetadata nestedProperty;

    @Before
    public void setUp() {
        actualProperty = createMock(PropertyMetadata.class);
        parentProperty = createMock(PropertyMetadata.class);
        nestedProperty = new NestedPropertyMetadataImpl(actualProperty,
                parentProperty);
    }

    @Test
    public void testPropertyGetters() {
        assertSame(actualProperty, nestedProperty.getActualProperty());
        assertSame(parentProperty, nestedProperty.getParentProperty());
    }

    @Test
    public void testGetAccessType() {
        expect(actualProperty.getAccessType()).andReturn(
                PropertyMetadata.AccessType.FIELD);
        replay(actualProperty);
        assertEquals(PropertyMetadata.AccessType.FIELD, nestedProperty.
                getAccessType());
        verify(actualProperty);
    }

    @Test
    public void testGetAnnotation() {
        expect(actualProperty.getAnnotation(Version.class)).andReturn(null);
        replay(actualProperty);
        assertNull(nestedProperty.getAnnotation(Version.class));
        verify(actualProperty);
    }

    @Test
    public void testGetAnnotations() {
        Annotation[] annotations = new Annotation[0];
        expect(actualProperty.getAnnotations()).andReturn(annotations);
        replay(actualProperty);
        assertSame(annotations, nestedProperty.getAnnotations());
        verify(actualProperty);
    }

    @Test
    public void testGetName() {
        expect(parentProperty.getName()).andReturn("parent");
        expect(actualProperty.getName()).andReturn("actual");
        replay(parentProperty);
        replay(actualProperty);
        assertEquals("parent.actual", nestedProperty.getName());
        verify(actualProperty);
        verify(parentProperty);
    }

    @Test
    public void testGetOwner() {
        expect(parentProperty.getOwner()).andReturn(null);
        replay(parentProperty);
        assertNull(nestedProperty.getOwner());
        verify(parentProperty);
    }

    @Test
    public void testGetType() {
        expect(actualProperty.getType()).andReturn(null);
        replay(actualProperty);
        assertNull(nestedProperty.getType());
        verify(actualProperty);
    }

    @Test
    public void testGetTypeMetadata() {
        expect(actualProperty.getTypeMetadata()).andReturn(null);
        replay(actualProperty);
        assertNull(nestedProperty.getTypeMetadata());
        verify(actualProperty);
    }

    @Test
    public void testIsCollection() {
        expect(actualProperty.isCollection()).andReturn(true);
        replay(actualProperty);
        assertTrue(nestedProperty.isCollection());
        verify(actualProperty);
    }

    @Test
    public void testIsEmbedded() {
        expect(actualProperty.isEmbedded()).andReturn(true);
        replay(actualProperty);
        assertTrue(nestedProperty.isEmbedded());
        verify(actualProperty);
    }

    @Test
    public void testIsReference() {
        expect(actualProperty.isReference()).andReturn(true);
        replay(actualProperty);
        assertTrue(nestedProperty.isReference());
        verify(actualProperty);
    }
}
