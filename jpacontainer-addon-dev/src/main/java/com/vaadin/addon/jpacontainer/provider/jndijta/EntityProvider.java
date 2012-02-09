package com.vaadin.addon.jpacontainer.provider.jndijta;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.vaadin.addon.jpacontainer.provider.LocalEntityProvider;

/**
 * An entity provider implementation that uses JTA transactions and JPA context
 * provided by the application server. To provide a generic non EJB provider
 * this class gets references to both {@link UserTransaction} and
 * {@link EntityManager} via JNDI lookup.
 * 
 * @param <T>
 */
public class EntityProvider<T> extends
        LocalEntityProvider<T> implements JndiJtaProvider<T> {

    private JndiAddresses jndiAddresses;

    public EntityProvider(Class<T> entityClass) {
        super(entityClass);
    }

    public EntityProvider(Class<T> entityClass, JndiAddresses jndiAddresses) {
        this(entityClass);
        setJndiAddresses(jndiAddresses);
    }

    @Override
    public boolean isEntitiesDetached() {
        return false;
    }
    
    @Override
    public EntityManager getEntityManager() {
        return Util.getEntityManager(getJndiAddresses());
    }

    public void setJndiAddresses(JndiAddresses addresses) {
        this.jndiAddresses = addresses;
    }

    public JndiAddresses getJndiAddresses() {
        if (jndiAddresses == null) {
            return JndiAddresses.DEFAULTS;
        }
        return jndiAddresses;
    }

}
