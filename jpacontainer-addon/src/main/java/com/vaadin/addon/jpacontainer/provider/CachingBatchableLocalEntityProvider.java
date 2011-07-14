/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider;

import com.vaadin.addon.jpacontainer.BatchableEntityProvider;
import com.vaadin.addon.jpacontainer.BatchableEntityProvider.BatchUpdateCallback;
import javax.persistence.EntityManager;

/**
 * A very simple implementation of {@link BatchableEntityProvider} with
 * caching support that simply
 * passes itself to the {@link BatchUpdateCallback}. No data consistency checks
 * are performed.
 *
 * @see CachingMutableLocalEntityProvider
 * @see BatchableLocalEntityProvider
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class CachingBatchableLocalEntityProvider<T> extends
		CachingMutableLocalEntityProvider<T> implements BatchableEntityProvider<T> {

	private static final long serialVersionUID = 9174163487778140520L;

	/**
	 * Creates a new <code>CachingBatchableLocalEntityProvider</code>. The entity
	 * manager must be set using
	 * {@link #setEntityManager(javax.persistence.EntityManager) }.
	 * 
	 * @param entityClass
	 *            the entity class (must not be null).
	 */
	public CachingBatchableLocalEntityProvider(Class<T> entityClass) {
		super(entityClass);
	}

	/**
	 * Creates a new <code>CachingBatchableLocalEntityProvider</code>.
	 * 
	 * @param entityClass
	 *            the entity class (must not be null).
	 * @param entityManager
	 *            the entity manager to use (must not be null).
	 */
	public CachingBatchableLocalEntityProvider(Class<T> entityClass,
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
					callback.batchUpdate(CachingBatchableLocalEntityProvider.this);
				}
			});
		} finally {
			setFireEntityProviderChangeEvents(true);
		}
		fireEntityProviderChangeEvent(new BatchUpdatePerformedEvent<T>(this));
	}
}
