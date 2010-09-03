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

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.Filter;
import com.vaadin.addon.jpacontainer.Filter.PropertyIdPreprocessor;
import com.vaadin.addon.jpacontainer.SortBy;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Query;

/**
 * Delegate class that implements caching for {@link LocalEntityProvider}s and
 * their subclasses. This class is internal and should never be used outside of JPAContainer.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
class CachingSupport<T> implements Serializable {

	private final LocalEntityProvider<T> entityProvider;
	private int maxCacheSize = 1000;
	private boolean cacheInUse = true;
	private boolean cloneCachedEntities = false;
	/**
	 * The number of entity IDs to fetch every time a query is made.
	 */
	protected static final int CHUNK_SIZE = 150;
	/**
	 * A {@link Filter}-instance representing the null-filter (i.e. no filter
	 * applied).
	 */
	protected static Filter NULL_FILTER = new Filter() {

		private static final long serialVersionUID = 6142104349424102387L;

		public String toQLString() {
			return "";
		}

		public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
			return "";
		}
	};
	/**
	 * The max size of the filter cache (i.e. how many different filters to cache).
	 * @see #getFilterCache()
	 */
	public static final int MAX_FILTER_CACHE_SIZE = 10;
	/**
	 * The max size of the sort by cache for each filter. Thus, the maximum
	 * number of cached filter-sortBy combinations is <code>MAX_FILTER_CACHE_SIZE * MAX_SORTBY_CACHE_SIZE</code>.
	 */
	public static final int MAX_SORTBY_CACHE_SIZE = 10;

	// TODO Make chunk size, filter cache size and sortBy cache size user configurable.

	/**
	 * Creates a new <code>CachingSupport</code> for the specified
	 * entity provider.
	 *
	 * @param entityProvider
	 *            the entity provider (never null).
	 */
	public CachingSupport(LocalEntityProvider<T> entityProvider) {
		assert entityProvider != null : "entityProvider should not be null";
		this.entityProvider = entityProvider;
	}

	/**
	 * Data structure used by {@link FilterCacheEntry} to store entityId lists
	 * sorted in different ways.
	 *
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	static class IdListEntry implements Serializable {

		private static final long serialVersionUID = -3552793234160831297L;
		public ArrayList<Object> idList;
		public int listOffset = 0;
		public boolean containsAll = false;
	}

	/**
	 * This class represents a cache for a specific {@link Filter}. The class
	 * contains counterparts of most of the methods defined in {@link EntityProvider}.
	 *
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	class FilterCacheEntry implements Serializable {

		// TODO Optimize the use of lists
		private static final long serialVersionUID = -2978864194978758736L;
		private Filter filter;
		private Integer entityCount;
		public Map<List<SortBy>, IdListEntry> idListMap = new CacheMap<List<SortBy>, IdListEntry>(
				MAX_SORTBY_CACHE_SIZE);
		public Set<Object> idSet = new CacheSet<Object>(getMaxCacheSize());

		/**
		 * Creates a new <code>FilterCacheEntry</code>.
		 *
		 * @param filter the filter for which this cache should be created.
		 */
		public FilterCacheEntry(Filter filter) {
			this.filter = filter;
		}

		/**
		 * Gets the number of entities that match this particular filter.
		 * @return the number of entities.
		 */
		public synchronized int getEntityCount() {
			if (entityCount == null) {
				entityCount = entityProvider.doGetEntityCount(
						getFilter());
			}
			return entityCount;
		}

		/**
		 * @see EntityProvider#containsEntity(java.lang.Object, com.vaadin.addons.jpacontainer.Filter)
		 */
		public synchronized boolean containsId(Object entityId) {
			if (!idSet.contains(entityId)) {
				if (entityProvider.doContainsEntity(entityId,
						getFilter())) {
					idSet.add(entityId);
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		}

		/**
		 * @see EntityProvider#getFirstEntityIdentifier(com.vaadin.addons.jpacontainer.Filter, java.util.List)
		 */
		public Object getFirstId(List<SortBy> sortBy) {
			return getIdAt(sortBy, 0);
		}

		/**
		 * @see EntityProvider#getNextEntityIdentifier(java.lang.Object, com.vaadin.addons.jpacontainer.Filter, java.util.List)
		 */
		public synchronized Object getNextId(Object entityId,
				List<SortBy> sortBy) {
			IdListEntry entry = idListMap.get(sortBy);
			if (entry == null) {
				entry = new IdListEntry();
				entry.idList = new ArrayList<Object>();
				entry.listOffset = -1;
				idListMap.put(sortBy, entry);
			}
			int index = entry.idList.indexOf(entityId);
			if (index == -1) {
				entry.idList = new ArrayList<Object>(getNextIds(getFilter(),
						sortBy, entityId, CHUNK_SIZE));
				if (entry.idList.isEmpty()) {
					return null;
				} else {
					return entry.idList.get(0);
				}
			} else {
				if (index == entry.idList.size() - 1) {
					if (getMaxCacheSize() > -1 && entry.idList.size() + CHUNK_SIZE > getMaxCacheSize()) {
						// Clean up the cache
						if (entry.idList.size() <= CHUNK_SIZE) {
							entry.idList.clear();
							index = -1;
						} else {
							entry.idList.subList(0, CHUNK_SIZE).clear();
							index -= CHUNK_SIZE;
						}
					}
					entry.idList.addAll(getNextIds(getFilter(), sortBy, entityId,
							CHUNK_SIZE));
				}
				if (index + 1 == entry.idList.size()) {
					return null;
				} else {
					return entry.idList.get(index + 1);
				}
			}
		}

		/**
		 * @see EntityProvider#getPreviousEntityIdentifier(java.lang.Object, com.vaadin.addons.jpacontainer.Filter, java.util.List)
		 */
		public synchronized Object getPreviousId(Object entityId,
				List<SortBy> sortBy) {
			IdListEntry entry = idListMap.get(sortBy);
			if (entry == null) {
				entry = new IdListEntry();
				entry.idList = new ArrayList<Object>();
				entry.listOffset = -1;
				idListMap.put(sortBy, entry);
			}
			int index = entry.idList.indexOf(entityId);
			if (index == -1) {
				List<Object> objects = getPreviousIds(getFilter(), sortBy,
						entityId,
						CHUNK_SIZE);
				// We have to reverse the list
				entry.idList = new ArrayList<Object>(objects.size());
				for (int i = objects.size() - 1; i >= 0; i--) {
					entry.idList.add(objects.get(i));
				}
				if (entry.idList.isEmpty()) {
					return null;
				} else {
					return entry.idList.get(entry.idList.size() - 1);
				}
			} else {
				if (index == 0) {
					List<Object> objects = getPreviousIds(getFilter(), sortBy,
							entityId, CHUNK_SIZE);
					if (objects.isEmpty()) {
						return null;
					}
					// Store the ID we are looking for
					Object theId = objects.get(0);
					// Save the rest of the IDs in the cache for future use
					ArrayList<Object> l = new ArrayList<Object>();
					for (int i = objects.size() - 1; i >= 0; i--) {
						l.add(objects.get(i));
					}
					if (getMaxCacheSize() > -1 && entry.idList.size() + CHUNK_SIZE > getMaxCacheSize()) {
						// Clean up the cache
						if (entry.idList.size() > CHUNK_SIZE) {
							l.addAll(entry.idList.subList(0,
									entry.idList.size() - CHUNK_SIZE));
						}
					} else {
						l.addAll(entry.idList);
					}
					entry.idList = l;
					return theId;
				} else {
					return entry.idList.get(index - 1);
				}
			}
		}

		/**
		 * @see EntityProvider#getLastEntityIdentifier(com.vaadin.addons.jpacontainer.Filter, java.util.List)
		 */
		public Object getLastId(List<SortBy> sortBy) {
			return getIdAt(sortBy, getEntityCount() - 1);
		}

		/**
		 * Informs the cache that <code>entityId</code> has been invalidated (changed or removed).
		 * If the entityId is currently in cache, the cache is flushed, forcing the data
		 * to be fetched from the database when requested the next time.
		 *
		 * @param entityId the entityId to invalidate.
		 */
		public synchronized void invalidate(Object entityId) {
			if (containsId(entityId)) {
				// Clear the caches to force the data to be re-fetched from the database
				// in case the ordering has changed
				idListMap.clear();
				// Removing the entity Id from the Id cache should be enough
				idSet.remove(entityId);
			}
		}

		/**
		 * @see EntityProvider#getEntityIdentifierAt(com.vaadin.addons.jpacontainer.Filter, java.util.List, int)
		 */
		public synchronized Object getIdAt(List<SortBy> sortBy, int index) {
			IdListEntry entry = idListMap.get(sortBy);
			if (entry == null) {
				entry = new IdListEntry();
				entry.idList = new ArrayList<Object>(CHUNK_SIZE * 2);
				idListMap.put(sortBy, entry);
			}

			// listOffset may be -1 if the list has been loaded by a call
			// to getNextId() or getPreviousId()
			if (!entry.containsAll
					&& (entry.idList.isEmpty() || index < entry.listOffset || index >= entry.listOffset
					+ entry.idList.size())) {

				// Check if we can concatenate the index lists
				if (entry.listOffset > -1 && index == entry.listOffset - 1) {
					if (getMaxCacheSize() > -1 && entry.idList.size() + CHUNK_SIZE > getMaxCacheSize()) {
						// Clean up the cache
						if (entry.idList.size() <= CHUNK_SIZE) {
							entry.idList.clear();
						} else {
							entry.idList.subList(
									entry.idList.size() - CHUNK_SIZE, entry.idList.size()).clear();
						}
					}
					ArrayList<Object> l = new ArrayList<Object>(CHUNK_SIZE + entry.idList.size());
					int startFrom = index - CHUNK_SIZE;
					if (startFrom < 0) {
						startFrom = 0;
					}
					l.addAll(getIds(getFilter(), sortBy, startFrom,
							index - startFrom + 1));
					l.addAll(entry.idList);
					entry.idList = l;
					entry.listOffset = startFrom;
				} else if (entry.listOffset > -1 && index == entry.listOffset + entry.idList.size()) {
					// It is possible that maxCacheSize < CHUNK_SIZE => we have to make sure that the list is at least as big as CHUNK_SIZE
					if (getMaxCacheSize() > -1 && entry.idList.size() + CHUNK_SIZE > getMaxCacheSize()) {
						// Clean up the cache
						if (entry.idList.size() <= CHUNK_SIZE) {
							entry.listOffset += entry.idList.size();
							entry.idList.clear();
						} else {
							entry.idList.subList(0, CHUNK_SIZE).clear();
							entry.listOffset += CHUNK_SIZE;
						}
					}
					entry.idList.addAll(getIds(getFilter(), sortBy, index,
							CHUNK_SIZE));
				} else {
					entry.idList.clear();
					entry.idList.addAll(getIds(getFilter(), sortBy, index,
							CHUNK_SIZE));
					entry.listOffset = index;
				}
			}
			int i = index - entry.listOffset;
			if (entry.idList.size() <= i) {
				return null;
			}
			return entry.idList.get(i);
		}

		/**
		 * @see EntityProvider#getAllEntityIdentifiers(com.vaadin.addons.jpacontainer.Filter, java.util.List)
		 */
		public synchronized List<Object> getAllIds(List<SortBy> sortBy) {
			IdListEntry entry = idListMap.get(sortBy);
			if (entry == null) {
				entry = new IdListEntry();
				idListMap.put(sortBy, entry);
			}
			if (!entry.containsAll) {
				entry.idList = new ArrayList(getIds(getFilter(), sortBy, 0, -1));
				entry.listOffset = 0;
				entry.containsAll = true;
			}
			return Collections.unmodifiableList(entry.idList);
		}

		/**
		 * Gets the filter for which this cache has been created.
		 *
		 * @return the filter (may be null).
		 */
		public Filter getFilter() {
			return filter == NULL_FILTER ? null : filter;
		}
	}

	/**
	 * TODO Document me!
	 *
	 * @param entityId
	 * @param updated
	 */
	public synchronized void invalidate(Object entityId, boolean updated) {
		getEntityCache().remove(entityId);
		if (updated) {
			// TODO Do something smarter than flushing the entire cache!
			getFilterCache().clear();
		} else {
			for (FilterCacheEntry fce : getFilterCache().values()) {
				fce.invalidate(entityId);
			}
		}
	}

	/**
	 * TODO Document me!
	 * @param entity
	 */
	public synchronized void entityAdded(T entity) {
		// TODO Do something smarter than flushing the entire cache!
		flush();
	}

	/**
	 * Gets all the identifiers that match <code>filter</code>, sorted by <code>sortBy</code>,
	 * starting with the identifier at position <code>startFrom</code> and retrieving a maximum
	 * number of <code>fetchMax</code> items.
	 *
	 * @param filter the filter to apply, if any (may be null).
	 * @param sortBy the ordering information (may not be null).
	 * @param startFrom the index of the first identifier to retrieve.
	 * @param fetchMax the maximum number of identifiers to retrieve, or 0 to retrieve all.
	 * @return a list of identifiers.
	 */
	@SuppressWarnings("unchecked")
	protected List<Object> getIds(Filter filter, List<SortBy> sortBy,
			int startFrom, int fetchMax) {
		Query query = entityProvider.createFilteredQuery("obj."
				+ entityProvider.getEntityClassMetadata().getIdentifierProperty().getName(),
				"obj", filter, entityProvider.addPrimaryKeyToSortList(sortBy), false, null);
		query.setFirstResult(startFrom);
		if (fetchMax > 0) {
			query.setMaxResults(fetchMax);
		}
		return query.getResultList();
	}

	/**
	 * Gets all the identifiers that match <code>filter</code>, sorted by <code>sortBy</code>,
	 * starting with the identifier next to <code>startFrom</code> and retrieving a maximum
	 * number of <code>fetchMax</code> items. If <code>startFrom</code> is at position n, then item
	 * n+1 will be the first item in the returnde list, n+2 the second, etc.
	 *
	 * @param filter the filter to apply, if any (may be null).
	 * @param sortBy the ordering information (may not be null).
	 * @param startFrom the entityId prioir to the first identifier to retrieve.
	 * @param fetchMax the maximum number of identifiers to retrieve, or 0 to retrieve all.
	 * @return a list of identifiers.
	 */
	protected List<Object> getNextIds(Filter filter, List<SortBy> sortBy,
			Object startFrom, int fetchMax) {
		Query query = entityProvider.createSiblingQuery(startFrom, filter, sortBy, false);
		if (fetchMax > 0) {
			query.setMaxResults(fetchMax);
		}
		return query.getResultList();
	}

	/**
	 * Gets all the identifiers that match <code>filter</code>, sorted backwards by <code>sortBy</code>,
	 * starting with the identifier prior to <code>startFrom</code> and retrieving a maximum number
	 * of <code>fetchMax</code> items. If <code>startFrom</code> is at position n, then item n-1 will
	 * be the first item in the returned list, n-2 the second, etc.
	 *
	 * @param filter the filter to apply, if any (may be null).
	 * @param sortBy the ordering information (may not be null).
	 * @param startFrom the entityId next to the first identifier to retrieve.
	 * @param fetchMax the maximum number of identifiers to retrieve, or 0 to retrieve all.
	 * @return a list of identifiers.
	 */
	protected List<Object> getPreviousIds(Filter filter, List<SortBy> sortBy,
			Object startFrom, int fetchMax) {
		Query query = entityProvider.createSiblingQuery(startFrom, filter, sortBy, true);
		if (fetchMax > 0) {
			query.setMaxResults(fetchMax);
		}
		return query.getResultList();
	}
	private Map<Object, T> entityCache;
	private Map<Filter, FilterCacheEntry> filterCache;

	/**
	 * A hash map that will remove the oldest items once
	 * its size reaches a specified max size.
	 *
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	protected static class CacheMap<K, V> extends HashMap<K, V> {

		private static final long serialVersionUID = 2900939583997256189L;
		private LinkedList<K> addOrder = new LinkedList<K>();
		private int maxSize;

		public CacheMap(int maxSize) {
			super(maxSize);
			this.maxSize = maxSize;
		}

		@Override
		public synchronized V put(K key, V value) {
			if (size() == maxSize) {
				// remove oldest item
				remove(addOrder.removeFirst());
			}
			addOrder.add(key);
			return super.put(key, value);
		}
	}

	/**
	 * A hash set that will remove the oldest items once its size reaches
	 * a specified max size.
	 *
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	protected static class CacheSet<V> extends HashSet<V> {

		private static final long serialVersionUID = 2900939583997256189L;
		private LinkedList<V> addOrder = new LinkedList<V>();
		private int maxSize;

		public CacheSet(int maxSize) {
			super(maxSize);
			this.maxSize = maxSize;
		}

		@Override
		public synchronized boolean add(V e) {
			if (size() == maxSize) {
				// remove oldest item
				remove(addOrder.removeFirst());
			}
			addOrder.add(e);
			return super.add(e);
		}
	}

	/**
	 * Gets the cache for entity instances. If no cache exists,
	 * it will be created.
	 * @return the entity cache (never null).
	 */
	synchronized Map<Object, T> getEntityCache() {
		if (entityCache == null) {
			entityCache = new CacheMap<Object, T>(getMaxCacheSize());
		}
		return entityCache;
	}

	/**
	 * Gets the cache for filter results. If no cache exists,
	 * it will be created.
	 *
	 * @return the filter cache (never null).
	 */
	synchronized Map<Filter, FilterCacheEntry> getFilterCache() {
		if (filterCache == null) {
			filterCache = new CacheMap<Filter, FilterCacheEntry>(
					MAX_FILTER_CACHE_SIZE);
		}
		return filterCache;
	}

	/**
	 * Gets the cache entry for the specified filter. If no cache entry exists,
	 * it will be created.
	 *
	 * @param filter the filter whose cache entry to fetch (may be null).
	 * @return the filter cache entry (never null).
	 */
	synchronized FilterCacheEntry getFilterCacheEntry(Filter filter) {
		if (filter == null) {
			filter = NULL_FILTER;
		}
		FilterCacheEntry e = getFilterCache().get(filter);
		if (e == null) {
			e = new FilterCacheEntry(filter);
			getFilterCache().put(filter, e);
		}
		return e;
	}

	public synchronized void flush() {
		if (entityCache != null) {
			entityCache.clear();
		}
		if (filterCache != null) {
			filterCache.clear();
		}
	}

	public int getMaxCacheSize() {
		return maxCacheSize;
	}

	public boolean isCacheInUse() {
		return cacheInUse;
	}

	/**
	 * Turns the cache on or off.
	 * @param cacheInUse true to turn on the cache, false to turn it off.
	 */
	public void setCacheInUse(boolean cacheInUse) {
		this.cacheInUse = cacheInUse;
		if (!cacheInUse) {
			flush();
		}
	}

	/**
	 * Sets the maximum number of items to keep in each cache. This method
	 * will cause any existing caches to be flushed and re-created.
	 *
	 * @param maxSize the maximum cache size to set.
	 */
	public void setMaxCacheSize(int maxSize) {
		this.maxCacheSize = maxSize;
		entityCache = null;
		filterCache = null;
	}

	public boolean containsEntity(Object entityId, Filter filter) {
		if (!isCacheInUse()) {
			return entityProvider.doContainsEntity(entityId, filter);
		} else {
			return getFilterCacheEntry(filter).containsId(entityId);
		}
	}

	public List<Object> getAllEntityIdentifiers(Filter filter,
			List<SortBy> sortBy) {
		if (sortBy == null) {
			sortBy = Collections.emptyList();
		}
		if (!isCacheInUse()) {
			return entityProvider.doGetAllEntityIdentifiers(filter, sortBy);
		} else {
			return getFilterCacheEntry(filter).getAllIds(sortBy);
		}
	}

	public synchronized T getEntity(Object entityId) {
		if (!isCacheInUse()) {
			return entityProvider.doGetEntity(entityId);
		} else {
			T entity = getEntityCache().get(entityId);
			if (entity == null) {
				// TODO Should we fetch several entities at once?
				entity = entityProvider.doGetEntity(entityId);
				if (entity == null) {
					return null;
				}
				getEntityCache().put(entityId, entity);
			}
			return cloneEntityIfNeeded(entity);
		}
	}

	/**
	 * Returns a clone of <code>entity</code> if {@link #isCloneCachedEntities() } is true.
	 * @param entity the entity to clone (must not be null and must be an instance of {@link Cloneable}).
	 * @return the cloned entity.
	 */
	@SuppressWarnings("unchecked")
	protected T cloneEntityIfNeeded(T entity) {
		if (isCloneCachedEntities()) {
			assert entity instanceof Cloneable : "entity is not cloneable";
			try {
				Method m = entity.getClass().getMethod("clone");
				T copy = (T) m.invoke(entity);
				return copy;
			} catch (Exception e) {
				throw new UnsupportedOperationException(
						"Could not clone entity", e);
			}
		} else {
			return entity;
		}
	}

	public boolean isEntitiesDetached() {
		return isCacheInUse() || entityProvider.isEntitiesDetached();
	}

	public boolean isCloneCachedEntities() {
		return cloneCachedEntities;
	}

	public void setCloneCachedEntities(boolean clone)
			throws UnsupportedOperationException {
		if (!clone) {
			this.cloneCachedEntities = false;
		} else {
			if (Cloneable.class.isAssignableFrom(entityProvider.getEntityClassMetadata().
					getMappedClass())) {
				this.cloneCachedEntities = true;
			} else {
				throw new UnsupportedOperationException(
						"Entity class is not cloneable");
			}
		}
	}

	public int getEntityCount(Filter filter) {
		if (!isCacheInUse()) {
			return entityProvider.doGetEntityCount(filter);
		} else {
			return getFilterCacheEntry(filter).getEntityCount();
		}
	}

	public Object getEntityIdentifierAt(Filter filter, List<SortBy> sortBy,
			int index) {
		if (sortBy == null) {
			sortBy = Collections.emptyList();
		}
		if (!isCacheInUse()) {
			return entityProvider.doGetEntityIdentifierAt(filter, sortBy, index);
		} else {
			return getFilterCacheEntry(filter).getIdAt(sortBy, index);
		}
	}

	public Object getFirstEntityIdentifier(Filter filter, List<SortBy> sortBy) {
		if (sortBy == null) {
			sortBy = Collections.emptyList();
		}
		if (!isCacheInUse()) {
			return entityProvider.doGetFirstEntityIdentifier(filter, sortBy);
		} else {
			return getFilterCacheEntry(filter).getFirstId(sortBy);
		}
	}

	public Object getLastEntityIdentifier(Filter filter, List<SortBy> sortBy) {
		if (sortBy == null) {
			sortBy = Collections.emptyList();
		}
		if (!isCacheInUse()) {
			return entityProvider.doGetLastEntityIdentifier(filter, sortBy);
		} else {
			return getFilterCacheEntry(filter).getLastId(sortBy);
		}
	}

	public Object getNextEntityIdentifier(Object entityId, Filter filter,
			List<SortBy> sortBy) {
		if (sortBy == null) {
			sortBy = Collections.emptyList();
		}
		if (!isCacheInUse()) {
			return entityProvider.doGetNextEntityIdentifier(entityId, filter, sortBy);
		} else {
			return getFilterCacheEntry(filter).getNextId(entityId, sortBy);
		}
	}

	public Object getPreviousEntityIdentifier(Object entityId, Filter filter,
			List<SortBy> sortBy) {
		if (sortBy == null) {
			sortBy = Collections.emptyList();
		}
		if (!isCacheInUse()) {
			return entityProvider.doGetPreviousEntityIdentifier(entityId, filter, sortBy);
		} else {
			return getFilterCacheEntry(filter).getPreviousId(entityId, sortBy);
		}
	}
}
