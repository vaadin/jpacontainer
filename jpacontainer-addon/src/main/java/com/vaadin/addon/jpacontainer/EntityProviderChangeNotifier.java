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

/**
 * Interface to be implemented by {@link EntityProvider}s that wish to notify
 * clients (in practice EntityContainers) when their contents change.
 * <p>
 * The EntityProvider should at least notify its listeners of the following
 * events:
 * 
 * <ul>
 * <li>An entity is added to the entity provider</li>
 * <li>An already existing entity is updated</li>
 * <li>An entity is removed from the entity provider</li>
 * <li>The contents of the entity provider is changed completely</li>
 * </ul>
 * <p>
 * This is useful in situations where several EntityContainers share the same
 * entity provider.
 * 
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface EntityProviderChangeNotifier<T> extends Serializable {

    /**
     * Registers <code>listener</code> to be notified of
     * {@link EntityProviderChangeEvent}s.
     * 
     * @param listener
     *            the listener to register (must not be null).
     */
    public void addListener(EntityProviderChangeListener<T> listener);

    /**
     * Removes the previously registered listener.
     * 
     * @param listener
     *            the listener to remove (must not be null).
     */
    public void removeListener(EntityProviderChangeListener<T> listener);
}
