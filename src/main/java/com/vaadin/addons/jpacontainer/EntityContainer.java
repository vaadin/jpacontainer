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

import com.vaadin.addons.jpacontainer.filter.AdvancedFilterable;
import com.vaadin.addons.jpacontainer.metadata.ClassMetadata;
import com.vaadin.data.Container;

/**
 * This interface defines a container for entities, i.e. objects that
 * are stored in some kind of persistence storage.
 * 
 * @author Petter Holmström (IT Mill)
 */
public interface EntityContainer<T> extends Container, Container.Indexed,
        Container.Sortable, AdvancedFilterable,
        Container.ItemSetChangeNotifier {

    /**
     * Entity container that maintains a cache of all the entities
     * retrieved from the persistence storage.
     *
     * @auhtor Petter Holmström (IT Mill)
     */
    public interface Caching<T> extends EntityContainer<T> {

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

    /**
     * Caching entity containter that preloads entities in chunks from the
     * persistence storage before they are actually needed.
     * 
     * @auhtor Petter Holmström (IT Mill)
     */
    public interface PreLoading<T> extends Caching<T> {

        /**
         * The default chunk size ({@value }).
         */
        public static final int DEFAULT_CHUNK_SIZE = 15;

        /**
         * Gets the number of entities to load at a time.
         *
         * @see #DEFAULT_CHUNK_SIZE
         * @return the chunk size.
         */
        public int getChunkSize();

        /**
         * Sets the number of entities to load at a time.
         *
         * @param chunkSize the chunk size to set (must be greater than 0).
         */
        public void setChunkSize(int chunkSize);
    }

    /**
     * Gets the entity class meta data of the entities contained in this 
     * container.
     *
     * @return the entity meta data (never null).
     */
    public ClassMetadata<T> getEntityClassMetaData();
}
