/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Validator.InvalidValueException;

/**
 * A delegate class used by {@link JPAContainer} to handle buffered changes.
 * This class is not part of the public API and should not be used outside of
 * JPAContainer.
 * <p>
 * If the entity implements the {@link Cloneable} interface, clones of the
 * entities will be stored instead of the entities themselves. This has the
 * advantage of tracking exactly which changes have been made to an entity and
 * in which order (e.g. if the same entity is modified twice before the changes
 * are committed).
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
final class BufferedContainerDelegate<T> implements Serializable {

    private static final long serialVersionUID = -4471665710680629463L;

    /**
     * Creates a new <code>BufferedContainerDelegate</code> for the specified
     * container.
     * 
     * @param container
     *            the <code>JPAContainer</code> (must not be null).
     */
    BufferedContainerDelegate(JPAContainer<T> container) {
        assert container != null : "container must not be null";
        this.container = container;
    }

    enum DeltaType {

        ADD, UPDATE, DELETE
    }

    final class Delta implements Serializable {

        private static final long serialVersionUID = -5907859901553818040L;
        final DeltaType type;
        final Object itemId;
        final T entity;

        Delta(DeltaType type, Object itemId, T entity) {
            this.type = type;
            this.itemId = itemId;
            this.entity = entity;
        }
    }

    private JPAContainer<T> container;
    // Delta list contains all changes
    private List<Delta> deltaList = new LinkedList<Delta>();
    // We need a list to maintain the order in which the items were added...
    private List<Object> addedItemIdsCache = new ArrayList<Object>();
    // ... and a map for storing the actual entities.
    private Map<Object, T> addedEntitiesCache = new HashMap<Object, T>();
    // The same goes for the other caches
    private HashMap<Object, Integer> deletedItemIdsCache = new HashMap<Object, Integer>();
    private Map<Object, T> updatedEntitiesCache = new HashMap<Object, T>();

    /**
     * Gets a list of IDs of added entity items. The IDs appear in the order in
     * which they were added.
     * 
     * @return an unmodifiable list of entity item IDs (never null).
     */
    public List<Object> getAddedItemIds() {
        return Collections.unmodifiableList(addedItemIdsCache);
    }

    /**
     * Gets a list of IDs of deleted entity items.
     * 
     * @return an unmodifiable list of entity item IDs (never null).
     */
    public Collection<Object> getDeletedItemIds() {
        return Collections.unmodifiableCollection(deletedItemIdsCache.keySet());
    }

    /**
     * Gets a list of IDs of update entity items.
     * 
     * @return an unmodifiable list of entity item IDs (never null);
     */
    public Collection<Object> getUpdatedItemIds() {
        return Collections
                .unmodifiableCollection(updatedEntitiesCache.keySet());
    }

    /**
     * Gets the added entity whose item ID is <code>itemId</code>.
     * 
     * @param itemId
     *            the ID of the added item (must not be null).
     * @return the entity, or null if not found.
     */
    public T getAddedEntity(Object itemId) {
        assert itemId != null : "itemId must not be null";
        return addedEntitiesCache.get(itemId);
    }

    /**
     * Gets the updated entity whose item ID is <code>itemId</code>.
     * 
     * @param itemId
     *            the ID of the updated item (must not be null).
     * @return the entity, or null if not found.
     */
    public T getUpdatedEntity(Object itemId) {
        assert itemId != null : "itemId must not be null";
        return updatedEntitiesCache.get(itemId);
    }

    /**
     * Checks if <code>itemId</code> is in the list of added item IDs.
     * 
     * @see #getAddedItemIds()
     * @param itemId
     *            the item ID to check (must not be null).
     * @return true if the item ID is in the list, false if not.
     */
    public boolean isAdded(Object itemId) {
        assert itemId != null : "itemId must not be null";
        return addedEntitiesCache.containsKey(itemId);
    }

    /**
     * Checks if <code>itemId</code> is in the collection of deleted item IDs.
     * 
     * @see #getDeletedItemIds()
     * @param itemId
     *            the item ID to check (must not be null).
     * @return true if the item ID is in the collection, false if not.
     */
    public boolean isDeleted(Object itemId) {
        assert itemId != null : "itemId must not be null";
        return deletedItemIdsCache.containsKey(itemId);
    }

    /**
     * Checks if <code>itemId</code> is in the collection of updated item IDs.
     * 
     * @see #getUpdatedItemIds()
     * @param itemId
     *            the item ID to check (must not be null).
     * @return true if the item ID is in the collection, false if not.
     */
    public boolean isUpdated(Object itemId) {
        assert itemId != null : "itemId must not be null";
        return updatedEntitiesCache.containsKey(itemId);
    }

    /**
     * Checks if there are any uncommitted changes.
     * 
     * @return true if there are uncommitted changes, false otherwise.
     */
    public boolean isModified() {
        return !deltaList.isEmpty();
    }

    private void clear() {
        deltaList.clear();
        addedEntitiesCache.clear();
        addedItemIdsCache.clear();
        updatedEntitiesCache.clear();
        deletedItemIdsCache.clear();
    }

    /**
     * Commits the changes to the {@link BatchableEntityProvider} of the
     * JPAContainer.
     * 
     * @throws com.vaadin.data.Buffered.SourceException
     *             if any errors occured.
     * @throws com.vaadin.data.Validator.InvalidValueException
     *             currently never thrown by this implementation.
     */
    public void commit() throws SourceException, InvalidValueException {
        assert container.getEntityProvider() instanceof BatchableEntityProvider : "entityProvider is not batchable";
        BatchableEntityProvider<T> ep = (BatchableEntityProvider<T>) container
                .getEntityProvider();
        ep.batchUpdate(new BatchableEntityProvider.BatchUpdateCallback<T>() {

            private static final long serialVersionUID = -5385980617323427732L;

            public void batchUpdate(
                    MutableEntityProvider<T> batchEnabledEntityProvider) {
                try {
                    for (Delta delta : deltaList) {
                        if (delta.type == DeltaType.ADD) {
                            batchEnabledEntityProvider.addEntity(delta.entity);
                        } else if (delta.type == DeltaType.UPDATE) {
                            batchEnabledEntityProvider
                                    .updateEntity(delta.entity);
                        } else if (delta.type == DeltaType.DELETE) {
                            batchEnabledEntityProvider
                                    .removeEntity(delta.itemId);
                        }
                    }
                } catch (Exception e) {
                    throw new SourceException(container, e);
                }
            }
        });
        // Clean up
        clear();
    }

    /**
     * Clears all the buffered changes.
     * 
     * @throws com.vaadin.data.Buffered.SourceException
     *             currently never thrown by this implementation.
     */
    public void discard() throws SourceException {
        clear();
    }

    /**
     * Adds <code>entity</code> to the list of entities to be saved when the
     * changes are committed.
     * 
     * @param entity
     *            the entity to save (must not be null).
     * @return the temporary item ID to be used to access the entity's item
     *         (never null).
     */
    public Object addEntity(T entity) {
        assert entity != null : "entity must not be null";
        UUID uuid = UUID.randomUUID();
        deltaList.add(new Delta(DeltaType.ADD, uuid, entity));
        addedEntitiesCache.put(uuid, entity);
        addedItemIdsCache.add(0, uuid);
        return uuid;
    }

    /**
     * Marks the item identified by <code>itemId</code> for deletion when the
     * changes are committed.
     * 
     * @param itemId
     *            the ID of the item to be deleted (must not be null).
     */
    public void deleteItem(Object itemId) {
        assert itemId != null : "itemId must not be null";
        if (isAdded(itemId)) {
            addedEntitiesCache.remove(itemId);
            addedItemIdsCache.remove(itemId);
            for (int i = deltaList.size() - 1; i >= 0; i--) {
                if (deltaList.get(i).itemId.equals(itemId)) {
                    deltaList.remove(i);
                }
            }
        } else {
            removeUpdateDelta(itemId);
            deltaList.add(new Delta(DeltaType.DELETE, itemId, null));
            List<Object> allDbEntityIdentifiers = container.getEntityProvider()
                    .getAllEntityIdentifiers(
                            container.getAppliedFiltersAsConjunction(),
                            container.getSortByList());
            int dbIndexOfDeletedItem = allDbEntityIdentifiers.indexOf(itemId);
            deletedItemIdsCache.put(itemId, dbIndexOfDeletedItem);

        }
    }

    private void removeUpdateDelta(Object itemId) {
        if (isUpdated(itemId)) {
            updatedEntitiesCache.remove(itemId);
            for (int i = deltaList.size() - 1; i >= 0; i--) {
                if (deltaList.get(i).itemId.equals(itemId)) {
                    deltaList.remove(i);
                }
            }
        }
    }

    /**
     * Adds <code>entity</code> to the list of entities to be updated when the
     * changes are committed.
     * 
     * @param itemId
     *            the item ID of the entity (must not be null).
     * @param entity
     *            the entity to save (must not be null).
     */
    public void updateEntity(Object itemId, T entity) {
        assert entity != null : "entity must not be null";
        assert itemId != null : "itemId must not be null";

        if (!isAdded(itemId)) {
            // remove possible old update, so that only the last update is
            // applied and order will be dictated by the last update
            removeUpdateDelta(itemId);
            deltaList.add(new Delta(DeltaType.UPDATE, itemId, entity));
            updatedEntitiesCache.put(itemId, entity);
        }
    }

    public int fixDbIndexWithDeletedItems(int index) {
        Integer[] removedDbIndexes = getDbIndexesOfDeletedItems();
        for (int i = 0; i < removedDbIndexes.length; i++) {
            if (removedDbIndexes[i] <= index) {
                index++;
            }
        }
        return index;
    }

    private Integer[] getDbIndexesOfDeletedItems() {
        Integer[] removedDbIndexes = new Integer[deletedItemIdsCache.size()];
        removedDbIndexes = deletedItemIdsCache.values().toArray(
                removedDbIndexes);
        return removedDbIndexes;
    }
}
