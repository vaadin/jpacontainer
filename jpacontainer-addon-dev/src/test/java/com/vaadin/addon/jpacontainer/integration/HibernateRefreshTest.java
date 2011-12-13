package com.vaadin.addon.jpacontainer.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.ejb.Ejb3Configuration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.addon.jpacontainer.testdata.Address;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Name;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.testdata.PersonSkill;
import com.vaadin.addon.jpacontainer.testdata.Skill;
import com.vaadin.addon.jpacontainer.util.HibernateLazyLoadingDelegate;

public class HibernateRefreshTest {
    private static EntityManagerFactory emf;
    private EntityManager em;
    private JPAContainer<Person> container;
    private JPAContainerItem<Person> firstItem;

    @BeforeClass
    public static void buildEntityManagerFactory() {
        Ejb3Configuration cfg = new Ejb3Configuration()
                .setProperty("hibernate.dialect",
                        "org.hibernate.dialect.HSQLDialect")
                .setProperty("hibernate.connection.driver_class",
                        "org.hsqldb.jdbcDriver")
                .setProperty("hibernate.connection.url",
                        "jdbc:hsqldb:mem:lazyload")
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.connection.pool_size", "1")
                .setProperty("hibernate.connection.autocommit", "true")
                .setProperty("hibernate.cache.provider_class",
                        "org.hibernate.cache.HashtableCacheProvider")
                .setProperty("hibernate.hbm2ddl.auto", "create")
                .setProperty("hibernate.show_sql", "false")
                .addAnnotatedClass(Person.class)
                .addAnnotatedClass(Address.class)
                .addAnnotatedClass(EmbeddedIdPerson.class)
                .addAnnotatedClass(Name.class)
                .addAnnotatedClass(PersonSkill.class)
                .addAnnotatedClass(Skill.class);
        emf = cfg.buildEntityManagerFactory();
        persistTestDataInSeparateSession();
    }

    private static void persistTestDataInSeparateSession() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Person p = new Person();
        p.setFirstName("Bob");
        p.setLastName("Cat");
        Skill s = new Skill();
        s.setSkillName("Typing");
        em.persist(s);
        p.addSkill(s, 1);
        s = new Skill();
        s.setSkillName("Cleaning");
        em.persist(s);
        p.addSkill(s, 1);
        Address a = new Address();
        a.setStreet("1124 Lion Ave");
        p.setAddress(a);
        Person m = new Person();
        m.setFirstName("Jim");
        m.setLastName("Manager");
        p.setManager(m);
        em.persist(p);
        em.persist(m);
        em.getTransaction().commit();
        em.close();
    }

    @Before
    public void setUp() throws Exception {
        em = emf.createEntityManager();
        container = JPAContainerFactory.makeNonCached(Person.class, em);
        firstItem = (JPAContainerItem<Person>) container.getItem(container
                .firstItemId());
    }

    @Test
    public void testRefreshItem() {
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        Person p = em.find(Person.class, 1L);
        p.setFirstName("foo");
        em.merge(p);
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
        container.getEntityProvider().setEntityManager(em);
        container.refreshItem(container.firstItemId());
        assertEquals("foo", firstItem.getItemProperty("firstName").getValue());
    }

    @Test
    public void testRefreshAll() {
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        Person p = em.find(Person.class, 1L);
        p.setFirstName("bar");
        em.merge(p);
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
        container.getEntityProvider().setEntityManager(em);
        container.refresh();
        assertEquals("bar", firstItem.getItemProperty("firstName").getValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRefreshItem_addedSkill() {
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        Person p = em.find(Person.class, 1L);
        Skill s = new Skill();
        s.setSkillName("foo");
        s = em.merge(s);
        PersonSkill ps = new PersonSkill();
        ps.setLevel(1);
        ps.setSkill(s);
        ps.setPerson(p);
        ps = em.merge(ps);
        p.getSkills().add(ps);
        em.merge(p);
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
        container.getEntityProvider().setLazyLoadingDelegate(
                new HibernateLazyLoadingDelegate());
        container.getEntityProvider().setEntityManager(em);
        container.refreshItem(container.firstItemId());
        boolean found = false;
        for (PersonSkill personSkill : (Collection<PersonSkill>) firstItem
                .getItemProperty("skills").getValue()) {
            if ("foo".equals(personSkill.getSkill().getSkillName())) {
                found = true;
            }
        }
        assertTrue(found);
    }

}
