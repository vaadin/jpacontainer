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
package com.vaadin.addon.jpacontainer.itest.lazyhibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;

import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.itest.lazyhibernate.domain.LazyPerson;
import com.vaadin.addon.jpacontainer.itest.lazyhibernate.domain.LazySkill;

public class LazyHibernateDataGenerator {

    final static String[] snames = { "Typing", "Reading", "Writing", "Walking" };
    final static String[] fnames = { "Peter", "Alice", "Joshua", "Mike",
            "Olivia", "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik",
            "Rene", "Lisa", "Marge" };
    final static String[] lnames = { "Smith", "Gordon", "Simpson", "Brown",
            "Clavel", "Simons", "Verne", "Scott", "Allison", "Gates",
            "Rowling", "Barks", "Ross", "Schneider", "Tate" };

    public static void create() {

        EntityManager em = JPAContainerFactory
                .createEntityManagerForPersistenceUnit("lazyhibernate");

        em.getTransaction().begin();
        Random r = new Random(0);
        LazyPerson manager = new LazyPerson();
        manager.setFirstName("Manny");
        manager.setLastName("Manager");
        LazySkill managingSkill = new LazySkill();
        managingSkill.setName("Managing");
        manager.setSkills(new HashSet<LazySkill>(Arrays.asList(managingSkill)));
        em.persist(manager);

        ArrayList<LazySkill> skills = new ArrayList<LazySkill>();
        skills.add(managingSkill);
        for (String sname : snames) {
            LazySkill skill = new LazySkill();
            skill.setName(sname);
            em.persist(skill);
            skills.add(skill);
        }

        for (int i = 0; i < 100; i++) {
            LazyPerson p = new LazyPerson();
            p.setFirstName(fnames[r.nextInt(fnames.length)]);
            p.setLastName(lnames[r.nextInt(lnames.length)]);
            Set<LazySkill> s = new HashSet<LazySkill>();
            s.add(skills.get(r.nextInt(snames.length + 1)));
            p.setManager(manager);
            p.setSkills(s);
            em.persist(p);
        }
        em.flush();
        em.getTransaction().commit();
        em.close();
    }
}
