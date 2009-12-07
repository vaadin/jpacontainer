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
 * Caching entity containter that preloads entities in chunks from the
 * persistence storage before they are actually needed.
 *
 * @auhtor Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface PreLoadingCachingEntityContainer<T> extends
        CachingEntityContainer<T> {

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
