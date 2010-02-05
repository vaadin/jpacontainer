/*
 * JPAContainer
 * Copyright (C) 2009 Oy IT Mill Ltd
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
package com.vaadin.addons.jpacontainer.util;

import com.vaadin.addons.jpacontainer.EntityContainer;
import com.vaadin.addons.jpacontainer.EntityProvider;
import com.vaadin.addons.jpacontainer.JPAContainer;
import com.vaadin.addons.jpacontainer.provider.LocalEntityProvider;
import javax.persistence.EntityManager;

/**
 * Utility class for easily creating and configuring different {@link EntityContainer}s. Unless stated otherwise,
 * all containers created by this factory are ready to be used without any further configuration.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public final class EntityContainerFactory {

    /**
     * TODO Document me!
     * @param entityClass
     * @param dataSource
     * @return
     */
    public static <T> EntityContainer<T> createEntityContainer(
            Class<T> entityClass, EntityProvider<T> dataSource) {
        JPAContainer<T> container = new JPAContainer(entityClass);
        container.setEntityProvider(dataSource);
        return container;
    }

    /**
     * TODO Document me!
     * @param <T>
     * @param entityClass
     * @param entityManager
     * @return
     */
    public static <T> EntityContainer<T> createJPAContainer(Class<T> entityClass,
            EntityManager entityManager) {
        return createEntityContainer(entityClass, new LocalEntityProvider<T>(
                entityClass, entityManager));
    }

    /**
     * TODO Document me!
     * @param <T>
     * @param entityClass
     * @param entityManager
     * @return
     */
    public static <T> EntityContainer<T> createCachingJPAContainer(
            Class<T> entityClass, EntityManager entityManager) {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
