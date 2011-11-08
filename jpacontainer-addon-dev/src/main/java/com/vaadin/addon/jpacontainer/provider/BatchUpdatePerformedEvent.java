/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider;

import java.util.Collection;
import java.util.Collections;

import com.vaadin.addon.jpacontainer.BatchableEntityProvider;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;

/**
 * Event indicating that a batch update has been performed.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class BatchUpdatePerformedEvent<T> implements
        EntityProviderChangeEvent<T> {

    private static final long serialVersionUID = -4080306860560561433L;
    private EntityProvider<T> entityProvider;

    /**
     * Creates a new <code>BatchUpdatePerformedEvent</code>.
     * 
     * @param entityProvider
     *            the batchable entity provider.
     */
    public BatchUpdatePerformedEvent(BatchableEntityProvider<T> entityProvider) {
        this.entityProvider = entityProvider;
    }

    public Collection<T> getAffectedEntities() {
        return Collections.emptyList();
    }

    public EntityProvider<T> getEntityProvider() {
        return entityProvider;
    }
}
