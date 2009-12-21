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
package com.vaadin.addons.jpacontainer.provider;

import com.vaadin.addons.jpacontainer.EntityProvider;
import com.vaadin.addons.jpacontainer.Filter;
import com.vaadin.addons.jpacontainer.Filter.PropertyIdPreprocessor;
import com.vaadin.addons.jpacontainer.SortBy;
import com.vaadin.addons.jpacontainer.filter.CompositeFilter;
import com.vaadin.addons.jpacontainer.filter.Filters;
import com.vaadin.addons.jpacontainer.filter.Junction;
import com.vaadin.addons.jpacontainer.filter.ValueFilter;
import com.vaadin.addons.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.MetadataFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

/**
 * Document me!
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class LocalEntityProvider<T> implements EntityProvider<T>, Serializable {

    private EntityManager entityManager;
    private EntityClassMetadata<T> entityClassMetadata;

    /**
     *
     * @param entityClass
     * @param entityManager
     */
    public LocalEntityProvider(Class<T> entityClass, EntityManager entityManager) {
        assert entityClass != null : "entityClass must not be null";
        assert entityManager != null : "entityManager must not be null";
        this.entityManager = entityManager;
        this.entityClassMetadata = MetadataFactory.getInstance().
                getEntityClassMetadata(entityClass);

        if (entityClassMetadata.hasEmbeddedIdentifier()) {
            // TODO Add support for embedded identifiers
            throw new IllegalArgumentException(
                    "Embedded identifiers are currently not supported!");
        }
    }

    /**
     *
     * @return
     */
    protected EntityClassMetadata<T> getEntityClassMetadata() {
        return this.entityClassMetadata;
    }

    /**
     *
     * @return
     */
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    /**
     *
     * @param original
     * @return
     */
    protected List<SortBy> addPrimaryKeyToSortList(List<SortBy> original) {
        ArrayList<SortBy> newList = new ArrayList<SortBy>();
        newList.addAll(original);
        newList.add(new SortBy(getEntityClassMetadata().getIdentifierProperty().
                getName(), true));
        return Collections.unmodifiableList(newList);
    }

    /**
     *
     * @param fieldsToSelect
     * @param entityAlias
     * @param filter
     * @param propertyIdPreprocessor
     * @return
     */
    protected Query createUnsortedFilteredQuery(String fieldsToSelect,
            String entityAlias, Filter filter,
            PropertyIdPreprocessor propertyIdPreprocessor) {
        return createFilteredQuery(fieldsToSelect, entityAlias, filter, null,
                false, propertyIdPreprocessor);
    }

    /**
     *
     * @param fieldsToSelect
     * @param entityAlias
     * @param filter the filter to apply, or null if no filters should be applied.
     * @param sortBy the fields to sort by (must include at least one field), or null if the result should not be sorted at all.
     * @param swapSortOrder true to swap the sort order, false to use the sort order specified in <code>sortBy</code>. Only applies if <code>sortBy</code> is not null.
     * @param propertyIdPreprocessor the property ID preprocessor to pass to {@link Filter#toQLString(com.vaadin.addons.jpacontainer.filter.PropertyIdPreprocessor) },
     * or null to use a default preprocessor (should be sufficient in most cases).
     * @return the query (never null).
     */
    protected Query createFilteredQuery(String fieldsToSelect,
            final String entityAlias, Filter filter, List<SortBy> sortBy,
            boolean swapSortOrder,
            PropertyIdPreprocessor propertyIdPreprocessor) {
        assert fieldsToSelect != null : "fieldsToSelect must not be null";
        assert entityAlias != null : "entityAlias must not be null";
        assert sortBy == null || !sortBy.isEmpty() :
                "sortBy must be either null or non-empty";

        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        sb.append(fieldsToSelect);
        sb.append(" from ");
        sb.append(getEntityClassMetadata().getEntityName());
        sb.append(" as ");
        sb.append(entityAlias);

        if (filter != null) {
            sb.append(" where ");

            if (propertyIdPreprocessor == null) {
                sb.append(filter.toQLString(new PropertyIdPreprocessor() {

                    @Override
                    public String process(Object propertyId) {
                        return entityAlias + "." + propertyId;
                    }
                }));
            } else {
                sb.append(filter.toQLString(propertyIdPreprocessor));
            }
        }

        if (sortBy != null) {
            sb.append(" order by ");
            for (Iterator<SortBy> it = sortBy.iterator(); it.hasNext();) {
                SortBy sortedProperty = it.next();
                sb.append(entityAlias);
                sb.append(".");
                sb.append(sortedProperty.propertyId);
                if (sortedProperty.ascending != swapSortOrder) {
                    sb.append(" asc");
                } else {
                    sb.append(" desc");
                }
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
        }

        String queryString = sb.toString();
        Query query = getEntityManager().createQuery(queryString);
        if (filter != null) {
            setQueryParameters(query, filter);
        }
        return query;
    }

    private void setQueryParameters(Query query, Filter filter) {
        if (filter instanceof ValueFilter) {
            ValueFilter vf = (ValueFilter) filter;
            query.setParameter(vf.getQLParameterName(), vf.getValue());
        } else if (filter instanceof CompositeFilter) {
            for (Filter f : ((CompositeFilter) filter).getFilters()) {
                setQueryParameters(query, f);
            }
        }
    }

    @Override
    public boolean containsEntity(Object entityId, Filter filter) {
        assert entityId != null : "entityId must not be null";
        Filter entityIdFilter = Filters.eq(getEntityClassMetadata().
                getIdentifierProperty().getName(), entityId);
        Filter f;
        if (filter == null) {
            f = entityIdFilter;
        } else {
            f = Filters.and(entityIdFilter, filter);
        }
        Query query = createUnsortedFilteredQuery("count(obj)", "obj", f,
                null);
        return ((Long) query.getSingleResult()) == 1;
    }

    @Override
    public T getEntity(Object entityId) {
        assert entityId != null : "entityId must not be null";
        return getEntityManager().find(getEntityClassMetadata().getMappedClass(),
                entityId);
    }

    @Override
    public T getEntityAt(Filter filter,
            List<SortBy> sortBy, int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEntityCount(Filter filter) {
        Query query = createUnsortedFilteredQuery("count(obj)", "obj", filter,
                null);
        return ((Long) query.getSingleResult()).intValue();
    }

    @Override
    public Object getFirstEntityIdentifier(Filter filter,
            List<SortBy> sortBy) {
        assert sortBy != null : "sortBy must not be null";
        Query query = createFilteredQuery("obj." + getEntityClassMetadata().
                getIdentifierProperty().getName(), "obj", filter, addPrimaryKeyToSortList(
                sortBy), false,
                null);
        query.setMaxResults(1);
        List<?> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    @Override
    public Object getLastEntityIdentifier(Filter filter,
            List<SortBy> sortBy) {
        assert sortBy != null : "sortBy must not be null";
        Query query = createFilteredQuery("obj." + getEntityClassMetadata().
                getIdentifierProperty().getName(), "obj", filter, addPrimaryKeyToSortList(
                sortBy), true,
                null);
        query.setMaxResults(1);
        List<?> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * 
     * @param entityId
     * @param filter
     * @param sortBy
     * @param backwards
     * @return
     */
    protected Object getSibling(Object entityId, Filter filter,
            List<SortBy> sortBy, boolean backwards) {
        assert entityId != null : "entityId must not be null";
        assert sortBy != null : "sortBy must not be null";
        Filter limitingFilter;
        sortBy = addPrimaryKeyToSortList(sortBy);
        if (sortBy.size() == 1) {
            // The list is sorted by primary key
            if (backwards) {
                limitingFilter = Filters.lt(getEntityClassMetadata().
                        getIdentifierProperty().getName(), entityId);
            } else {
                limitingFilter = Filters.gt(getEntityClassMetadata().
                        getIdentifierProperty().getName(), entityId);
            }
        } else {
            // We have to fetch the values of the sorted fields
            T currentEntity = getEntity(entityId);
            if (currentEntity == null) {
                throw new EntityNotFoundException("No entity found with the ID "
                        + entityId);
            }
            // Collect the values into a map for easy access
            Map<Object, Object> filterValues = new HashMap<Object, Object>();
            for (SortBy sb : sortBy) {
                filterValues.put(sb.propertyId, getEntityClassMetadata().
                        getPropertyValue(
                        currentEntity, sb.propertyId.toString()));
            }
            // Now we can build a filter that limits the query to the entities
            // below entityId
            limitingFilter = Filters.or();
            for (int i = sortBy.size() - 1; i >= 0; i--) {
                // TODO Document this code snippet once it works
                Junction caseFilter = Filters.and();
                SortBy sb;
                for (int j = 0; j < i; j++) {
                    sb = sortBy.get(j);
                    caseFilter.add(Filters.eq(sb.propertyId, filterValues.get(
                            sb.propertyId)));
                }
                sb = sortBy.get(i);
                if (sb.ascending ^ backwards) {
                    caseFilter.add(Filters.gt(sb.propertyId, filterValues.get(
                            sb.propertyId)));
                } else {
                    caseFilter.add(Filters.lt(sb.propertyId, filterValues.get(
                            sb.propertyId)));
                }
                ((Junction) limitingFilter).add(caseFilter);
            }
        }
        // Now, we execute the query
        Filter queryFilter;
        if (filter == null) {
            queryFilter = limitingFilter;
        } else {
            queryFilter = Filters.and(filter, limitingFilter);
        }
        Query query = createFilteredQuery("obj." + getEntityClassMetadata().
                getIdentifierProperty().getName(), "obj", queryFilter, sortBy,
                backwards, null);
        query.setMaxResults(1);
        List<?> result = query.getResultList();
        if (result.size() != 1) {
            return null;
        } else {
            return result.get(0);
        }
    }

    @Override
    public Object getNextEntityIdentifier(Object entityId, Filter filter,
            List<SortBy> sortBy) {
        return getSibling(entityId, filter, sortBy, false);
    }

    @Override
    public Object getPreviousEntityIdentifier(Object entityId, Filter filter,
            List<SortBy> sortBy) {
        return getSibling(entityId, filter, sortBy, true);
    }
}
