/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider;

import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;

/**
 * Event indicating that one or more entities have been removed.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
class EntitiesRemovedEvent<T> extends EntityEvent<T> implements EntityProviderChangeEvent.EntitiesRemovedEvent<T> {

	private static final long serialVersionUID = -7174185739064265869L;

	public EntitiesRemovedEvent(MutableEntityProvider<T> entityProvider,
			T... entities) {
		super(entityProvider, entities);
	}
}
