package com.vaadin.addon.jpacontainer;

import javax.persistence.EntityManager;

/**
 * This interface is used by EntityProvider to find the correct EntityManager to
 * use.
 * 
 * @author Jonatan Kronqvist / Vaadin Ltd
 */
public interface EntityManagerProvider {
    /**
     * Gets the entity manager.
     * 
     * @return the entity manager, or null if none has been specified.
     */
    EntityManager getEntityManager();
}
