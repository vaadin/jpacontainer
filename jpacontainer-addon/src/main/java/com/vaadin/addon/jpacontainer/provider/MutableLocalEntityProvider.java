/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Petter Holmström (Vaadin Ltd)
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

    @SuppressWarnings({"unchecked", "rawtypes"})
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
                    // make sure we are working with the latest versions
                    em.refresh(entity);
                    getEntityClassMetadata().setPropertyValue(entity,
                            propertyName, propertyValue);
                    // re-attach also referenced entities to the persistence
                    // context
                    entity = em.merge(entity);
                    em.flush();
                    entityA[0] = detachEntity(entity);
                }
            }
        });
        if (entityA[0] != null) {
            fireEntityProviderChangeEvent(new EntityPropertyUpdatedEvent(this,
                    propertyName, entityA));
        }
    }

    /*
     * Transient note: Listeners (read: JPAContainers) should re attach themselves when deserialized 
     */
    transient private LinkedList<WeakReference<EntityProviderChangeListener<T>>> listeners;
    
    private LinkedList<WeakReference<EntityProviderChangeListener<T>>> getListeners() {
        if(listeners == null) {
            listeners = new LinkedList<WeakReference<EntityProviderChangeListener<T>>>();
        }
        return listeners;
    }

    public void addListener(EntityProviderChangeListener<T> listener) {
        synchronized (getListeners()) {
            assert listener != null : "listener must not be null";
            getListeners().add(new WeakReference<EntityProviderChangeListener<T>>(
                    listener));
        }
    }

    public void removeListener(EntityProviderChangeListener<T> listener) {
        synchronized (getListeners()) {
            assert listener != null : "listener must not be null";

            Iterator<WeakReference<EntityProviderChangeListener<T>>> it = getListeners()
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
        synchronized (getListeners()) {
            assert event != null : "event must not be null";
            if (getListeners().isEmpty() && !isFireEntityProviderChangeEvent()) {
                return;
            }
            // cleanup
            Iterator<WeakReference<EntityProviderChangeListener<T>>> it = getListeners()
                    .iterator();
            while (it.hasNext()) {
                if (null == it.next().get()) {
                    it.remove();
                }
            }
            // copy list to use outside the synchronized block
            list = (LinkedList<WeakReference<EntityProviderChangeListener<T>>>) getListeners()
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
