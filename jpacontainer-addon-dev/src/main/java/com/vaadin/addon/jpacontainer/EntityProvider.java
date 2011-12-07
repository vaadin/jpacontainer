/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;

import com.vaadin.data.Container.Filter;

/**
 * Like the name suggests, the purpose of the <code>EntityProvider</code> is to
 * provide entities to {@link EntityContainer}s. It basically contains a subset
 * of the methods found in the standard {@link com.vaadin.data.Container}
 * interface. Note, that most of the methods return entity IDs and not entity
 * instances - only {@link #getEntity(java.lang.Object) } actually returns
 * instances.
 * <p>
 * Entity providers should at least implement this interface according to the
 * contracts specified in the methods JavaDocs. Additional functionality may be
 * added by also implementing e.g. {@link MutableEntityProvider}.
 * <p>
 * Once implemented, the entity provider can be plugged into an entity container
 * by using the
 * {@link EntityContainer#setEntityProvider(com.vaadin.addon.jpacontainer.EntityProvider) }
 * method.
 * <p>
 * Please note the {@link #isEntitiesDetached() } flag, as this may have weird
 * consequences if used inproperly.
 * 
 * @see MutableEntityProvider
 * @see CachingEntityProvider
 * @see BatchableEntityProvider
 * @see EntityProviderChangeNotifier
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface EntityProvider<T> extends Serializable {

    /**
     * Loads the entity identified by <code>entityId</code> from the persistence
     * storage.
     * 
     * @param entityId
     *            the entity identifier (must not be null).
     * @return the entity, or null if not found.
     */
    public T getEntity(Object entityId);

    /**
     * If this method returns true, all entities returned from this entity
     * provider are explicitly detached from the persistence context before
     * returned, regardless of whether the persistence context is extended or
     * transaction-scoped. Thus, no lazy-loaded associations will work and any
     * changes made to the entities will not be reflected in the persistence
     * context unless the entity is merged.
     * <p>
     * If this method returns false, the entities returned may be managed or
     * detached, depending on the scope of the persistence context.
     * <p>
     * The default value is implementation specific.
     * 
     * @see #setEntitiesDetached(boolean)
     * 
     * @return true if the entities are explicitly detached, false otherwise.
     */
    public boolean isEntitiesDetached();

    /**
     * Specifies whether the entities returned by the entity provider should be
     * explicitly detached or not. See {@link #isEntitiesDetached() } for a more
     * detailed description of the consequences.
     * 
     * @param detached
     *            true to request explicitly detached entities, false otherwise.
     * @throws UnsupportedOperationException
     *             if the implementation does not allow the user to change the
     *             way entities are returned.
     */
    public void setEntitiesDetached(boolean detached)
            throws UnsupportedOperationException;

    /**
     * Gets the identifier of the entity at position <code>index</code> in the
     * result set determined from <code>filter</code> and <code>sortBy</code>.
     * 
     * @param filter
     *            the filter that should be used to filter the entities (may be
     *            null).
     * @param sortBy
     *            the properties to sort by, if any (may be null).
     * @param index
     *            the index of the entity to fetch.
     * @return the entity identifier, or null if not found.
     */
    public Object getEntityIdentifierAt(Filter filter, List<SortBy> sortBy,
            int index);

    /**
     * Gets the identifier of the first item in the list of entities determined
     * by <code>filter</code> and <code>sortBy</code>.
     * 
     * @param filter
     *            the filter that should be used to filter the entities (may be
     *            null).
     * @param sortBy
     *            the properties to sort by, if any (may be null).
     * @return the identifier of the first entity, or null if there are no
     *         entities matching <code>filter</code>.
     */
    public Object getFirstEntityIdentifier(Filter filter, List<SortBy> sortBy);

    /**
     * Gets the identifier of the last item in the list of entities determined
     * by <code>filter</code> and <code>sortBy</code>.
     * 
     * @param filter
     *            the filter that should be used to filter the entities (may be
     *            null).
     * @param sortBy
     *            the properties to sort by, if any (may be null).
     * @return the identifier of the last entity, or null if there are no
     *         entities matching <code>filter</code>.
     */
    public Object getLastEntityIdentifier(Filter filter, List<SortBy> sortBy);

    /**
     * Gets the identifier of the item next to the item identified by
     * <code>entityId</code> in the list of entities determined by
     * <code>filter</code> and <code>sortBy</code>.
     * 
     * @param filter
     *            the filter that should be used to filter the entities (may be
     *            null).
     * @param sortBy
     *            the properties to sort by, if any (may be null).
     * @return the identifier of the next entity, or null if there are no
     *         entities matching <code>filter</code> or <code>entityId</code> is
     *         the last item.
     */
    public Object getNextEntityIdentifier(Object entityId, Filter filter,
            List<SortBy> sortBy);

    /**
     * Gets the identifier of the item previous to the item identified by
     * <code>entityId</code> in the list of entities determined by
     * <code>filter</code> and <code>sortBy</code>.
     * 
     * @param filter
     *            the filter that should be used to filter the entities (may be
     *            null).
     * @param sortBy
     *            the properties to sort by, if any (may be null).
     * @return the identifier of the previous entity, or null if there are no
     *         entities matching <code>filter</code> or <code>entityId</code> is
     *         the first item.
     */
    public Object getPreviousEntityIdentifier(Object entityId, Filter filter,
            List<SortBy> sortBy);

    /**
     * Gets the identifiers of all items that match <code>filter</code>. This
     * method only exists to speed up
     * {@link com.vaadin.data.Container#getItemIds() }, which in turn is used by
     * {@link com.vaadin.ui.AbstractSelect} and its subclasses (e.g. ComboBox).
     * Using this method is not recommended, as it does not use lazy loading.
     * 
     * @param filter
     *            the filter that should be used to filter the entities (may be
     *            null).
     * @param sortBy
     *            the properties to sort by, if any (may be null).
     * @return an unmodifiable list of entity identifiers (never null).
     */
    public List<Object> getAllEntityIdentifiers(Filter filter,
            List<SortBy> sortBy);

    /**
     * Checks if the persistence storage contains an entity identified by
     * <code>entityId</code> that is also matched by <code>filter</code>.
     * 
     * @param entityId
     *            the entity identifier (must not be null).
     * @param filter
     *            the filter that the entity should match (may be null).
     * @return true if the entity exists, false if not.
     */
    public boolean containsEntity(Object entityId, Filter filter);

    /**
     * Gets the number of entities that are matched by <code>filter</code>. If
     * no filter has been specified, the total number of entities is returned.
     * 
     * @param filter
     *            the filter that should be used to filter the entities (may be
     *            null).
     * @return the number of matches.
     */
    public int getEntityCount(Filter filter);

    /**
     * Sets the {@link QueryModifierDelegate}, which is called in the different
     * stages that the EntityProvider builds a criteria query.
     * 
     * @param delegate
     *            the delegate.
     */
    public void setQueryModifierDelegate(QueryModifierDelegate delegate);

    /**
     * @return the registered {@link QueryModifierDelegate}.
     */
    public QueryModifierDelegate getQueryModifierDelegate();

    /**
     * The QueryModifierDelegate interface defines methods that will be called
     * at the different stages of {@link CriteriaQuery} generation. Implement
     * this interface and call
     * {@link EntityProvider#setQueryModifierDelegate(QueryModifierDelegate)} to
     * receive calls while the {@link CriteriaQuery} is being built. The methods
     * are allowed to modify the CriteriaQuery.
     * 
     * @since 2.0
     */
    public interface QueryModifierDelegate {
        /**
         * This method is called after the {@link CriteriaQuery} instance (
         * <code>query</code>) has been instantiated, but before any state has
         * been set.
         * 
         * Operations and configuration may be performed on the query instance.
         * 
         * @param criteriaBuilder
         *            the {@link CriteriaBuilder} used to build the query
         * @param query
         *            the {@link CriteriaQuery} being built
         */
        public void queryWillBeBuilt(CriteriaBuilder criteriaBuilder,
                CriteriaQuery<?> query);

        /**
         * This method is called after the {@link CriteriaQuery} instance has
         * been completely built (configured).
         * 
         * Any operations may be performed on the query instance.
         * 
         * @param criteriaBuilder
         *            the {@link CriteriaBuilder} used to build the query
         * @param query
         *            the {@link CriteriaQuery} being built
         */
        public void queryHasBeenBuilt(CriteriaBuilder criteriaBuilder,
                CriteriaQuery<?> query);

        /**
         * This method is called after filters (in the form of {@link Filter})
         * have been translated into instances of {@link Predicate}, but before
         * the resulting predicates have been added to <code>query</code>.
         * 
         * The contents of the <code>predicates</code> list may be modified at
         * this point. Any operations may be performed on the query instance.
         * 
         * @param criteriaBuilder
         *            the {@link CriteriaBuilder} used to build the query
         * @param query
         *            the {@link CriteriaQuery} being built
         * @param predicates
         *            the list of predicates ({@link Predicate}) to be applied.
         *            The contents of this list may be modified.
         */
        public void filtersWillBeAdded(CriteriaBuilder criteriaBuilder,
                CriteriaQuery<?> query, List<Predicate> predicates);

        /**
         * This method is called after all filters have been applied to the
         * query.
         * 
         * Any operations may be performed on the query instance.
         * 
         * @param criteriaBuilder
         *            the {@link CriteriaBuilder} used to build the query
         * @param query
         *            the {@link CriteriaQuery} being built
         */
        public void filtersWereAdded(CriteriaBuilder criteriaBuilder,
                CriteriaQuery<?> query);

        /**
         * This method is called after all {@link SortBy} instances have been
         * translated into {@link Order} instances, but before they have been
         * applied to the query.
         * 
         * The contents of the <code>orderBy</code> list may be modified at this
         * point. Any operations may be performed on the query instance.
         * 
         * @param criteriaBuilder
         *            the {@link CriteriaBuilder} used to build the query
         * @param query
         *            the {@link CriteriaQuery} being built
         * @param orderBy
         *            the list of order by rules ({@link Order}) to be applied.
         *            The contents of this list may be modified.
         */
        public void orderByWillBeAdded(CriteriaBuilder criteriaBuilder,
                CriteriaQuery<?> query, List<Order> orderBy);

        /**
         * This method is called after the order by has been applied for the
         * query.
         * 
         * Any operations may be performed on the query instance.
         * 
         * @param criteriaBuilder
         *            the {@link CriteriaBuilder} used to build the query
         * @param query
         *            the {@link CriteriaQuery} being built
         */
        public void orderByWereAdded(CriteriaBuilder criteriaBuilder,
                CriteriaQuery<?> query);
    }

    /**
     * Returns identifier for given entity
     * 
     * @param entity
     */
    public Object getIdentifier(T entity);

    /**
     * Refreshes an entity from DB. If entity no more exists, null is returned.
     * 
     * @param entity
     * @return the refreshed entity or null
     */
    public T refreshEntity(T entity);

    /**
     * Sets the entity manager.
     * 
     * @param entityManager
     *            the entity manager to set.
     */
    void setEntityManager(EntityManager entityManager);

    /**
     * Gets the entity manager.
     * 
     * @return the entity manager, or null if none has been specified.
     */
    EntityManager getEntityManager();

    /**
     * Set the delegate used for lazy loading.
     * 
     * @param delegate
     *            the {@link LazyLoadingDelegate} to use.
     */
    public void setLazyLoadingDelegate(LazyLoadingDelegate delegate);

    /**
     * @return the {@link LazyLoadingDelegate} in use or null if none
     *         registered.
     */
    public LazyLoadingDelegate getLazyLoadingDelegate();

    /**
     * The LazyLoadingDelegate is called when a property that is lazily loaded
     * is being accessed through the Vaadin data API. The LazyLoadingDelegate is
     * responsible for ensuring that the lazily loaded property is loaded and
     * accessible.
     * 
     * @since 2.0
     */
    public interface LazyLoadingDelegate {
        /**
         * This method is called when a lazily loaded property is accessed in an
         * entity. The implementation of this method is responsible for ensuring
         * that the property in question is accessible on the instance of
         * <code>entity</code> that is returned.
         * 
         * @param entity
         *            The entity containing a lazy property.
         * @param propertyName
         *            The name of the lazy property to be accessed.
         * @return an instance of <code>entity</code> with
         *         <code>propertyName</code> attached and accessible. This may
         *         be the same instance as passed in or a new one.
         */
        public <E> E ensureLazyPropertyLoaded(E entity, String propertyName);

        /**
         * Sets the EntityProvider that this delegate is associated with.
         * Automatically called by
         * {@link EntityProvider#setLazyLoadingDelegate(LazyLoadingDelegate)}.
         * The EntityProvider is used to get the current {@link EntityManager}.
         * 
         * @param ep
         */
        public void setEntityProvider(EntityProvider<?> ep);
    }

    /**
     * Clears all caches and refreshes any loaded that cannot be discarded
     * entities.
     */
    public void refresh();
}
