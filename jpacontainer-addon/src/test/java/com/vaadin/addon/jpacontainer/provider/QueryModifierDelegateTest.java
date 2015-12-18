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
package com.vaadin.addon.jpacontainer.provider;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.QueryModifierDelegate;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.testdata.StubEntity;
import com.vaadin.addon.jpacontainer.util.DefaultQueryModifierDelegate;

@SuppressWarnings({"rawtypes","unchecked"})
public class QueryModifierDelegateTest {
    
    static EntityContainer container = new JPAContainer<StubEntity>(
            StubEntity.class);

    private static EntityManagerFactory emf = Persistence
            .createEntityManagerFactory("eclipselink-in-memory");

    private EntityManager em;

    private LocalEntityProvider<Person> entityProvider;

    @Before
    public void setUp() {
        em = emf.createEntityManager();

        EntityTransaction t = em.getTransaction();
        t.begin();
        em.createQuery("DELETE FROM Person a").executeUpdate();
        t.commit();

        // Create a bunch of test Skills
        t = em.getTransaction();
        t.begin();
        for (int i = 0; i < 20; i++) {
            Person p = new Person();
            p.setFirstName("firstName" + i);
            p.setLastName("lastName" + i);
            em.persist(p);
        }
        t.commit();

        entityProvider = new LocalEntityProvider<Person>(Person.class, em);
    }

    private QueryModifierDelegate createMockDelegate() {
        QueryModifierDelegate delegate = createMock(QueryModifierDelegate.class);
        delegate.queryWillBeBuilt(isA(CriteriaBuilder.class),
                isA(CriteriaQuery.class));
        delegate.filtersWillBeAdded(isA(CriteriaBuilder.class),
                isA(CriteriaQuery.class), isA(List.class));
        delegate.filtersWereAdded(isA(CriteriaBuilder.class),
                isA(CriteriaQuery.class));
        delegate.queryHasBeenBuilt(isA(CriteriaBuilder.class),
                isA(CriteriaQuery.class));
        return delegate;
    }

    private QueryModifierDelegate createDelegateAddingFirstNameEqualFilter(
            final String value) {
        QueryModifierDelegate delegate = new DefaultQueryModifierDelegate() {
            @Override
            public void filtersWillBeAdded(CriteriaBuilder criteriaBuilder,
                    CriteriaQuery<?> query, List<Predicate> predicates) {
                Root<?> root = query.getRoots().iterator().next();
                predicates.add(criteriaBuilder.equal(
                        root.<String> get("firstName"),
                        criteriaBuilder.literal(value)));
            }
        };
        return delegate;
    }

    private QueryModifierDelegate createDelegateReplacingOrderByWithFirstNameDesc() {
        QueryModifierDelegate delegate = new DefaultQueryModifierDelegate() {
            @Override
            public void orderByWillBeAdded(CriteriaBuilder criteriaBuilder,
                    CriteriaQuery<?> query, List<Order> orderBy) {
                Root<?> root = query.getRoots().iterator().next();
                orderBy.clear();
                orderBy.add(criteriaBuilder.desc(root.get("firstName")));
            }
        };
        return delegate;
    }

    @Test
    public void testAddQueryModifierDelegate() {
        QueryModifierDelegate delegate = createMock(QueryModifierDelegate.class);
        entityProvider.setQueryModifierDelegate(delegate);
        assertEquals(delegate, entityProvider.getQueryModifierDelegate());
        entityProvider.setQueryModifierDelegate(null);
        assertNull(entityProvider.getQueryModifierDelegate());
    }

    @Test
    public void testQueryModifierDelegateCalledBeforeAddingFilters() {
        QueryModifierDelegate delegate = createMockDelegate();
        delegate.orderByWillBeAdded(isA(CriteriaBuilder.class),
                isA(CriteriaQuery.class), isA(List.class));
        delegate.orderByWasAdded(isA(CriteriaBuilder.class),
                isA(CriteriaQuery.class));
        replay(delegate);
        entityProvider.setQueryModifierDelegate(delegate);
        entityProvider.getFirstEntityIdentifier(container, null, null);
        entityProvider.setQueryModifierDelegate(null);
        verify(delegate);
    }

    @Test
    public void testQueryModifierDelegateCalledWhenCounting() {
        QueryModifierDelegate delegate = createMockDelegate();
        replay(delegate);
        entityProvider.setQueryModifierDelegate(delegate);
        entityProvider.getEntityCount(container, null);
        entityProvider.setQueryModifierDelegate(null);
        verify(delegate);
    }

    @Test
    public void testQueryModifierDelegateCalledWhenContainsEntity() {
        QueryModifierDelegate delegate = createMockDelegate();
        replay(delegate);
        entityProvider.setQueryModifierDelegate(delegate);
        entityProvider.containsEntity(container, 0L, null);
        entityProvider.setQueryModifierDelegate(null);
        verify(delegate);
    }

    @Test
    public void testCanAddPredicateThroughQueryModifierDelegate_count() {
        QueryModifierDelegate delegate = createDelegateAddingFirstNameEqualFilter("firstName9");
        entityProvider.setQueryModifierDelegate(delegate);
        assertEquals(1, entityProvider.getEntityCount(container, null));
        entityProvider.setQueryModifierDelegate(null);
    }

    @Test
    public void testCanAddPredicateThroughQueryModifierDelegate_firstEntityId() {
        Object entityId = entityProvider.getFirstEntityIdentifier(container, null, null);
        entityId = entityProvider.getNextEntityIdentifier(container, entityId, null, null);
        entityId = entityProvider.getNextEntityIdentifier(container, entityId, null, null);
        entityId = entityProvider.getNextEntityIdentifier(container, entityId, null, null);
        Object actualId = entityProvider.getNextEntityIdentifier(container, entityId,
                null, null);
        Person actualPerson = entityProvider.getEntity(container, actualId);

        QueryModifierDelegate delegate = createDelegateAddingFirstNameEqualFilter(actualPerson
                .getFirstName());
        entityProvider.setQueryModifierDelegate(delegate);
        Object id = entityProvider.getFirstEntityIdentifier(container, null, null);
        assertEquals(actualId, id);
        entityProvider.setQueryModifierDelegate(null);
    }

    @Test
    public void testCanAddPredicateThroughQueryModifierDelegate_containsEntity() {
        final Object entityId = entityProvider.getFirstEntityIdentifier(container, null,
                null);

        QueryModifierDelegate delegate = createDelegateAddingFirstNameEqualFilter("this does not exist");
        entityProvider.setQueryModifierDelegate(delegate);
        assertFalse(entityProvider.containsEntity(container, entityId, null));
        entityProvider.setQueryModifierDelegate(null);
    }

    @Test
    public void testCanAddOrderByThroughQueryModifierDelegate() {
        QueryModifierDelegate delegate = createDelegateReplacingOrderByWithFirstNameDesc();
        entityProvider.setQueryModifierDelegate(delegate);
        Object firstId = entityProvider.getFirstEntityIdentifier(container, null, null);
        Person person = entityProvider.getEntity(container, firstId);
        assertEquals("firstName9", person.getFirstName());
        entityProvider.setQueryModifierDelegate(null);
    }
}
