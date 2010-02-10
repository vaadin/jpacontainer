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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * TODO Document me!
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class CachingLocalEntityProvider<T> extends LocalEntityProvider<T> implements CachingEntityProvider<T> {

    private CacheManager cacheManager;
    private Cache entityCache;
    private Cache filterCache;
    private int maxCacheSize = 1000;
    private boolean cacheInUse = false;
    protected static final long CACHE_TIME_TO_LIVE = 600;
    protected static final long CACHE_TIME_TO_IDLE = 600;
    protected static final int CHUNK_SIZE = 150;
    protected static Filter NULL_FILTER = new Filter() {

        public String toQLString() {
            return "";
        }

        public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
            return "";
        }
    };

    protected static class IdListEntry implements Serializable {

        public List<Object> idList;
        public int listOffset;
        public boolean containsAll;
    }

    protected class FilterCacheEntry implements Serializable {

        public final Filter filter;
        private Integer entityCount;
        // TODO We need to limit the size of this cache as well!
        public Map<List<SortBy>, IdListEntry> idListMap = new HashMap<List<SortBy>, IdListEntry>();

        public FilterCacheEntry(Filter filter) {
            this.filter = filter;
        }

        public synchronized int getEntityCount() {
            if (entityCount == null) {
                entityCount = CachingLocalEntityProvider.super.getEntityCount(filter);
            }
            return entityCount;
        }

        public synchronized Object getIdAt(List<SortBy> sortBy, int index) {
            IdListEntry entry = idListMap.get(sortBy);
            if (entry == null) {
                entry = new IdListEntry();
                idListMap.put(sortBy, entry);
            }

            if (!entry.containsAll && (entry.idList == null || index < entry.listOffset || index >= entry.listOffset + entry.idList.size())) {
                // TODO Improve this code so that the cache grows until it reaches a certain max size
                entry.idList = getIds(filter, sortBy, index, CHUNK_SIZE);
            }
            return entry.idList.get(index - entry.listOffset);
        }

        public synchronized List<Object> getAllIds(List<SortBy> sortBy) {
            IdListEntry entry = idListMap.get(sortBy);
            if (entry == null) {
                entry = new IdListEntry();
                idListMap.put(sortBy, entry);
            }
            if (!entry.containsAll) {
                entry.idList = getIds(filter, sortBy, 0, -1);
                entry.listOffset = 0;
                entry.containsAll = true;
            }
            return Collections.unmodifiableList(entry.idList);
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
    protected List<Object> getIds(Filter filter, List<SortBy> sortBy, int startFrom, int fetchMax) {
        Query query = createFilteredQuery("obj." + getEntityClassMetadata().
                getIdentifierProperty().getName(), "obj", filter, sortBy, false,
                null);
        query.setFirstResult(startFrom);
        if (fetchMax > 0) {
            query.setMaxResults(fetchMax);
        }
        return query.getResultList();
    }

    /**
     *
     * @param entityClass
     */
    public CachingLocalEntityProvider(Class<T> entityClass) {
        super(entityClass);
        cacheManager = CacheManager.create();
    }

    /**
     * 
     * @param entityClass
     * @param entityManager
     */
    public CachingLocalEntityProvider(Class<T> entityClass, EntityManager entityManager) {
        super(entityClass, entityManager);
        cacheManager = CacheManager.create();
    }

    /**
     * 
     * @return
     */
    protected synchronized Cache getEntityCache() {
        if (!cacheManager.cacheExists("entityCache")) {
            entityCache = new Cache("entityCache", maxCacheSize, false, true, CACHE_TIME_TO_LIVE, CACHE_TIME_TO_IDLE);
            cacheManager.addCache("entityCache");
            entityCache = cacheManager.getCache("entityCache");
        }
        return entityCache;
    }

    /**
     * 
     * @return
     */
    protected synchronized Cache getFilterCache() {
        if (!cacheManager.cacheExists("filterCache")) {
            filterCache = new Cache("filterCache", maxCacheSize, false, true, CACHE_TIME_TO_LIVE, CACHE_TIME_TO_IDLE);
            cacheManager.addCache("filterCache");
            filterCache = cacheManager.getCache("filterCache");
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
        Element e = getFilterCache().get(filter);
        if (e == null) {
            FilterCacheEntry entry = new FilterCacheEntry(filter);
            e = new Element(filter, entry);
            getFilterCache().put(e);
        }
        return (FilterCacheEntry) e.getObjectValue();
    }

    public void flush() {
        cacheManager.clearAll();
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

    public void setMaxCacheSize(int maxSize) throws UnsupportedOperationException {
        if (maxSize != maxCacheSize) {
            this.maxCacheSize = maxSize;
            if (cacheManager.cacheExists("entityCache")) {
                cacheManager.removeCache("entityCache");
                entityCache = null;
            }
            if (cacheManager.cacheExists("filterCache")) {
                cacheManager.removeCache("filterCache");
                filterCache = null;
            }
        }
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
    public List<Object> getAllEntityIdentifiers(Filter filter, List<SortBy> sortBy) {
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
            Element e = getEntityCache().get(entityId);
            if (e == null) {
                // TODO Should we fetch several entities at once?
                T entity = super.getEntity(entityId);
                if (entity == null) {
                    return null;
                }
                e = new Element(entityId, entity);
                getEntityCache().put(e);
            }
            return (T) e.getObjectValue();
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
    public Object getEntityIdentifierAt(Filter filter, List<SortBy> sortBy, int index) {
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
            // TODO Implement me!
            System.out.println("Warning - cache support not implemented yet");
            return super.getFirstEntityIdentifier(filter, sortBy);
        }
    }

    @Override
    public Object getLastEntityIdentifier(Filter filter, List<SortBy> sortBy) {
        if (!isCacheInUse()) {
            return super.getLastEntityIdentifier(filter, sortBy);
        } else {
            // TODO Implement me!
            System.out.println("Warning - cache support not implemented yet");
            return super.getLastEntityIdentifier(filter, sortBy);
        }
    }

    @Override
    public Object getNextEntityIdentifier(Object entityId, Filter filter, List<SortBy> sortBy) {
        if (!isCacheInUse()) {
            return super.getNextEntityIdentifier(entityId, filter, sortBy);
        } else {
            // TODO Implement me!
            System.out.println("Warning - cache support not implemented yet");
            return super.getNextEntityIdentifier(entityId, filter, sortBy);
        }
    }

    @Override
    public Object getPreviousEntityIdentifier(Object entityId, Filter filter, List<SortBy> sortBy) {
        if (!isCacheInUse()) {
            return super.getPreviousEntityIdentifier(entityId, filter, sortBy);
        } else {
            // TODO Implement me!
            System.out.println("Warning - cache support not implemented yet");
            return super.getPreviousEntityIdentifier(entityId, filter, sortBy);
        }
    }

    @Override
    public T addEntity(T entity) {
        flush(); // TODO Replace with smarter code
        return super.addEntity(entity);
    }

    @Override
    public T updateEntity(T entity) {
        flush(); // TODO Replace with smarter code
        return super.updateEntity(entity);
    }

    @Override
    public void removeEntity(Object entityId) {
        flush(); // TODO Replace with smarter code
        super.removeEntity(entityId);
    }

    @Override
    public void updateEntityProperty(Object entityId, String propertyName, Object propertyValue) throws IllegalArgumentException {
        flush(); // TODO Replace with smarter code
        super.updateEntityProperty(entityId, propertyName, propertyValue);
    }
}
