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
