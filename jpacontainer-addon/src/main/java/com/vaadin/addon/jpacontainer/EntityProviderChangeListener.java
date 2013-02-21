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
 * Listener interface to be implemented by classes that want to be notified when
 * the contents of a {@link EntityProvider} is changed (e.g. entities are added
 * or removed).
 * 
 * @see EntityProviderChangeNotifier
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface EntityProviderChangeListener<T> extends Serializable {

    /**
     * Notifies the client that <code>event</code> has occurred.
     * 
     * @param event
     *            the occurred event (never null).
     */
    public void entityProviderChange(EntityProviderChangeEvent<T> event);
}
