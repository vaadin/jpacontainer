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

import java.util.Collection;
import java.util.Collections;

import com.vaadin.addon.jpacontainer.BatchableEntityProvider;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;

/**
 * Event indicating that a batch update has been performed.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class BatchUpdatePerformedEvent<T> implements
        EntityProviderChangeEvent<T> {

    private static final long serialVersionUID = -4080306860560561433L;
    private EntityProvider<T> entityProvider;

    /**
     * Creates a new <code>BatchUpdatePerformedEvent</code>.
     * 
     * @param entityProvider
     *            the batchable entity provider.
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
