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