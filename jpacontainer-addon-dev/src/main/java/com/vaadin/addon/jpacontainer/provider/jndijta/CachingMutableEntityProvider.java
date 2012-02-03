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

    private String userTransactionName = "java:comp/UserTransaction";
    private String entityManagerName = "java:comp/env/persistence/em";

    public CachingMutableEntityProvider(Class<T> entityClass) {
        super(entityClass);
        setTransactionsHandledByProvider(false);
    }

    @Override
    public boolean isEntitiesDetached() {
        return false;
    }

    @Override
    protected void runInTransaction(Runnable operation) {
        try {
            UserTransaction utx = (UserTransaction) (new InitialContext())
                    .lookup(userTransactionName);
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
                    .lookup(entityManagerName);
            return lookup;
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /* (non-Javadoc)
     * @see com.vaadin.addon.jpacontainer.provider.jndijta.JndiJtaProvider#getUserTransactionName()
     */
    public String getUserTransactionName() {
        return userTransactionName;
    }

    /* (non-Javadoc)
     * @see com.vaadin.addon.jpacontainer.provider.jndijta.JndiJtaProvider#setUserTransactionName(java.lang.String)
     */
    public void setUserTransactionName(String userTransactionName) {
        this.userTransactionName = userTransactionName;
    }

    /* (non-Javadoc)
     * @see com.vaadin.addon.jpacontainer.provider.jndijta.JndiJtaProvider#getEntityManagerName()
     */
    public String getEntityManagerName() {
        return entityManagerName;
    }

    /* (non-Javadoc)
     * @see com.vaadin.addon.jpacontainer.provider.jndijta.JndiJtaProvider#setEntityManagerName(java.lang.String)
     */
    public void setEntityManagerName(String entityManagerName) {
        this.entityManagerName = entityManagerName;
    }

}
