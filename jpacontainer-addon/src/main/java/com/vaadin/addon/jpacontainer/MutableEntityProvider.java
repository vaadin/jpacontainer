/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

/**
 * Entity provider that also supports adding, updating and removing entities.
 * Implementations should pay special attention to the usage of the
 * {@link #isEntitiesDetached() } property.
 * <p>
 * All the methods defined in this interface should run in their own transactions.
 * The implementation may either handle the transactions itself, or rely on
 * declarative transaction handling as provided by e.g. Spring or EJB.
 * If a method completes successfully, its transaction should be committed.
 * If an error occurs while accessing the persistence storage, the transaction
 * should be rolled back and a runtime exception should be thrown.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface MutableEntityProvider<T> extends EntityProvider<T> {

	/**
	 * Adds <code>entity</code> to the persistence storage. This method returns
	 * the same entity after adding to make it possible for the client to access
	 * the entity identifier. Note, however, that depending on the
	 * implementation of the entity provider and the state of
	 * {@link #isEntitiesDetached() }, this may or may not be the same instance
	 * as <code>entity</code>. Therefore, if {@link #isEntitiesDetached() } is
	 * true, clients should always assume that
	 * <code>entity != returnedEntity</code>.
	 * 
	 * @param entity
	 *            the entity to add (must not be null).
	 * @return the added entity.
	 * @throws RuntimeException if an error occurs while adding the entity to the persistence storage.
	 */
	public T addEntity(T entity) throws RuntimeException;

	/**
	 * Saves the changes made to <code>entity</code> to the persistence storage.
	 * This method returns the same entity after saving the changes. Note,
	 * however, that depending on the implementation of the entity provider and
	 * the state of {@link #isEntitiesDetached() }, this may or may not be the
	 * same instance as <code>entity</code>. Therefore, if
	 * {@link #isEntitiesDetached() } is true, clients should always assume that
	 * <code>entity != returnedEntity</code>.
	 * 
	 * @param entity
	 *            the entity to update (must not be null).
	 * @return the updated entity.
	 * @throws RuntimeException if an error occurs while saving the changes to the persistence storage.
	 */
	public T updateEntity(T entity) throws RuntimeException;

	/**
	 * 
	 * Updates a single property value of a specific entity. If the entity is
	 * not found, nothing happens.
	 * 
	 * @param entityId
	 *            the identifier of the entity (must not be null).
	 * @param propertyName
	 *            the name of the property to update (must not be null).
	 * @param propertyValue
	 *            the new property value.
	 * @throws IllegalArgumentException
	 *             if <code>propertyName</code> is not a valid property name.
	 * @throws RuntimeException if an error occurs while saving the change to the persistence storage.
	 */
	public void updateEntityProperty(Object entityId, String propertyName,
			Object propertyValue) throws IllegalArgumentException, RuntimeException;

	/**
	 * Removes the entity identified by <code>entityId</code>. If no entity is
	 * found, nothing happens.
	 * 
	 * @param entityId
	 *            the identifier of the entity to remove.
	 * @throws RuntimeException if an error occurs while removing the entity from the persistence storage.
	 */
	public void removeEntity(Object entityId) throws RuntimeException;
}
