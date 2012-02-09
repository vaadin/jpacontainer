package com.vaadin.addon.jpacontainer.provider.jndijta;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.vaadin.addon.jpacontainer.provider.CachingBatchableLocalEntityProvider;

/**
 * An entity provider implementation that uses JTA transactions and JPA context
 * provided by the application server. To provide a generic non EJB provider
 * this class gets references to both {@link UserTransaction} and
 * {@link EntityManager} via JNDI lookup.
 * 
 * @param <T>
 */
public class CachingBatchableEntityProvider<T> extends
        CachingBatchableLocalEntityProvider<T> implements JndiJtaProvider<T> {

    private JndiAddresses jndiAddresses;

    public CachingBatchableEntityProvider(Class<T> entityClass) {
        super(entityClass);
        setTransactionsHandledByProvider(false);
    }
    
    public CachingBatchableEntityProvider(Class<T> entityClass, JndiAddresses addresses) {
        this(entityClass);
        setJndiAddresses(addresses);
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
