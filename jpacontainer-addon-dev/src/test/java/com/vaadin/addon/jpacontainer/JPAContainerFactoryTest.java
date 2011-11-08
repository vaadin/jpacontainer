package com.vaadin.addon.jpacontainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.LocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.testdata.Skill;

public class JPAContainerFactoryTest {
    private static EntityManagerFactory emf = Persistence
            .createEntityManagerFactory("eclipselink-in-memory");

    private EntityManager entityManager;

    @Before
    public void setUp() {
        entityManager = emf.createEntityManager();
    }

    @Test
    public void testCreateJPAContainer() {
        JPAContainer<Person> c = JPAContainerFactory.make(Person.class,
                entityManager);
        assertNotNull(c);
    }

    @Test
    public void testCreateJPAContainerHasCorrectEntityProvider() {
        JPAContainer<Person> c = JPAContainerFactory.make(Person.class,
                entityManager);
        assertEquals(CachingMutableLocalEntityProvider.class, c
                .getEntityProvider().getClass());
        EntityManager entityManagerOfProvider = ((CachingMutableLocalEntityProvider<?>) c
                .getEntityProvider()).getEntityManager();
        assertNotNull(entityManagerOfProvider);
        assertEquals(entityManager, entityManagerOfProvider);
    }

    @Test
    public void testCreateJPAContainerUsingPersistenceUnitNameHasCorrectEntityProvider() {
        JPAContainer<Person> c = JPAContainerFactory.make(Person.class,
                "eclipselink-in-memory");
        assertNotNull(c);
        assertEquals(CachingMutableLocalEntityProvider.class, c
                .getEntityProvider().getClass());
        EntityManager entityManagerOfProvider = ((CachingMutableLocalEntityProvider<?>) c
                .getEntityProvider()).getEntityManager();
        assertNotNull(entityManagerOfProvider);
    }

    @Test
    public void testCreateJPAContainerUsingPersistenceUnitNameReusesEntityManagerFactory() {
        JPAContainer<Person> c = JPAContainerFactory.make(Person.class,
                "eclipselink-in-memory");
        EntityManager em = ((CachingMutableLocalEntityProvider<?>) c
                .getEntityProvider()).getEntityManager();

        JPAContainer<Skill> c2 = JPAContainerFactory.make(Skill.class,
                "eclipselink-in-memory");
        EntityManager em2 = ((CachingMutableLocalEntityProvider<?>) c2
                .getEntityProvider()).getEntityManager();

        assertEquals(em.getEntityManagerFactory(),
                em2.getEntityManagerFactory());
    }

    @Test
    public void testCreateReadOnlyJPAContainer() {
        JPAContainer<Person> c = JPAContainerFactory.makeReadOnly(Person.class,
                entityManager);
        assertEquals(CachingLocalEntityProvider.class, c.getEntityProvider()
                .getClass());
    }

    @Test
    public void testCreateReadOnlyJPAContainerWithPersistenceUnitName() {
        JPAContainer<Person> c = JPAContainerFactory.makeReadOnly(Person.class,
                "eclipselink-in-memory");
        assertEquals(CachingLocalEntityProvider.class, c.getEntityProvider()
                .getClass());
    }

    @Test
    public void testCreateBatchableJPAContainer() {
        JPAContainer<Person> c = JPAContainerFactory.makeBatchable(
                Person.class, entityManager);
        assertEquals(BatchableLocalEntityProvider.class, c.getEntityProvider()
                .getClass());
    }

    @Test
    public void testCreateBatchableJPAContainerWithPersistenceUnitName() {
        JPAContainer<Person> c = JPAContainerFactory.makeBatchable(
                Person.class, "eclipselink-in-memory");
        assertEquals(BatchableLocalEntityProvider.class, c.getEntityProvider()
                .getClass());
    }

    @Test
    public void testCreateNonCachedJPAContainer() {
        JPAContainer<Person> c = JPAContainerFactory.makeNonCached(
                Person.class, entityManager);
        assertEquals(MutableLocalEntityProvider.class, c.getEntityProvider()
                .getClass());
    }

    @Test
    public void testCreateNonCachedJPAContainerWithPersistenceUnitName() {
        JPAContainer<Person> c = JPAContainerFactory.makeNonCached(
                Person.class, "eclipselink-in-memory");
        assertEquals(MutableLocalEntityProvider.class, c.getEntityProvider()
                .getClass());
    }

    @Test
    public void testCreateNonCachedReadOnlyJPAContainer() {
        JPAContainer<Person> c = JPAContainerFactory.makeNonCachedReadOnly(
                Person.class, entityManager);
        assertEquals(LocalEntityProvider.class, c.getEntityProvider()
                .getClass());
    }

    @Test
    public void testCreateNonCachedReadOnlyJPAContainerWithPersistenceUnitName() {
        JPAContainer<Person> c = JPAContainerFactory.makeNonCachedReadOnly(
                Person.class, "eclipselink-in-memory");
        assertEquals(LocalEntityProvider.class, c.getEntityProvider()
                .getClass());
    }
}
