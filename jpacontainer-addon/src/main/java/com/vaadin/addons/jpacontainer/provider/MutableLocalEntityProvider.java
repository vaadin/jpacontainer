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
package com.vaadin.addons.jpacontainer.provider;

import com.vaadin.addons.jpacontainer.EntityProvider;
import com.vaadin.addons.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addons.jpacontainer.EntityProviderChangeListener;
import com.vaadin.addons.jpacontainer.EntityProviderChangeNotifier;
import com.vaadin.addons.jpacontainer.MutableEntityProvider;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Extended version of {@link LocalEntityProvider} that provides editing
 * support. In addition to all the features of the
 * <code>LocalEntityProvider<code>, it supports
 * both internal and external transaction handling.
 *
 * TODO Improve documentation
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

	private boolean transactionsHandled = false;

	/**
	 * Specifies whether the entity provider should handle transactions itself
	 * or whether they should be handled outside (e.g. if declarative
	 * transactions are used).
	 * 
	 * @param transactionsHandled
	 *            true to handle the transactions internally, false to rely on
	 *            external transaction handling.
	 */
	public void setTransactionsHandled(boolean transactionsHandled) {
		this.transactionsHandled = transactionsHandled;
	}

	/**
	 * Returns whether the entity provider is handling transactions internally
	 * or relies on external transaction handling (the default).
	 * 
	 * @return true if transactions are handled internally, false if not.
	 */
	public boolean isTransactionsHandled() {
		return transactionsHandled;
	}

	/**
	 * If {@link #isTransactionsHandled() } is true, <code>operation</code> will
	 * be executed inside a transaction that is commited after the operation is
	 * completed. Otherwise, <code>operation</code> will just be executed.
	 * 
	 * @param operation
	 *            the operation to run (must not be null).
	 */
	protected void runInTransaction(Runnable operation) {
		assert operation != null : "operation must not be null";
		if (isTransactionsHandled()) {
			EntityTransaction et = getEntityManager().getTransaction();
			try {
				et.begin();
				operation.run();
				et.commit();
			} finally {
				if (et.isActive()) {
					et.rollback();
				}
			}
		} else {
			operation.run();
		}
	}

	@SuppressWarnings("unchecked")
	public T addEntity(final T entity) {
		assert entity != null;
		runInTransaction(new Runnable() {

			public void run() {
				EntityManager em = getEntityManager();
				em.persist(entity);
				em.flush();
			}
		});
		T dEntity = detachEntity(entity);
		fireEntityProviderChangeEvent(new EntitiesAddedEvent(dEntity));
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
					em.remove(entity);
					em.flush();
					entityA[0] = detachEntity(entity);
				}
			}
		});
		if (entityA[0] != null) {
			fireEntityProviderChangeEvent(new EntitiesRemovedEvent(
					(T) entityA[0]));
		}
	}

	@SuppressWarnings("unchecked")
	public T updateEntity(final T entity) {
		assert entity != null : "entity must not be null";
		runInTransaction(new Runnable() {

			public void run() {
				EntityManager em = getEntityManager();
				em.merge(entity);
				em.flush();
			}
		});
		T dEntity = detachEntity(entity);
		fireEntityProviderChangeEvent(new EntitiesUpdatedEvent(dEntity));
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
			fireEntityProviderChangeEvent(new EntitiesUpdatedEvent(
					(T) entityA[0]));
		}
	}

	private LinkedList<EntityProviderChangeListener<T>> listeners = new LinkedList<EntityProviderChangeListener<T>>();

	public synchronized void addListener(
			EntityProviderChangeListener<T> listener) {
		assert listener != null : "listener must not be null";
		listeners.add(listener);
	}

	public synchronized void removeListener(
			EntityProviderChangeListener<T> listener) {
		assert listener != null : "listener must not be null";
		listeners.remove(listener);
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
		assert event != null : "event must not be null";
		if (listeners.isEmpty() && !isFireEntityProviderChangeEvent()) {
			return;
		}
		LinkedList<EntityProviderChangeListener<T>> list = (LinkedList<EntityProviderChangeListener<T>>) listeners
				.clone();
		for (EntityProviderChangeListener<T> l : list) {
			l.entityProviderChanged(event);
		}
	}

	/**
	 * Base class for {@link EntityProviderChangeEvent}s.
	 * 
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	protected abstract class EntityEvent implements
			EntityProviderChangeEvent<T>, Serializable {

		private static final long serialVersionUID = -3703337782681273703L;
		private Collection<T> entities;

		/**
		 * Creates a new <code>EntityEvent</code>.
		 * 
		 * @param entities the affected entities.
		 */
		protected EntityEvent(T... entities) {
			if (entities.length == 0) {
				this.entities = Collections.emptyList();
			} else {
				this.entities = Collections.unmodifiableCollection(Arrays
						.asList(entities));
			}
		}

		public Collection<T> getAffectedEntities() {
			return entities;
		}

		public EntityProvider<T> getEntityProvider() {
			return (EntityProvider<T>) MutableLocalEntityProvider.this;
		}
	}

	/**
	 * Event indicating that one or more entities have been added.
	 * 
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	protected class EntitiesAddedEvent extends EntityEvent {

		private static final long serialVersionUID = -7251967169102897952L;

		public EntitiesAddedEvent(T... entities) {
			super(entities);
		}
	}

	/**
	 * Event indicating that one or more entities have been updated.
	 * 
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	protected class EntitiesUpdatedEvent extends EntityEvent {

		private static final long serialVersionUID = -7472733082448613781L;

		public EntitiesUpdatedEvent(T... entities) {
			super(entities);
		}
	}

	/**
	 * Event indicating that one or more entities have been removed.
	 * 
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	protected class EntitiesRemovedEvent extends EntityEvent {

		private static final long serialVersionUID = -7174185739064265869L;

		public EntitiesRemovedEvent(T... entities) {
			super(entities);
		}
	}
}
