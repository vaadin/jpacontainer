package com.vaadin.addon.jpacontainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.LocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;

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
    private static Map<String, EntityManagerFactory> puToEmfMap = Collections.synchronizedMap(new HashMap<String, EntityManagerFactory>());

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
    public synchronized static EntityManager createEntityManagerForPersistenceUnit(String name) {
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
     * {@link BatchableLocalEntityProvider}. This method should be used if you
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
    public static <T> JPAContainer<T> makeBatchable(Class<T> entityClass,
            EntityManager entityManager) {
        return makeWithEntityProvider(entityClass,
                new BatchableLocalEntityProvider<T>(entityClass, entityManager));
    }

    /**
     * Creates a new instance of JPAContainer backed by a
     * {@link BatchableLocalEntityProvider}. This method can be used if you do
     * not know and do not need/want to know the instance of
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

}
