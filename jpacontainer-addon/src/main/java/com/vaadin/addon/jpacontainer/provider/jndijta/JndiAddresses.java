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

public interface JndiAddresses {

    public static final JndiAddresses DEFAULTS = new JndiAddressesImpl(
            "java:comp/UserTransaction", "java:comp/env/persistence/em");

    /**
     * @return the JNDI name used to fetch JTA transaction
     */
    public abstract String getUserTransactionName();

    /**
     * @return the JNDI name used to fetch entity manager
     */
    public abstract String getEntityManagerName();

}
