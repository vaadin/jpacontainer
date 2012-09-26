package com.vaadin.addon.jpacontainer.provider.jndijta;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

class Util {

    static EntityManager getEntityManager(JndiAddresses addresses) {
        try {
            InitialContext initialContext = new InitialContext();
            EntityManager lookup = (EntityManager) initialContext
                    .lookup(addresses.getEntityManagerName());
            return lookup;
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    static void runInJTATransaction(JndiAddresses jndiAddresses,
            Runnable operation) {
        try {
            UserTransaction utx = (UserTransaction) (new InitialContext())
                    .lookup(jndiAddresses.getUserTransactionName());
            try {
                utx.begin();
                operation.run();
                utx.commit();
            } catch (Exception e) {
                try {
                    utx.rollback();
                } catch (Exception e2) {
                    Logger.getLogger(Util.class.getName()).log(Level.WARNING,
                            "Rollback failed", e2);
                }
                throw e;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
