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

import javax.persistence.EntityManager;

import com.vaadin.addon.jpacontainer.EntityManagerProvider;

/**
 * Provides the entity manager for all JPAContainers in the LazyHibernate
 * example. The entity manager is created and set for each request by the
 * {@link LazyHibernateServletFilter} servlet filter.
 * 
 * @author Jonatan Kronqvist / Vaadin Ltd
 */
public class LazyHibernateEntityManagerProvider implements
        EntityManagerProvider {
    private static ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();

    @Override
    public EntityManager getEntityManager() {
        return entityManagerThreadLocal.get();
    }

    public static void setCurrentEntityManager(EntityManager em) {
        entityManagerThreadLocal.set(em);
    }
}
