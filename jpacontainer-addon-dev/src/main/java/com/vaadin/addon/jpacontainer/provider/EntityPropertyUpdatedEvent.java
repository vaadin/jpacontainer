/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider;

import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;

/**
 * Event indicating that one or more entities have updated their property.
 * 
 * @since 2.0
 */
class EntityPropertyUpdatedEvent<T> extends EntityEvent<T> implements
        EntityProviderChangeEvent.EntityPropertyUpdatedEvent<T> {

    private static final long serialVersionUID = -7472733082448613781L;
    private String propertyId;

    public EntityPropertyUpdatedEvent(MutableEntityProvider<T> entityProvider,
            String propertyId, T... entities) {
        super(entityProvider, entities);
        this.propertyId = propertyId;
    }

    public String getPropertyId() {
        return propertyId;
    }
}
