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
package com.vaadin.addon.jpacontainer.util;

import java.util.WeakHashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainer;

/**
 * A helper class, easing the implementation of the entitymanager-per-request
 * (or session-per-request) pattern recommended when using Hibernate.
 * 
 * This class keeps track of {@link JPAContainer} instances (using weak
 * references) and opens and closes the used {@link EntityManager} when
 * {@link #requestStart()} and {@link #requestEnd()} are called.
 * 
 * @author Jonatan Kronqvist / Vaadin Ltd
 */
public class EntityManagerPerRequestHelper {

    private WeakHashMap<EntityProvider<?>, EntityManagerFactory> providerToEMF = new WeakHashMap<EntityProvider<?>, EntityManagerFactory>();

    /**
     * Adds a container to handle {@link EntityManager} changes for.
     * 
     * @param container
     */
    public void addContainer(JPAContainer<?> container) {
        providerToEMF.put(container.getEntityProvider(), container
                .getEntityProvider().getEntityManager()
                .getEntityManagerFactory());
    }

    /**
     * Remove a container from the {@link EntityManager} change updates.
     * 
     * @param container
     */
    public void removeContainer(JPAContainer<?> container) {
        providerToEMF.remove(container.getEntityProvider());
    }

    /**
     * Creates a new {@link EntityManager} for each of the registered
     * containers. Should be called from e.g. a Vaadin
     * HttpServletRequestListener.
     */
    public void requestStart() {
        for (EntityProvider<?> provider : providerToEMF.keySet()) {
            provider.setEntityManager(providerToEMF.get(provider)
                    .createEntityManager());
        }
    }

    /**
     * Closes the {@link EntityManager} for each of the registered containers.
     * Should be called from e.g. a Vaadin HttpServletRequestListener.
     */
    public void requestEnd() {
        for (EntityProvider<?> provider : providerToEMF.keySet()) {
            if (provider.getEntityManager().isOpen()) {
                provider.getEntityManager().close();
            }
        }
    }
}
