/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class EntityEvent<T> implements EntityProviderChangeEvent<T>,
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
