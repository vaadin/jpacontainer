/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vaadin.addon.jpacontainer.provider;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.TypedQuery;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.QueryModifierDelegate;
import com.vaadin.addon.jpacontainer.SortBy;
import com.vaadin.v7.data.Container.Filter;
import com.vaadin.v7.data.Item;

/**
 * Delegate class that implements caching for {@link LocalEntityProvider}s and
 * their subclasses. This class is internal and should never be used outside of
 * JPAContainer.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
class CachingSupport<T> implements Serializable {

    private final LocalEntityProvider<T> entityProvider;
    private int maxCacheSize = 1000;
    private boolean cacheEnabled = true;
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

        public boolean passesFilter(Object itemId, Item item)
                throws UnsupportedOperationException {
            return true;
        }

        public boolean appliesToProperty(Object propertyId) {
            return false;
        }
    };
    /**
     * The max size of the filter cache (i.e. how many different filters to
     * cache).
     * 
     * @see #getFilterCache()
     */
    public static final int MAX_FILTER_CACHE_SIZE = 10;
    /**
     * The max size of the sort by cache for each filter. Thus, the maximum
     * number of cached filter-sortBy combinations is
     * <code>MAX_FILTER_CACHE_SIZE * MAX_SORTBY_CACHE_SIZE</code>.
     */
    public static final int MAX_SORTBY_CACHE_SIZE = 10;

    // TODO Make chunk size, filter cache size and sortBy cache size user
    // configurable.

    /**
     * Creates a new <code>CachingSupport</code> for the specified entity
     * provider.
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
     * @author Petter Holmström (Vaadin Ltd)
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
     * contains counterparts of most of the methods defined in
     * {@link EntityProvider}.
     * 
     * @author Petter Holmström (Vaadin Ltd)
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
         * @param filter
         *            the filter for which this cache should be created.
         */
        public FilterCacheEntry(Filter filter) {
            this.filter = filter;
        }

        /**
         * Gets the number of entities that match this particular filter.
         * 
         * @return the number of entities.
         */
        public synchronized int getEntityCount(EntityContainer<T> container) {
            if(!isCachingPossible(container)) {
                return entityProvider.doGetEntityCount(container, getFilter());
            }
            if (entityCount == null) {
                entityCount = entityProvider.doGetEntityCount(container, getFilter());
            }
            return entityCount;
        }

        /**
         * @see EntityProvider#containsEntity(java.lang.Object,
         *      com.vaadin.addons.jpacontainer.Filter)
         */
        public synchronized boolean containsId(EntityContainer<T> container,
                Object entityId) {
            if (!idSet.contains(entityId)) {
                if (entityProvider.doContainsEntity(container, entityId,
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
         * @see EntityProvider#getFirstEntityIdentifier(com.vaadin.addons.jpacontainer.Filter,
         *      java.util.List)
         */
        public Object getFirstId(EntityContainer<T> container,
                List<SortBy> sortBy) {
            return getIdAt(container, sortBy, 0);
        }

        /**
         * @see EntityProvider#getNextEntityIdentifier(java.lang.Object,
         *      com.vaadin.addons.jpacontainer.Filter, java.util.List)
         */
        public synchronized Object getNextId(EntityContainer<T> container,
                Object entityId, List<SortBy> sortBy) {
            IdListEntry entry = idListMap.get(sortBy);
            if (entry == null) {
                entry = new IdListEntry();
                entry.idList = new ArrayList<Object>();
                entry.listOffset = -1;
                idListMap.put(sortBy, entry);
            }
            int index = entry.idList.indexOf(entityId);
            if (index == -1) {
                entry.idList = new ArrayList<Object>(getNextIds(container,
                        getFilter(), sortBy, entityId, CHUNK_SIZE));
                if (entry.idList.isEmpty()) {
                    return null;
                } else {
                    return entry.idList.get(0);
                }
            } else {
                if (index == entry.idList.size() - 1) {
                    if (getMaxCacheSize() > -1
                            && entry.idList.size() + CHUNK_SIZE > getMaxCacheSize()) {
                        // Clean up the cache
                        if (entry.idList.size() <= CHUNK_SIZE) {
                            entry.idList.clear();
                            index = -1;
                        } else {
                            entry.idList.subList(0, CHUNK_SIZE).clear();
                            index -= CHUNK_SIZE;
                        }
                    }
                    entry.idList.addAll(getNextIds(container, getFilter(),
                            sortBy, entityId, CHUNK_SIZE));
                }
                if (index + 1 == entry.idList.size()) {
                    return null;
                } else {
                    return entry.idList.get(index + 1);
                }
            }
        }

        /**
         * @see EntityProvider#getPreviousEntityIdentifier(java.lang.Object,
         *      com.vaadin.addons.jpacontainer.Filter, java.util.List)
         */
        public synchronized Object getPreviousId(EntityContainer<T> container,
                Object entityId, List<SortBy> sortBy) {
            IdListEntry entry = idListMap.get(sortBy);
            if (entry == null) {
                entry = new IdListEntry();
                entry.idList = new ArrayList<Object>();
                entry.listOffset = -1;
                idListMap.put(sortBy, entry);
            }
            int index = entry.idList.indexOf(entityId);
            if (index == -1) {
                List<Object> objects = getPreviousIds(container, getFilter(),
                        sortBy, entityId, CHUNK_SIZE);
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
                    List<Object> objects = getPreviousIds(container,
                            getFilter(), sortBy, entityId, CHUNK_SIZE);
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
                    if (getMaxCacheSize() > -1
                            && entry.idList.size() + CHUNK_SIZE > getMaxCacheSize()) {
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
         * @see EntityProvider#getLastEntityIdentifier(com.vaadin.addons.jpacontainer.Filter,
         *      java.util.List)
         */
        public Object getLastId(EntityContainer<T> container,
                List<SortBy> sortBy) {
            return getIdAt(container, sortBy, getEntityCount(container) - 1);
        }

        /**
         * Informs the cache that <code>entityId</code> has been invalidated
         * (changed or removed). If the entityId is currently in cache, the
         * cache is flushed, forcing the data to be fetched from the database
         * when requested the next time.
         * 
         * @param entityId
         *            the entityId to invalidate.
         */
        public synchronized void invalidate(Object entityId) {
            // Clear the caches to force the data to be re-fetched from the
            // database
            // in case the ordering has changed
            idListMap.clear();
            // Removing the entity Id from the Id cache should be enough
            idSet.remove(entityId);
        }

        /**
         * @see EntityProvider#getEntityIdentifierAt(com.vaadin.addons.jpacontainer.Filter,
         *      java.util.List, int)
         */
        public synchronized Object getIdAt(EntityContainer<T> container,
                List<SortBy> sortBy, int index) {
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
                    if (getMaxCacheSize() > -1
                            && entry.idList.size() + CHUNK_SIZE > getMaxCacheSize()) {
                        // Clean up the cache
                        if (entry.idList.size() <= CHUNK_SIZE) {
                            entry.idList.clear();
                        } else {
                            entry.idList.subList(
                                    entry.idList.size() - CHUNK_SIZE,
                                    entry.idList.size()).clear();
                        }
                    }
                    ArrayList<Object> l = new ArrayList<Object>(CHUNK_SIZE
                            + entry.idList.size());
                    int startFrom = index - CHUNK_SIZE;
                    if (startFrom < 0) {
                        startFrom = 0;
                    }
                    l.addAll(getIds(container, getFilter(), sortBy, startFrom, index
                            - startFrom + 1));
                    l.addAll(entry.idList);
                    entry.idList = l;
                    entry.listOffset = startFrom;
                } else if (entry.listOffset > -1
                        && index == entry.listOffset + entry.idList.size()) {
                    // It is possible that maxCacheSize < CHUNK_SIZE => we have
                    // to make sure that the list is at least as big as
                    // CHUNK_SIZE
                    if (getMaxCacheSize() > -1
                            && entry.idList.size() + CHUNK_SIZE > getMaxCacheSize()) {
                        // Clean up the cache
                        if (entry.idList.size() <= CHUNK_SIZE) {
                            entry.listOffset += entry.idList.size();
                            entry.idList.clear();
                        } else {
                            entry.idList.subList(0, CHUNK_SIZE).clear();
                            entry.listOffset += CHUNK_SIZE;
                        }
                    }
                    entry.idList.addAll(getIds(container, getFilter(), sortBy, index,
                            CHUNK_SIZE));
                } else {
                    entry.idList.clear();
                    entry.idList.addAll(getIds(container, getFilter(), sortBy, index,
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
         * @see EntityProvider#getAllEntityIdentifiers(com.vaadin.addons.jpacontainer.Filter,
         *      java.util.List)
         */
        public synchronized List<Object> getAllIds(EntityContainer<T> container, List<SortBy> sortBy) {
            IdListEntry entry = idListMap.get(sortBy);
            if (entry == null) {
                entry = new IdListEntry();
                idListMap.put(sortBy, entry);
            }
            if (!entry.containsAll) {
                entry.idList = new ArrayList<Object>(getIds(container, getFilter(),
                        sortBy, 0, -1));
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
     * 
     * @param entity
     */
    public synchronized void entityAdded(T entity) {
        // TODO Do something smarter than flushing the entire cache!
        flush();
        // This is currently obsolete, but when above todo is implemented,
        // uncomment this or somehow increment relevat filter caches.
        // invalidateSize();

    }

    /**
     * Gets all the identifiers that match <code>filter</code>, sorted by
     * <code>sortBy</code>, starting with the identifier at position
     * <code>startFrom</code> and retrieving a maximum number of
     * <code>fetchMax</code> items.
     * 
     * @param filter
     *            the filter to apply, if any (may be null).
     * @param sortBy
     *            the ordering information (may not be null).
     * @param startFrom
     *            the index of the first identifier to retrieve.
     * @param fetchMax
     *            the maximum number of identifiers to retrieve, or 0 to
     *            retrieve all.
     * @return a list of identifiers.
     */
    protected List<Object> getIds(EntityContainer<T> container, Filter filter,
            List<SortBy> sortBy, int startFrom, int fetchMax) {
        TypedQuery<Object> query = entityProvider.createFilteredQuery(
                container,
                Arrays.asList(entityProvider.getEntityClassMetadata()
                        .getIdentifierProperty().getName()), filter,
                entityProvider.addPrimaryKeyToSortList(sortBy), false);
        query.setFirstResult(startFrom);
        if (fetchMax > 0) {
            query.setMaxResults(fetchMax);
        }
        return query.getResultList();
    }

    /**
     * Gets all the identifiers that match <code>filter</code>, sorted by
     * <code>sortBy</code>, starting with the identifier next to
     * <code>startFrom</code> and retrieving a maximum number of
     * <code>fetchMax</code> items. If <code>startFrom</code> is at position n,
     * then item n+1 will be the first item in the returnde list, n+2 the
     * second, etc.
     * 
     * @param filter
     *            the filter to apply, if any (may be null).
     * @param sortBy
     *            the ordering information (may not be null).
     * @param startFrom
     *            the entityId prioir to the first identifier to retrieve.
     * @param fetchMax
     *            the maximum number of identifiers to retrieve, or 0 to
     *            retrieve all.
     * @return a list of identifiers.
     */
    protected List<Object> getNextIds(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy, Object startFrom, int fetchMax) {
        TypedQuery<Object> query = entityProvider.createSiblingQuery(container,
                startFrom, filter, sortBy, false);
        if (fetchMax > 0) {
            query.setMaxResults(fetchMax);
        }
        return query.getResultList();
    }

    /**
     * Gets all the identifiers that match <code>filter</code>, sorted backwards
     * by <code>sortBy</code>, starting with the identifier prior to
     * <code>startFrom</code> and retrieving a maximum number of
     * <code>fetchMax</code> items. If <code>startFrom</code> is at position n,
     * then item n-1 will be the first item in the returned list, n-2 the
     * second, etc.
     * 
     * @param filter
     *            the filter to apply, if any (may be null).
     * @param sortBy
     *            the ordering information (may not be null).
     * @param startFrom
     *            the entityId next to the first identifier to retrieve.
     * @param fetchMax
     *            the maximum number of identifiers to retrieve, or 0 to
     *            retrieve all.
     * @return a list of identifiers.
     */
    protected List<Object> getPreviousIds(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy, Object startFrom, int fetchMax) {
        TypedQuery<Object> query = entityProvider.createSiblingQuery(container,
                startFrom, filter, sortBy, true);
        if (fetchMax > 0) {
            query.setMaxResults(fetchMax);
        }
        return query.getResultList();
    }

    private Map<Object, T> entityCache;
    private Map<Filter, FilterCacheEntry> filterCache;

    /**
     * A hash map that will remove the oldest items once its size reaches a
     * specified max size.
     * 
     * @author Petter Holmström (Vaadin Ltd)
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
     * A hash set that will remove the oldest items once its size reaches a
     * specified max size.
     * 
     * @author Petter Holmström (Vaadin Ltd)
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
     * Gets the cache for entity instances. If no cache exists, it will be
     * created.
     * 
     * @return the entity cache (never null).
     */
    synchronized Map<Object, T> getEntityCache() {
        if (entityCache == null) {
            entityCache = new CacheMap<Object, T>(getMaxCacheSize());
        }
        return entityCache;
    }

    /**
     * Gets the cache for filter results. If no cache exists, it will be
     * created.
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
     * @param filter
     *            the filter whose cache entry to fetch (may be null).
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

    /**
     * Check whether caching is possible or not. Caching is not possible if
     * there is a {@link QueryModifierDelegate}, that modifies the filters
     * applied to queries, attached to the entity provider.
     * 
     * @return true if caching is possible
     */
    public boolean isCachingPossible(EntityContainer<T> container) {
        if (container != null && container.getQueryModifierDelegate() != null) {
            return false;
        }
        QueryModifierDelegate d = entityProvider.getQueryModifierDelegate();
        if (d != null) {
            // Try to tell the delegate that filters will be added and pass in
            // all nulls. If the delegate throws an NPE it most probably
            // modifies the filters, which means that we cannot reliably cache
            // anything at this level.
            try {
                d.filtersWillBeAdded(null, null, null);
            } catch (NullPointerException npe) {
                // The delegate modifies filters
                return false;
            }
        }
        return true;
    }

    /**
     * Only returns true if both {@link #isCacheEnabled()} and
     * {@link #isCachingPossible()} are true.
     * 
     * @return true if the caching mechanism is actually used.
     */
    public boolean usesCache(EntityContainer<T> container) {
        return isCacheEnabled() && isCachingPossible(container);
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * Turns the cache on or off.
     * 
     * @param cacheEnabled
     *            true to turn on the cache, false to turn it off.
     */
    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        if (!cacheEnabled) {
            flush();
        }
    }

    /**
     * Sets the maximum number of items to keep in each cache. This method will
     * cause any existing caches to be flushed and re-created.
     * 
     * @param maxSize
     *            the maximum cache size to set.
     */
    public void setMaxCacheSize(int maxSize) {
        this.maxCacheSize = maxSize;
        entityCache = null;
        filterCache = null;
    }

    public boolean containsEntity(EntityContainer<T> container,
            Object entityId, Filter filter) {
        if (usesCache(container)) {
            return getFilterCacheEntry(filter).containsId(container, entityId);
        } else {
            return entityProvider.doContainsEntity(container, entityId, filter);
        }
    }

    public List<Object> getAllEntityIdentifiers(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }
        if (usesCache(container)) {
            return getFilterCacheEntry(filter).getAllIds(container, sortBy);
        } else {
            return entityProvider.doGetAllEntityIdentifiers(container, filter,
                    sortBy);
        }
    }

    public synchronized T getEntity(EntityContainer<T> container,
            Object entityId) {
        if (usesCache(container)) {
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
        } else {
            return entityProvider.doGetEntity(entityId);
        }
    }

    /**
     * Returns a clone of <code>entity</code> if
     * {@link #isCloneCachedEntities() } is true.
     * 
     * @param entity
     *            the entity to clone (must not be null and must be an instance
     *            of {@link Cloneable}).
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
        return usesCache(null) || entityProvider.isEntitiesDetached();
    }

    public boolean isCloneCachedEntities() {
        return cloneCachedEntities;
    }

    public void setCloneCachedEntities(boolean clone)
            throws UnsupportedOperationException {
        if (!clone) {
            this.cloneCachedEntities = false;
        } else {
            if (Cloneable.class.isAssignableFrom(entityProvider
                    .getEntityClassMetadata().getMappedClass())) {
                this.cloneCachedEntities = true;
            } else {
                throw new UnsupportedOperationException(
                        "Entity class is not cloneable");
            }
        }
    }

    public int getEntityCount(EntityContainer<T> container, Filter filter) {
        if (usesCache(container)) {
            return getFilterCacheEntry(filter).getEntityCount(container);
        } else {
            return entityProvider.doGetEntityCount(container, filter);
        }
    }

    public Object getEntityIdentifierAt(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy, int index) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }
        if (usesCache(container)) {
            return getFilterCacheEntry(filter).getIdAt(container, sortBy, index);
        } else {
            return entityProvider.doGetEntityIdentifierAt(container, filter,
                    sortBy, index);
        }
    }

    public Object getFirstEntityIdentifier(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }
        if (usesCache(container)) {
            return getFilterCacheEntry(filter).getFirstId(container, sortBy);
        } else {
            return entityProvider.doGetFirstEntityIdentifier(container, filter,
                    sortBy);
        }
    }

    public Object getLastEntityIdentifier(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }
        if (usesCache(container)) {
            return getFilterCacheEntry(filter).getLastId(container, sortBy);
        } else {
            return entityProvider.doGetLastEntityIdentifier(container, filter,
                    sortBy);
        }
    }

    public Object getNextEntityIdentifier(EntityContainer<T> container,
            Object entityId, Filter filter, List<SortBy> sortBy) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }
        if (usesCache(container)) {
            return getFilterCacheEntry(filter).getNextId(container, entityId,
                    sortBy);
        } else {
            return entityProvider.doGetNextEntityIdentifier(container,
                    entityId, filter, sortBy);
        }
    }

    public Object getPreviousEntityIdentifier(EntityContainer<T> container,
            Object entityId, Filter filter, List<SortBy> sortBy) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }
        if (usesCache(container)) {
            return getFilterCacheEntry(filter).getPreviousId(container,
                    entityId, sortBy);
        } else {
            return entityProvider.doGetPreviousEntityIdentifier(container,
                    entityId, filter, sortBy);
        }
    }

    public void invalidateSize() {
        // TODO review synchronization of this whole class
        Object[] array = filterCache.keySet().toArray();
        for (Object filter : array) {
            FilterCacheEntry filterCacheEntry = filterCache.get(filter);
            if (filterCacheEntry.entityCount != null) {
                synchronized (filterCacheEntry.entityCount) {
                    filterCacheEntry.entityCount = null;
                }
            }
        }

    }

    public void entityRemoved(Object entityId) {
        invalidate(entityId, false);
        invalidateSize();
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        if (entityCache != null) {
            entityCache.clear();
        }
        if (filterCache != null) {
            filterCache.clear();
        }
    }
}
