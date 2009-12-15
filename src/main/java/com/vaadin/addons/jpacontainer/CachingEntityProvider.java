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
 * Interface to be implemented by all <code>EntityProvider</code>s that
 * perform some kind of internal caching.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface CachingEntityProvider<T> extends EntityProvider<T> {

    /**
     * Gets the maximum size of the internal cache. The default value is
     * implementation specific.
     *
     * @return the max size, or -1 for unlimited size.
     */
    public int getMaxCacheSize();

    /**
     * Sets the maximum size of the internal cache. The implementation may decide
     * what to do when the cache is full, but a full cache may never cause
     * an exception. This feature is optional.
     *
     * @param maxSize the new maximum size, or -1 for unlimited size.
     * @throws UnsupportedOperationException if this implementation does not support configuring the maximum cache size.
     */
    public void setMaxCacheSize(int maxSize) throws
            UnsupportedOperationException;

    /**
     * Flushes the cache, forcing all entities to be loaded from the
     * persistence storage upon next request. This feature is compulsory.
     */
    public void flush();

    /**
     * Returns whether the entity provider is currently using the internal cache,
     * or if data is fetched/stored directly from/to the persistence storage.
     *
     * @return true if the cache is in use, false otherwise.
     */
    public boolean isCacheInUse();
}