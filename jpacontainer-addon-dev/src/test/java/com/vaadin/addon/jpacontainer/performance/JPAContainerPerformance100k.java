package com.vaadin.addon.jpacontainer.performance;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.testdata.Skill;

public class JPAContainerPerformance100k {

    private static int NUM_ENTITIES = 100000;

    private static EntityManagerFactory emf = Persistence
            .createEntityManagerFactory("eclipselink-in-memory");
    private EntityManager em;

    static {
        long time = System.currentTimeMillis();
        EntityManager em = emf.createEntityManager();
        EntityTransaction t = em.getTransaction();
        t.begin();
        em.createQuery("DELETE FROM Skill a").executeUpdate();
        t.commit();

        em.setFlushMode(FlushModeType.COMMIT);
        // Create a bunch of test Skills
        t = em.getTransaction();
        t.begin();
        for (int i = 0; i < NUM_ENTITIES; i++) {
            Skill s = new Skill();
            s.setSkillName("Skill " + i);
            em.persist(s);
        }
        t.commit();
        em.close();
        System.out.println("Database filled in "
                + (System.currentTimeMillis() - time) + " ms");
    }

    @Before
    public void setUp() {
        em = emf.createEntityManager();
    }

    @After
    public void tareDown() {
        em.close();
    }

    /* The test fails if it takes more than 18(!!) seconds to run */
    @Test(timeout = 18000)
    public void testJPAContainerWithPagelength75() {
        long t = System.currentTimeMillis();
        JPAContainer<Skill> c = JPAContainerFactory.makeNonCached(Skill.class,
                em);

        Object id = c.getIdByIndex(0);
        for (int i = 0; i < 75; i++) {
            id = c.nextItemId(id);
        }
        System.out.println("Time (ms): " + (System.currentTimeMillis() - t));
    }

    /* The test fails if it takes more than 20(!!) seconds to run */
    @Test(timeout = 20000)
    public void testJPAContainerWithPagelength100() {
        long t = System.currentTimeMillis();
        JPAContainer<Skill> c = JPAContainerFactory.makeNonCached(Skill.class,
                em);

        Object id = c.getIdByIndex(0);
        for (int i = 0; i < 100; i++) {
            id = c.nextItemId(id);
        }
        System.out.println("Time (ms): " + (System.currentTimeMillis() - t));
    }

    /* The test fails if it takes more than 5(!!) seconds to run */
    @Test(timeout = 5000)
    public void testJPAContainerWithPagelength20() {
        long t = System.currentTimeMillis();
        JPAContainer<Skill> c = JPAContainerFactory.makeNonCached(Skill.class,
                em);

        Object id = c.getIdByIndex(0);
        for (int i = 0; i < 20; i++) {
            id = c.nextItemId(id);
        }
        System.out.println("Time (ms): " + (System.currentTimeMillis() - t));
    }

}
