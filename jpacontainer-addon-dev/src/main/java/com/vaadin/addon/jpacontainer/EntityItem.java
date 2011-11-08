/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import java.util.Collection;

import com.vaadin.data.Buffered;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Interface defining the Items that are contained in a {@link EntityContainer}.
 * Each item is always backed by an underlying Entity instance that normally is
 * a POJO. EntityItems are always linked to a specific {@link EntityContainer}
 * instance and cannot be added to other {@link com.vaadin.data.Container}s.
 * <p>
 * By default, whenever a Property of the item is changed, the corresponding
 * property of the Entity is updated accordingly. However, it is also possible
 * to buffer all the changes by setting {@link #setWriteThrough(boolean)} to
 * false. In this mode, the underlying Entity will remain untouched until
 * {@link #commit() } is called. Please note, that this has nothing to do with
 * the buffering of the {@link EntityContainer}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface EntityItem<T> extends Item, Buffered,
        Property.ValueChangeNotifier {

    /**
     * Gets the item ID of the item. This ID can be used to uniquely identify
     * the item inside the container. It may not necessarily be the same as the
     * entity ID.
     * <p>
     * If the item ID is null, the entity item was created by a container, but
     * not yet added to it.
     * 
     * @see EntityContainer#createEntityItem(java.lang.Object)
     * 
     * @return the item ID or null if the item is not yet inside a container.
     */
    public Object getItemId();

    /**
     * Gets the underlying entity instance that contains the actual data being
     * accessed by this item.
     * 
     * @return the entity (never null).
     */
    public T getEntity();

    /**
     * Checks if the underlying entity ({@link #getEntity() }) is persistent
     * (i.e. fetched from a persistence storage) or transient (created and
     * buffered by the container). This method always returns false if
     * {@link #getItemId() } is null, even if the underlying entity actually is
     * persistent.
     * 
     * @return true if the underlying entity is persistent, false if it is
     *         transient.
     */
    public boolean isPersistent();

    /**
     * Checks whether the underlying entity (returned by {@link #getEntity() })
     * has been modified after it was fetched from the entity provider. When the
     * changes have been persisted, this flag will be reset.
     * <p>
     * This flag is only of relevance when container buffering is used. If the
     * container is in write-through mode, any changes made to the entity will
     * automatically be propagated back to the entity provider and hence, this
     * method will always return false as there are no dirty entities.
     * <p>
     * However, if container write-through mode is turned off, any changes made
     * to the entity will not be propagated back until explicitly committed.
     * Modified entities that have not yet been propagated back to the entity
     * provider are considered dirty.
     * <p>
     * Please note, that this is not the same as the {@link #isModified() } flag,
     * which is of relevance when item buffering is used.
     * <p>
     * If the item is not persistent, this method always returns false.
     * 
     * @return true if the underlying entity has been modified, false if not.
     */
    public boolean isDirty();

    /**
     * When using item-level buffering, this method tests whether there are
     * changes made to the EntityItem that have not yet been committed to the
     * underlying Entity ({@link #getEntity() }). If item-level buffering is not
     * used, this method always returns false.
     * 
     * @see #isDirty()
     * 
     * @return true if {@link #isWriteThrough() } returns false and there are
     *         changes that have not yet been commited to the underlying Entity,
     *         false otherwise.
     */
    public boolean isModified();

    /**
     * Checks whether this item has been marked for deletion. This method can
     * only return true if {@link #isPersistent() } is true and the container is
     * running in buffered mode.
     * 
     * @return true if the item has been deleted, false otherwise.
     */
    public boolean isDeleted();

    /**
     * Gets the container that contains this item. If {@link #getItemId() } is
     * null, the container created the item but does not yet contain it.
     * 
     * @return the container (never null).
     */
    public EntityContainer<T> getContainer();

    /**
     * Registers a new value change listener for all the properties of this
     * item.
     * 
     * @param listener
     *            the new listener to be registered.
     */
    public void addListener(Property.ValueChangeListener listener);

    /**
     * Removes a previously registered value change listener. The listener will
     * be unregistered from all the properties of this item.
     * 
     * @param listener
     *            listener to be removed.
     */
    public void removeListener(Property.ValueChangeListener listener);

    /**
     * {@inheritDoc }
     */
    public EntityItemProperty getItemProperty(Object id);

    /**
     * Originally, all nested properties are inherited from the
     * {@link EntityContainer}. However, if additional properties are needed,
     * this method can be used to add the nested property
     * <code>nestedProperty</code> to the set of properties for this particular
     * item.
     * <p>
     * Otherwise, this method behaves just like
     * {@link EntityContainer#addNestedContainerProperty(java.lang.String) }.
     * 
     * @param nestedProperty
     *            the nested property to add (must not be null).
     * @throws UnsupportedOperationException
     *             if nested properties are not supported by the container.
     * @throws IllegalArgumentException
     *             if <code>nestedProperty</code> is illegal.
     */
    public void addNestedContainerProperty(String nestedProperty)
            throws UnsupportedOperationException, IllegalArgumentException;

    /**
     * Removes a nested property added with
     * {@link #addNestedContainerProperty(java.lang.String) }. This method cannot
     * be used to remove any other properties.
     * 
     * @param propertyId
     *            the ID (name) of the nested property.
     * @return true if a nested property was removed by this method, false
     *         otherwise.
     * @throws UnsupportedOperationException
     *             if the implementation does not support removing nested
     *             properties.
     */
    public boolean removeItemProperty(Object propertyId)
            throws UnsupportedOperationException;

    /**
     * <strong>This functionality is not supported.</strong>
     * <p>
     * {@inheritDoc }
     * 
     * @throws UnsupportedOperationException
     *             always thrown.
     */
    public boolean addItemProperty(Object id, Property property)
            throws UnsupportedOperationException;

    /**
     * {@inheritDoc }
     * <p>
     * In practice, this means all the properties of the underlying entity
     * class, any nested properties defined in the container and any nested
     * properties added using
     * {@link #addNestedContainerProperty(java.lang.String) }. Any non-nested
     * properties that have been removed from the container will still show up
     * in this collection.
     */
    public Collection<?> getItemPropertyIds();
}
