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
