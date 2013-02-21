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
package com.vaadin.addon.jpacontainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import com.vaadin.addon.jpacontainer.provider.CachingBatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.LocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.jndijta.JndiAddresses;

/**
 * A factory for creating instances of JPAContainers backed by different default
 * entity providers.
 * 
 * @author Jonatan Kronqvist / Vaadin Ltd
 */
public class JPAContainerFactory {

    /**
     * Cache of entity manager factories. These are cached, since the creation
     * of an EntityManagerFactory can be quite resource intensive.
     */
    private static Map<String, EntityManagerFactory> puToEmfMap = Collections
            .synchronizedMap(new HashMap<String, EntityManagerFactory>());

    /**
     * Creates a new instance of JPAContainer backed by a
     * {@link CachingMutableLocalEntityProvider}. This method should be used if
     * you already know an instance of {@link EntityManager}.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param entityManager
     *            the entity manager to use
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> make(Class<T> entityClass,
            EntityManager entityManager) {
        return makeWithEntityProvider(entityClass,
                new CachingMutableLocalEntityProvider<T>(entityClass,
                        entityManager));
    }

    /**
     * Builds a JPAContainer with the specified {@link EntityProvider}
     * 
     * @param entityClass
     *            the class of the entity
     * @param entityProvider
     *            the entity provider to use
     * @return a fully configured JPAContainer instance
     */
    private static <T> JPAContainer<T> makeWithEntityProvider(
            Class<T> entityClass, EntityProvider<T> entityProvider) {
        JPAContainer<T> container = new JPAContainer<T>(entityClass);
        container.setEntityProvider(entityProvider);
        return container;
    }

    /**
     * Creates a new instance of a JPAContainer backed by a
     * {@link CachingMutableLocalEntityProvider}. This method can be used if you
     * do not know and do not need/want to know the instance of
     * {@link EntityManager} that is used, which is the case in simplistic
     * instances.
     * 
     * An instance of {@link EntityManagerFactory} will be created for the
     * persistence unit and used to build entity managers.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param persistenceUnitName
     *            the persistency context to use to create entity managers
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> make(Class<T> entityClass,
            String persistenceUnitName) {
        return make(entityClass,
                createEntityManagerForPersistenceUnit(persistenceUnitName));
    }

    /**
     * Creates an {@link EntityManager} using the cached
     * {@link EntityManagerFactory} for the persistence unit. If no entity
     * manager factory exists, one is created before using it to build the
     * entity manager.
     * 
     * @param name
     *            the name of the persistence unit.
     * @return an entity manager for the persistence unit.
     */
    public synchronized static EntityManager createEntityManagerForPersistenceUnit(
            String name) {
        if (!puToEmfMap.containsKey(name)) {
            puToEmfMap.put(name, Persistence.createEntityManagerFactory(name));
        }
        return puToEmfMap.get(name).createEntityManager();
    }

    /**
     * Creates a new instance of JPAContainer backed by a
     * {@link CachingLocalEntityProvider}. This method should be used if you
     * already know an instance of {@link EntityManager}.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param entityManager
     *            the entity manager to use
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeReadOnly(Class<T> entityClass,
            EntityManager entityManager) {
        return makeWithEntityProvider(entityClass,
                new CachingLocalEntityProvider<T>(entityClass, entityManager));
    }

    /**
     * Creates a new instance of JPAContainer backed by a
     * {@link CachingLocalEntityProvider}. This method can be used if you do not
     * know and do not need/want to know the instance of {@link EntityManager}
     * that is used, which is the case in simplistic instances.
     * 
     * An instance of {@link EntityManagerFactory} will be created for the
     * persistence unit and used to build entity managers.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param persistenceUnitName
     *            the persistency context to use to create entity managers
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeReadOnly(Class<T> entityClass,
            String persistenceUnitName) {
        return makeReadOnly(entityClass,
                createEntityManagerForPersistenceUnit(persistenceUnitName));
    }

    /**
     * Creates a new instance of JPAContainer backed by a
     * {@link CachingBatchableLocalEntityProvider}. This method should be used
     * if you already know an instance of {@link EntityManager}.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param entityManager
     *            the entity manager to use
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeBatchable(Class<T> entityClass,
            EntityManager entityManager) {
        return makeWithEntityProvider(entityClass,
                new CachingBatchableLocalEntityProvider<T>(entityClass,
                        entityManager));
    }

    /**
     * Creates a new instance of JPAContainer backed by a
     * {@link CachingBatchableLocalEntityProvider}. This method can be used if
     * you do not know and do not need/want to know the instance of
     * {@link EntityManager} that is used, which is the case in simplistic
     * instances.
     * 
     * An instance of {@link EntityManagerFactory} will be created for the
     * persistence unit and used to build entity managers.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param persistenceUnitName
     *            the persistency context to use to create entity managers
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeBatchable(Class<T> entityClass,
            String persistenceUnitName) {
        return makeBatchable(entityClass,
                createEntityManagerForPersistenceUnit(persistenceUnitName));
    }

    /**
     * Creates a new instance of JPAContainer backed by a
     * {@link MutableLocalEntityProvider}. This method should be used if you
     * already know an instance of {@link EntityManager}.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param entityManager
     *            the entity manager to use
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeNonCached(Class<T> entityClass,
            EntityManager entityManager) {
        return makeWithEntityProvider(entityClass,
                new MutableLocalEntityProvider<T>(entityClass, entityManager));
    }

    /**
     * Creates a new instance of JPAContainer backed by a
     * {@link MutableLocalEntityProvider}. This method can be used if you do not
     * know and do not need/want to know the instance of {@link EntityManager}
     * that is used, which is the case in simplistic instances.
     * 
     * An instance of {@link EntityManagerFactory} will be created for the
     * persistence unit and used to build entity managers.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param persistenceUnitName
     *            the persistency context to use to create entity managers
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeNonCached(Class<T> entityClass,
            String persistenceUnitName) {
        return makeNonCached(entityClass,
                createEntityManagerForPersistenceUnit(persistenceUnitName));
    }

    /**
     * Creates a new instance of JPAContainer backed by a
     * {@link LocalEntityProvider}. This method should be used if you already
     * know an instance of {@link EntityManager}.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param entityManager
     *            the entity manager to use
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeNonCachedReadOnly(
            Class<T> entityClass, EntityManager entityManager) {
        return makeWithEntityProvider(entityClass, new LocalEntityProvider<T>(
                entityClass, entityManager));
    }

    /**
     * Creates a new instance of JPAContainer backed by a
     * {@link LocalEntityProvider}. This method can be used if you do not know
     * and do not need/want to know the instance of {@link EntityManager} that
     * is used, which is the case in simplistic instances.
     * 
     * An instance of {@link EntityManagerFactory} will be created for the
     * persistence unit and used to build entity managers.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param persistenceUnitName
     *            the persistency context to use to create entity managers
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeNonCachedReadOnly(
            Class<T> entityClass, String persistenceUnitName) {
        return makeNonCachedReadOnly(entityClass,
                createEntityManagerForPersistenceUnit(persistenceUnitName));
    }

    /**
     * Creates a JPAContainer that uses JNDI lookups to fetch entity manager
     * from "java:comp/env/persistence/em". Container also uses JTA
     * transactions. This type of container commonly suits for JEE6 environment.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeNonCachedReadOnlyJndi(
            Class<T> entityClass) {
        return makeWithEntityProvider(
                entityClass,
                new com.vaadin.addon.jpacontainer.provider.jndijta.EntityProvider<T>(
                        entityClass));
    }

    /**
     * Creates a JPAContainer that uses JNDI lookups to fetch entity manager
     * from "java:comp/env/persistence/em". Container also uses JTA
     * transactions. This type of container commonly suits for JEE6 environment.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param jndiAddresses
     *            to be used to get references to {@link EntityManager} and
     *            {@link UserTransaction}
     * @return a fully configured JPAContainer instance
     * @return
     */
    public static <T> JPAContainer<T> makeNonCachedReadOnlyJndi(
            Class<T> entityClass, JndiAddresses jndiAddresses) {
        return makeWithEntityProvider(
                entityClass,
                new com.vaadin.addon.jpacontainer.provider.jndijta.EntityProvider<T>(
                        entityClass, jndiAddresses));
    }

    /**
     * Creates a JPAContainer that uses JNDI lookups to fetch entity manager
     * from "java:comp/env/persistence/em". Container also uses JTA
     * transactions. This type of container commonly suits for JEE6 environment.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeNonCachedJndi(Class<T> entityClass) {
        return makeWithEntityProvider(
                entityClass,
                new com.vaadin.addon.jpacontainer.provider.jndijta.MutableEntityProvider<T>(
                        entityClass));
    }

    /**
     * Creates a JPAContainer that uses JNDI lookups to fetch entity manager
     * from "java:comp/env/persistence/em". Container also uses JTA
     * transactions. This type of container commonly suits for JEE6 environment.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param jndiAddresses
     *            to be used to get references to {@link EntityManager} and
     *            {@link UserTransaction}
     * @return a fully configured JPAContainer instance
     * @return
     */
    public static <T> JPAContainer<T> makeNonCachedJndi(Class<T> entityClass,
            JndiAddresses jndiAddresses) {
        return makeWithEntityProvider(
                entityClass,
                new com.vaadin.addon.jpacontainer.provider.jndijta.MutableEntityProvider<T>(
                        entityClass, jndiAddresses));
    }

    /**
     * Creates a JPAContainer that uses JNDI lookups to fetch entity manager
     * from "java:comp/env/persistence/em". Container also uses JTA
     * transactions. This type of container commonly suits for JEE6 environment.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeJndi(Class<T> entityClass) {
        return makeWithEntityProvider(
                entityClass,
                new com.vaadin.addon.jpacontainer.provider.jndijta.CachingMutableEntityProvider<T>(
                        entityClass));
    }

    /**
     * Creates a JPAContainer that uses JNDI lookups to fetch entity manager
     * from "java:comp/env/persistence/em". Container also uses JTA
     * transactions. This type of container commonly suits for JEE6 environment.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param jndiAddresses
     *            to be used to get references to {@link EntityManager} and
     *            {@link UserTransaction}
     * @return a fully configured JPAContainer instance
     * @return
     */
    public static <T> JPAContainer<T> makeJndi(Class<T> entityClass,
            JndiAddresses jndiAddresses) {
        return makeWithEntityProvider(
                entityClass,
                new com.vaadin.addon.jpacontainer.provider.jndijta.CachingMutableEntityProvider<T>(
                        entityClass, jndiAddresses));
    }

    /**
     * Creates a JPAContainer that uses JNDI lookups to fetch entity manager
     * from "java:comp/env/persistence/em". Container also uses JTA
     * transactions. This type of container commonly suits for JEE6 environment.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @return a fully configured JPAContainer instance
     */
    public static <T> JPAContainer<T> makeBatchableJndi(Class<T> entityClass) {
        return makeWithEntityProvider(
                entityClass,
                new com.vaadin.addon.jpacontainer.provider.jndijta.CachingBatchableEntityProvider<T>(
                        entityClass));
    }

    /**
     * Creates a JPAContainer that uses JNDI lookups to fetch entity manager
     * from "java:comp/env/persistence/em". Container also uses JTA
     * transactions. This type of container commonly suits for JEE6 environment.
     * 
     * @param <T>
     *            the type of entity to be contained in the JPAContainer
     * @param entityClass
     *            the class of the entity
     * @param jndiAddresses
     *            to be used to get references to {@link EntityManager} and
     *            {@link UserTransaction}
     * @return a fully configured JPAContainer instance
     * @return
     */
    public static <T> JPAContainer<T> makeBatchableJndi(Class<T> entityClass,
            JndiAddresses jndiAddresses) {
        return makeWithEntityProvider(
                entityClass,
                new com.vaadin.addon.jpacontainer.provider.jndijta.CachingBatchableEntityProvider<T>(
                        entityClass, jndiAddresses));
    }

}
