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
package com.vaadin.addons.jpacontainer;

/**
 * Entity container that maintains a cache of all the entities
 * retrieved from the persistence storage.
 *
 * @auhtor Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface CachingEntityContainer<T> extends EntityContainer<T> {

    /**
     * The default maximum size of the cache ({@value }).
     */
    public static final int DEFAULT_MAX_CACHE_SIZE = 500;

    /**
     * Gets the maximum number of items to store in the cache. A value
     * of 0 means that the size is unlimited, i.e. all items are cached.
     *
     * @see #DEFAULT_MAX_CACHE_SIZE
     * @return the maximum cache size, or 0 if the size is unlimited.
     */
    public int getMaxCacheSize();

    /**
     * Sets the maximum size of the cache. A value of 0 means
     * that the maximum size is unlimited.
     *
     * @param maxCacheSize the maximum cache size (must be equal to or
     *      greater than 0)
     */
    public void setMaxCacheSize(int maxCacheSize);

    /**
     * Flushes the cache, forcing the container to reload all entities
     * from the persistence storage the next time they are needed.
     */
    public void flush();
}
