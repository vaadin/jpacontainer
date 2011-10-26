/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Base class for {@link EntityProviderChangeEvent}s.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
abstract class EntityEvent<T> implements EntityProviderChangeEvent<T>,
		Serializable {

	private static final long serialVersionUID = -3703337782681273703L;
	private Collection<T> entities;
	private MutableEntityProvider<T> entityProvider;

	public EntityEvent(MutableEntityProvider<T> entityProvider,
			T... entities) {
		this.entityProvider = entityProvider;
		if (entities.length == 0) {
			this.entities = Collections.emptyList();
		} else {
			this.entities = Collections.unmodifiableCollection(Arrays.asList(
					entities));
		}
	}

	public Collection<T> getAffectedEntities() {
		return entities;
	}

	public EntityProvider<T> getEntityProvider() {
		return entityProvider;
	}
}
