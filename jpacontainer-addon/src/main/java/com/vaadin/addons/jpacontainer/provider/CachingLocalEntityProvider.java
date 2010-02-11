/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
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
package com.vaadin.addons.jpacontainer.provider;

import com.vaadin.addons.jpacontainer.CachingEntityProvider;
import com.vaadin.addons.jpacontainer.Filter;
import com.vaadin.addons.jpacontainer.SortBy;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * TODO Document me!
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class CachingLocalEntityProvider<T> extends LocalEntityProvider<T>
        implements CachingEntityProvider<T> {

    private int maxCacheSize = 1000;
    private boolean cacheInUse = false;
    private boolean cloneCachedEntities = false;
    /**
     * 
     */
    protected static final int CHUNK_SIZE = 150;
    /**
     * 
     */
    protected static Filter NULL_FILTER = new Filter() {

        public String toQLString() {
            return "";
        }

        public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
            return "";
        }
    };

    /**
     *
     * @param entityClass
     */
    public CachingLocalEntityProvider(Class<T> entityClass) {
        super(entityClass);
    }

    /**
     *
     * @param entityClass
     * @param entityManager
     */
    public CachingLocalEntityProvider(Class<T> entityClass,
            EntityManager entityManager) {
        super(entityClass, entityManager);
    }

    /**
     *
     */
    protected static class IdListEntry implements Serializable {

        public List<Object> idList;
        public int listOffset;
        public boolean containsAll;
    }

    /**
     *
     */
    protected class FilterCacheEntry implements Serializable {

        private Filter filter;
        private Integer entityCount;
        // TODO We need to limit the size of this cache as well!
        public Map<List<SortBy>, IdListEntry> idListMap = new HashMap<List<SortBy>, IdListEntry>();

        public FilterCacheEntry(Filter filter) {
            this.filter = filter;
        }

        public synchronized int getEntityCount() {
            if (entityCount == null) {
                entityCount = CachingLocalEntityProvider.super.getEntityCount(
                        getFilter());
            }
            return entityCount;
        }

        public synchronized Object getIdAt(List<SortBy> sortBy, int index) {
            IdListEntry entry = idListMap.get(sortBy);
            if (entry == null) {
                entry = new IdListEntry();
                idListMap.put(sortBy, entry);
            }

            if (!entry.containsAll && (entry.idList == null || index < entry.listOffset || index >= entry.listOffset + entry.idList.
                    size())) {
                // TODO Improve this code so that the cache grows until it reaches a certain max size
                entry.idList = getIds(getFilter(), sortBy, index, CHUNK_SIZE);
                entry.listOffset = index;
            }
            int i = index - entry.listOffset;
            if (entry.idList.size() <= i) {
                return null;
            }
            return entry.idList.get(i);
        }

        public synchronized List<Object> getAllIds(List<SortBy> sortBy) {
            IdListEntry entry = idListMap.get(sortBy);
            if (entry == null) {
                entry = new IdListEntry();
                idListMap.put(sortBy, entry);
            }
            if (!entry.containsAll) {
                entry.idList = getIds(getFilter(), sortBy, 0, -1);
                entry.listOffset = 0;
                entry.containsAll = true;
            }
            return Collections.unmodifiableList(entry.idList);
        }

        public Filter getFilter() {
            return filter == NULL_FILTER ? null : filter;
        }
    }

    /**
     * TODO Document me!
     * 
     * @param filter
     * @param sortBy
     * @param startFrom
     * @param fetchMax
     * @return
     */
    protected List<Object> getIds(Filter filter, List<SortBy> sortBy,
            int startFrom, int fetchMax) {
        Query query = createFilteredQuery("obj." + getEntityClassMetadata().
                getIdentifierProperty().getName(), "obj", filter, addPrimaryKeyToSortList(
                sortBy), false,
                null);
        query.setFirstResult(startFrom);
        if (fetchMax > 0) {
            query.setMaxResults(fetchMax);
        }
        return query.getResultList();
    }
    private Map<Object, T> entityCache;
    private Map<Filter, FilterCacheEntry> filterCache;

    private class CacheMap<K, V> extends HashMap<K, V> {

        private LinkedList<K> addOrder = new LinkedList<K>();

        public CacheMap() {
        }

        public CacheMap(int initialCapacity) {
            super(initialCapacity);
        }

        public CacheMap(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }

        @Override
        public V put(K key, V value) {
            if (size() == getMaxCacheSize()) {
                // remove oldest item
                remove(addOrder.pop());
            }
            addOrder.add(key);
            return super.put(key, value);
        }
    }

    /**
     * 
     * @return
     */
    protected synchronized Map<Object, T> getEntityCache() {
        if (entityCache == null) {
            entityCache = new CacheMap<Object, T>();
        }
        return entityCache;
    }

    /**
     * 
     * @return
     */
    protected synchronized Map<Filter, FilterCacheEntry> getFilterCache() {
        if (filterCache == null) {
            filterCache = new CacheMap<Filter, FilterCacheEntry>();
        }
        return filterCache;
    }

    /**
     *
     * @param filter
     * @return
     */
    protected FilterCacheEntry getFilterCacheEntry(Filter filter) {
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

    public void flush() {
        entityCache.clear();
        filterCache.clear();
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public boolean isCacheInUse() {
        return cacheInUse;
    }

    /**
     * 
     * @param cacheInUse
     */
    public void setCacheInUse(boolean cacheInUse) {
        this.cacheInUse = cacheInUse;
    }

    public void setMaxCacheSize(int maxSize) throws
            UnsupportedOperationException {
        this.maxCacheSize = maxSize;
        entityCache = null;
        filterCache = null;
    }

    @Override
    public boolean containsEntity(Object entityId, Filter filter) {
        if (!isCacheInUse()) {
            return super.containsEntity(entityId, filter);
        } else {
            // TODO Implement me!
            System.out.println("Warning - cache support not implemented yet");
            return super.containsEntity(entityId, filter);
        }
    }

    @Override
    public List<Object> getAllEntityIdentifiers(Filter filter,
            List<SortBy> sortBy) {
        if (!isCacheInUse()) {
            return super.getAllEntityIdentifiers(filter, sortBy);
        } else {
            return getFilterCacheEntry(filter).getAllIds(sortBy);
        }
    }

    @Override
    public T getEntity(Object entityId) {
        if (!isCacheInUse()) {
            return super.getEntity(entityId);
        } else {
            T entity = getEntityCache().get(entityId);
            if (entity == null) {
                // TODO Should we fetch several entities at once?
                entity = super.getEntity(entityId);
                if (entity == null) {
                    return null;
                }
                getEntityCache().put(entityId, entity);
            }
            return cloneEntityIfNeeded(entity);
        }
    }

    /**
     * 
     * @param entity
     * @return
     */
    protected T cloneEntityIfNeeded(T entity) {
        if (isCloneCachedEntities()) {
            assert entity instanceof Cloneable : "entity is not cloneable";
            try {
                Method m = entity.getClass().getMethod("clone");
                T copy = (T) m.invoke(entity);
                return copy;
            } catch (Exception e) {
                throw new UnsupportedOperationException("Could not clone entity",
                        e);
            }
        } else {
            return entity;
        }
    }

    @Override
    public boolean isEntitiesDetached() {
        return isCacheInUse() || super.isEntitiesDetached();
    }

    public boolean isCloneCachedEntities() {
        return cloneCachedEntities;
    }

    public void setCloneCachedEntities(boolean clone) throws
            UnsupportedOperationException {
        if (!clone) {
            this.cloneCachedEntities = false;
        } else {
            if (Cloneable.class.isAssignableFrom(getEntityClassMetadata().
                    getMappedClass())) {
                this.cloneCachedEntities = true;
            } else {
                throw new UnsupportedOperationException(
                        "Entity class is not cloneable");
            }
        }
    }

    @Override
    public int getEntityCount(Filter filter) {
        if (!isCacheInUse()) {
            return super.getEntityCount(filter);
        } else {
            return getFilterCacheEntry(filter).getEntityCount();
        }
    }

    @Override
    public Object getEntityIdentifierAt(Filter filter, List<SortBy> sortBy,
            int index) {
        if (!isCacheInUse()) {
            return super.getEntityIdentifierAt(filter, sortBy, index);
        } else {
            return getFilterCacheEntry(filter).getIdAt(sortBy, index);
        }
    }

    @Override
    public Object getFirstEntityIdentifier(Filter filter, List<SortBy> sortBy) {
        if (!isCacheInUse()) {
            return super.getFirstEntityIdentifier(filter, sortBy);
        } else {
            return getEntityIdentifierAt(filter, sortBy, 0);
        }
    }

    @Override
    public Object getLastEntityIdentifier(Filter filter, List<SortBy> sortBy) {
        if (!isCacheInUse()) {
            return super.getLastEntityIdentifier(filter, sortBy);
        } else {
            return getEntityIdentifierAt(filter, sortBy,
                    getEntityCount(filter) - 1);
        }
    }

    @Override
    public Object getNextEntityIdentifier(Object entityId, Filter filter,
            List<SortBy> sortBy) {
        if (!isCacheInUse()) {
            return super.getNextEntityIdentifier(entityId, filter, sortBy);
        } else {
            // TODO Implement me!
            System.out.println(
                    "getNextEntityIdentifier: Warning - cache support not implemented yet");
            return super.getNextEntityIdentifier(entityId, filter, sortBy);
        }
    }

    @Override
    public Object getPreviousEntityIdentifier(Object entityId, Filter filter,
            List<SortBy> sortBy) {
        if (!isCacheInUse()) {
            return super.getPreviousEntityIdentifier(entityId, filter, sortBy);
        } else {
            // TODO Implement me!
            System.out.println(
                    "getPreviousEntityIdentifier: Warning - cache support not implemented yet");
            return super.getPreviousEntityIdentifier(entityId, filter, sortBy);
        }
    }
}
