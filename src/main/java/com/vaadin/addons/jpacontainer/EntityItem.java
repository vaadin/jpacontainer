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

import com.vaadin.addons.jpacontainer.util.PropertyList;
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
            return isNested ? propertyList.getPropertyType(propertyId) : propertyList.
                    getClassMetadata().getProperty(propertyId).getType();
        }

        @Override
        public Object getValue() {
            return isNested ? propertyList.getPropertyValue(entity, propertyId) : propertyList.
                    getClassMetadata().getPropertyValue(entity, propertyId);
        }

        @Override
        public boolean isReadOnly() {
            return isNested ? !propertyList.isPropertyWritable(propertyId) : !propertyList.
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
                    propertyList.setPropertyValue(entity, propertyId, newValue);
                } else {
                    propertyList.getClassMetadata().setPropertyValue(entity,
                            propertyId, newValue);
                }
                modified = true;
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }
    }
    private T entity;
    private Map<Object, EntityItemProperty> propertyMap;
    private PropertyList<T> propertyList;
    private boolean modified = false;
    private Collection<String> propertyIds;

    // TODO Add a reference to JPAContainer
    // - hold list of property Ids in container instead of in item
    // - update modified state of container when item is modified

    /**
     * Creates a new <code>EntityItem</code>. Note, that if any new nested properties
     * are added to <code>propertyList</code> after this entity item has been created,
     * they will not show up in the {@link #getItemPropertyIds() } collection.
     * 
     * @param propertyList the property list containing all the properties that are available in the container (must not be null).
     * @param entity the entity for which the item should be created (must not be null).
     */
    EntityItem(PropertyList<T> propertyList, T entity) {
        assert propertyList != null : "propertyList must not be null";
        assert entity != null : "entity must not be null";
        this.entity = entity;
        this.propertyList = propertyList;
        propertyMap = new HashMap<Object, EntityItemProperty>();
        propertyIds = new HashSet<String>();
        propertyIds.addAll(propertyList.getNestedPropertyNames());
        propertyIds.addAll(propertyList.getClassMetadata().getPropertyNames());
        propertyIds = Collections.unmodifiableCollection(propertyIds);
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
            if (!propertyIds.contains(id.toString())) {
                return null;
            }
            p = new EntityItemProperty(id.toString());
            propertyMap.put(id, p);
        }
        return p;
    }

    @Override
    public Collection<String> getItemPropertyIds() {
        return propertyIds;
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
