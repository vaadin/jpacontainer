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
package com.vaadin.addon.jpacontainer.provider;

import com.vaadin.addon.jpacontainer.CachingEntityProvider;
import com.vaadin.addon.jpacontainer.Filter;
import com.vaadin.addon.jpacontainer.SortBy;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * En extended version of {@link MutableLocalEntityProvider} that also implements the {@link CachingEntityProvider} interface.
 * <p>
 * This provider can be used in applications in the same manner as {@link MutableLocalEntityProvider}, with a few exceptions. By default,
 * the cache is turned off which means that this provider effectively works as a {@link MutableLocalEntityProvider}. The cache
 * can be turned on using {@link #setCacheInUse(boolean) }.
 * <p>
 * If you are going to edit the entities returned by the container, you should check the {@link #setCloneCachedEntities(boolean) } before
 * continuing.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class CachingMutableLocalEntityProvider<T> extends MutableLocalEntityProvider<T> implements CachingEntityProvider<T> {

	private CachingSupport<T> cachingSupport = new CachingSupport<T>(this);

	/**
	 * Creates a new <code>CachingMutableLocalEntityProvider</code>.
	 *
	 * @param entityClass
	 *            the entity class (must not be null).
	 * @param entityManager
	 *            the entity manager to use (must not be null).
	 */
	public CachingMutableLocalEntityProvider(Class<T> entityClass, EntityManager entityManager) {
		super(entityClass, entityManager);
	}

	/**
	 * Creates a new <code>CachingLocalEntityProvider</code>. The entity manager must
	 * be set using {@link #setEntityManager(javax.persistence.EntityManager) }.
	 *
	 * @param entityClass
	 *            the entity class (must not be null).
	 */
	public CachingMutableLocalEntityProvider(Class<T> entityClass) {
		super(entityClass);
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

	@Override
	public T addEntity(T entity) {
		T result = super.addEntity(entity);
		cachingSupport.entityAdded(result);
		return result;
	}

	@Override
	public void removeEntity(Object entityId) {
		super.removeEntity(entityId);
		cachingSupport.invalidate(entityId, false);
	}

	@Override
	public T updateEntity(T entity) {
		T result = super.updateEntity(entity);
		cachingSupport.invalidate(getEntityClassMetadata().getPropertyValue(entity, getEntityClassMetadata().getIdentifierProperty().getName()), true);
		return result;
	}

	@Override
	public void updateEntityProperty(Object entityId, String propertyName, Object propertyValue) throws IllegalArgumentException {
		super.updateEntityProperty(entityId, propertyName, propertyValue);
		cachingSupport.invalidate(entityId, true);
	}
}
