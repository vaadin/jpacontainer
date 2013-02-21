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

import com.vaadin.data.Container;

/**
 * This is a preliminary interface for adding hierarchical support to
 * JPAContainer. It works if the entities can be nested by means of a parent
 * property, e.g. like this: <code>
 * <pre>
 * class MyNodeEntity {
 *   MyNodeEntity parent;
 *   ...
 * }
 * </pre>
 * </code>
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface HierarchicalEntityContainer<T> extends EntityContainer<T>,
        Container.Hierarchical {

    /**
     * Sets the persistent property (may be nested) that contains the reference
     * to the parent entity. The parent entity is expected to be of the same
     * type as the child entity.
     * 
     * @param parentProperty
     *            the name of the parent property.
     */
    public void setParentProperty(String parentProperty);

    /**
     * Gets the name of the persistent property that contains the reference to
     * the parent entity.
     * 
     * @return the name of the parent property, or null if not specified.
     */
    public String getParentProperty();
}
