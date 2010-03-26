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
package com.vaadin.addon.jpacontainer.provider;

import com.vaadin.addon.jpacontainer.CachingEntityProvider;
import com.vaadin.addon.jpacontainer.Filter;
import com.vaadin.addon.jpacontainer.SortBy;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * En extended version of {@link LocalEntityProvider} that also implements the {@link CachingEntityProvider} interface.
 * <p>
 * This provider can be used in applications in the same manner as {@link LocalEntityProvider}, with a few exceptions. By default the cache is on.
 * The cache can be turned off using {@link #setCacheInUse(boolean) }, in which case the provider effectively works as a {@link LocalEntityProvider}.
 * <p>
 * If you are going to edit the entities returned by the container, you should check the {@link #setCloneCachedEntities(boolean) } before
 * continuing.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class CachingLocalEntityProvider<T> extends LocalEntityProvider<T>
		implements CachingEntityProvider<T> {

	// TODO Check how well caching works with concurrent users
	// Maybe some of the collections/maps should be replaced with
	// concurrent implementations? What about synchronization?

	private static final long serialVersionUID = 302600441430870363L;
	private CachingSupport<T> cachingSupport = new CachingSupport<T>(this);

	/**
	 * Creates a new <code>CachingLocalEntityProvider</code>. The entity manager must
	 * be set using {@link #setEntityManager(javax.persistence.EntityManager) }.
	 *
	 * @param entityClass
	 *            the entity class (must not be null).
	 */
	public CachingLocalEntityProvider(Class<T> entityClass) {
		super(entityClass);
	}

	/**
	 * Creates a new <code>CachingLocalEntityProvider</code>.
	 *
	 * @param entityClass
	 *            the entity class (must not be null).
	 * @param entityManager
	 *            the entity manager to use (must not be null).
	 */
	public CachingLocalEntityProvider(Class<T> entityClass,
			EntityManager entityManager) {
		super(entityClass, entityManager);
	}

	public void flush() {
		cachingSupport.flush();
	}

	public int getEntityCacheMaxSize() {
		return cachingSupport.getMaxCacheSize();
	}

	public boolean isCacheInUse() {
		return cachingSupport.isCacheInUse();
	}

	public void setCacheInUse(boolean cacheInUse) {
		cachingSupport.setCacheInUse(cacheInUse);
	}

	public void setEntityCacheMaxSize(int maxSize) {
		cachingSupport.setMaxCacheSize(maxSize);
	}

	@Override
	public boolean containsEntity(Object entityId, Filter filter) {
		return cachingSupport.containsEntity(entityId, filter);
	}

	@Override
	public List<Object> getAllEntityIdentifiers(Filter filter,
			List<SortBy> sortBy) {
		return cachingSupport.getAllEntityIdentifiers(filter, sortBy);
	}

	@Override
	public synchronized T getEntity(Object entityId) {
		return cachingSupport.getEntity(entityId);
	}

	@Override
	public boolean isEntitiesDetached() {
		return isCacheInUse() || super.isEntitiesDetached();
	}

	public boolean isCloneCachedEntities() {
		return cachingSupport.isCloneCachedEntities();
	}

	public void setCloneCachedEntities(boolean clone)
			throws UnsupportedOperationException {
		cachingSupport.setCloneCachedEntities(clone);
	}

	@Override
	public int getEntityCount(Filter filter) {
		return cachingSupport.getEntityCount(filter);
	}

	@Override
	public Object getEntityIdentifierAt(Filter filter, List<SortBy> sortBy,
			int index) {
		return cachingSupport.getEntityIdentifierAt(filter, sortBy, index);
	}

	@Override
	public Object getFirstEntityIdentifier(Filter filter, List<SortBy> sortBy) {
		return cachingSupport.getFirstEntityIdentifier(filter, sortBy);
	}

	@Override
	public Object getLastEntityIdentifier(Filter filter, List<SortBy> sortBy) {
		return cachingSupport.getLastEntityIdentifier(filter, sortBy);
	}

	@Override
	public Object getNextEntityIdentifier(Object entityId, Filter filter,
			List<SortBy> sortBy) {
		return cachingSupport.getNextEntityIdentifier(entityId, filter, sortBy);
	}

	@Override
	public Object getPreviousEntityIdentifier(Object entityId, Filter filter,
			List<SortBy> sortBy) {
		return cachingSupport.getPreviousEntityIdentifier(entityId, filter, sortBy);
	}
}
