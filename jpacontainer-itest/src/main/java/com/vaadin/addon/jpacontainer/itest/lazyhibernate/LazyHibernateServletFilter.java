package com.vaadin.addon.jpacontainer.itest.lazyhibernate;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A simple servlet filter that implements the "entity manager per request"
 * pattern by creating a new {@link javax.persistence.EntityManager} before the
 * processing of each request and passes it to the
 * {@link LazyHibernateEntityManagerProvider}, which is used by JPAContainers to
 * find the active entity manager.
 * 
 * @author Jonatan Kronqvist / Vaadin Ltd
 */
public class LazyHibernateServletFilter implements Filter {

    private EntityManagerFactory entityManagerFactory;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        entityManagerFactory = Persistence
                .createEntityManagerFactory("lazyhibernate");
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
            ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            // Create and set the entity manager
            LazyHibernateEntityManagerProvider
                    .setCurrentEntityManager(entityManagerFactory
                            .createEntityManager());
            // Handle the request
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            // Reset the entity manager
            LazyHibernateEntityManagerProvider.setCurrentEntityManager(null);
        }
    }

    @Override
    public void destroy() {
        entityManagerFactory = null;
    }
}
