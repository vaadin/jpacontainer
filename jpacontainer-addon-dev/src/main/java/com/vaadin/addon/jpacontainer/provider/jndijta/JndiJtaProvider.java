package com.vaadin.addon.jpacontainer.provider.jndijta;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

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
     * @return settings that are used for JNDI lookups for
     *         {@link UserTransaction} and {@link EntityManager}
     */
    public abstract JndiAddresses getJndiAddresses();

    /**
     * @param addresses
     *            JNDI addresses that the provider should uses to lookup
     *            {@link UserTransaction} and {@link EntityManager}
     */
    public abstract void setJndiAddresses(JndiAddresses addresses);

}