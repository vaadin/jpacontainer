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

package com.vaadin.addon.jpacontainer;

import java.io.Serializable;
import java.util.Collection;

/**
 * Event indicating that the contents of a {@link EntityProvider} has been
 * changed (e.g. entities have been added or removed).
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface EntityProviderChangeEvent<T> extends Serializable {

    /**
     * Gets the entity provider whose contents has been changed.
     * 
     * @return the entity manager (never null).
     */
    public EntityProvider<T> getEntityProvider();

    /**
     * Gets the affected entities, if supported by the implementation. If
     * entities have been modified, this collection may contain all modified
     * entities, if entities have been added, this collection may contain all
     * added entities, etc. If the number of changed entities is very large,
     * e.g. due to a major change in the entire data source, the collection may
     * be empty.
     * 
     * @return an unmodifiable collection of affected entities (never null, but
     *         may be empty).
     */
    public Collection<T> getAffectedEntities();

    /**
     * Event indicating that one or more entities have been added to the entity
     * provider.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public interface EntitiesAddedEvent<T> extends EntityProviderChangeEvent<T> {
        // No additional methods
    }

    /**
     * Event indicating that one or more entities have been updated in the
     * entity provider.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public interface EntitiesUpdatedEvent<T> extends
            EntityProviderChangeEvent<T> {
        // No additional methods
    }

    /**
     * Event indicating that one or more entities have been updated a specific
     * property in the entity provider.
     * 
     * @since 2.0
     */
    public interface EntityPropertyUpdatedEvent<T> extends
            EntityProviderChangeEvent<T> {

        /**
         * @return identifier of the modified property
         */
        public String getPropertyId();
    }

    /**
     * Event indicating that one or more entities have been removed from the
     * entity provider.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public interface EntitiesRemovedEvent<T> extends
            EntityProviderChangeEvent<T> {
        // No additional methods
    }
}
