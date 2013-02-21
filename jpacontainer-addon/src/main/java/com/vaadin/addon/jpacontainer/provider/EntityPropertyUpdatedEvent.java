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

import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;

/**
 * Event indicating that one or more entities have updated their property.
 * 
 * @since 2.0
 */
class EntityPropertyUpdatedEvent<T> extends EntityEvent<T> implements
        EntityProviderChangeEvent.EntityPropertyUpdatedEvent<T> {

    private static final long serialVersionUID = -7472733082448613781L;
    private String propertyId;

    public EntityPropertyUpdatedEvent(MutableEntityProvider<T> entityProvider,
            String propertyId, T... entities) {
        super(entityProvider, entities);
        this.propertyId = propertyId;
    }

    public String getPropertyId() {
        return propertyId;
    }
}
