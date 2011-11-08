/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider;

import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;

/**
 * Event indicating that one or more entities have been updated.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
class EntitiesUpdatedEvent<T> extends EntityEvent<T> implements
        EntityProviderChangeEvent.EntitiesUpdatedEvent<T> {

    private static final long serialVersionUID = -7472733082448613781L;

    public EntitiesUpdatedEvent(MutableEntityProvider<T> entityProvider,
            T... entities) {
        super(entityProvider, entities);
    }
}
