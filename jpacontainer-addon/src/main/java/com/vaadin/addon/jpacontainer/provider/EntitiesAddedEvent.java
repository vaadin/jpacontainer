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

import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;

/**
 * Event indicating that one or more entities have been added.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
class EntitiesAddedEvent<T> extends EntityEvent<T> implements EntityProviderChangeEvent.EntitiesAddedEvent<T> {

	private static final long serialVersionUID = -7251967169102897952L;

	public EntitiesAddedEvent(MutableEntityProvider<T> entityProvider,
			T... entities) {
		super(entityProvider, entities);
	}
}
