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
import java.util.Collection;
import javax.persistence.Embeddable;

/**
 * This interface defines a way of accessing the JPA mapping metadata
 * of {@link Entity} and {@link Embeddable} classes. This information may
 * be used to construct queries or decide whether a property is sortable or not.
 *
 * @see EntityClassMetadata
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface ClassMetadata<T> extends Serializable {

    /**
     * Gets the mapped class.
     *
     * @return the class (never null).
     */
    public Class<T> getMappedClass();

    /**
     * Gets all the persistent properties of the class.
     *
     * @return an unmodifiable collection of property metadata.
     */
    public Collection<PersistentPropertyMetadata> getPersistentProperties();

    /**
     * Gets all the properties of the class. In addition to the persistent properties,
     * all public JavaBean properties are also included (even those who do not have
     * setter methods).
     *
     * @return an unmodifiable collection of property metadata.
     */
    public Collection<PropertyMetadata> getProperties();

    /**
     * Gets the metadata of the named property.
     * 
     * @param propertyName the name of the property (must not be null).
     * @return the property metadata, or null if not found.
     */
    public PropertyMetadata getProperty(String propertyName);

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
            IllegalArgumentException;

    /**
     * Sets the value of <code>object.propertyName</code> to <code>value</code>.
     *
     * @param object the object whose property should be set (must not be null).
     * @param propertyName the name of the property to set (must not be null).
     * @param value the value to set.
     * @throws IllegalArgumentException if the value could not be set, e.g. due to <code>propertyName</code> being invalid.
     */
    public void setPropertyValue(T object, String propertyName,
            Object value) throws IllegalArgumentException;
}
