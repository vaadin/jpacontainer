/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
