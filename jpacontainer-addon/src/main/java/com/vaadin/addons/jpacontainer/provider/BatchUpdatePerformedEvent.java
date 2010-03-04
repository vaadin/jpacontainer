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
package com.vaadin.addons.jpacontainer.provider;

import com.vaadin.addons.jpacontainer.BatchableEntityProvider;
import com.vaadin.addons.jpacontainer.EntityProvider;
import com.vaadin.addons.jpacontainer.EntityProviderChangeEvent;
import java.util.Collection;
import java.util.Collections;

/**
 * Event indicating that a batch update has been performed.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class BatchUpdatePerformedEvent<T> implements
		EntityProviderChangeEvent<T> {

	private static final long serialVersionUID = -4080306860560561433L;
	private EntityProvider<T> entityProvider;

	/**
	 * Creates a new <code>BatchUpdatePerformedEvent</code>.
	 *
	 * @param entityProvider the batchable entity provider.
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
