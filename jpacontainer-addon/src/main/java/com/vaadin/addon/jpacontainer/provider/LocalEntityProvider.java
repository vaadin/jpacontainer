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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityManagerProvider;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.LazyLoadingDelegate;
import com.vaadin.addon.jpacontainer.QueryModifierDelegate;
import com.vaadin.addon.jpacontainer.SortBy;
import com.vaadin.addon.jpacontainer.filter.util.AdvancedFilterableSupport;
import com.vaadin.addon.jpacontainer.filter.util.FilterConverter;
import com.vaadin.addon.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addon.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addon.jpacontainer.metadata.PropertyKind;
import com.vaadin.addon.jpacontainer.util.CollectionUtil;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Compare.Greater;
import com.vaadin.data.util.filter.Compare.Less;
import com.vaadin.data.util.filter.Or;

/**
 * A read-only entity provider that works with a local {@link EntityManager}.
 * Most important features and limitations:
 * <ul>
 * <li>Does not do any internal caching, all information is always accessed
 * directly from the EntityManager</li>
 * <li>Explicitly detaches entities by default (see
 * {@link #isEntitiesDetached() })
 * <ul>
 * <li>Performs a serialize-deserialize cycle to clone entities in order to
 * explicitly detach them from the persistence context (<b>This is ugly!</b>).</li>
 * </ul>
 * </li>
 * <li>Uses lazy-loading of entities (when using detached entities, references
 * and collections within the entities should be configured to be fetched
 * eagerly, though)</li>
 * </ul>
 * 
 * This entity provider does not perform very well, as every method call results
 * in at least one query being sent to the entity manager. If speed is desired,
 * {@link CachingLocalEntityProvider} should be used instead. However, this
 * entity provider consumes less memory than the caching provider.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class LocalEntityProvider<T> implements EntityProvider<T>, Serializable {

    private static final long serialVersionUID = 1601796410565144708L;
    private transient EntityManager entityManager;
    private EntityClassMetadata<T> entityClassMetadata;
    private boolean entitiesDetached = true;
    private EntityManagerProvider entityManagerProvider = null;

    /**
     * Creates a new <code>LocalEntityProvider</code>.
     * 
     * @param entityClass
     *            the entity class (must not be null).
     * @param entityManager
     *            the entity manager to use (must not be null).
     */
    public LocalEntityProvider(Class<T> entityClass, EntityManager entityManager) {
        this(entityClass);
        assert entityManager != null : "entityManager must not be null";
        setEntityManager(entityManager);
    }

    /**
     * Creates a new <code>LocalEntityProvider</code>. The entity manager or an
     * entity manager provider must be set using
     * {@link #setEntityManager(javax.persistence.EntityManager)} or
     * {@link #setEntityManagerProvider(com.vaadin.addon.jpacontainer.EntityManagerProvider)}
     * respectively.
     * 
     * @param entityClass
     *            the entity class (must not be null).
     */
    public LocalEntityProvider(Class<T> entityClass) {
        assert entityClass != null : "entityClass must not be null";
        this.entityClassMetadata = MetadataFactory.getInstance()
                .getEntityClassMetadata(entityClass);
    }

    /**
     * Creates a new <code>LocalEntityProvider</code> with the specified
     * {@link EntityManagerProvider}.
     * 
     * @param entityClass
     * @param entityManagerProvider
     */
    public LocalEntityProvider(Class<T> entityClass,
            EntityManagerProvider entityManagerProvider) {
        this(entityClass);
        assert entityManagerProvider != null : "entityManagerProvider must not be null";
        setEntityManagerProvider(entityManagerProvider);
    }

    private Serializable serializableEntityManager;
    private QueryModifierDelegate queryModifierDelegate;

    /**
     * The lazy loading delegate explicitly handles loading lazy collections
     * where needed (e.g. when using Hibernate)
     */
    private LazyLoadingDelegate lazyLoadingDelegate;

    // TODO Test serialization of entity manager
    protected Object writeReplace() throws ObjectStreamException {
        if (entityManager != null && entityManager instanceof Serializable) {
            serializableEntityManager = (Serializable) entityManager;
        }
        return this;
    }

    protected Object readResolve() throws ObjectStreamException {
        if (serializableEntityManager != null) {
            this.entityManager = (EntityManager) serializableEntityManager;
        }
        return this;
    }

    /**
     * Sets the {@link EntityManagerProvider} that is used to find the current
     * entity manager unless set using
     * {@link #setEntityManager(javax.persistence.EntityManager)}
     * 
     * @param entityManagerProvider
     *            The entity manager provider to set.
     */
    public void setEntityManagerProvider(
            EntityManagerProvider entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    /**
     * Gets the {@link EntityManagerProvider} that is used to find the current
     * entity manager unless one is specified using
     * {@link #setEntityManager(javax.persistence.EntityManager)}.
     * 
     * @return the entity manager provider,
     */
    public EntityManagerProvider getEntityManagerProvider() {
        return entityManagerProvider;
    }

    /**
     * Sets the entity manager.
     * 
     * @param entityManager
     *            the entity manager to set.
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Gets the metadata for the entity class.
     * 
     * @return the metadata (never null).
     */
    protected EntityClassMetadata<T> getEntityClassMetadata() {
        return this.entityClassMetadata;
    }

    /**
     * Gets the entity manager. If no entity manager has been set, the one
     * returned by the registered entity manager provider is returned.
     * 
     * @return the entity manager.
     */
    public EntityManager getEntityManager() {
        if (entityManager != null) {
            return entityManager;
        }
        return entityManagerProvider.getEntityManager();
    }

    /**
     * Gets the entity manager.
     * 
     * @return the entity manager (never null).
     * @throws IllegalStateException
     *             if no entity manager is set.
     */
    protected EntityManager doGetEntityManager() throws IllegalStateException {
        if (getEntityManager() == null) {
            throw new IllegalStateException("No entity manager specified");
        }
        return getEntityManager();
    }

    /**
     * Creates a copy of <code>original</code> and adds an entry for the primary
     * key to the end of the list.
     * 
     * @param original
     *            the original list of sorting instructions (must not be null,
     *            but may be empty).
     * @return a new list with the added entry for the primary key.
     */
    protected List<SortBy> addPrimaryKeyToSortList(List<SortBy> original) {
        if (sortByListContainsPrimaryKey(original)) {
            return original;
        }
        ArrayList<SortBy> newList = new ArrayList<SortBy>();
        newList.addAll(original);
        if (getEntityClassMetadata().hasEmbeddedIdentifier()) {
            for (String p : getEntityClassMetadata().getIdentifierProperty()
                    .getTypeMetadata().getPersistentPropertyNames()) {
                newList.add(new SortBy(getEntityClassMetadata()
                        .getIdentifierProperty().getName() + "." + p, true));
            }
        } else {
            newList.add(new SortBy(getEntityClassMetadata()
                    .getIdentifierProperty().getName(), true));
        }
        return Collections.unmodifiableList(newList);
    }

    /**
     * @param original
     * @return
     */
    private boolean sortByListContainsPrimaryKey(List<SortBy> original) {
        for (SortBy sb : original) {
            EntityClassMetadata<T> metadata = getEntityClassMetadata();
            if (metadata.hasEmbeddedIdentifier()) {
                if (sb.getPropertyId()
                        .equals(metadata.getIdentifierProperty()
                                .getTypeMetadata().getPersistentPropertyNames()
                                .iterator().next())) {
                    return true;
                }
            } else {
                if (sb.getPropertyId().equals(
                        metadata.getIdentifierProperty().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Translates SortBy instances, which possibly contain nested properties
     * (e.g. name.firstName, name.lastName) into Order instances which can be
     * used in a CriteriaQuery.
     * 
     * @param sortBy
     *            the SortBy instance to translate
     * @param swapSortOrder
     *            swaps the specified sort order if true.
     * @param cb
     *            the {@link CriteriaBuilder} to use
     * @param root
     *            the {@link CriteriaQuery} {@link Root} to be used.
     * @return
     */
    protected Order translateSortBy(SortBy sortBy, boolean swapSortOrder,
            CriteriaBuilder cb, Root<T> root) {
        String sortedPropId = sortBy.getPropertyId().toString();
        // First split the id and build a Path.
        String[] idStrings = sortedPropId.split("\\.");
        Path<T> path = null;
        if (idStrings.length > 1 && !isEmbedded(idStrings[0])) {
            // This is a nested property, we need to LEFT JOIN
            path = root.join(idStrings[0], JoinType.LEFT);
            for (int i = 1; i < idStrings.length; i++) {
                if (i < idStrings.length - 1) {
                    path = ((Join<?, ?>) path)
                            .join(idStrings[i], JoinType.LEFT);
                } else {
                    path = path.get(idStrings[i]);
                }
            }
        } else {
            // non-nested or embedded, we can select as usual
            path = AdvancedFilterableSupport.getPropertyPathTyped(root,
                    sortedPropId);
        }

        // Make and return the Order instances.
        if (sortBy.isAscending() != swapSortOrder) {
            return cb.asc(path);
        } else {
            return cb.desc(path);
        }
    }

    /**
     * @param propertyId
     * @return
     */
    private boolean isEmbedded(String propertyId) {
        return entityClassMetadata.getProperty(propertyId).getPropertyKind() == PropertyKind.EMBEDDED;
    }

    /**
     * Creates a filtered query that does not do any sorting.
     * 
     * @see #createFilteredQuery(com.vaadin.addon.jpacontainer.EntityContainer,
     *      java.util.List, com.vaadin.data.Container.Filter, java.util.List,
     *      boolean)
     * @param fieldsToSelect
     *            the fields to select (must not be null).
     * @param filter
     *            the filter to apply, or null if no filters should be applied.
     * @return the query (never null).
     */
    protected TypedQuery<Object> createUnsortedFilteredQuery(
            EntityContainer<T> container, List<String> fieldsToSelect,
            Filter filter) {
        return createFilteredQuery(container, fieldsToSelect, filter, null,
                false);
    }

    /**
     * Creates a filtered, optionally sorted, query.
     * 
     * @param fieldsToSelect
     *            the fields to select (must not be null).
     * @param filter
     *            the filter to apply, or null if no filters should be applied.
     * @param sortBy
     *            the fields to sort by (must include at least one field), or
     *            null if the result should not be sorted at all.
     * @param swapSortOrder
     *            true to swap the sort order, false to use the sort order
     *            specified in <code>sortBy</code>. Only applies if
     *            <code>sortBy</code> is not null.
     * @return the query (never null).
     */
    protected TypedQuery<Object> createFilteredQuery(
            EntityContainer<T> container, List<String> fieldsToSelect,
            Filter filter, List<SortBy> sortBy, boolean swapSortOrder) {
        assert fieldsToSelect != null : "fieldsToSelect must not be null";
        assert sortBy == null || !sortBy.isEmpty() : "sortBy must be either null or non-empty";

        CriteriaBuilder cb = doGetEntityManager().getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery();
        Root<T> root = query.from(entityClassMetadata.getMappedClass());

        tellDelegateQueryWillBeBuilt(container, cb, query,false);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (filter != null) {
            predicates.add(FilterConverter.convertFilter(filter, cb, root));
        }
        tellDelegateFiltersWillBeAdded(container, cb, query, predicates);
        if (!predicates.isEmpty()) {
            query.where(CollectionUtil.toArray(Predicate.class, predicates));
        }
        tellDelegateFiltersWereAdded(container, cb, query);

        List<Order> orderBy = new ArrayList<Order>();
        if (sortBy != null && sortBy.size() > 0) {
            for (SortBy sortedProperty : sortBy) {
                orderBy.add(translateSortBy(sortedProperty, swapSortOrder, cb,
                        root));
            }
        }
        tellDelegateOrderByWillBeAdded(container, cb, query, orderBy);
        query.orderBy(orderBy);
        tellDelegateOrderByWereAdded(container, cb, query);

        if (fieldsToSelect.size() > 1
                || getEntityClassMetadata().hasEmbeddedIdentifier()) {
            List<Path<?>> paths = new ArrayList<Path<?>>();
            for (String fieldPath : fieldsToSelect) {
                paths.add(AdvancedFilterableSupport.getPropertyPathTyped(root,
                        fieldPath));
            }
            query.multiselect(paths.toArray(new Path<?>[paths.size()]));
        } else {
            query.select(AdvancedFilterableSupport.getPropertyPathTyped(root,
                    fieldsToSelect.get(0)));
        }
        tellDelegateQueryHasBeenBuilt(container, cb, query);
        return doGetEntityManager().createQuery(query);
    }

    protected boolean doContainsEntity(EntityContainer<T> container,
            Object entityId, Filter filter) {
        assert entityId != null : "entityId must not be null";
        String entityIdPropertyName = getEntityClassMetadata()
                .getIdentifierProperty().getName();

        CriteriaBuilder cb = doGetEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(getEntityClassMetadata().getMappedClass());

        tellDelegateQueryWillBeBuilt(container, cb, query,true);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(entityIdPropertyName),
                cb.literal(entityId)));
        if (filter != null) {
            predicates.add(FilterConverter.convertFilter(filter, cb, root));
        }
        tellDelegateFiltersWillBeAdded(container, cb, query, predicates);
        if (!predicates.isEmpty()) {
            query.where(CollectionUtil.toArray(Predicate.class, predicates));
        }
        tellDelegateFiltersWereAdded(container, cb, query);

        if (getEntityClassMetadata().hasEmbeddedIdentifier()) {
             /*
             * Hibernate will generate SQL for "count(obj)" that does not run on
             * HSQLDB. "count(*)" works fine, but then EclipseLink won't work.
             * With this hack, this method should work with both Hibernate and
             * EclipseLink.
             */
            if (query.isDistinct()) {
        	query.select(cb.countDistinct(getCountHackRoot(entityIdPropertyName, root)));
            }else {
        	query.select(cb.count(getCountHackRoot(entityIdPropertyName, root)));
            }
        } else {
            if (query.isDistinct()) {
		query.select(cb.countDistinct(root.get(entityIdPropertyName)));
	    }
	    else {
		query.select(cb.count(root.get(entityIdPropertyName)));
	    }
        }
        tellDelegateQueryHasBeenBuilt(container, cb, query);
        TypedQuery<Long> tq = doGetEntityManager().createQuery(query);
        return tq.getSingleResult() == 1;
    }

    private Path<Object> getCountHackRoot(String entityIdPropertyName, Root<T> root)
    {
	return root.get(entityIdPropertyName).get(
	        getEntityClassMetadata().getIdentifierProperty()
	                .getTypeMetadata().getPersistentPropertyNames()
	                .iterator().next());
    }

    public boolean containsEntity(EntityContainer<T> container,
            Object entityId, Filter filter) {
        return doContainsEntity(container, entityId, filter);
    }

    protected T doGetEntity(Object entityId) {
        assert entityId != null : "entityId must not be null";
        T entity = doGetEntityManager().find(
                getEntityClassMetadata().getMappedClass(), entityId);
        return detachEntity(entity);
    }

    public T getEntity(EntityContainer<T> container, Object entityId) {
        return doGetEntity(entityId);
    }
    
    @Override
    public List<Object> getEntityIdentifierAt(EntityContainer<T> entityContainer, Filter filter, List<SortBy> sortBy,
	    int index, int qty)
    {
	if (sortBy == null)
	{
	    sortBy = Collections.emptyList();
	}
	TypedQuery<Object> query = createFilteredQuery(entityContainer,
		Arrays.asList(getEntityClassMetadata().getIdentifierProperty().getName()), filter,
		addPrimaryKeyToSortList(sortBy), false);
	query.setMaxResults(qty);
	query.setFirstResult(index);
	return query.getResultList();
    }
    
    protected Object doGetEntityIdentifierAt(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy, int index) {
       
        List<?> result = getEntityIdentifierAt(container,filter,sortBy,index,1);
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public Object getEntityIdentifierAt(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy, int index) {
        return doGetEntityIdentifierAt(container, filter, sortBy, index);
    }

    protected int doGetEntityCount(EntityContainer<T> container, Filter filter) {
        String entityIdPropertyName = getEntityClassMetadata()
                .getIdentifierProperty().getName();

        CriteriaBuilder cb = doGetEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(getEntityClassMetadata().getMappedClass());

        tellDelegateQueryWillBeBuilt(container, cb, query,true);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (filter != null) {
            predicates.add(FilterConverter.convertFilter(filter, cb, root));
        }
        tellDelegateFiltersWillBeAdded(container, cb, query, predicates);
        if (!predicates.isEmpty()) {
            query.where(CollectionUtil.toArray(Predicate.class, predicates));
        }
        tellDelegateFiltersWereAdded(container, cb, query);

        if (getEntityClassMetadata().hasEmbeddedIdentifier()) {
            /*
             * Hibernate will generate SQL for "count(obj)" that does not run on
             * HSQLDB. "count(*)" works fine, but then EclipseLink won't work.
             * With this hack, this method should work with both Hibernate and
             * EclipseLink.
             */
            if (query.isDistinct()) {
        	query.select(cb.countDistinct(getCountHackRoot(entityIdPropertyName, root)));
            }else {
        	query.select(cb.count(getCountHackRoot(entityIdPropertyName, root)));
            }
        } else {
            if (query.isDistinct()) {
		query.select(cb.countDistinct(root.get(entityIdPropertyName)));
	    }
	    else {
		query.select(cb.count(root.get(entityIdPropertyName)));
	    }
        }
        tellDelegateQueryHasBeenBuilt(container, cb, query);
        TypedQuery<Long> tq = doGetEntityManager().createQuery(query);
        return tq.getSingleResult().intValue();
    }

    public int getEntityCount(EntityContainer<T> container, Filter filter) {
        return doGetEntityCount(container, filter);
    }

    protected Object doGetFirstEntityIdentifier(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }

        List<String> keyFields = Arrays.asList(getEntityClassMetadata()
                .getIdentifierProperty().getName());
        // if (getEntityClassMetadata().hasEmbeddedIdentifier()) {
        // keyFields = new ArrayList<String>();
        // for (String p : getEntityClassMetadata().getIdentifierProperty()
        // .getTypeMetadata().getPersistentPropertyNames()) {
        // keyFields.add(getEntityClassMetadata().getIdentifierProperty()
        // .getName() + "." + p);
        // }
        // }
        TypedQuery<Object> query = createFilteredQuery(container, keyFields,
                filter, addPrimaryKeyToSortList(sortBy), false);
        query.setMaxResults(1);
        List<?> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public Object getFirstEntityIdentifier(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy) {
        return doGetFirstEntityIdentifier(container, filter, sortBy);
    }

    protected Object doGetLastEntityIdentifier(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }
        // The last 'true' parameter switches the sort order -> the last row is
        // the first result.
        TypedQuery<Object> query = createFilteredQuery(container,
                Arrays.asList(getEntityClassMetadata().getIdentifierProperty()
                        .getName()), filter, addPrimaryKeyToSortList(sortBy),
                true);
        query.setMaxResults(1);
        List<?> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public Object getLastEntityIdentifier(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy) {
        return doGetLastEntityIdentifier(container, filter, sortBy);
    }

    /**
     * If <code>backwards</code> is false, this method will return the
     * identifier of the entity next to the entity identified by
     * <code>entityId</code>. If true, this method will return the identifier of
     * the entity previous to the entity identified by <code>entityId</code>.
     * <code>filter</code> and <code>sortBy</code> is used to define and limit
     * the list of entities to be used for determining the sibling.
     * 
     * @param entityId
     *            the identifier of the entity whose sibling to retrieve (must
     *            not be null).
     * @param filter
     *            an optional filter to limit the entities (may be null).
     * @param sortBy
     *            the order in which the list should be sorted (must not be
     *            null).
     * @param backwards
     *            true to fetch the previous sibling, false to fetch the next
     *            sibling.
     * @return the identifier of the "sibling".
     */
    protected Object getSibling(EntityContainer<T> container, Object entityId,
            Filter filter, List<SortBy> sortBy, boolean backwards) {
        TypedQuery<Object> query = createSiblingQuery(container, entityId,
                filter, sortBy, backwards);
        query.setMaxResults(1);
        List<?> result = query.getResultList();
        if (result.size() != 1) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * This method creates a query that can be used to fetch the siblings of a
     * specific entity. If <code>backwards</code> is false, the query will begin
     * with the entity next to the entity identified by <code>entityId</code>.
     * If <code>backwards</code> is false, the query will begin with the entity
     * prior to the entity identified by <code>entityId</code>.
     * 
     * @param entityId
     *            the identifier of the entity whose sibling to retrieve (must
     *            not be null).
     * @param filter
     *            an optional filter to limit the entities (may be null).
     * @param sortBy
     *            the order in which the list should be sorted (must not be
     *            null).
     * @param backwards
     *            true to fetch the previous sibling, false to fetch the next
     *            sibling.
     * @return the query that will return the sibling and all the subsequent
     *         entities unless limited.
     */
    protected TypedQuery<Object> createSiblingQuery(
            EntityContainer<T> container, Object entityId, Filter filter,
            List<SortBy> sortBy, boolean backwards) {
        assert entityId != null : "entityId must not be null";
        assert sortBy != null : "sortBy must not be null";
        Filter limitingFilter;
        sortBy = addPrimaryKeyToSortList(sortBy);
        if (sortBy.size() == 1) {
            // The list is sorted by primary key
            if (backwards) {
                limitingFilter = new Less(getEntityClassMetadata()
                        .getIdentifierProperty().getName(), entityId);
            } else {
                limitingFilter = new Greater(getEntityClassMetadata()
                        .getIdentifierProperty().getName(), entityId);
            }
        } else {
            // We have to fetch the values of the sorted fields
            T currentEntity = getEntity(container, entityId);
            if (currentEntity == null) {
                throw new EntityNotFoundException(
                        "No entity found with the ID " + entityId);
            }
            // Collect the values into a map for easy access
            Map<Object, Object> filterValues = new HashMap<Object, Object>();
            for (SortBy sb : sortBy) {
                filterValues.put(
                        sb.getPropertyId(),
                        getEntityClassMetadata().getPropertyValue(
                                currentEntity, sb.getPropertyId().toString()));
            }
            // Now we can build a filter that limits the query to the entities
            // below entityId
            List<Filter> orFilters = new ArrayList<Filter>();
            for (int i = sortBy.size() - 1; i >= 0; i--) {
                // TODO Document this code snippet once it works
                // TODO What happens with null values?
                List<Filter> caseFilters = new ArrayList<Filter>();
                SortBy sb;
                for (int j = 0; j < i; j++) {
                    sb = sortBy.get(j);
                    caseFilters.add(new Equal(sb.getPropertyId(), filterValues
                            .get(sb.getPropertyId())));
                }
                sb = sortBy.get(i);
                if (sb.isAscending() ^ backwards) {
                    caseFilters.add(new Greater(sb.getPropertyId(),
                            filterValues.get(sb.getPropertyId())));
                } else {
                    caseFilters.add(new Less(sb.getPropertyId(), filterValues
                            .get(sb.getPropertyId())));
                }
                orFilters.add(new And(CollectionUtil.toArray(Filter.class,
                        caseFilters)));
            }
            limitingFilter = new Or(CollectionUtil.toArray(Filter.class,
                    orFilters));
        }
        // Now, we can create the query
        Filter queryFilter;
        if (filter == null) {
            queryFilter = limitingFilter;
        } else {
            queryFilter = new And(filter, limitingFilter);
        }
        TypedQuery<Object> query = createFilteredQuery(container,
                Arrays.asList(getEntityClassMetadata().getIdentifierProperty()
                        .getName()), queryFilter, sortBy, backwards);
        return query;
    }

    protected Object doGetNextEntityIdentifier(EntityContainer<T> container,
            Object entityId, Filter filter, List<SortBy> sortBy) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }
        return getSibling(container, entityId, filter, sortBy, false);
    }

    public Object getNextEntityIdentifier(EntityContainer<T> container,
            Object entityId, Filter filter, List<SortBy> sortBy) {
        return doGetNextEntityIdentifier(container, entityId, filter, sortBy);
    }

    protected Object doGetPreviousEntityIdentifier(
            EntityContainer<T> container, Object entityId, Filter filter,
            List<SortBy> sortBy) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }
        return getSibling(container, entityId, filter, sortBy, true);
    }

    public Object getPreviousEntityIdentifier(EntityContainer<T> container,
            Object entityId, Filter filter, List<SortBy> sortBy) {
        return doGetPreviousEntityIdentifier(container, entityId, filter,
                sortBy);
    }

    /**
     * Detaches <code>entity</code> from the entity manager. If
     * <code>entity</code> is null, then null is returned. If
     * {@link #isEntitiesDetached() } is false, <code>entity</code> is returned
     * directly.
     * 
     * @param entity
     *            the entity to detach.
     * @return the detached entity.
     */
    protected T detachEntity(T entity) {
        if (entity == null) {
            return null;
        }
        if (isEntitiesDetached()) {
            getEntityManager().detach(entity);
        }
        return entity;
    }

    public boolean isEntitiesDetached() {
        return entitiesDetached;
    }

    public void setEntitiesDetached(boolean detached)
            throws UnsupportedOperationException {
        this.entitiesDetached = detached;
    }

    protected List<Object> doGetAllEntityIdentifiers(
            EntityContainer<T> container, Filter filter, List<SortBy> sortBy) {
        if (sortBy == null) {
            sortBy = Collections.emptyList();
        }
        sortBy = addPrimaryKeyToSortList(sortBy);
        TypedQuery<Object> query = createFilteredQuery(container,
                Arrays.asList(getEntityClassMetadata().getIdentifierProperty()
                        .getName()), filter, sortBy, false);
        return Collections.unmodifiableList(query.getResultList());
    }

    public List<Object> getAllEntityIdentifiers(EntityContainer<T> container,
            Filter filter, List<SortBy> sortBy) {
        return doGetAllEntityIdentifiers(container, filter, sortBy);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.addon.jpacontainer.EntityProvider#setQueryModifierDelegate
     * (com.vaadin.addon.jpacontainer.EntityProvider.QueryModifierDelegate)
     */
    public void setQueryModifierDelegate(QueryModifierDelegate delegate) {
        this.queryModifierDelegate = delegate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.addon.jpacontainer.EntityProvider#getQueryModifierDelegate()
     */
    public QueryModifierDelegate getQueryModifierDelegate() {
        return queryModifierDelegate;
    }

    // QueryModifierDelegate helper methods

    private void tellDelegateQueryWillBeBuilt(EntityContainer<T> container,
            CriteriaBuilder cb, CriteriaQuery<?> query,boolean forCount) {
        if (queryModifierDelegate != null) {
            if (queryModifierDelegate instanceof QueryModifierDelegateCountAware)
            {
        	((QueryModifierDelegateCountAware)queryModifierDelegate).startQueryForCount(forCount);
            }
            queryModifierDelegate.queryWillBeBuilt(cb, query);
        } else if (container.getQueryModifierDelegate() != null) {
            if (container.getQueryModifierDelegate() instanceof QueryModifierDelegateCountAware)
            {
        	((QueryModifierDelegateCountAware)container.getQueryModifierDelegate()).startQueryForCount(forCount);
            }
            container.getQueryModifierDelegate().queryWillBeBuilt(cb, query);
        }
    }

    private void tellDelegateQueryHasBeenBuilt(EntityContainer<T> container,
            CriteriaBuilder cb, CriteriaQuery<?> query) {
        if (queryModifierDelegate != null) {
            queryModifierDelegate.queryHasBeenBuilt(cb, query);
        } else if (container.getQueryModifierDelegate() != null) {
            container.getQueryModifierDelegate().queryHasBeenBuilt(cb, query);
        }
    }

    private void tellDelegateFiltersWillBeAdded(EntityContainer<T> container,
            CriteriaBuilder cb, CriteriaQuery<?> query,
            List<Predicate> predicates) {
        if (queryModifierDelegate != null) {
            queryModifierDelegate.filtersWillBeAdded(cb, query, predicates);
        } else if (container.getQueryModifierDelegate() != null) {
            container.getQueryModifierDelegate().filtersWillBeAdded(cb, query,
                    predicates);
        }
    }

    private void tellDelegateFiltersWereAdded(EntityContainer<T> container,
            CriteriaBuilder cb, CriteriaQuery<?> query) {
        if (queryModifierDelegate != null) {
            queryModifierDelegate.filtersWereAdded(cb, query);
        } else if (container.getQueryModifierDelegate() != null) {
            container.getQueryModifierDelegate().filtersWereAdded(cb, query);
        }
    }

    private void tellDelegateOrderByWillBeAdded(EntityContainer<T> container,
            CriteriaBuilder cb, CriteriaQuery<?> query, List<Order> orderBy) {
        if (queryModifierDelegate != null) {
            queryModifierDelegate.orderByWillBeAdded(cb, query, orderBy);
        } else if (container.getQueryModifierDelegate() != null) {
            container.getQueryModifierDelegate().orderByWillBeAdded(cb, query,
                    orderBy);
        }
    }

    private void tellDelegateOrderByWereAdded(EntityContainer<T> container,
            CriteriaBuilder cb, CriteriaQuery<?> query) {
        if (queryModifierDelegate != null) {
            queryModifierDelegate.orderByWasAdded(cb, query);
        } else if (container.getQueryModifierDelegate() != null) {
            container.getQueryModifierDelegate().orderByWasAdded(cb, query);
        }
    }

    public Object getIdentifier(T entity) {
        return entityClassMetadata.getPropertyValue(entity, entityClassMetadata
                .getIdentifierProperty().getName());
    }

    public T refreshEntity(T entity) {
        if (getEntityManager().contains(entity)) {
            try {
                getEntityManager().refresh(entity);
            } catch (IllegalArgumentException e) {
                // detached, removed or something, get by id from em and refresh
                // than non-detached object
                entity = findAndRefresh(entity);
            } catch (EntityNotFoundException e) {
                return null;
            } catch (TransactionRequiredException e) {
                // TODO: handle exception, only in transactional?
            }
        } else {
            entity = findAndRefresh(entity);
        }
        return entity;
    }

    private T findAndRefresh(T entity) {
        entity = getEntityManager().find(
                getEntityClassMetadata().getMappedClass(),
                getIdentifier(entity));
        if (entity != null) {
            try {
                // now try to refresh the attached entity
                getEntityManager().refresh(entity);
                entity = detachEntity(entity);
            } catch (TransactionRequiredException e) {
                // NOP
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return entity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.addon.jpacontainer.EntityContainer#setLazyLoadingDelegate(
     * com.vaadin.addon.jpacontainer.EntityContainer.LazyLoadingDelegate)
     */
    public void setLazyLoadingDelegate(LazyLoadingDelegate delegate) {
        lazyLoadingDelegate = delegate;
        if (lazyLoadingDelegate != null) {
            lazyLoadingDelegate.setEntityProvider(this);
        }
    }

    public LazyLoadingDelegate getLazyLoadingDelegate() {
        return lazyLoadingDelegate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.jpacontainer.EntityProvider#refresh()
     */
    public void refresh() {
        // Nothing to do in this implementation, since we don't keep any
        // items/entities cached.
    }

 
}
