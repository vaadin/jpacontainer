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

	/**
	 * Event indicating that one or more entities have been added to the entity provider.
	 *
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	public interface EntitiesAddedEvent<T> extends EntityProviderChangeEvent<T> {
		// No additional methods
	}

	/**
	 * Event indicating that one or more entities have been updated in the entity provider.
	 *
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	public interface EntitiesUpdatedEvent<T> extends EntityProviderChangeEvent<T> {
		// No additional methods
	}

	/**
	 * Event indicating that one or more entities have been removed from the entity provider.
	 *
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	public interface EntitiesRemovedEvent<T> extends EntityProviderChangeEvent<T> {
		// No additional methods
	}
}
