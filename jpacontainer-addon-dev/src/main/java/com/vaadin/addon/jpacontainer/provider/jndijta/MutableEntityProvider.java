package com.vaadin.addon.jpacontainer.provider.jndijta;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;

/**
 * An entity provider implementation that uses JTA transactions and JPA context
 * provided by the application server. To provide a generic non EJB provider
 * this class gets references to both {@link UserTransaction} and
 * {@link EntityManager} via JNDI lookup.
 * 
 * @param <T>
 */
public class MutableEntityProvider<T> extends MutableLocalEntityProvider<T>
        implements JndiJtaProvider<T> {

    private JndiAddresses jndiAddresses;

    public MutableEntityProvider(Class<T> entityClass) {
        super(entityClass);
        setTransactionsHandledByProvider(false);
    }

    public MutableEntityProvider(Class<T> entityClass,
            JndiAddresses jndiAddresses) {
        this(entityClass);
        setJndiAddresses(jndiAddresses);
    }

    @Override
    public boolean isEntitiesDetached() {
        return false;
    }

    @Override
    protected void runInTransaction(Runnable operation) {
        Util.runInJTATransaction(getJndiAddresses(), operation);
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
