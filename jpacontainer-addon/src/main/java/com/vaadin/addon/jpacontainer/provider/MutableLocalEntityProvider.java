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
package com.vaadin.addon.jpacontainer.provider;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.EntityProviderChangeListener;
import com.vaadin.addon.jpacontainer.EntityProviderChangeNotifier;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;

/**
 * Extended version of {@link LocalEntityProvider} that provides editing
 * support. Transactions can either be handled internally by the provider, or by
 * an external container such as Spring or EJB (see the JPAContainer manual for
 * examples of how to do this). By default, transactions are handled internally
 * by invoking the transaction methods of the EntityManager.
 * <p>
 * This entity provider fires {@link EntityProviderChangeEvent}s every time an
 * entity is added, updated or deleted.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class MutableLocalEntityProvider<T> extends LocalEntityProvider<T>
		implements MutableEntityProvider<T>, EntityProviderChangeNotifier<T> {

	private static final long serialVersionUID = -6628293930338167750L;

	/**
	 * Creates a new <code>MutableLocalEntityProvider</code>. The entity manager
	 * must be set using
	 * {@link #setEntityManager(javax.persistence.EntityManager) }.
	 * 
	 * @param entityClass
	 *            the entity class (must not be null).
	 */
	public MutableLocalEntityProvider(Class<T> entityClass) {
		super(entityClass);
	}

	/**
	 * Creates a new <code>MutableLocalEntityProvider</code>.
	 * 
	 * @param entityClass
	 *            the entity class (must not be null).
	 * @param entityManager
	 *            the entity manager to use (must not be null).
	 */
	public MutableLocalEntityProvider(Class<T> entityClass,
			EntityManager entityManager) {
		super(entityClass, entityManager);
	}

	private boolean transactionsHandled = true;

	/**
	 * Specifies whether the entity provider should handle transactions itself
	 * or whether they should be handled outside (e.g. if declarative
	 * transactions are used).
	 * 
	 * @param transactionsHandled
	 *            true to handle the transactions internally, false to rely on
	 *            external transaction handling.
	 */
	public void setTransactionsHandledByProvider(boolean transactionsHandled) {
		this.transactionsHandled = transactionsHandled;
	}

	/**
	 * Returns whether the entity provider is handling transactions internally
	 * (the default) or relies on external transaction handling.
	 * 
	 * @return true if transactions are handled internally, false if not.
	 */
	public boolean isTransactionsHandledByProvider() {
		return transactionsHandled;
	}

	/**
	 * If {@link #isTransactionsHandledByProvider() } is true,
	 * <code>operation</code> will be executed inside a transaction that is
	 * commited after the operation is completed. Otherwise,
	 * <code>operation</code> will just be executed.
	 * 
	 * @param operation
	 *            the operation to run (must not be null).
	 */
	protected void runInTransaction(Runnable operation) {
		assert operation != null : "operation must not be null";
		if (isTransactionsHandledByProvider()) {
			EntityTransaction et = getEntityManager().getTransaction();
			if (et.isActive()) {
				// The transaction has been started outside of this method
				// and should also be committed/rolled back outside of
				// this method
				operation.run();
			} else {
				try {
					et.begin();
					operation.run();
					et.commit();
				} finally {
					if (et.isActive()) {
						et.rollback();
					}
				}
			}
		} else {
			operation.run();
		}
	}

	@SuppressWarnings("unchecked")
	public T addEntity(final T entity) {
		assert entity != null;
		final Object[] entityA = new Object[1];
		runInTransaction(new Runnable() {

			public void run() {
				EntityManager em = getEntityManager();
				entityA[0] = em.merge(entity);
				em.flush();
			}
		});
		T dEntity = detachEntity((T) entityA[0]);
		fireEntityProviderChangeEvent(new EntitiesAddedEvent<T>(this, dEntity));
		return dEntity;
	}

	@SuppressWarnings("unchecked")
	public void removeEntity(final Object entityId) {
		assert entityId != null;
		final Object[] entityA = new Object[1];
		runInTransaction(new Runnable() {

			public void run() {
				EntityManager em = getEntityManager();
				T entity = em.find(getEntityClassMetadata().getMappedClass(),
						entityId);
				if (entity != null) {
					em.remove(em.merge(entity));
					em.flush();
					entityA[0] = detachEntity(entity);
				}
			}
		});
		if (entityA[0] != null) {
			fireEntityProviderChangeEvent(new EntitiesRemovedEvent<T>(this,
					(T) entityA[0]));
		}
	}

	@SuppressWarnings("unchecked")
	public T updateEntity(final T entity) {
		assert entity != null : "entity must not be null";
		final Object[] entityA = new Object[1];
		runInTransaction(new Runnable() {

			public void run() {
				EntityManager em = getEntityManager();
				entityA[0] = em.merge(entity);
				em.flush();
			}
		});
		T dEntity = detachEntity((T) entityA[0]);
		fireEntityProviderChangeEvent(new EntitiesUpdatedEvent<T>(this, dEntity));
		return dEntity;
	}

	@SuppressWarnings("unchecked")
	public void updateEntityProperty(final Object entityId,
			final String propertyName, final Object propertyValue)
			throws IllegalArgumentException {
		assert entityId != null : "entityId must not be null";
		assert propertyName != null : "propertyName must not be null";
		final Object[] entityA = new Object[1];
		runInTransaction(new Runnable() {

			public void run() {
				EntityManager em = getEntityManager();
				T entity = em.find(getEntityClassMetadata().getMappedClass(),
						entityId);
				if (entity != null) {
					getEntityClassMetadata().setPropertyValue(entity,
							propertyName, propertyValue);
					em.flush();
					entityA[0] = detachEntity(entity);
				}
			}
		});
		if (entityA[0] != null) {
			fireEntityProviderChangeEvent(new EntitiesUpdatedEvent<T>(this,
					(T) entityA[0]));
		}
	}

	private LinkedList<WeakReference<EntityProviderChangeListener<T>>> listeners = new LinkedList<WeakReference<EntityProviderChangeListener<T>>>();

	public void addListener(EntityProviderChangeListener<T> listener) {
		synchronized (listeners) {
			assert listener != null : "listener must not be null";
			listeners.add(new WeakReference<EntityProviderChangeListener<T>>(
					listener));
		}
	}

	public void removeListener(EntityProviderChangeListener<T> listener) {
		synchronized (listeners) {
			assert listener != null : "listener must not be null";

			Iterator<WeakReference<EntityProviderChangeListener<T>>> it = listeners
					.iterator();
			while (it.hasNext()) {
				EntityProviderChangeListener<T> l = it.next().get();
				// also clean up old references
				if (null == l || listener.equals(l)) {
					it.remove();
				}
			}
		}
	}

	private boolean fireEntityProviderChangeEvent = true;

	/**
	 * Sets whether {@link EntityProviderChangeEvent}s should be fired by this
	 * entity provider.
	 */
	protected void setFireEntityProviderChangeEvents(boolean fireEvents) {
		this.fireEntityProviderChangeEvent = fireEvents;
	}

	/**
	 * Returns whether {@link EntityProviderChangeEvent}s should be fired by
	 * this entity provider.
	 */
	protected boolean isFireEntityProviderChangeEvent() {
		return fireEntityProviderChangeEvent;
	}

	/**
	 * Sends <code>event</code> to all registered listeners if
	 * {@link #isFireEntityProviderChangeEvent() } is true.
	 * 
	 * @param event
	 *            the event to send (must not be null).
	 */
	@SuppressWarnings("unchecked")
	protected void fireEntityProviderChangeEvent(
			final EntityProviderChangeEvent<T> event) {
		LinkedList<WeakReference<EntityProviderChangeListener<T>>> list;
		synchronized (listeners) {
			assert event != null : "event must not be null";
			if (listeners.isEmpty() && !isFireEntityProviderChangeEvent()) {
				return;
			}
			// cleanup
			Iterator<WeakReference<EntityProviderChangeListener<T>>> it = listeners
					.iterator();
			while (it.hasNext()) {
				if (null == it.next().get()) {
					it.remove();
				}
			}
			// copy list to use outside the synchronized block
			list = (LinkedList<WeakReference<EntityProviderChangeListener<T>>>) listeners
					.clone();
		}
		for (WeakReference<EntityProviderChangeListener<T>> ref : list) {
			EntityProviderChangeListener<T> listener = ref.get();
			if (null != listener) {
				listener.entityProviderChange(event);
			}
		}
	}
}
