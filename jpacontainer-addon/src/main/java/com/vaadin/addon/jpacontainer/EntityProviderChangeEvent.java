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
package com.vaadin.addon.jpacontainer;

import java.io.Serializable;
import java.util.Collection;

/**
 * Event indicating that the contents of a {@link EntityProvider} has been changed (e.g.
 * entities have been added or removed).
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface EntityProviderChangeEvent<T> extends Serializable {

	/**
	 * Gets the entity provider whose contents has been changed.
	 * 
	 * @return the entity manager (never null).
	 */
	public EntityProvider<T> getEntityProvider();

	/**
	 * Gets the affected entities, if supported by the implementation. If entities
	 * have been modified, this collection may contain all modified entities,
	 * if entities have been added, this collection may contain all added entities,
	 * etc. If the number of changed entities is very large, e.g. due to a major change
	 * in the entire data source, the collection may be empty.
	 * 
	 * @return an unmodifiable collection of affected entities (never null, but
	 *         may be empty).
	 */
	public Collection<T> getAffectedEntities();
}
