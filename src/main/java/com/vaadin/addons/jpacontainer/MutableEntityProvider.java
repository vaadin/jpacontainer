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

/**
 * Entity provider that also supports adding, updating and removing entities.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface MutableEntityProvider<T> extends EntityProvider<T> {

    /**
     * Adds <code>entity</code> to the persistence storage. This method
     * returns the same entity after adding to make it possible
     * for the client to access the entity identifier. Note, however,
     * that depending on the implementation of the entity provider,
     * this may or may not be the same instance as <code>entity</code>. Therefore,
     * clients should always assume that <code>entity != returnedEntity</code>.
     * 
     * @param entity the entity to add (must not be null).
     * @return the added entity.
     */
    public T addEntity(T entity);

    /**
     * Saves the changes made to <code>entity</code> to the persistence storage.
     * This method returns the same entity after saving the changes. Note, however,
     * that depending on the implementation of the entity provider, this may or may
     * not be the same instance as <code>entity</code>. Therefore, clients should
     * always assume that <code>entity != returnedEntity</code>.
     *
     * @param entity the entity to update (must not be null).
     * @return the updated entity.
     */
    public T updateEntity(T entity);

    /**
     * Updates a single property value of a specific entity. If the entity is not found,
     * nothing happens.
     *
     * @param entityId the identifier of the entity (must not be null).
     * @param propertyName the name of the property to update (must not be null).
     * @param propertyValue the new property value.
     * @throws IllegalArgumentException if <code>propertyName</code> is not a valid property name.
     */
    public void updateEntityProperty(Object entityId, String propertyName,
            Object propertyValue) throws IllegalArgumentException;

    /**
     * Removes the entity identified by <code>entityId</code>. If
     * no entity is found, nothing happens.
     * 
     * @param entityId the identifier of the entity to remove.
     */
    public void removeEntity(Object entityId);
}
