package com.vaadin.addon.jpacontainer.provider.jndijta;

public class JndiAddressesImpl implements JndiAddresses {

    private final String userTransactionName;
    private final String entityManagerName;

    /**
     * @param trxName
     *            the JNDI name to be used to lookup UserTransaction object
     * @param entityManagerName
     *            the JNDI name to be used to lookup entitymanager
     */
    public JndiAddressesImpl(String trxName, String entityManagerName) {
        this.userTransactionName = trxName;
        this.entityManagerName = entityManagerName;
    }

    public String getUserTransactionName() {
        return userTransactionName;
    }

    public String getEntityManagerName() {
        return entityManagerName;
    }

}
