/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import java.io.Serializable;

/**
 * An extended version of {@link MutableEntityProvider} that can execute several
 * update operations (that is, adding new entities, and updating and removing
 * existing entities) inside a single transaction. This feature is used by
 * {@link JPAContainer} when write-through/auto-commit is turned off.
 * <p>
 * If the entites handled by this provider contain complex associations, special
 * care should be taken to enforce data integrity. The following example
 * scenarios might give unexpected results if not dealt with properly:
 * <p>
 * <b>Example 1</b>
 * <ol>
 * <li>Add Entity1</li>
 * <li>Add Entity2</li>
 * <li>Update Entity1 to reference Entity2</li>
 * <li>Run batch</li>
 * </ol>
 * Depending on the entity manager implementation and cascading settings, this
 * might cause an exception due to a nonexistent reference to Entity2 at step 1,
 * or a duplicate of Entity2 at step 2.
 * <p>
 * <b>Example 2</b>
 * <ol>
 * <li>Add Entity1</li>
 * <li>Add Entity2</li>
 * <li>Update Entity1 to reference Entity2</li>
 * <li>Remove Entity2</li>
 * <li>Run batch</li>
 * </ol>
 * First of all, Entity2 should not be added at all as it is added and removed
 * inside the same transaction. However, there is still a reference to Entity2
 * from Entity1. If cascading is turned on, this might result in both Entity1
 * and Entity2 being added nonetheless.
 * 
 * @see JPAContainer#setWriteThrough(boolean)
 * @see JPAContainer#isWriteThrough()
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface BatchableEntityProvider<T> extends MutableEntityProvider<T> {

    /**
     * Callback interface used by the
     * {@link BatchableEntityProvider#batchUpdate(com.vaadin.addon.jpacontainer.BatchableEntityProvider.BatchUpdateCallback) }
     * method.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public static interface BatchUpdateCallback<T> extends Serializable {

        /**
         * Runs the updates using a special batch enabled mutable entity
         * provider.
         * 
         * @param batchEnabledEntityProvider
         *            a {@link MutableEntityProvider} that knows how to handle
         *            multiple updates and executes them within a single
         *            transaction.
         */
        public void batchUpdate(
                MutableEntityProvider<T> batchEnabledEntityProvider);
    }

    /**
     * Executes a batch update using the specified callback parameter. The batch
     * update should be run inside a single transaction. The implementation may
     * either handle the transaction itself, or rely on external transaction
     * handling provided by a container such as Spring or EJB. If the batch
     * update fails, the entire transaction should be rolled back and an
     * exception thrown. Otherwise, it should be committed.
     * <p>
     * Clients should instantiate {@link BatchUpdateCallback}, implement the
     * {@link BatchUpdateCallback#batchUpdate(MutableEntityProvider) } method and
     * execute the updates as if they were using an ordinary
     * {@link MutableEntityProvider}. The following example saves a list of
     * transient entities: <code>
     * <pre>
     *  provider.batchUpdate(new BatchUpdateCallback<MyEntity>() {
     *      public void batchUpdate(MutableEntityProvider<MyEntity> batchEnabledEntityProvider) {
     *          for (Entity e : myListOfEntitiesToAdd) {
     *              batchEnabledEntityProvider.addEntity(e);
     *          }
     *      }
     *  });
     * </pre></code>
     * 
     * @param callback
     *            the callback that will be used to run the batch update.
     * @throws UnsupportedOperationException
     *             if this entity provider does not support batch updates.
     */
    public void batchUpdate(BatchUpdateCallback<T> callback)
            throws UnsupportedOperationException;
}
