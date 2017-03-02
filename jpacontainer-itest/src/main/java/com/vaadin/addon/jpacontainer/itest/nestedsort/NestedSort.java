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
package com.vaadin.addon.jpacontainer.itest.nestedsort;

import javax.persistence.EntityManager;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.itest.nestedsort.domain.Base;
import com.vaadin.addon.jpacontainer.itest.nestedsort.domain.Nested;
import com.vaadin.server.VaadinRequest;
import com.vaadin.v7.ui.Table;
import com.vaadin.ui.UI;

public class NestedSort extends UI {
    static {
        generateData();
    }

    private static void generateData() {
        EntityManager em = JPAContainerFactory
                .createEntityManagerForPersistenceUnit("nestedsort");

        em.getTransaction().begin();

        persistBaseWithNested("foo", em);
        persistBaseWithNested("bar", em);
        persistBaseWithNested("baz", em);
        em.persist(new Base("none"));

        em.getTransaction().commit();
        em.close();
    }

    private static void persistBaseWithNested(String name, EntityManager em) {
        Base b = new Base(name);
        Nested n = new Nested(name + "-nested");
        b.setNested(n);
        em.persist(n);
        em.persist(b);
    }

    @Override
    protected void init(VaadinRequest request) {
        JPAContainer<Base> baseContainer = JPAContainerFactory.make(Base.class,
                "nestedsort");
        baseContainer.addNestedContainerProperty("nested.name");
        Table t = new Table(null, baseContainer);
        setContent(t);
    }
}
