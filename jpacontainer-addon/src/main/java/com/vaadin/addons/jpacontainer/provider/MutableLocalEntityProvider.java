/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.provider;

import com.vaadin.addons.jpacontainer.MutableEntityProvider;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Extended version of {@link LocalEntityProvider} that provides editing support.
 * In addition to all the features of the <code>LocalEntityProvider<code>, it supports
 * both internal and external transaction handling.
 *
 * TODO Improve documentation
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class MutableLocalEntityProvider<T> extends LocalEntityProvider<T>
        implements MutableEntityProvider<T> {

    /**
     * Creates a new <code>MutableLocalEntityProvider</code>. The entity manager
     * must be set using {@link #setEntityManager(javax.persistence.EntityManager) }.
     *
     * @param entityClass the entity class (must not be null).
     */
    public MutableLocalEntityProvider(Class<T> entityClass) {
        super(entityClass);
    }

    /**
     * Creates a new <code>MutableLocalEntityProvider</code>.
     *
     * @param entityClass the entity class (must not be null).
     * @param entityManager the entity manager to use (must not be null).
     */
    public MutableLocalEntityProvider(Class<T> entityClass,
            EntityManager entityManager) {
        super(entityClass, entityManager);
    }
    private boolean transactionsHandled = false;

    /**
     * Specifies whether the entity provider should handle transactions
     * itself or whether they should be handled outside (e.g. if declarative
     * transactions are used).
     * 
     * @param transactionsHandled true to handle the transactions internally,
     * false to rely on external transaction handling.
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
     * be executed inside a transaction that is commited after the operation is completed.
     * Otherwise, <code>operation</code> will just be executed.
     * 
     * @param operation the operation to run (must not be null).
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

    @Override
    public T addEntity(final T entity) {
        assert entity != null;
        runInTransaction(new Runnable() {

            @Override
            public void run() {
                EntityManager em = getEntityManager();
                em.persist(entity);
                em.flush();
            }
        });
        return detachEntity(entity);
    }

    @Override
    public void removeEntity(final Object entityId) {
        assert entityId != null;
        runInTransaction(new Runnable() {

            @Override
            public void run() {
                EntityManager em = getEntityManager();
                T entity = em.find(getEntityClassMetadata().getMappedClass(),
                        entityId);
                if (entity != null) {
                    em.remove(entity);
                    em.flush();
                }
            }
        });
    }

    @Override
    public T updateEntity(final T entity) {
        assert entity != null : "entity must not be null";
        runInTransaction(new Runnable() {

            @Override
            public void run() {
                EntityManager em = getEntityManager();
                em.merge(entity);
                em.flush();
            }
        });
        return detachEntity(entity);
    }

    @Override
    public void updateEntityProperty(final Object entityId,
            final String propertyName,
            final Object propertyValue) throws IllegalArgumentException {
        assert entityId != null : "entityId must not be null";
        assert propertyName != null : "propertyName must not be null";
        runInTransaction(new Runnable() {

            @Override
            public void run() {
                EntityManager em = getEntityManager();
                T entity = em.find(getEntityClassMetadata().getMappedClass(),
                        entityId);
                if (entity != null) {
                    getEntityClassMetadata().setPropertyValue(entity,
                            propertyName, propertyValue);
                    em.flush();
                }
            }
        });
    }
}
