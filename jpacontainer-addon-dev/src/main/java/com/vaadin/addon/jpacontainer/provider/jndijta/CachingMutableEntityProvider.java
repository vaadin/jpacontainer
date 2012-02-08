package com.vaadin.addon.jpacontainer.provider.jndijta;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;

/**
 * An entity provider implementation that uses JTA transactions and JPA context
 * provided by the application server. To provide a generic non EJB provider
 * this class gets references to both {@link UserTransaction} and
 * {@link EntityManager} via JNDI lookup.
 * 
 * @param <T>
 */
public class CachingMutableEntityProvider<T> extends
        CachingMutableLocalEntityProvider<T> implements JndiJtaProvider<T> {

    private JndiAddresses jndiAddresses;

    public CachingMutableEntityProvider(Class<T> entityClass) {
        super(entityClass);
        setTransactionsHandledByProvider(false);
    }

    public CachingMutableEntityProvider(Class<T> entityClass,
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
        try {
            UserTransaction utx = (UserTransaction) (new InitialContext())
                    .lookup(getJndiAddresses().getUserTransactionName());
            utx.begin();
            super.runInTransaction(operation);
            utx.commit();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public EntityManager getEntityManager() {
        try {
            InitialContext initialContext = new InitialContext();
            EntityManager lookup = (EntityManager) initialContext
                    .lookup(getJndiAddresses().getEntityManagerName());
            return lookup;
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
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
