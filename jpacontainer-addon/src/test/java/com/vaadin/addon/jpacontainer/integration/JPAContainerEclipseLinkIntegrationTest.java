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
package com.vaadin.addon.jpacontainer.integration;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.testdata.Skill;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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

    static class TestProxyListener implements ValueChangeListener {
        transient ValueChangeListener vcl;

        public TestProxyListener() {
        }

        public void valueChange(ValueChangeEvent event) {
            if (vcl != null) {
                vcl.valueChange(event);
            }
        }
    }

    /**
     * The container must be serializable as vaadin apps may end up written to
     * disc in case e.g. app server runs out of memory.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSerialization() throws FileNotFoundException, IOException,
            ClassNotFoundException {
        JPAContainer<Skill> c = JPAContainerFactory.make(Skill.class, em);
        Object firstItemId = c.firstItemId();
        EntityItem<Skill> item = c.getItem(firstItemId);
        TestProxyListener testProxyListener = new TestProxyListener();
        item.addListener(testProxyListener);

        FileOutputStream fos = null;
        ObjectOutputStream out = null;

        // Write container and one item to disk
        File file = File.createTempFile("jpasertest", null);
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(c);
        out.writeObject(item);
        out.writeObject(testProxyListener);
        out.close();

        // read them up and see if they are functional

        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        JPAContainer<Skill> readContainer = (JPAContainer<Skill>) ois
                .readObject();
        EntityItem<Skill> readItem = (EntityItem<Skill>) ois.readObject();
        TestProxyListener readListener = (TestProxyListener) ois.readObject();

        // Eclipse link entity manager cannot be serialized (localprovider marks
        // it as transient), reset it from then  non serialized
        // If "Session(aka EntityMananger) per request pattern is used" (see
        // EntityManagerPerRequestHelper), similar
        // thing happens: http session read from disc, entitymanager reset in
        // request start listener.

        readContainer.getEntityProvider().setEntityManager(em);

        EntityItemProperty itemProperty = readItem.getItemProperty("skillName");

        final int[] called = new int[] { 0 };

        ValueChangeListener l = new ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                called[0]++;
            }
        };

        // set one new
        itemProperty.addListener(l);
        // and also ensure the persisted works
        readListener.vcl = l;

        // set value via new instance, listeners in deserialized property should
        // be fired
        EntityItem<Skill> item2 = readContainer.getItem(firstItemId);
        item2.getItemProperty("skillName").setValue("foo2)");

        assertEquals(2, called[0]);

    }
}
