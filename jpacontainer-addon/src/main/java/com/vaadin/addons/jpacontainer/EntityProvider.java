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
package com.vaadin.addons.jpacontainer;

import java.util.List;

/**
 * Like the name suggests, the purpose of the <code>EntityProvider</code> is to
 * provide entities to {@link EntityContainer}s.
 * <p>
 * Please note the {@link #isEntitiesDetached() } flag, as this may have weird
 * consequences if used inproperly. 
 *
 * @see MutableEntityProvider
 * @see CachingEntityProvider
 * @see BatchableEntityProvider
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface EntityProvider<T> {

    /**
     * Loads the entity identified by <code>entityId</code> from the persistence storage.
     *
     * @param entityId the entity identifier (must not be null).
     * @return the entity, or null if not found.
     */
    public T getEntity(Object entityId);

    /**
     * If this method returns true, all entities returned from this entity provider
     * are explicitly detached from the persistence context before returned, regardless
     * of whether the persistence context is extended or transaction-scoped. Thus,
     * no lazy-loaded associations will work and any changes made to the entities will
     * not be reflected in the persistence context unless the entity is merged.
     * <p>
     * If this method returns false, the entities returned may be managed or detached,
     * depending on the scope of the persistence context.
     * <p>
     * The default value is implementation specific.
     * @see #setEntitiesDetached(boolean)
     * 
     * @return true if the entities are explicitly detached, false otherwise.
     */
    public boolean isEntitiesDetached();

    /**
     * Specifies whether the entities returned by the entity provider should
     * be explicitly detached or not. See {@link #isEntitiesDetached() } for a more
     * detailed description of the consequences.
     *
     * @param detached true to request explicitly detached entities, false otherwise.
     * @throws UnsupportedOperationException if the implementation does not allow the user to change the way entities are returned.
     */
    public void setEntitiesDetached(boolean detached) throws
            UnsupportedOperationException;

    /**
     * Gets the identifier of the entity at position <code>index</code> in the result set determined
     * from <code>filter</code> and <code>sortBy</code>.
     *
     * @param filter the filter that should be used to filter the entities (may be null).
     * @param sortBy the properties to sort by, if any (never null, but may be empty).
     * @param index the index of the entity to fetch.
     * @return the entity identifier, or null if not found.
     */
    public Object getEntityIdentifierAt(Filter filter, List<SortBy> sortBy,
            int index);

    /**
     * Gets the identifier of the first item in the list of entities determined
     * by <code>filter</code> and <code>sortBy</code>.
     * 
     * @param filter the filter that should be used to filter the entities (may be null).
     * @param sortBy the properties to sort by, if any (never null, but may be empty).
     * @return the identifier of the first entity, or null if there are no entities matching <code>filter</code>.
     */
    public Object getFirstEntityIdentifier(Filter filter, List<SortBy> sortBy);

    /**
     * Gets the identifier of the last item in the list of entities determined
     * by <code>filter</code> and <code>sortBy</code>.
     *
     * @param filter the filter that should be used to filter the entities (may be null).
     * @param sortBy the properties to sort by, if any (never null, but may be empty).
     * @return the identifier of the last entity, or null if there are no entities matching <code>filter</code>.
     */
    public Object getLastEntityIdentifier(Filter filter, List<SortBy> sortBy);

    /**
     * Gets the identifier of the item next to the item identified by <code>entityId</code> in the list of entities determined
     * by <code>filter</code> and <code>sortBy</code>.
     *
     * @param filter the filter that should be used to filter the entities (may be null).
     * @param sortBy the properties to sort by, if any (never null, but may be empty).
     * @return the identifier of the next entity, or null if there are no entities matching <code>filter</code> or <code>entityId</code> is the last item.
     */
    public Object getNextEntityIdentifier(Object entityId, Filter filter,
            List<SortBy> sortBy);

    /**
     * Gets the identifier of the item previous to the item identified by <code>entityId</code> in the list of entities determined
     * by <code>filter</code> and <code>sortBy</code>.
     *
     * @param filter the filter that should be used to filter the entities (may be null).
     * @param sortBy the properties to sort by, if any (never null, but may be empty).
     * @return the identifier of the previous entity, or null if there are no entities matching <code>filter</code> or <code>entityId</code> is the first item.
     */
    public Object getPreviousEntityIdentifier(Object entityId, Filter filter,
            List<SortBy> sortBy);

    /**
     * Gets the identifiers of all items that match <code>filter</code>. This
     * method only exists to speed up {@link com.vaadin.data.Container#getItemIds() },
     * which in turn is used by {@link com.vaadin.ui.AbstractSelect} and its subclasses
     * (e.g. ComboBox). Using this method is not recommended, as it does not
     * use lazy loading.
     * 
     * @param filter the filter that should be used to filter the entities (may be null).
     * @param sortBy the properties to sort by, if any (never null, but may be empty).
     * @return an unmodifiable list of entity identifiers (never null).
     */
    public List<Object> getAllEntityIdentifiers(Filter filter,
            List<SortBy> sortBy);

    /**
     * Checks if the persistence storage contains an entity identified by <code>entityId</code>
     * that is also matched by <code>filter</code>.
     *
     * @param entityId the entity identifier (must not be null).
     * @param filter the filter that the entity should match (may be null).
     * @return true if the entity exists, false if not.
     */
    public boolean containsEntity(Object entityId, Filter filter);

    /**
     * Gets the number of entities that are matched by <code>filter</code>. If no filter
     * has been specified, the total number of entities is returned.
     *
     * @param filter the filter that should be used to filter the entities (may be null).
     * @return the number of matches.
     */
    public int getEntityCount(Filter filter);
}
