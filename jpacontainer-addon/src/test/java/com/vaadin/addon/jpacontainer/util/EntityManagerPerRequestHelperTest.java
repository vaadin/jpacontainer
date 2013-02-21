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
package com.vaadin.addon.jpacontainer.util;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.testdata.Person;

public class EntityManagerPerRequestHelperTest {

    private EntityManager entityManagerMock;
    private EntityProvider<Person> entityProviderMock;
    private JPAContainer<Person> container;
    private EntityManagerPerRequestHelper helper;
    private EntityManagerFactory entityManagerFactoryMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        entityManagerMock = createNiceMock(EntityManager.class);
        expect(entityManagerMock.isOpen()).andStubReturn(true);
        entityManagerFactoryMock = createMock(EntityManagerFactory.class);
        expect(entityManagerMock.getEntityManagerFactory()).andStubReturn(
                entityManagerFactoryMock);
        entityProviderMock = createNiceMock(EntityProvider.class);
        expect(entityProviderMock.getEntityManager()).andStubReturn(
                entityManagerMock);
        container = new JPAContainer<Person>(Person.class);
        container.setEntityProvider(entityProviderMock);

        helper = new EntityManagerPerRequestHelper();
    }

    @Test
    public void testCanAddContainer() {
        replay(entityManagerMock, entityProviderMock);
        helper.addContainer(container);
        verify(entityManagerMock, entityProviderMock);
    }

    @Test
    public void testRequestEndClosesEntityManager() {
        entityManagerMock.close();
        expectLastCall();
        replay(entityManagerMock, entityProviderMock);

        helper.addContainer(container);
        helper.requestEnd();

        verify(entityManagerMock, entityProviderMock);
    }

    @Test
    public void testRequestStartOpensNewEntityManager() {
        expect(entityManagerFactoryMock.createEntityManager()).andReturn(
                createNiceMock(EntityManager.class));
        entityProviderMock.setEntityManager(isA(EntityManager.class));
        expectLastCall();
        replay(entityManagerFactoryMock, entityManagerMock, entityProviderMock);

        helper.addContainer(container);
        helper.requestStart();

        verify(entityManagerFactoryMock, entityManagerMock, entityProviderMock);
    }

    @Test
    public void testTwoContainersWithTheSameEntityProviderAndManagerShouldOpenOnlyOneEntityManagerPerProvider() {
        expect(entityManagerFactoryMock.createEntityManager()).andReturn(
                createNiceMock(EntityManager.class)).times(1);
        entityProviderMock.setEntityManager(isA(EntityManager.class));
        expectLastCall().once();

        replay(entityManagerFactoryMock, entityManagerMock, entityProviderMock);

        JPAContainer<Person> c2 = new JPAContainer<Person>(Person.class);
        c2.setEntityProvider(entityProviderMock);
        helper.addContainer(container);
        helper.addContainer(c2);
        helper.requestStart();

        verify(entityManagerFactoryMock, entityManagerMock, entityProviderMock);
    }
}
