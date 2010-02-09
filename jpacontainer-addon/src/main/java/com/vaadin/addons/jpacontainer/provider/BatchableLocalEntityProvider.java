/*
 * JPAContainer
 * Copyright (C) 200 Oy IT Mill Ltd
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

import com.vaadin.addons.jpacontainer.BatchableEntityProvider;
import com.vaadin.addons.jpacontainer.BatchableEntityProvider.BatchUpdateCallback;
import javax.persistence.EntityManager;

/**
 * TODO Document me!
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class BatchableLocalEntityProvider<T> extends LocalEntityProvider<T>
        implements BatchableEntityProvider<T> {

    public BatchableLocalEntityProvider(Class<T> entityClass) {
        super(entityClass);
    }

    public BatchableLocalEntityProvider(Class<T> entityClass,
            EntityManager entityManager) {
        super(entityClass, entityManager);
    }

    public void batchUpdate(BatchUpdateCallback<T> callback) throws
            UnsupportedOperationException {
        assert callback != null : "callback must not be null";
        callback.batchUpdate(this);
    }
}
