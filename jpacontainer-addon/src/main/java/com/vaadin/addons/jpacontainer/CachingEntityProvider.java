/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer;

/**
 * Interface to be implemented by all <code>EntityProvider</code>s that perform
 * some kind of internal caching.
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
	 * Sets the maximum size of the internal cache. The implementation may
	 * decide what to do when the cache is full, but a full cache may never
	 * cause an exception. This feature is optional.
	 * 
	 * @param maxSize
	 *            the new maximum size, or -1 for unlimited size.
	 * @throws UnsupportedOperationException
	 *             if this implementation does not support configuring the
	 *             maximum cache size.
	 */
	public void setMaxCacheSize(int maxSize)
			throws UnsupportedOperationException;

	/**
	 * Flushes the cache, forcing all entities to be loaded from the persistence
	 * storage upon next request. This feature is compulsory.
	 */
	public void flush();

	/**
	 * Returns whether the entity provider is currently using the internal
	 * cache, or if data is fetched/stored directly from/to the persistence
	 * storage.
	 * 
	 * @return true if the cache is in use, false otherwise.
	 */
	public boolean isCacheInUse();

	/**
	 * Turns the cache on or off.
	 *
	 * @param cacheInUse true to turn the cache on, false to turn it off.
	 * @throws UnsupportedOperationException if the cache cannot be turned on or off programmatically.
	 */
	public void setCacheInUse(boolean cacheInUse) throws UnsupportedOperationException;

	/**
	 * If the cache is in use, all entities are automatically detached
	 * regardless of the state of this flag.
	 * <p>
	 * {@inheritDoc }
	 * 
	 * @see #isCacheInUse()
	 */
	public boolean isEntitiesDetached();

	/**
	 * If the cache is in use, all entities are automatically detached
	 * regardless of the state of this flag.
	 * <p>
	 * {@inheritDoc }
	 * 
	 * @see #isCacheInUse()
	 */
	public void setEntitiesDetached(boolean detached)
			throws UnsupportedOperationException;

	/**
	 * Returns whether entities found in the cache should be cloned before they
	 * are returned or not. If this flag is false, two subsequent calls to
	 * {@link #getEntity(java.lang.Object) } with the same entity ID and without
	 * flushing the cache in between may return the same Java instance.
	 * <p>
	 * If the Java instance is serialized somewhere on the way, or the container
	 * is read-only, this is OK. However, if the client makes changes to the
	 * Java instance, the changes might be automatically reflected in the cache,
	 * which is not always desired.
	 * <p>
	 * The default value of this flag is implementation dependent.
	 * 
	 * @see #setCloneCachedEntities(boolean)
	 * @return true if cached entities should be cloned before they are
	 *         returned, false to return them directly.
	 */
	public boolean isCloneCachedEntities();

	/**
	 * Changes the value of the {@link #isCloneCachedEntities() } flag.
	 * 
	 * @param clone
	 *            true to clone cached entities before returning them, false to
	 *            return them directly.
	 * @throws UnsupportedOperationException
	 *             if the implementation does not support changing the state of
	 *             this flag.
	 */
	public void setCloneCachedEntities(boolean clone)
			throws UnsupportedOperationException;
}
