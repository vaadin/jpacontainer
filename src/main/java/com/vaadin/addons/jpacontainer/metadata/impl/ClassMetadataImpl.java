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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.metadata.impl;

import com.vaadin.addons.jpacontainer.metadata.ClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.NestedPropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata.AccessType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link ClassMetadata}.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class ClassMetadataImpl implements ClassMetadata {

    private final Class<?> mappedClass;

    private final Map<String, PropertyMetadata> properties;

    ClassMetadataImpl(Class<?> mappedClass) {
        assert mappedClass != null : "mappedClass must not be null";
        this.mappedClass = mappedClass;
        this.properties = new HashMap<String, PropertyMetadata>();
    }

    private PropertyMetadata doAddProperty(PropertyMetadata pm) {
        if (this.properties.containsKey(pm.getName())) {
            throw new IllegalArgumentException("A property named "
                    + pm.getName()
                    + " already exists!");
        }
        this.properties.put(pm.getName(), pm);
        return pm;
    }

    final PropertyMetadata addProperty(String name, Class<?> type, Field field,
            Method getter, Method setter) {
        PropertyMetadata pm = new PropertyMetadataImpl(name, type, this, false,
                false, false, null, field, getter, setter);
        return doAddProperty(pm);
    }

    final PropertyMetadata addEmbeddedProperty(String name,
            ClassMetadata type, Field field, Method getter, Method setter) {
        PropertyMetadata pm = new PropertyMetadataImpl(name,
                type.getMappedClass(), this, true, false, false, type,
                field, getter, setter);
        doAddProperty(pm);
        // Add nested properties
        for (PropertyMetadata nestedProperty : pm.getTypeMetadata().
                getMappedProperties()) {
            doAddProperty(new NestedPropertyMetadataImpl(nestedProperty, pm));
        }
        return pm;
    }

    final PropertyMetadata addCollectionProperty(String name, Class<?> type,
            Field field, Method getter, Method setter) {
        PropertyMetadata pm = new PropertyMetadataImpl(name, type, this, false,
                false, true, null, field, getter, setter);
        return doAddProperty(pm);
    }

    final PropertyMetadata addReferenceProperty(String name,
            ClassMetadata type, Field field, Method getter, Method setter) {
        PropertyMetadata pm = new PropertyMetadataImpl(name,
                type.getMappedClass(), this, false, true, false, type,
                field, getter, setter);
        return doAddProperty(pm);
    }

    @Override
    public final Class<?> getMappedClass() {
        return mappedClass;
    }

    @Override
    public final Collection<PropertyMetadata> getMappedProperties() {
        return Collections.unmodifiableCollection(properties.values());
    }

    @Override
    public final PropertyMetadata getMappedProperty(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public final Object getPropertyValue(Object object,
            String propertyName) throws IllegalArgumentException {
        try {
            PropertyMetadata prop = getMappedProperty(
                    propertyName);
            if (prop == null) {
                throw new IllegalArgumentException("Invalid property name: "
                        + propertyName);
            } else if (prop instanceof NestedPropertyMetadata) {
                NestedPropertyMetadata nestedProp =
                        (NestedPropertyMetadata) prop;
                Object embedded = getPropertyValue(object, nestedProp.
                        getParentProperty().getName());
                return nestedProp.getActualProperty().getOwner().
                        getPropertyValue(embedded, nestedProp.getActualProperty().
                        getName());
            } else {
                PropertyMetadataImpl propImpl = (PropertyMetadataImpl) prop;
                if (propImpl.getAccessType() == AccessType.FIELD) {
                    propImpl.field.setAccessible(true);
                    return propImpl.field.get(object);
                } else {
                    return propImpl.getter.invoke(object);
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access the field value",
                    e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Cannot access the getter", e);
        }
    }

    @Override
    public final void setPropertyValue(Object object, String propertyName,
            Object value) throws IllegalArgumentException {
        try {
            PropertyMetadata prop = getMappedProperty(
                    propertyName);
            if (prop == null) {
                throw new IllegalArgumentException("Invalid property name: "
                        + propertyName);
            } else if (prop instanceof NestedPropertyMetadata) {
                NestedPropertyMetadata nestedProp =
                        (NestedPropertyMetadata) prop;
                Object embedded = getPropertyValue(object, nestedProp.
                        getParentProperty().getName());
                nestedProp.getActualProperty().getOwner().
                        setPropertyValue(embedded, nestedProp.getActualProperty().
                        getName(), value);
            } else {
                PropertyMetadataImpl propImpl = (PropertyMetadataImpl) prop;
                if (propImpl.getAccessType() == AccessType.FIELD) {
                    propImpl.field.setAccessible(true);
                    propImpl.field.set(object, value);
                } else {
                    propImpl.setter.invoke(object, value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access the field value",
                    e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Cannot access the setter", e);
        }
    }
}
