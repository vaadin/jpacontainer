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

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@link Item}-implementation that is used by {@link JPAContainer}. Should not be
 * used directly by clients.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public final class EntityItem<T> implements Item {

    /**
     * {@link Property}-implementation that is used by {@link EntityItem}. Should not be
     * used directly by clients.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public final class EntityItemProperty implements Property,
            Property.ValueChangeNotifier {

        private String propertyId;
        private boolean isNested;

        /**
         * Creates a new <code>EntityItemProperty</code>.
         * 
         * @param propertyId the property id of the new property (must not be null).
         */
        EntityItemProperty(String propertyId) {
            assert propertyId != null : "propertyId must not be null";
            this.propertyId = propertyId;
            this.isNested = propertyId.indexOf('.') != -1;
        }

        /*
         * If the property is nested, we can access it from the propertyList.
         * If it is not, it is better to access it from the class metadata.
         * The reason for this is that an item always contains all the properties
         * of a mapped class, whereas a container (and hence the property list) may
         * only contain a small number of properties.
         */
        @Override
        public Class<?> getType() {
            return isNested ? container.getPropertyList().getPropertyType(
                    propertyId) : container.getPropertyList().
                    getClassMetadata().getProperty(propertyId).getType();
        }

        @Override
        public Object getValue() {
            return isNested ? container.getPropertyList().getPropertyValue(
                    entity, propertyId) : container.getPropertyList().
                    getClassMetadata().getPropertyValue(entity, propertyId);
        }

        @Override
        public String toString() {
            final Object value = getValue();
            if (value == null) {
                return null;
            }
            return value.toString();
        }

        @Override
        public boolean isReadOnly() {
            return isNested ? !container.getPropertyList().isPropertyWritable(
                    propertyId) : !container.getPropertyList().
                    getClassMetadata().getProperty(propertyId).isWritable();
        }

        /**
         * <strong>This functionality is not supported by this implementation.</strong>
         * <p>
         * {@inheritDoc }
         */
        @Override
        public void setReadOnly(boolean newStatus) {
            throw new UnsupportedOperationException(
                    "The read only state cannot be changed");
        }

        @Override
        public void setValue(Object newValue) throws ReadOnlyException,
                ConversionException {
            if (isReadOnly()) {
                throw new ReadOnlyException();
            }

            if (newValue != null && !getType().isAssignableFrom(newValue.
                    getClass())) {
                /*
                 * The type we try to set is incompatible with the type of
                 * the property. We therefore try to convert the value
                 * to a string and see if there is a constructor that takes
                 * a single string argument. If this fails, we throw
                 * an exception.
                 */
                Object value;
                try {
                    // Gets the string constructor
                    final Constructor constr = getType().getConstructor(
                            new Class[]{String.class});

                    value = constr.newInstance(new Object[]{newValue.toString()});
                } catch (Exception e) {
                    throw new ConversionException(e);
                }
            }
            try {
                if (isNested) {
                    container.getPropertyList().setPropertyValue(entity,
                            propertyId, newValue);
                } else {
                    container.getPropertyList().getClassMetadata().
                            setPropertyValue(entity,
                            propertyId, newValue);
                }
                modified = true;
                // Notify the listeners
                notifyListeners();
                // Finally, notify the container directly. We do this
                // in order to avoid having the container registering with
                // each and every item property.
                container.containerItemPropertyModified(EntityItem.this,
                        propertyId);
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }
        private List<ValueChangeListener> listeners;

        private class ValueChangeEvent extends EventObject implements
                Property.ValueChangeEvent {

            private ValueChangeEvent(EntityItemProperty source) {
                super(source);
            }

            @Override
            public Property getProperty() {
                return (Property) getSource();
            }
        }

        private void notifyListeners() {
            if (listeners != null) {
                final Object[] l = listeners.toArray();
                final Property.ValueChangeEvent event = new ValueChangeEvent(
                        this);
                for (int i = 0; i < l.length; i++) {
                    ((Property.ValueChangeListener) l[i]).valueChange(event);
                }
            }
        }

        @Override
        public void addListener(ValueChangeListener listener) {
            assert listener != null : "listener must not be null";
            if (listeners == null) {
                listeners = new LinkedList<ValueChangeListener>();
            }
            listeners.add(listener);
        }

        @Override
        public void removeListener(ValueChangeListener listener) {
            assert listener != null : "listener must not be null";
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    listeners = null;
                }
            }
        }
    }
    private T entity;
    private JPAContainer<T> container;
    private Map<Object, EntityItemProperty> propertyMap;
    private boolean modified = false;
    private boolean persistent = true;

    /**
     * Creates a new <code>EntityItem</code>. This constructor assumes that
     * <code>entity</code> is persistent. If not, the <code>persistent</code> flag
     * should be changed using {@link #isPersistent() }.
     *
     * @param container the container that holds the item (must not be null).
     * @param entity the entity for which the item should be created (must not be null).
     */
    EntityItem(JPAContainer<T> container, T entity) {
        assert container != null : "container must not be null";
        assert entity != null : "entity must not be null";
        this.entity = entity;
        this.container = container;
        propertyMap = new HashMap<Object, EntityItemProperty>();
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public boolean addItemProperty(Object id, Property property) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Property getItemProperty(Object id) {
        assert id != null : "id must not be null";
        EntityItemProperty p = propertyMap.get(id);
        if (p == null) {
            if (!getItemPropertyIds().contains(id.toString())) {
                return null;
            }
            p = new EntityItemProperty(id.toString());
            propertyMap.put(id, p);
        }
        return p;
    }

    @Override
    public Collection<String> getItemPropertyIds() {
        return container.getPropertyList().getAllAvailablePropertyNames();
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public boolean removeItemProperty(Object id) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this entity item has been modified.
     *
     * @return true if the item has been modified, false if not.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Changes the <code>modified</code> flag of this item.
     *
     * @see #isModified()
     * @param modified true to mark the item as modified, false to mark it as untouched.
     */
    void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Checks if this entity item has been persisted
     *
     * @return true if the item is persistent, false if not.
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Changes the <code>persistent</code> flag of this item.
     * 
     * @see #isPersistent() 
     * @param persistent true to mark the item as persistent, false to mark it as transient.
     */
    void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }
}
