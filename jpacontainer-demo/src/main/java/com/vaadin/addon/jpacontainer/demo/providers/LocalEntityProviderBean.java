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
/*
JPAContainer
Copyright (C) 2009-2011 Oy Vaadin Ltd

This program is available under GNU Affero General Public License (version
3 or later at your option).

See the file licensing.txt distributed with this software for more
information about licensing.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public abstract class LocalEntityProviderBean<T> extends BatchableLocalEntityProvider<T> {

    protected final Log logger = LogFactory.getLog(getClass());
    @PersistenceContext
    private EntityManager em;

    protected LocalEntityProviderBean(Class<T> entityClass) {
        super(entityClass);
		setTransactionsHandledByProvider(false);
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
