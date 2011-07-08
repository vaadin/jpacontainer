/*
 * JPAContainer
 * Copyright (C) 2010-2011 Oy Vaadin Ltd
 *
 * This program is available both under Commercial Vaadin Add-On
 * License 2.0 (CVALv2) and under GNU Affero General Public License (version
 * 3 or later) at your option.
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and CVALv2 along with this program.  If not, see
 * <http://www.gnu.org/licenses/> and <http://vaadin.com/license/cval-2.0>.
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
