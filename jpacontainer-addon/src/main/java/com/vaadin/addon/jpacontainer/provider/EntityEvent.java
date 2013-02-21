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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;

/**
 * Base class for {@link EntityProviderChangeEvent}s.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
abstract class EntityEvent<T> implements EntityProviderChangeEvent<T>,
        Serializable {

    private static final long serialVersionUID = -3703337782681273703L;
    private Collection<T> entities;
    private MutableEntityProvider<T> entityProvider;

    public EntityEvent(MutableEntityProvider<T> entityProvider, T... entities) {
        this.entityProvider = entityProvider;
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
        return entityProvider;
    }
}
