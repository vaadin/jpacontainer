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
import java.lang.annotation.Annotation;

/**
 * Interface defining the metadata of a persistent property.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface PropertyMetadata extends Serializable {

    /**
     * The name of the property.
     */
    public String getName();

    /**
     * The type of the property.
     */
    public Class<?> getType();

    /**
     * The metadata of the property type, if it is embedded or a reference.
     * Otherwise, this method returns null.
     *
     * @see #getType() 
     * @see #isEmbedded()
     * @see #isReference() 
     */
    public ClassMetadata<?> getTypeMetadata();

    /**
     * The metadata of the class that owns this property.
     * 
     * @see ClassMetadata#getMappedProperties() 
     */
    public ClassMetadata<?> getOwner();

    /**
     * Returns whether this property is an embedded property or not.
     *
     * @see javax.persistence.Embeddable
     * @see javax.persistence.Embedded
     * @see #getTypeMetadata()
     */
    public boolean isEmbedded();

    /**
     * Returns whether this property is a reference or not.
     * 
     * @see javax.persistence.OneToOne
     * @see javax.persistence.ManyToOne
     * @see #getTypeMetadata() 
     */
    public boolean isReference();

    /**
     * Returns whether this property is a collection or not.
     *
     * @see javax.persistence.OneToMany
     * @see javax.persistence.ManyToMany
     */
    public boolean isCollection();

    /**
     * The way the property value is accessed (as a JavaBean property or as a field).
     */
    public AccessType getAccessType();

    /**
     * The annotations of the property. If the access type is {@link AccessType#FIELD}, these
     * are the annotations of the field. If the access type is {@link AccessType#METHOD}, these
     * are the annotations of the getter method.
     *
     * @see #getAnnotation(java.lang.Class) 
     */
    public Annotation[] getAnnotations();

    /**
     * Gets the annotation of the specified annotation class, if available.
     *
     * @see #getAnnotations() tA
     * @see Class#getAnnotation(java.lang.Class) 
     * @param annotationClass the annotation class.
     * @return the annotation, or null if not found.
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * Enumeration defining the property access types.
     *
     * @author Petter Holmström (IT Mill)
     */
    public enum AccessType {

        /**
         * The property is accessed as a JavaBean property using getters and setters.
         */
        METHOD,
        /**
         * The property is accessed directly as a field.
         */
        FIELD
    }
}
