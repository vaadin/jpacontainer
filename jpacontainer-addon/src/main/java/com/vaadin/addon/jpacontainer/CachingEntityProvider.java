/*
 * JPAContainer
 * Copyright (C) 2010-2011 Oy Vaadin Ltd
 *
 * This program is available both under Commercial Vaadin Add-On
 * License 2.0 (CVALv2) and under GNU Affero General Public License (version
 * 3 or later) at your option.
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and CVALv2 along with this program.  If not, see
 * <http://www.gnu.org/licenses/> and <http://vaadin.com/license/cval-2.0>.
 */
package com.vaadin.addon.jpacontainer;

/**
 * Interface to be implemented by all <code>EntityProvider</code>s that perform
 * some kind of internal caching.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface CachingEntityProvider<T> extends EntityProvider<T> {

	/**
	 * Gets the maximum number of entity instances to store in the cache. The default value is
	 * implementation specific.
	 * 
	 * @return the max size, or -1 for unlimited size.
	 */
	public int getEntityCacheMaxSize();

	/**
	 * Sets the maximum number of entity instances to store in the cache. The implementation may
	 * decide what to do when the cache is full, but a full cache may never
	 * cause an exception. This feature is optional.
	 * 
	 * @param maxSize
	 *            the new maximum size, or -1 for unlimited size.
	 * @throws UnsupportedOperationException
	 *             if this implementation does not support configuring the
	 *             maximum cache size.
	 */
	public void setEntityCacheMaxSize(int maxSize)
			throws UnsupportedOperationException;

	/**
	 * Flushes the cache, forcing all entities to be loaded from the persistence
	 * storage upon next request. This feature should be implemented by all caching entity providers.
	 */
	public void flush();

	/**
	 * Returns whether the entity provider is currently using the internal
	 * cache, or if data is fetched/stored directly from/to the persistence
	 * storage. By default, caching should be in use.
	 * 
	 * @return true if the cache is in use, false otherwise.
	 */
	public boolean isCacheInUse();

	/**
	 * Turns the cache on or off. When the cache is turned off, it is automatically flushed.
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
	 * flushing the cache in between may return the same entity instance. This
	 * could be a problem if the instance is modified, as the cache would then
	 * contain the locally modified entity instance and not the one that was fetched
	 * from the persistence storage.
	 * <p>
	 * If the entity instances are serialized and deserialized before they reach the container, or the container
	 * is read-only, entities need not be cloned.
	 * <p>
	 * It is undefined what happens if this flag is true and the entities are
	 * not cloneable.
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
