/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider;

import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;

/**
 * Event indicating that one or more entities have been added.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
class EntitiesAddedEvent<T> extends EntityEvent<T> implements EntityProviderChangeEvent.EntitiesAddedEvent<T> {

	private static final long serialVersionUID = -7251967169102897952L;

	public EntitiesAddedEvent(MutableEntityProvider<T> entityProvider,
			T... entities) {
		super(entityProvider, entities);
	}
}
