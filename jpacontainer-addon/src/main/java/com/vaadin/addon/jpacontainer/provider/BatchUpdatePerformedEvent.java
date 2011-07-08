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

import com.vaadin.addon.jpacontainer.BatchableEntityProvider;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
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
