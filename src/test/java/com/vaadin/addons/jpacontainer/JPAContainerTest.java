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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer;

import com.vaadin.addons.jpacontainer.filter.Filters;
import com.vaadin.addons.jpacontainer.testdata.Person;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * Test case for {@link JPAContainer}.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class JPAContainerTest {

    private JPAContainer<Person> container;
    private EntityProvider<Person> entityProviderMock;
    private CachingEntityProvider<Person> cachingEntityProviderMock;
    private MutableEntityProvider<Person> mutableEntityProviderMock;

    @Before
    public void setUp() throws Exception {
        entityProviderMock = createMock(EntityProvider.class);
        cachingEntityProviderMock = createMock(CachingEntityProvider.class);
        mutableEntityProviderMock = createMock(MutableEntityProvider.class);

        container = new JPAContainer<Person>(Person.class);
    }

    @Test
    public void testGetEntityClass() {
        assertSame(Person.class, container.getEntityClass());
    }

    @Test
    public void testSetEntityProvider() {
        assertNull(container.getEntityProvider());
        container.setEntityProvider(cachingEntityProviderMock);
        assertSame(cachingEntityProviderMock, container.getEntityProvider());
    }

    @Test
    public void testAddContainerProperty() {
        try {
            container.addContainerProperty("a new property", String.class, "hello");
            fail("No exception thrown");
        } catch (UnsupportedOperationException e) {
            // This was expected
        }
    }

    @Test
    public void testSize() {
        expect(entityProviderMock.getEntityCount(Filters.and(Filters.eq("firstName", "Hello", false), Filters.eq("lastName", "World", false)))).andReturn(123);
        replay(entityProviderMock);

        container.addFilter(Filters.eq("firstName", "Hello", false));
        container.addFilter(Filters.eq("lastName", "World", false));
        container.setEntityProvider(entityProviderMock);

        assertEquals(123, container.size());

        verify(entityProviderMock);
    }

}
