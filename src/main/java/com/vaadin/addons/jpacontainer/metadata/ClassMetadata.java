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

/**
 * Interface that exposes entity class metadata to the application.
 *
 * @author Petter Holmström (IT Mill)
 */
public interface ClassMetadata<T> extends Serializable {

    // TODO Improve the documentation
    /**
     * The name of the entity.
     */
    public String getEntityName();

    /**
     * The class of the entity.
     */
    public Class<T> getEntityClass();

    /**
     * The mapped (persistent) properties.
     */
    public Collection<PropertyMetadata> getMappedProperties();

    /**
     * Gets the metadata of the named mapped property.
     * 
     * @param propertyName the name of the property.
     * @return the property metadata, or null if not found.
     */
    public PropertyMetadata getMappedProperty(String propertyName);

    /**
     * TODO Document me!
     * @return
     */
    public boolean hasVersionProperty();

    /**
     * TODO Document me!
     * @return
     */
    public PropertyMetadata getVersionProperty();

    /**
     * If the entity has an identifier property or not.
     * 
     * @see #getIdentifierProperty()
     * @see #hasEmbeddedIdentifier()
     * @see #getEmbeddedIdentifierProperties() 
     */
    public boolean hasIdentifierProperty();

    /**
     * Gets the identifier property, if it exists.
     *
     * @see #hasIdentifierProperty() 
     * @return the identifier property metadata, or null if not available.
     */
    public PropertyMetadata getIdentifierProperty();

    /**
     * If the entity has an embedded identifier. This property cannot be
     * true unless {@link #hasIdentifierProperty() } returns false.
     *
     * @see #getEmbeddedIdentifierProperties() 
     */
    public boolean hasEmbeddedIdentifier();

    /**
     * The properties that constitute the embedded identifier.
     *
     * @return the embedded identifier properties, or an empty collection if
     * the entity does not have an embedded identifier.
     */
    public Collection<PropertyMetadata> getEmbeddedIdentifierProperties();

    /**
     * Gets the value of <code>property</code> from <code>object</code>.
     *
     * @param object the entity object from which the property value should be
     * fetched.
     * @param property the metadata of the property.
     * @return the property value.
     * @throws IllegalArgumentException if the property value could not be fetched.
     */
    public Object getPropertyValue(T object, PropertyMetadata property) throws
            IllegalArgumentException;
}
