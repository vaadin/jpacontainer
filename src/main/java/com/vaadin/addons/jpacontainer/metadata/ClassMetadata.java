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
import javax.persistence.Entity;

/**
 * Interface that exposes metadata for an {@link Entity} or an {@link Embeddable} class to the application.
 *
 * @see EntityClassMetadata
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface ClassMetadata extends Serializable {

    // TODO Improve the documentation
    
    /**
     * The mapped class.
     */
    public Class<?> getMappedClass();

    /**
     * The mapped (persistent) properties.
     */
    public Collection<PropertyMetadata> getMappedProperties();

    /**
     * Gets the metadata of the named mapped property.
     * 
     * @param propertyName the name of the property (must not be null).
     * @return the property metadata, or null if not found.
     */
    public PropertyMetadata getMappedProperty(String propertyName);

    /**
     * Gets the value of <code>object.propertyName</code>. Nested property names
     * are supported.
     *
     * @param object the entity object from which the property value should be
     * fetched (must not be null).
     * @param propertyName the name of the property (must not be null).
     * @return the property value.
     * @throws IllegalArgumentException if the property value could not be fetched.
     */
    public Object getPropertyValue(Object object, String propertyName) throws
            IllegalArgumentException;

    /**
     * Sets the value of <code>object.propertyName</code> to <code>value</code>. Nested
     * property names are supported.
     *
     * @param object the object whose property should be set (must not be null).
     * @param propertyName the name of the property to set (must not be null).
     * @param value the value to set (must not be null).
     * @throws IllegalArgumentException if the value could not be set.
     */
    public void setPropertyValue(Object object, String propertyName,
            Object value) throws IllegalArgumentException;
}
