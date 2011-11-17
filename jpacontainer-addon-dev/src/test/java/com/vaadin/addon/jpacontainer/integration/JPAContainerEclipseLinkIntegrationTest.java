package com.vaadin.addon.jpacontainer.integration;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.testdata.Skill;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Or;

public class JPAContainerEclipseLinkIntegrationTest {
    private static final int NUM_SKILLS = 50;
    private EntityManagerFactory emf = Persistence
            .createEntityManagerFactory("eclipselink-in-memory");
    private EntityManager em;

    @Before
    public void setUp() {
        em = emf.createEntityManager();
        EntityTransaction t = em.getTransaction();
        t.begin();
        em.createQuery("DELETE FROM Skill a").executeUpdate();
        t.commit();

        // Create a bunch of test Skills
        t = em.getTransaction();
        t.begin();
        for (int i = 0; i < NUM_SKILLS; i++) {
            Skill s = new Skill();
            s.setSkillName("Skill " + i);
            em.persist(s);
        }
        t.commit();
    }

    @After
    public void tearDown() {
        em.close();
    }

    @Test
    public void testSizeEqualsNumberOfRowsInDatabase() {
        JPAContainer<Skill> c = JPAContainerFactory.make(Skill.class, em);
        assertEquals(NUM_SKILLS, c.size());
    }

    @Test
    public void testFilteredContainerReportsCorrectSize() {
        JPAContainer<Skill> c = JPAContainerFactory.make(Skill.class, em);
        c.addContainerFilter(new Equal("skillName", "Skill 1"));
        assertEquals(1, c.size());
    }

    @Test
    public void testJunctionFilterReportsCorrectSize() {
        JPAContainer<Skill> c = JPAContainerFactory.make(Skill.class, em);
        c.addContainerFilter(new Or(new Equal("skillName", "Skill 1"),
                new Equal("skillName", "Skill 2")));
        assertEquals(2, c.size());
    }
}
