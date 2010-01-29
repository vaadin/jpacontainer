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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    public final class EntityItemProperty implements Property {

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
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }
    }
    private T entity;
    private JPAContainer<T> container;
    private Map<Object, EntityItemProperty> propertyMap;
    private boolean modified = false;

    // TODO Add a reference to JPAContainer
    // - update modified state of container when item is modified
    
    /**
     * Creates a new <code>EntityItem</code>.
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
}
