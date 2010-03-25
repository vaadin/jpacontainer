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

/**
 * Interface to be implemented by {@link EntityProvider}s that wish to notify
 * clients (in practice EntityContainers) when their contents change.
 * <p>
 * The EntityProvider should at least notify its listeners of the following events:
 *
 * <ul>
 *   <li>An entity is added to the entity provider</li>
 *   <li>An already existing entity is updated</li>
 *   <li>An entity is removed from the entity provider</li>
 *   <li>The contents of the entity provider is changed completely</li>
 * </ul>
 * <p>
 * This is useful in situations where several EntityContainers share the same
 * entity provider.
 * 
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface EntityProviderChangeNotifier<T> extends Serializable {

	/**
	 * Registers <code>listener</code> to be notified of
	 * {@link EntityProviderChangeEvent}s.
	 * 
	 * @param listener
	 *            the listener to register (must not be null).
	 */
	public void addListener(EntityProviderChangeListener<T> listener);

	/**
	 * Removes the previously registered listener.
	 * 
	 * @param listener
	 *            the listener to remove (must not be null).
	 */
	public void removeListener(EntityProviderChangeListener<T> listener);
}
