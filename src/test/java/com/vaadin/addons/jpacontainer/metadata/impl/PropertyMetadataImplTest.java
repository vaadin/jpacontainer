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
    PropertyMetadata pmd2;

    ClassMetadata cmd;
    ClassMetadata cmd2;

    @Version
    public Integer getDummyField() {
        return dummyField;
    }

    public void setDummyField(Integer dummyField) {
        this.dummyField = dummyField;
    }

    @Before
    public void setUp() throws Exception {
        cmd = new ClassMetadataImpl(PropertyMetadataImplTest.class);
        cmd2 = new ClassMetadataImpl(Long.class);
        
        pmd = new PropertyMetadataImpl("name", Long.class, cmd, true,
                false, false, cmd2, getClass().getField("dummyField"), null, null);
        pmd2 = new PropertyMetadataImpl("name", Long.class, cmd, false,
                false, false, null, null, getClass().getMethod("getDummyField"),
                getClass().getMethod("setDummyField", Integer.class));
    }

    @Test
    public void testGetters1() throws Exception {
        Assert.assertEquals("name", pmd.getName());
        Assert.assertEquals(Long.class, pmd.getType());
        Assert.assertTrue(pmd.isEmbedded());
        Assert.assertFalse(pmd.isReference());
        Assert.assertFalse(pmd.isCollection());
        Assert.assertSame(cmd, pmd.getOwner());
        Assert.assertSame(cmd2, pmd.getTypeMetadata());
        Assert.assertEquals(AccessType.FIELD, pmd.getAccessType());
        Assert.assertArrayEquals(getClass().getField("dummyField").
                getAnnotations(), pmd.getAnnotations());
    }

    @Test
    public void testGetters2() throws Exception {
        Assert.assertEquals("name", pmd2.getName());
        Assert.assertEquals(Long.class, pmd2.getType());
        Assert.assertFalse(pmd2.isEmbedded());
        Assert.assertFalse(pmd2.isReference());
        Assert.assertFalse(pmd2.isCollection());
        Assert.assertSame(cmd, pmd2.getOwner());
        Assert.assertNull(pmd2.getTypeMetadata());
        Assert.assertEquals(AccessType.METHOD, pmd2.getAccessType());
        Assert.assertArrayEquals(getClass().getMethod("getDummyField").
                getAnnotations(), pmd2.getAnnotations());
    }


    @Test
    public void testGetAnnotation() {
        Assert.assertNull(pmd.getAnnotation(Id.class));
        Assert.assertNotNull(pmd.getAnnotation(Version.class));
        
        Assert.assertNull(pmd2.getAnnotation(Id.class));
        Assert.assertNotNull(pmd2.getAnnotation(Version.class));
    }
}
