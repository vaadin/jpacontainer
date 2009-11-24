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
import javax.persistence.Id;
import javax.persistence.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link PropertyMetadataImpl}.
 *
 * @author Petter Holmström (IT Mill)
 */
public class PropertyMetadataImplTest {

    @Version
    public int dummyField;

    PropertyMetadata pmd;

    @Before
    public void setUp() throws Exception {
        pmd = new PropertyMetadataImpl("name", Long.class, true,
                false, false, AccessType.FIELD, getClass().getField("dummyField").
                getAnnotations());
    }

    @Test
    public void testGetters() throws Exception {
        Assert.assertEquals("name", pmd.getName());
        Assert.assertEquals(Long.class, pmd.getType());
        Assert.assertTrue(pmd.isEmbedded());
        Assert.assertFalse(pmd.isReference());
        Assert.assertFalse(pmd.isCollection());
        Assert.assertEquals(AccessType.FIELD, pmd.getAccessType());
        Assert.assertArrayEquals(getClass().getField("dummyField").
                getAnnotations(), pmd.getAnnotations());
    }

    @Test
    public void testGetAnnotation() {
        Assert.assertNull(pmd.getAnnotation(Id.class));
        Assert.assertNotNull(pmd.getAnnotation(Version.class));
    }
}
