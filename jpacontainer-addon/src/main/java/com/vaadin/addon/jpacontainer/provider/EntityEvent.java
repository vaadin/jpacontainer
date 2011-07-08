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
