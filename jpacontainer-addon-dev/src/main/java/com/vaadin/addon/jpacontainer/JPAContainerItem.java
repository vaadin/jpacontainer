/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.addon.jpacontainer.util.HibernateUtil;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.Converter.ConversionException;

/**
 * {@link EntityItem}-implementation that is used by {@link JPAContainer}.
 * Should not be used directly by clients.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public final class JPAContainerItem<T> implements EntityItem<T> {

    private static final long serialVersionUID = 3835181888110236341L;

    private static boolean nullSafeEquals(Object o1, Object o2) {
        try {
            return o1 == o2 || o1.equals(o2);
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * {@link Property}-implementation that is used by {@link EntityItem}.
     * Should not be used directly by clients.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    final class ItemProperty implements EntityItemProperty {

        private static final long serialVersionUID = 2791934277775480650L;
        private String propertyId;
        private Object cachedValue;

        /**
         * Creates a new <code>ItemProperty</code>.
         * 
         * @param propertyId
         *            the property id of the new property (must not be null).
         */
        ItemProperty(String propertyId) {
            assert propertyId != null : "propertyId must not be null";
            this.propertyId = propertyId;

            // Initialize cached value if necessary
            if (!isWriteThrough()) {
                cacheRealValue();
            }
        }

        public String getPropertyId() {
            return propertyId;
        }

        /**
         * Like the name suggests, this method notifies the listeners if the
         * cached value and real value are different.
         */
        void notifyListenersIfCacheAndRealValueDiffer() {
            Object realValue = getRealValue();
            if (!nullSafeEquals(realValue, cachedValue)) {
                fireValueChangeEvent();
            }
        }

        /**
         * Caches the real value of the property.
         */
        void cacheRealValue() {
            Object realValue = getRealValue();
            cachedValue = realValue;
        }

        /**
         * Clears the cached value, without notifying any listeners.
         */
        void clearCache() {
            cachedValue = null;
        }

        /**
         * <b>Note! This method assumes that write through is OFF!</b>
         * <p>
         * Sets the real value to the cached value. If read through is on, the
         * listeners are also notified as the value will appear to have changed
         * to them.
         * <p>
         * If the property is read only, nothing happens.
         * 
         * @throws ConversionException
         *             if the real value could not be set for some reason.
         */
        void commit() throws ConversionException {
            if (!isReadOnly()) {
                try {
                    setRealValue(cachedValue);
                } catch (Exception e) {
                    throw new ConversionException(e);
                }
            }
        }

        /**
         * <b>Note! This method assumes that write through is OFF!</b>
         * <p>
         * Replaces the cached value with the real value. If read through is
         * off, the listeners are also notified as the value will appera to have
         * changed to them.
         */
        void discard() {
            Object realValue = getRealValue();
            if (!nullSafeEquals(realValue, cachedValue)) {
                cacheRealValue();
                fireValueChangeEvent();
            } else {
                cacheRealValue();
            }
        }

        public EntityItem<?> getItem() {
            return JPAContainerItem.this;
        }

        public Class<?> getType() {
            return propertyList.getPropertyType(propertyId);
        }

        public Object getValue() {
            if (isReadThrough() && isWriteThrough()) {
                return getRealValue();
            } else {
                return cachedValue;
            }
        }

        /**
         * Gets the real value from the backend entity.
         * 
         * @return the real value.
         */
        private Object getRealValue() {
            ensurePropertyLoaded(propertyId);
            return propertyList.getPropertyValue(entity, propertyId);
        }

        @Override
        public String toString() {
            final Object value = getValue();
            if (value == null) {
                return null;
            }
            return value.toString();
        }

        public boolean isReadOnly() {
            return !propertyList.isPropertyWritable(propertyId);
        }

        /**
         * <strong>This functionality is not supported by this
         * implementation.</strong>
         * <p>
         * {@inheritDoc }
         */
        public void setReadOnly(boolean newStatus) {
            throw new UnsupportedOperationException(
                    "The read only state cannot be changed");
        }

        /**
         * Sets the real value of the property to <code>newValue</code>. The
         * value is expected to be of the correct type at this point (i.e. any
         * conversions from a String should have been done already). As this
         * method updates the backend entity object, it also turns on the
         * <code>dirty</code> flag of the item.
         * 
         * @see JPAContainerItem#isDirty()
         * @param newValue
         *            the new value to set.
         */
        private void setRealValue(Object newValue) {
            ensurePropertyLoaded(propertyId);
            propertyList.setPropertyValue(entity, propertyId, newValue);
            dirty = true;
        }

        /**
         * Ensures that any lazy loaded properties are available.
         * 
         * @param propertyId
         *            the id of the property to check.
         */
        private void ensurePropertyLoaded(String propertyId) {
            LazyLoadingDelegate lazyLoadingDelegate = getContainer()
                    .getEntityProvider().getLazyLoadingDelegate();
            if (lazyLoadingDelegate == null
                    || !propertyList.isPropertyLazyLoaded(propertyId)) {
                // Don't need to do anything
                return;
            }
            boolean shouldLoadEntity = false;
            try {
                Object value = propertyList
                        .getPropertyValue(entity, propertyId);
                if (value != null) {
                    shouldLoadEntity = HibernateUtil
                            .isUninitializedAndUnattachedProxy(value);
                    if (Collection.class.isAssignableFrom(propertyList
                            .getPropertyType(propertyId))) {
                        ((Collection<?>) value).iterator().hasNext();
                    }
                }
            } catch (IllegalArgumentException e) {
                shouldLoadEntity = true;
            } catch (RuntimeException e) {
                if (HibernateUtil.isLazyInitializationException(e)) {
                    shouldLoadEntity = true;
                } else {
                    throw e;
                }
            }
            if (shouldLoadEntity) {
                entity = lazyLoadingDelegate.ensureLazyPropertyLoaded(entity,
                        propertyId);
            }
        }

        public void setValue(Object newValue) throws ReadOnlyException,
                ConversionException {
            if (isReadOnly()) {
                throw new ReadOnlyException();
            }

            if (newValue != null
                    && !getType().isAssignableFrom(newValue.getClass())) {
                /*
                 * The type we try to set is incompatible with the type of the
                 * property. We therefore try to convert the value to a string
                 * and see if there is a constructor that takes a single string
                 * argument. If this fails, we throw an exception.
                 */
                try {
                    // Gets the string constructor
                    final Constructor<?> constr = getType().getConstructor(
                            new Class[] { String.class });

                    newValue = constr.newInstance(new Object[] { newValue
                            .toString() });
                } catch (Exception e) {
                    throw new ConversionException(e);
                }
            }
            try {
                if (isWriteThrough()) {
                    setRealValue(newValue);
                    container.containerItemPropertyModified(
                            JPAContainerItem.this, propertyId);
                } else {
                    cachedValue = newValue;
                    modified = true;
                }
            } catch (Exception e) {
                throw new ConversionException(e);
            }
            fireValueChangeEvent();
        }

        private List<ValueChangeListener> listeners;

        private class ValueChangeEvent extends EventObject implements
                Property.ValueChangeEvent {

            private static final long serialVersionUID = 4999596001491426923L;

            private ValueChangeEvent(ItemProperty source) {
                super(source);
            }

            public Property getProperty() {
                return (Property) getSource();
            }
        }

        /**
         * Notifies all the listeners that the value of the property has
         * changed.
         */
        public void fireValueChangeEvent() {
            if (listeners != null) {
                final Object[] l = listeners.toArray();
                final Property.ValueChangeEvent event = new ValueChangeEvent(
                        this);
                for (int i = 0; i < l.length; i++) {
                    ((Property.ValueChangeListener) l[i]).valueChange(event);
                }
            }
        }

        public void addListener(ValueChangeListener listener) {
            assert listener != null : "listener must not be null";
            if (listeners == null) {
                listeners = new LinkedList<ValueChangeListener>();
            }
            listeners.add(listener);
        }

        public void removeListener(ValueChangeListener listener) {
            assert listener != null : "listener must not be null";
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    listeners = null;
                }
            }
        }

        public void addValueChangeListener(ValueChangeListener listener) {
            addListener(listener);
        }

        public void removeValueChangeListener(ValueChangeListener listener) {
            addListener(listener);
        }
    }

    private T entity;
    private JPAContainer<T> container;
    private PropertyList<T> propertyList;
    private Map<Object, ItemProperty> propertyMap;
    private boolean modified = false;
    private boolean dirty = false;
    private boolean persistent = true;
    private boolean readThrough = true;
    private boolean writeThrough = true;
    private boolean deleted = false;
    private Object itemId;

    /**
     * Creates a new <code>JPAContainerItem</code>. This constructor assumes
     * that <code>entity</code> is persistent. The item ID is the entity
     * identifier.
     * 
     * @param container
     *            the container that holds the item (must not be null).
     * @param entity
     *            the entity for which the item should be created (must not be
     *            null).
     */
    JPAContainerItem(JPAContainer<T> container, T entity) {
        this(container, entity, container.getEntityClassMetadata()
                .getPropertyValue(
                        entity,
                        container.getEntityClassMetadata()
                                .getIdentifierProperty().getName()), true);
    }

    /**
     * Creates a new <code>JPAContainerItem</code>.
     * 
     * @param container
     *            the container that created the item (must not be null).
     * @param entity
     *            the entity for which the item should be created (must not be
     *            null).
     * @param itemId
     *            the item ID, or null if the item is not yet inside the
     *            container that created it.
     * @param persistent
     *            true if the entity is persistent, false otherwise. If
     *            <code>itemId</code> is null, this parameter will be ignored.
     */
    JPAContainerItem(JPAContainer<T> container, T entity, Object itemId,
            boolean persistent) {
        assert container != null : "container must not be null";
        assert entity != null : "entity must not be null";
        this.entity = entity;
        this.container = container;
        this.propertyList = new PropertyList<T>(container.getPropertyList());
        this.itemId = itemId;
        if (itemId == null) {
            this.persistent = false;
        } else {
            this.persistent = persistent;
        }
        this.propertyMap = new HashMap<Object, ItemProperty>();
        container.registerItem(this);
    }

    public Object getItemId() {
        return itemId;
    }

    public boolean addItemProperty(Object id, Property property)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public void addNestedContainerProperty(String nestedProperty)
            throws UnsupportedOperationException {
        propertyList.addNestedProperty(nestedProperty);
    }

    public EntityItemProperty getItemProperty(Object id) {
        assert id != null : "id must not be null";
        ItemProperty p = propertyMap.get(id);
        if (p == null) {
            if (!getItemPropertyIds().contains(id.toString())) {
                return null;
            }
            p = new ItemProperty(id.toString());
            propertyMap.put(id, p);
        }
        return p;
    }

    public Collection<String> getItemPropertyIds() {
        /*
         * Although the container may only contain a few properties, all
         * properties are available for items.
         */
        return propertyList.getAllAvailablePropertyNames();
    }

    public boolean removeItemProperty(Object id)
            throws UnsupportedOperationException {
        assert id != null : "id must not be null";
        if (id.toString().indexOf('.') > -1) {
            return propertyList.removeProperty(id.toString());
        } else {
            return false;
        }
    }

    public boolean isModified() {
        return modified;
    }

    /**
     * Changes the <code>dirty</code> flag of this item.
     * 
     * @see #isDirty()
     * @param dirty
     *            true to mark the item as dirty, false to mark it as untouched.
     */
    void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return isPersistent() && dirty;
    }

    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Changes the <code>persistent</code> flag of this item.
     * 
     * @see #isPersistent()
     * @param persistent
     *            true to mark the item as persistent, false to mark it as
     *            transient.
     */
    void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public boolean isDeleted() {
        return isPersistent() && !getContainer().isBuffered() && deleted;
    }

    /**
     * Changes the <code>deleted</code> flag of this item.
     * 
     * @see #isDeleted()
     * @param deleted
     *            true to mark the item as deleted, false to mark it as
     *            undeleted.
     */
    void setDeleted(boolean deleted) {
        this.deleted = true;
    }

    public EntityContainer<T> getContainer() {
        return container;
    }

    public T getEntity() {
        return entity;
    }

    public void commit() throws SourceException, InvalidValueException {
        if (!isWriteThrough()) {
            try {
                /*
                 * Commit all properties. The commit() operation will check if
                 * the property is read only and ignore it if that is the case.
                 */
                for (ItemProperty prop : propertyMap.values()) {
                    prop.commit();
                }
                modified = false;
                container.containerItemModified(this);
            } catch (Property.ReadOnlyException e) {
                throw new SourceException(this, e);
            }
        }
    }

    public void discard() throws SourceException {
        if (!isWriteThrough()) {
            for (ItemProperty prop : propertyMap.values()) {
                prop.discard();
            }
            modified = false;
        }
    }

    public boolean isReadThrough() {
        return readThrough;
    }

    public boolean isWriteThrough() {
        return writeThrough;
    }

    public void setReadThrough(boolean readThrough) throws SourceException {
        if (this.readThrough != readThrough) {
            if (!readThrough && writeThrough) {
                throw new IllegalStateException(
                        "ReadThrough can only be turned off if WriteThrough is turned off");
            }
            this.readThrough = readThrough;
        }
    }

    public void setWriteThrough(boolean writeThrough) throws SourceException,
            InvalidValueException {
        if (this.writeThrough != writeThrough) {
            if (writeThrough) {
                /*
                 * According to the Buffered interface, commit must be executed
                 * if writeThrough is turned on.
                 */
                commit();
                /*
                 * Do some cleaning up
                 */
                for (ItemProperty prop : propertyMap.values()) {
                    prop.clearCache();
                }
            } else {
                /*
                 * We can iterate directly over the map, as this operation only
                 * affects existing properties. Properties that are lazily
                 * created afterwards will work automatically.
                 */
                for (ItemProperty prop : propertyMap.values()) {
                    prop.cacheRealValue();
                }
            }
            this.writeThrough = writeThrough;
        }
    }

    public void addListener(ValueChangeListener listener) {
        /*
         * This operation affects ALL properties, so we have to iterate over the
         * list of ids instead of the map.
         */
        for (String propertyId : getItemPropertyIds()) {
            ((Property.ValueChangeNotifier) getItemProperty(propertyId))
                    .addValueChangeListener(listener);
        }
    }

    public void removeListener(ValueChangeListener listener) {
        /*
         * This operation affects ALL properties, so we have to iterate over the
         * list of ids instead of the map.
         */
        for (String propertyId : getItemPropertyIds()) {
            ((Property.ValueChangeNotifier) getItemProperty(propertyId))
                    .removeValueChangeListener(listener);
        }
    }

    public void addValueChangeListener(ValueChangeListener listener) {
        addListener(listener);
    }

    public void removeValueChangeListener(ValueChangeListener listener) {
        removeListener(listener);
    }

    @Override
    public String toString() {
        return entity.toString();
    }

    @SuppressWarnings("serial")
    public void refresh() {
        if (isPersistent()) {
            T refreshedEntity = getContainer().getEntityProvider()
                    .refreshEntity(entity);
            if (refreshedEntity == null) {
                /*
                 * Entity has been removed, fire item set change for the
                 * container
                 */
                setPersistent(false);
                container.fireContainerItemSetChange(new ItemSetChangeEvent() {
                    public Container getContainer() {
                        return container;
                    }
                });
                return;
            } else {
                entity = refreshedEntity;
            }
            if (isDirty()) {
                discard();
            }
            Collection<String> itemPropertyIds = getItemPropertyIds();
            for (String string : itemPropertyIds) {
                getItemProperty(string).fireValueChangeEvent();
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        container.registerItem(this);
    }

    public void setBuffered(boolean buffered) {
        setReadThrough(!buffered);
        setWriteThrough(!buffered);
    }

    public boolean isBuffered() {
        return !isReadThrough() && !isWriteThrough();
    }
}
