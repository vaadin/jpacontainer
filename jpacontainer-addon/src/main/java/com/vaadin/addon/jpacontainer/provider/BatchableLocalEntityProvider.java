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
import com.vaadin.addon.jpacontainer.BatchableEntityProvider.BatchUpdateCallback;
import javax.persistence.EntityManager;

/**
 * A very simple implementation of {@link BatchableEntityProvider} that simply
 * passes itself to the {@link BatchUpdateCallback}. No data consistency checks
 * are performed. On some occations, this can cause trouble in real applications.
 * Please see the {@link BatchableEntityProvider} interface JavaDocs and the
 * JPAContainer manual for more information.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class BatchableLocalEntityProvider<T> extends
		MutableLocalEntityProvider<T> implements BatchableEntityProvider<T> {

	private static final long serialVersionUID = 9174163487778140520L;

	/**
	 * Creates a new <code>BatchableLocalEntityProvider</code>. The entity
	 * manager must be set using
	 * {@link #setEntityManager(javax.persistence.EntityManager) }.
	 * 
	 * @param entityClass
	 *            the entity class (must not be null).
	 */
	public BatchableLocalEntityProvider(Class<T> entityClass) {
		super(entityClass);
	}

	/**
	 * Creates a new <code>BatchableLocalEntityProvider</code>.
	 * 
	 * @param entityClass
	 *            the entity class (must not be null).
	 * @param entityManager
	 *            the entity manager to use (must not be null).
	 */
	public BatchableLocalEntityProvider(Class<T> entityClass,
			EntityManager entityManager) {
		super(entityClass, entityManager);
	}

	public void batchUpdate(final BatchUpdateCallback<T> callback)
			throws UnsupportedOperationException {
		assert callback != null : "callback must not be null";
		setFireEntityProviderChangeEvents(false);
		try {
			runInTransaction(new Runnable() {

				public void run() {
					callback.batchUpdate(BatchableLocalEntityProvider.this);
				}
			});
		} finally {
			setFireEntityProviderChangeEvents(true);
		}
		fireEntityProviderChangeEvent(new BatchUpdatePerformedEvent<T>(this));
	}
}
