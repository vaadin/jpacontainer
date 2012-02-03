package com.vaadin.addon.jpacontainer.provider.jndijta;

import com.vaadin.addon.jpacontainer.EntityProvider;

/**
 * A custom type of EntityProvider suitable for JEE6 environment with JTA and
 * server provided JPA context. This kind of provider uses JNDI to lookup
 * EntityManager and UserTransaction.
 * 
 * @param <T>
 */
public interface JndiJtaProvider<T> extends EntityProvider<T> {

    /**
     * @return the JNDI name used to fetch transaction implementation
     */
    public abstract String getUserTransactionName();

    public abstract void setUserTransactionName(String userTransactionName);

    /**
     * @return the JNDI name used to fetch entity manager
     */
    public abstract String getEntityManagerName();

    public abstract void setEntityManagerName(String entityManagerName);

}