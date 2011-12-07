package com.vaadin.addon.jpacontainer;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.LazyInitializationException;
import org.hibernate.ejb.Ejb3Configuration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.testdata.Address;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Name;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.testdata.PersonSkill;
import com.vaadin.addon.jpacontainer.testdata.Skill;
import com.vaadin.addon.jpacontainer.util.HibernateLazyLoadingDelegate;

public class JPAContainerItemHibernateLazyLoadingTest {
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
    public void testCanSetLazyLoadingDelegate() {
        LazyLoadingDelegate delegate = createNiceMock(LazyLoadingDelegate.class);
        container.getEntityProvider().setLazyLoadingDelegate(delegate);
        assertEquals(delegate, container.getEntityProvider()
                .getLazyLoadingDelegate());
    }

    @Test
    @Ignore
    public void testHibernateMergingLazyLoadingDelegate() {
        Person detachedPerson = container.getItem(container.firstItemId())
                .getEntity();
        em.close(); // Make sure all entities really are detached.
        em = emf.createEntityManager();

        @SuppressWarnings("unchecked")
        EntityProvider<Person> epMock = createNiceMock(EntityProvider.class);
        expect(epMock.getEntityManager()).andStubReturn(em);
        replay(epMock);
        HibernateLazyLoadingDelegate delegate = new HibernateLazyLoadingDelegate();
        delegate.setEntityProvider(epMock);

        detachedPerson = delegate.ensureLazyPropertyLoaded(detachedPerson,
                "skills");
        assertEquals("Typing", detachedPerson.getSkills().iterator().next()
                .getSkill().getSkillName());
    }

    @Test
    public void testEntityEmbeddedProperty() {
        assertEquals("1124 Lion Ave",
                ((Address) firstItem.getItemProperty("address").getValue())
                        .getStreet());
    }

    @Test(expected = LazyInitializationException.class)
    @SuppressWarnings("unchecked")
    public void testEntityLazyLoading_noLazyLoader() {
        assertEquals("Typing",
                ((Set<PersonSkill>) firstItem.getItemProperty("skills")
                        .getValue()).iterator().next().getSkill()
                        .getSkillName());
    }

    @Test
    @SuppressWarnings("unchecked")
    @Ignore
    public void testEntityLazyLoading() {
        container.getEntityProvider().setLazyLoadingDelegate(
                new HibernateLazyLoadingDelegate());
        assertEquals("Typing",
                ((Set<PersonSkill>) firstItem.getItemProperty("skills")
                        .getValue()).iterator().next().getSkill()
                        .getSkillName());
    }

    @Test
    public void testEntityLazyLoading_nested() {
        em.close();
        em = emf.createEntityManager();
        container.getEntityProvider().setEntityManager(em);
        container.getEntityProvider().setLazyLoadingDelegate(
                new HibernateLazyLoadingDelegate());
        container.addNestedContainerProperty("manager.firstName");
        container.addNestedContainerProperty("manager.lastName");
        assertEquals("Jim", firstItem.getItemProperty("manager.firstName")
                .getValue());
        assertEquals("Manager", firstItem.getItemProperty("manager.lastName")
                .getValue());
    }

    @Test
    public void testSetLazyLoadedProperty() {
        firstItem.getItemProperty("skills")
                .setValue(new HashSet<PersonSkill>());
    }

    @Test
    public void testEntityLazyLoading_lazyManyToOne() {
        em.close();
        em = emf.createEntityManager();
        container.getEntityProvider().setEntityManager(em);
        container.getEntityProvider().setLazyLoadingDelegate(
                new HibernateLazyLoadingDelegate());
        assertNotNull(firstItem.getItemProperty("manager").getValue());
        assertEquals("Jim", ((Person) firstItem.getItemProperty("manager")
                .getValue()).getFirstName());
    }

    @Test
    public void testSetNestedPropertyBehindLazyLoadedProperty() {
        em.close();
        em = emf.createEntityManager();
        container.getEntityProvider().setEntityManager(em);
        container.getEntityProvider().setLazyLoadingDelegate(
                new HibernateLazyLoadingDelegate());
        container.addNestedContainerProperty("manager.firstName");
        firstItem.getItemProperty("manager.firstName").setValue("Jimmy");
        assertEquals("Jimmy", firstItem.getEntity().getManager().getFirstName());
        assertEquals("Jimmy", firstItem.getItemProperty("manager.firstName")
                .getValue());
    }
}
