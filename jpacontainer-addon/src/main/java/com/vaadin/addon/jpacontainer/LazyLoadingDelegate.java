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

import javax.persistence.EntityManager;

/**
 * The LazyLoadingDelegate is called when a property that is lazily loaded is
 * being accessed through the Vaadin data API. The LazyLoadingDelegate is
 * responsible for ensuring that the lazily loaded property is loaded and
 * accessible.
 * 
 * @since 2.0
 */
public interface LazyLoadingDelegate {
    /**
     * This method is called when a lazily loaded property is accessed in an
     * entity. The implementation of this method is responsible for ensuring
     * that the property in question is accessible on the instance of
     * <code>entity</code> that is returned.
     * 
     * @param entity
     *            The entity containing a lazy property.
     * @param propertyName
     *            The name of the lazy property to be accessed.
     * @return an instance of <code>entity</code> with <code>propertyName</code>
     *         attached and accessible. This may be the same instance as passed
     *         in or a new one.
     */
    public <E> E ensureLazyPropertyLoaded(E entity, String propertyName);

    /**
     * Sets the EntityProvider that this delegate is associated with.
     * Automatically called by
     * {@link EntityProvider#setLazyLoadingDelegate(LazyLoadingDelegate)}. The
     * EntityProvider is used to get the current {@link EntityManager}.
     * 
     * @param ep
     */
    public void setEntityProvider(EntityProvider<?> ep);
}
