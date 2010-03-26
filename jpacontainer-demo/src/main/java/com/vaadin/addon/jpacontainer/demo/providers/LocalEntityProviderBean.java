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
package com.vaadin.addon.jpacontainer.demo.providers;

import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * This is an extended version of {@link BatchableLocalEntityProvider} that adds
 * declarative transactions and logging to the entity provider. It receives the
 * entity manager instance through dependency injection.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class LocalEntityProviderBean<T> extends BatchableLocalEntityProvider<T> {

    protected final Log logger = LogFactory.getLog(getClass());
    @PersistenceContext
    private EntityManager em;

    protected LocalEntityProviderBean(Class<T> entityClass) {
        super(entityClass);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public T updateEntity(T entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating entity [" + entity + "]");
        }
        return super.updateEntity(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public T addEntity(T entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding entity [" + entity + "]");
        }
        return super.addEntity(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeEntity(Object entityId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing entity identified by [" + entityId + "]");
        }
        super.removeEntity(entityId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateEntityProperty(Object entityId, String propertyName,
            Object propertyValue) throws IllegalArgumentException {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Updating property [" + propertyName + "] of entity identified by [" + entityId + "] to [" + propertyValue + "]");
        }
        super.updateEntityProperty(entityId, propertyName, propertyValue);
    }

    @PostConstruct
    public void init() {
        Assert.notNull(em, "no entity manager has been specified");
        if (logger.isInfoEnabled()) {
            logger.info("Initializing entity provider bean");
        }
        setEntityManager(em);
        /*
         * The entity manager is transaction scoped, which means that the
         * entities will be automatically detached when the transaction is closed.
         * Therefore, we do not need to explicitly detach them.
         */
        setEntitiesDetached(false);
    }
}
