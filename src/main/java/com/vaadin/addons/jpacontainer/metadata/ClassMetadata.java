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
package com.vaadin.addons.jpacontainer.metadata;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

/**
 * This class provides a way of accessing the JPA mapping metadata
 * of {@link Entity} and {@link Embeddable} classes. This information may
 * be used to construct queries or decide whether a property is sortable or not.
 *
 * @see EntityClassMetadata
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class ClassMetadata<T> implements Serializable {

    private final Class<T> mappedClass;
    private final Map<String, PropertyMetadata> allProperties = new HashMap<String, PropertyMetadata>();
    private final Map<String, PersistentPropertyMetadata> persistentProperties = new HashMap<String, PersistentPropertyMetadata>();

    /**
     * Constructs a new <code>ClassMetadata</code> instance. Properties can be
     * added using the {@link #addProperties(com.vaadin.addons.jpacontainer.metadata.PropertyMetadata[]) } method.
     * 
     * @param mappedClass the mapped class (must not be null).
     */
    ClassMetadata(Class<T> mappedClass) {
        assert mappedClass != null : "mappedClass must not be null";
        this.mappedClass = mappedClass;
    }

    /**
     * Adds the specified property metadata to the class.
     * 
     * @param properties an array of properties to add.
     */
    final void addProperties(PropertyMetadata... properties) {
        assert properties != null : "properties must not be null";
        for (PropertyMetadata pm : properties) {
            allProperties.put(pm.getName(), pm);
            if (pm instanceof PersistentPropertyMetadata) {
                persistentProperties.put(pm.getName(),
                        (PersistentPropertyMetadata) pm);
            }
        }
    }

    /**
     * Gets the mapped class.
     *
     * @return the class (never null).
     */
    public Class<T> getMappedClass() {
        return mappedClass;
    }

    /**
     * Gets all the persistent properties of the class.
     *
     * @return an unmodifiable collection of property metadata.
     */
    public Collection<PersistentPropertyMetadata> getPersistentProperties() {
        return Collections.unmodifiableCollection(persistentProperties.values());
    }

    /**
     * Gets all the properties of the class. In addition to the persistent properties,
     * all public JavaBean properties are also included (even those who do not have
     * setter methods).
     *
     * @return an unmodifiable collection of property metadata.
     */
    public Collection<PropertyMetadata> getProperties() {
        return Collections.unmodifiableCollection(allProperties.values());
    }

    /**
     * Gets the metadata of the named property.
     * 
     * @param propertyName the name of the property (must not be null).
     * @return the property metadata, or null if not found.
     */
    public PropertyMetadata getProperty(String propertyName) {
        return allProperties.get(propertyName);
    }

    /**
     * Gets the value of <code>object.propertyName</code>.
     *
     * @param object the entity object from which the property value should be
     * fetched (must not be null).
     * @param propertyName the name of the property (must not be null).
     * @return the property value.
     * @throws IllegalArgumentException if the property value could not be fetched, e.g. due to <code>propertyName</code> being invalid.
     */
    public Object getPropertyValue(T object, String propertyName) throws
            IllegalArgumentException {
        assert object != null : "object must not be null";
        assert propertyName != null : "propertyName must not be null";

        PropertyMetadata pmd = getProperty(propertyName);
        if (pmd != null) {
            try {
                if (pmd instanceof PersistentPropertyMetadata) {
                    PersistentPropertyMetadata ppmd = (PersistentPropertyMetadata) pmd;
                    if (ppmd.field != null) {
                        try {
                            ppmd.field.setAccessible(true);
                            return ppmd.field.get(object);
                        } finally {
                            ppmd.field.setAccessible(false);
                        }
                    }
                }
                return pmd.getter.invoke(object);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(
                        "Cannot access the property value",
                        e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(
                        "Cannot access the property value",
                        e);
            }
        } else {
            throw new IllegalArgumentException("No such property");
        }
    }

    /**
     * Sets the value of <code>object.propertyName</code> to <code>value</code>.
     *
     * @param object the object whose property should be set (must not be null).
     * @param propertyName the name of the property to set (must not be null).
     * @param value the value to set.
     * @throws IllegalArgumentException if the value could not be set, e.g. due to <code>propertyName</code> being invalid or the property being read only.
     */
    public void setPropertyValue(T object, String propertyName,
            Object value) throws IllegalArgumentException {
        assert object != null : "object must not be null";
        assert propertyName != null : "propertyName must not be null";

        PropertyMetadata pmd = getProperty(propertyName);
        if (pmd != null && pmd.isWritable()) {
            try {
                if (pmd instanceof PersistentPropertyMetadata) {
                    PersistentPropertyMetadata ppmd = (PersistentPropertyMetadata) pmd;
                    if (ppmd.field != null) {
                        try {
                            ppmd.field.setAccessible(true);
                            ppmd.field.set(object, value);
                            return;
                        } finally {
                            ppmd.field.setAccessible(false);
                        }
                    }
                }
                pmd.setter.invoke(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(
                        "Cannot set the property value",
                        e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(
                        "Cannot set the property value",
                        e);
            }
        } else {
            throw new IllegalArgumentException("No such writable property");
        }
    }
}
