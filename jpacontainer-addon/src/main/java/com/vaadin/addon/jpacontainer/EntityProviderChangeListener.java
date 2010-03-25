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
 * Listener interface to be implemented by classes that want to be notified when
 * the contents of a {@link EntityProvider} is changed (e.g. entities are added or removed).
 * 
 * @see EntityProviderChangeNotifier
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface EntityProviderChangeListener<T> extends Serializable {

	/**
	 * Notifies the client that <code>event</code> has occurred.
	 * 
	 * @param event
	 *            the occurred event (never null).
	 */
	public void entityProviderChange(EntityProviderChangeEvent<T> event);
}
